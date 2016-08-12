package com.renwey.emi.myrxbus.bean;

/**
 * 作者：陈东  —  www.renwey.com
 * 日期：2016/8/11 - 20:59
 * 注释：对象对象
 */
public class Lover {

    String name;
    int age;

    public Lover(String name,  int age) {
        this.name = name;
        this.age = age;
    }

    @Override
    public String toString() {
        return "Lover{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}
