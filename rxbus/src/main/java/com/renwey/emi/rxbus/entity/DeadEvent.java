package com.renwey.emi.rxbus.entity;


 /**
  *
  * 作者：陈东   www.renwey.com
  * 日期：2016/8/11 - 20:05
  *
  */
public class DeadEvent {

    public final Object source;
    public final Object event;


    public DeadEvent(Object source, Object event) {
        this.source = source;
        this.event = event;
    }

}
