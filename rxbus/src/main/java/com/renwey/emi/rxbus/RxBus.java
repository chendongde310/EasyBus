package com.renwey.emi.rxbus;


import com.renwey.emi.rxbus.thread.ThreadEnforcer;

/**
 *
 * 作者：陈东   www.renwey.com
 * 日期：2016/8/11 - 20:26
 *
 */
public class RxBus {


    private static Bus sBus;

    /**
     * 获取实例 {@link Bus}
     *
     * @return
     */
    public static synchronized Bus get() {
        if (sBus == null) {
            sBus = new Bus(ThreadEnforcer.ANY);
        }
        return sBus;
    }



}