package com.renwey.emi.rxbus.entity;

import java.lang.reflect.InvocationTargetException;
/**
 * 
 * 作者：陈东   www.renwey.com
 * 日期：2016/8/11 - 20:25
 * 
 */
abstract class Event {
 
    public void throwRuntimeException(String msg, InvocationTargetException e) {
        throwRuntimeException(msg, e.getCause());
    }

 
    public void throwRuntimeException(String msg, Throwable e) {
        Throwable cause = e.getCause();
        if (cause != null) {
            throw new RuntimeException(msg + ": " + cause.getMessage(), cause);
        } else {
            throw new RuntimeException(msg + ": " + e.getMessage(), e);
        }
    }
}