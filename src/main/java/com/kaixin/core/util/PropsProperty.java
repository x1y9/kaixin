package com.kaixin.core.util;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
 * 给property使用的注解，说明一个property的类型，缺省值，是否需要重启以及填写帮助
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PropsProperty {
    public String type();
    public String defaultValue();
    public boolean needRestart();
    public boolean forClient() default false;
    public String help();
}