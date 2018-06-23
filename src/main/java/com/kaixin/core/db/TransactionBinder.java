package com.kaixin.core.db;


import org.glassfish.hk2.utilities.binding.AbstractBinder;

import javax.inject.Singleton;

/**
 * 在 HK2里注册事务注解
 * 
 */
public class TransactionBinder extends AbstractBinder {

    @Override
    protected void configure() {
        bind(TransactionService.class)
                .to(org.glassfish.hk2.api.InterceptionService.class)
                .in(Singleton.class);
    }
}