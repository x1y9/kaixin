package com.kaixin.core.sql2o.reflection;

import java.lang.reflect.Method;

/**
 * @author mdelapenya
 */
public interface MethodGetterFactory {
    Getter newGetter(Method method);
}