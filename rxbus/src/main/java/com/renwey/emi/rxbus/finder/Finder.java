package com.renwey.emi.rxbus.finder;


import com.renwey.emi.rxbus.entity.EventType;
import com.renwey.emi.rxbus.entity.ProducerEvent;
import com.renwey.emi.rxbus.entity.SubscriberEvent;

import java.util.Map;
import java.util.Set;

/**
 * Finds producer and subscriber methods.
 */
public interface Finder {

    Finder ANNOTATED = new Finder() {
        @Override
        public Map<EventType, ProducerEvent> findAllProducers(Object listener) {
            return AnnotatedFinder.findAllProducers(listener);
        }

        @Override
        public Map<EventType, Set<SubscriberEvent>> findAllSubscribers(Object listener) {
            return AnnotatedFinder.findAllSubscribers(listener);
        }
    };

    Map<EventType, ProducerEvent> findAllProducers(Object listener);

    Map<EventType, Set<SubscriberEvent>> findAllSubscribers(Object listener);
}
