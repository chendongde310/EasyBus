package com.renwey.emi.rxbus.finder;


import com.renwey.emi.rxbus.annotation.Produce;
import com.renwey.emi.rxbus.annotation.Subscribe;
import com.renwey.emi.rxbus.annotation.Tag;
import com.renwey.emi.rxbus.entity.EventType;
import com.renwey.emi.rxbus.entity.ProducerEvent;
import com.renwey.emi.rxbus.entity.SubscriberEvent;
import com.renwey.emi.rxbus.thread.EventThread;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 作者：陈东   www.renwey.com
 * 日期：2016/8/11 - 20:12
 */
public final class AnnotatedFinder {


    private static final ConcurrentMap<Class<?>, Map<EventType, SourceMethod>> PRODUCERS_CACHE =
            new ConcurrentHashMap<>();


    private static final ConcurrentMap<Class<?>, Map<EventType, Set<SourceMethod>>> SUBSCRIBERS_CACHE =
            new ConcurrentHashMap<>();

    private AnnotatedFinder() {

    }

    private static void loadAnnotatedProducerMethods(Class<?> listenerClass,
                                                     Map<EventType, SourceMethod> producerMethods) {
        Map<EventType, Set<SourceMethod>> subscriberMethods = new HashMap<>();
        loadAnnotatedMethods(listenerClass, producerMethods, subscriberMethods);
    }

    private static void loadAnnotatedSubscriberMethods(Class<?> listenerClass,
                                                       Map<EventType, Set<SourceMethod>> subscriberMethods) {
        Map<EventType, SourceMethod> producerMethods = new HashMap<>();
        loadAnnotatedMethods(listenerClass, producerMethods, subscriberMethods);
    }

    private static void loadAnnotatedMethods(Class<?> listenerClass,
                                             Map<EventType, SourceMethod> producerMethods, Map<EventType, Set<SourceMethod>> subscriberMethods) {
        for (Method method : listenerClass.getDeclaredMethods()) {

            if (method.isBridge()) {
                continue;
            }
            if (method.isAnnotationPresent(Subscribe.class)) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length != 1) {
                    throw new IllegalArgumentException("方法 " + method + " 有@Subscribe，但 "
                            + parameterTypes.length + " 必须需要一个单独的参数。");
                }

                Class<?> parameterClazz = parameterTypes[0];
                if (parameterClazz.isInterface()) {
                    throw new IllegalArgumentException("方法 " + method + " 有@Subscribe，但 " + parameterClazz
                            + " 这是一个接口。订阅过程必须在一个具体的类.");
                }

                if ((method.getModifiers() & Modifier.PUBLIC) == 0) {
                    throw new IllegalArgumentException("方法 " + method + " 有@Subscribe，但 " + parameterClazz
                            + " 不是 'public'.");
                }

                Subscribe annotation = method.getAnnotation(Subscribe.class);
                EventThread thread = annotation.thread();
                Tag[] tags = annotation.tags();
                int tagLength = (tags == null ? 0 : tags.length);
                do {
                    String tag = Tag.DEFAULT;
                    if (tagLength > 0) {
                        tag = tags[tagLength - 1].value();
                    }
                    EventType type = new EventType(tag, parameterClazz);
                    Set<SourceMethod> methods = subscriberMethods.get(type);
                    if (methods == null) {
                        methods = new HashSet<>();
                        subscriberMethods.put(type, methods);
                    }
                    methods.add(new SourceMethod(thread, method));
                    tagLength--;
                } while (tagLength > 0);
            } else if (method.isAnnotationPresent(Produce.class)) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length != 0) {
                    throw new IllegalArgumentException("方法 " + method + "有@Produce但 "
                            + parameterTypes.length + " 方法必须要求无参数");
                }
                if (method.getReturnType() == Void.class) {
                    throw new IllegalArgumentException("方法 " + method
                            + " 有一个空类型。你必须声明一个非空类型.");
                }

                Class<?> parameterClazz = method.getReturnType();
                if (parameterClazz.isInterface()) {
                    throw new IllegalArgumentException("方法 " + method + " 有@Produce但 " + parameterClazz
                            + " 这是一个接口，你需要实例化");
                }
                if (parameterClazz.equals(Void.TYPE)) {
                    throw new IllegalArgumentException("方法 " + method + " 有@Produce但没有返回类型");
                }

                if ((method.getModifiers() & Modifier.PUBLIC) == 0) {
                    throw new IllegalArgumentException("方法 " + method + " 有@Produce但 " + parameterClazz
                            + "不是  'public'.");
                }

                Produce annotation = method.getAnnotation(Produce.class);
                EventThread thread = annotation.thread();
                Tag[] tags = annotation.tags();
                int tagLength = (tags == null ? 0 : tags.length);
                do {
                    String tag = Tag.DEFAULT;
                    if (tagLength > 0) {
                        tag = tags[tagLength - 1].value();
                    }
                    EventType type = new EventType(tag, parameterClazz);
                    if (producerMethods.containsKey(type)) {
                        throw new IllegalArgumentException("这个类生产者" + type + " 已注册.");
                    }
                    producerMethods.put(type, new SourceMethod(thread, method));
                    tagLength--;
                } while (tagLength > 0);
            }
        }

        PRODUCERS_CACHE.put(listenerClass, producerMethods);
        SUBSCRIBERS_CACHE.put(listenerClass, subscriberMethods);
    }

    static Map<EventType, ProducerEvent> findAllProducers(Object listener) {
        final Class<?> listenerClass = listener.getClass();
        Map<EventType, ProducerEvent> producersInMethod = new HashMap<>();

        Map<EventType, SourceMethod> methods = PRODUCERS_CACHE.get(listenerClass);
        if (null == methods) {
            methods = new HashMap<>();
            loadAnnotatedProducerMethods(listenerClass, methods);
        }
        if (!methods.isEmpty()) {
            for (Map.Entry<EventType, SourceMethod> e : methods.entrySet()) {
                ProducerEvent producer = new ProducerEvent(listener, e.getValue().method, e.getValue().thread);
                producersInMethod.put(e.getKey(), producer);
            }
        }

        return producersInMethod;
    }

    static Map<EventType, Set<SubscriberEvent>> findAllSubscribers(Object listener) {
        Class<?> listenerClass = listener.getClass();
        Map<EventType, Set<SubscriberEvent>> subscribersInMethod = new HashMap<>();

        Map<EventType, Set<SourceMethod>> methods = SUBSCRIBERS_CACHE.get(listenerClass);
        if (null == methods) {
            methods = new HashMap<>();
            loadAnnotatedSubscriberMethods(listenerClass, methods);
        }
        if (!methods.isEmpty()) {
            for (Map.Entry<EventType, Set<SourceMethod>> e : methods.entrySet()) {
                Set<SubscriberEvent> subscribers = new HashSet<>();
                for (SourceMethod m : e.getValue()) {
                    subscribers.add(new SubscriberEvent(listener, m.method, m.thread));
                }
                subscribersInMethod.put(e.getKey(), subscribers);
            }
        }

        return subscribersInMethod;
    }

    private static class SourceMethod {
        private EventThread thread;
        private Method method;

        private SourceMethod(EventThread thread, Method method) {
            this.thread = thread;
            this.method = method;
        }
    }

}
