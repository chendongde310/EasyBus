package com.renwey.emi.rxbus;


import com.renwey.emi.rxbus.annotation.Tag;
import com.renwey.emi.rxbus.entity.DeadEvent;
import com.renwey.emi.rxbus.entity.EventType;
import com.renwey.emi.rxbus.entity.ProducerEvent;
import com.renwey.emi.rxbus.entity.SubscriberEvent;
import com.renwey.emi.rxbus.finder.Finder;
import com.renwey.emi.rxbus.thread.ThreadEnforcer;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import rx.functions.Action1;


public class Bus {
    public static final String DEFAULT_IDENTIFIER = "default";


    private final ConcurrentMap<EventType, Set<SubscriberEvent>> subscribersByType =
            new ConcurrentHashMap<>();


    private final ConcurrentMap<EventType, ProducerEvent> producersByType =
            new ConcurrentHashMap<>();


    private final String identifier;


    private final ThreadEnforcer enforcer;


    private final Finder finder;

    private final ConcurrentMap<Class<?>, Set<Class<?>>> flattenHierarchyCache =
            new ConcurrentHashMap<>();
    private boolean registerFlag=false;


    public Bus() {
        this(DEFAULT_IDENTIFIER);
    }


    public Bus(String identifier) {
        this(ThreadEnforcer.MAIN, identifier);
    }


    public Bus(ThreadEnforcer enforcer) {
        this(enforcer, DEFAULT_IDENTIFIER);
    }

    public Bus(ThreadEnforcer enforcer, String identifier) {
        this(enforcer, identifier, Finder.ANNOTATED);
    }

    Bus(ThreadEnforcer enforcer, String identifier, Finder finder) {
        this.enforcer = enforcer;
        this.identifier = identifier;
        this.finder = finder;
    }

    @Override
    public String toString() {
        return "[Bus \"" + identifier + "\"]";
    }


    public void register(Object object) {
          registerFlag = true;
        if (object == null) {
            throw new NullPointerException("注册对象不能为空");
        }
        enforcer.enforce(this);

        Map<EventType, ProducerEvent> foundProducers = finder.findAllProducers(object);
        for (EventType type : foundProducers.keySet()) {

            final ProducerEvent producer = foundProducers.get(type);
            ProducerEvent previousProducer = producersByType.putIfAbsent(type, producer);
            //checking if the previous producer existed
            if (previousProducer != null) {
                throw new IllegalArgumentException("Producer method for type " + type
                        + " found on type " + producer.getTarget().getClass()
                        + ", but already registered by type " + previousProducer.getTarget().getClass() + ".");
            }
            Set<SubscriberEvent> subscribers = subscribersByType.get(type);
            if (subscribers != null && !subscribers.isEmpty()) {
                for (SubscriberEvent subscriber : subscribers) {
                    dispatchProducerResult(subscriber, producer);
                }
            }
        }

        Map<EventType, Set<SubscriberEvent>> foundSubscribersMap = finder.findAllSubscribers(object);
        for (EventType type : foundSubscribersMap.keySet()) {
            Set<SubscriberEvent> subscribers = subscribersByType.get(type);
            if (subscribers == null) {
                //concurrent put if absent
                Set<SubscriberEvent> SubscribersCreation = new CopyOnWriteArraySet<>();
                subscribers = subscribersByType.putIfAbsent(type, SubscribersCreation);
                if (subscribers == null) {
                    subscribers = SubscribersCreation;
                }
            }
            final Set<SubscriberEvent> foundSubscribers = foundSubscribersMap.get(type);
            if (!subscribers.addAll(foundSubscribers)) {
                throw new IllegalArgumentException("Object already registered.");
            }
        }

        for (Map.Entry<EventType, Set<SubscriberEvent>> entry : foundSubscribersMap.entrySet()) {
            EventType type = entry.getKey();
            ProducerEvent producer = producersByType.get(type);
            if (producer != null && producer.isValid()) {
                Set<SubscriberEvent> subscriberEvents = entry.getValue();
                for (SubscriberEvent subscriberEvent : subscriberEvents) {
                    if (!producer.isValid()) {
                        break;
                    }
                    if (subscriberEvent.isValid()) {
                        dispatchProducerResult(subscriberEvent, producer);
                    }
                }
            }
        }
    }

