package com.kaixin.core.sql2o.reflection;

/**
 * The Setter interface is used by sql2o to set property values when doing automatic column to property mapping
 */
public interface Setter {

    void setProperty(Object obj, Object value);
    Class getType();
}
