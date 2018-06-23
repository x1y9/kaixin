package com.kaixin.core.db;

import com.kaixin.core.app.KxApp;
import com.kaixin.core.app.KxConsts;
import com.kaixin.core.sql2o.Connection;
import com.kaixin.core.util.ThreadLocalUtil;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * 实现事务注入代码,要考虑嵌套注解的情况
 */
public class TransactionInterceptor implements MethodInterceptor {

    @Override
    public Object invoke(final MethodInvocation methodInvocation) throws Throwable {

        //已经在事务中
    	if (ThreadLocalUtil.get(KxConsts.TL_TRANSACTION_HANDLE) != null)
    		return methodInvocation.proceed();

        Connection connection = KxApp.sql2o.beginTransaction();
        try {
        	ThreadLocalUtil.set(KxConsts.TL_TRANSACTION_HANDLE, new DbHandle(connection));
            final Object result = methodInvocation.proceed();
            connection.commit();
            return result;
            
        } catch (Exception re) {
            connection.rollback();
            throw re;
        }
        finally {
            ThreadLocalUtil.remove(KxConsts.TL_TRANSACTION_HANDLE);
            connection.close();
        }
    }

}