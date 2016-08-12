package com.renwey.emi.rxbus.entity;


import com.renwey.emi.rxbus.thread.EventThread;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import rx.functions.Action1;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;


public class SubscriberEvent extends Event {


    private final Object target;

    private final Method method;

    private final EventThread thread;
    private final int hashCode;
    private Subject subject;
    private boolean valid = true;

    public SubscriberEvent(Object target, Method method, EventThread thread) {
        if (target == null) {
            throw new NullPointerException("SubscriberEvent target cannot be null.");
        }
        if (method == null) {
            throw new NullPointerException("SubscriberEvent method cannot be null.");
        }
        if (thread == null) {
            throw new NullPointerException("SubscriberEvent thread cannot be null.");
        }

        this.target = target;
        this.method = method;
        this.thread = thread;
        method.setAccessible(true);
        initObservable();


        final int prime = 31;
        hashCode = (prime + method.hashCode()) * prime + target.hashCode();
    }

    private void initObservable() {
        subject = PublishSubject.create();
        subject.onBackpressureBuffer().observeOn(EventThread.getScheduler(thread))
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object event) {
                        try {
                            if (valid) {
                                handleEvent(event);
                            }
                        } catch (InvocationTargetException e) {
                            throwRuntimeException("Could not dispatch event: " + event.getClass() + " to subscriber " + SubscriberEvent.this, e);
                        }
                    }
                });
    }

    public boolean isValid() {
        return valid;
    }

    public void invalidate() {
        valid = false;
    }

    public void handle(Object event) {
        subject.onNext(event);
    }

    public Subject getSubject() {
        return subject;
    }

    protected void handleEvent(Object event) throws InvocationTargetException {
        if (!valid) {
            throw new IllegalStateException(toString() + " has been invalidated and can no longer handle events.");
        }
        try {
            method.invoke(target, event);
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
        return "[SubscriberEvent " + method + "]";
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

        final SubscriberEvent other = (SubscriberEvent) obj;

        return method.equals(other.method) && target == other.target;
    }

}
