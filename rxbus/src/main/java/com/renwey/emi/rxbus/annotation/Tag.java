package com.renwey.emi.rxbus.annotation;

 

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

 /**
  * 
  * 作者：陈东   www.renwey.com
  * 日期：2016/8/11 - 20:22
  * 
  */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Tag {
    static final String DEFAULT = "rxbus_default_tag";

    String value() default DEFAULT;
}