    private void dispatchProducerResult(final SubscriberEvent subscriberEvent, ProducerEvent producer) {
        producer.produce().subscribe(new Action1<Object>() {
            @Override
            public void call(Object event) {
                if (event != null) {
                    dispatch(event, subscriberEvent);
                }
            }
        });
    }


    public void unregister(Object object) {
        registerFlag = false;
        if (object == null) {
            throw new NullPointerException("Object to unregister must not be null.");
        }
        enforcer.enforce(this);

        Map<EventType, ProducerEvent> producersInListener = finder.findAllProducers(object);
        for (Map.Entry<EventType, ProducerEvent> entry : producersInListener.entrySet()) {
            final EventType key = entry.getKey();
            ProducerEvent producer = getProducerForEventType(key);
            ProducerEvent value = entry.getValue();

            if (value == null || !value.equals(producer)) {
                throw new IllegalArgumentException(
                        "Missing event producer for an annotated method. Is " + object.getClass()
                                + " registered?");
            }
            producersByType.remove(key).invalidate();
        }

        Map<EventType, Set<SubscriberEvent>> subscribersInListener = finder.findAllSubscribers(object);
        for (Map.Entry<EventType, Set<SubscriberEvent>> entry : subscribersInListener.entrySet()) {
            Set<SubscriberEvent> currentSubscribers = getSubscribersForEventType(entry.getKey());
            Collection<SubscriberEvent> eventMethodsInListener = entry.getValue();

            if (currentSubscribers == null || !currentSubscribers.containsAll(eventMethodsInListener)) {
                throw new IllegalArgumentException(
                        "Missing event subscriber for an annotated method. Is " + object.getClass()
                                + " registered?");
            }

            for (SubscriberEvent subscriber : currentSubscribers) {
                if (eventMethodsInListener.contains(subscriber)) {
                    subscriber.invalidate();
                }
            }
            currentSubscribers.removeAll(eventMethodsInListener);
        }
    }


    public void post(Object event) {
        post(Tag.DEFAULT, event);
    }


    public void post(String tag, Object event) {
        if (event == null) {
            throw new NullPointerException("Event to post must not be null.");
        }
        enforcer.enforce(this);

        Set<Class<?>> dispatchClasses = flattenHierarchy(event.getClass());

        boolean dispatched = false;
        for (Class<?> clazz : dispatchClasses) {
            Set<SubscriberEvent> wrappers = getSubscribersForEventType(new EventType(tag, clazz));

            if (wrappers != null && !wrappers.isEmpty()) {
                dispatched = true;
                for (SubscriberEvent wrapper : wrappers) {
                    dispatch(event, wrapper);
                }
            }
        }

        if (!dispatched && !(event instanceof DeadEvent)) {
            post(new DeadEvent(this, event));
        }
    }


    protected void dispatch(Object event, SubscriberEvent wrapper) {
        if (wrapper.isValid()) {
            wrapper.handle(event);
        }
    }


    ProducerEvent getProducerForEventType(EventType type) {
        return producersByType.get(type);
    }


    Set<SubscriberEvent> getSubscribersForEventType(EventType type) {
        return subscribersByType.get(type);
    }

    Set<Class<?>> flattenHierarchy(Class<?> concreteClass) {
        Set<Class<?>> classes = flattenHierarchyCache.get(concreteClass);
        if (classes == null) {
            Set<Class<?>> classesCreation = getClassesFor(concreteClass);
            classes = flattenHierarchyCache.putIfAbsent(concreteClass, classesCreation);
            if (classes == null) {
                classes = classesCreation;
            }
        }

        return classes;
    }

    private Set<Class<?>> getClassesFor(Class<?> concreteClass) {
        List<Class<?>> parents = new LinkedList<>();
        Set<Class<?>> classes = new HashSet<>();

        parents.add(concreteClass);

        while (!parents.isEmpty()) {
            Class<?> clazz = parents.remove(0);
            classes.add(clazz);

            Class<?> parent = clazz.getSuperclass();
            if (parent != null) {
                parents.add(parent);
            }
        }
        return classes;
    }

    /**
     * 判断是否在当前页面注册
     * @return
     */
    public boolean isRegister(){
        return registerFlag;
    }

}
