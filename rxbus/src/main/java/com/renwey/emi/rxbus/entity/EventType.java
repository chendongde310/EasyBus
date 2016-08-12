package com.renwey.emi.rxbus.entity;

/**
 * 作者：陈东   www.renwey.com
 * 日期：2016/8/11 - 20:25
 */
public class EventType {


    private final String tag;

    private final Class<?> clazz;

    private final int hashCode;


    public EventType(String tag, Class<?> clazz) {
        if (tag == null) {
            throw new NullPointerException("EventType Tag cannot be null.");
        }
        if (clazz == null) {
            throw new NullPointerException("EventType Clazz cannot be null.");
        }

        this.tag = tag;
        this.clazz = clazz;


        final int prime = 31;
        hashCode = (prime + tag.hashCode()) * prime + clazz.hashCode();
    }

    @Override
    public String toString() {
        return "[EventType " + tag + " && " + clazz + "]";
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

        final EventType other = (EventType) obj;

        return tag.equals(other.tag) && clazz == other.clazz;
    }

}