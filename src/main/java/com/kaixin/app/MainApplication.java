package com.kaixin.app;

import com.kaixin.core.api.AppApi;
import com.kaixin.core.app.KxApp;
import com.kaixin.core.app.KxConfiguration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class MainApplication extends KxApp {
	
    public static void main(String[] args) throws Exception {

    	if (args.length == 0)
    		new MainApplication().run("server", "server.yml");
    	else
    		new MainApplication().run(args);
    }

    @Override    
    public void initialize(Bootstrap<KxConfiguration> bootstrap) {
    	super.initialize(bootstrap);
    }
    
    @Override
    public void run(KxConfiguration configuration, Environment environment) throws Exception {
    	super.run(configuration, environment);
    	
    	//注册app自己的资源
        environment.jersey().register(TopGoodsApi.class);
    	environment.jersey().register(MainView.class);    	
    }
}