package com.renwey.emi.rxbus.entity;


import com.renwey.emi.rxbus.thread.EventThread;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import rx.Observable;
import rx.Subscriber;


public class ProducerEvent extends Event {


    private final Object target;

    private final Method method;

    private final EventThread thread;

    private final int hashCode;

    private boolean valid = true;

    public ProducerEvent(Object target, Method method, EventThread thread) {
        if (target == null) {
            throw new NullPointerException("EventProducer target cannot be null.");
        }
        if (method == null) {
            throw new NullPointerException("EventProducer method cannot be null.");
        }

        this.target = target;
        this.thread = thread;
        this.method = method;
        method.setAccessible(true);

        final int prime = 31;
        hashCode = (prime + method.hashCode()) * prime + target.hashCode();
    }

    public boolean isValid() {
        return valid;
    }


    public void invalidate() {
        valid = false;
    }


    public Observable produce() {
        return Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                try {
                    subscriber.onNext(produceEvent());
                    subscriber.onCompleted();
                } catch (InvocationTargetException e) {
                    throwRuntimeException("Producer " + ProducerEvent.this + " threw an exception.", e);
                }
            }
        }).subscribeOn(EventThread.getScheduler(thread));
    }


    private Object produceEvent() throws InvocationTargetException {
        if (!valid) {
            throw new IllegalStateException(toString() + " has been invalidated and can no longer produce events.");
        }
        try {
            return method.invoke(target);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Error) {
                throw (Error) e.getCause();
            }
            throw e;
        }
    }

    @Override
    public String toString() {
        return "[EventProducer " + method + "]";
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ProducerEvent other = (ProducerEvent) obj;
        return method.equals(other.method) && target == other.target;
    }

    public Object getTarget() {
        return target;
    }
}
