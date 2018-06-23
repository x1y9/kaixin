package com.kaixin.core.db;

import org.aopalliance.intercept.ConstructorInterceptor;
import org.aopalliance.intercept.MethodInterceptor;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.Filter;
import org.glassfish.hk2.api.InterceptionService;
import org.jvnet.hk2.annotations.Service;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

/*
 * 事务注解的Service定义
 */

@Service
public class TransactionService implements InterceptionService {

    private static final TransactionInterceptor RESOURCE_INTERCEPTOR = new TransactionInterceptor();

    private static final List<MethodInterceptor> RESOURCE_METHOD_INTERCEPTORS =
            Collections.<MethodInterceptor>singletonList(RESOURCE_INTERCEPTOR);
    
	@Override
	public Filter getDescriptorFilter() {
		// 这里效率不是很多，给的包名太大了，导致下面的getMethodInterceptors调用次数太多
        return new Filter() {
            @Override
            public boolean matches(final Descriptor d) {
                final String clazz = d.getImplementation();
                return clazz.startsWith("com.kaixin");
            }
        };
	}

	@Override
	public List<MethodInterceptor> getMethodInterceptors(Method method) {
		if (method.isAnnotationPresent(Transactional.class)) {
			return RESOURCE_METHOD_INTERCEPTORS;
	    }
        return null;
	}

	@Override
	public List<ConstructorInterceptor> getConstructorInterceptors(
			Constructor<?> constructor) {
        return null;
	}

}
