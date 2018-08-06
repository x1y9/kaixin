package com.kaixin.core.app;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.kaixin.core.api.*;
import com.kaixin.core.filter.AuthFilter;
import com.kaixin.core.auth.Authenticator;
import com.kaixin.core.db.TransactionBinder;
import com.kaixin.core.filter.CORSFilter;
import com.kaixin.core.filter.HttpsFilter;
import com.kaixin.core.filter.MetricListener;
import com.kaixin.core.profile.Profile;
import com.kaixin.core.module.KxLocale;
import com.kaixin.core.module.KxSmtp;
import com.kaixin.core.servlet.ProxyServlet;
import com.kaixin.core.sql.Sql;
import com.kaixin.core.sql.SqlFactory;
import com.kaixin.core.sql2o.Sql2o;
import com.kaixin.core.util.PropsKeys;
import com.kaixin.core.util.PropsUtil;
import com.kaixin.core.view.AuthView;
import com.kaixin.core.view.FormView;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.lifecycle.ServerLifecycleListener;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.sql.DataSource;
import java.io.InputStream;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/*
 * KxApp基类
 */
public class KxApp extends Application<KxConfiguration> implements ServerLifecycleListener {
	private static final Logger log = LoggerFactory.getLogger(KxApp.class);

	public static ObjectMapper mapper;
	public static KxConfiguration configuration;
	public static KxSmtp smtp;
	public static KxLocale locale;
	public static Authenticator authenticator;
	public static Profile profile;
	public static Sql sql;
	public static Sql2o sql2o;
	public static DataSource dataSource;  //RWSPLIT?
	public static ExecutorService executor;
	public static Scheduler scheduler;

    @Override
    public String getName() {
        return "FusionApplication";
    }

    @Override
    public void initialize(Bootstrap<KxConfiguration> bootstrap) {
    	//因为jersey占据了/,这里的uri不能是/，只能用/public
    	bootstrap.addBundle(new AssetsBundle("/public", "/public", "index.html"));
		//多个分应用可以占据不同的url，通过proerty配置
		for (String sub: PropsUtil.getArray(PropsKeys.SYS_ASSERT_PATH)) {
			if (sub.trim().length() > 0) {
				bootstrap.addBundle(new AssetsBundle("/" + sub.trim(), "/" + sub.trim(), "index.html"));
			}
		}

    	bootstrap.addBundle(new ViewBundle<KxConfiguration>(){
    		@Override
    		public Map<String,Map<String,String>> getViewConfiguration(KxConfiguration config){
    			return config.view;
    		}
    	});
	    bootstrap.addBundle(new MigrationsBundle<KxConfiguration>() {
	        public DataSourceFactory getDataSourceFactory(KxConfiguration config) {
	            return config.database;
	       }
	    });
    }

    @Override
    public void run(KxConfiguration configuration, Environment environment) throws Exception {
		
    	this.configuration = configuration;
    	
    	mapper = new ObjectMapper();
    	mapper.enable(SerializationFeature.INDENT_OUTPUT);
    	mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
    	mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
    	
    	locale = new KxLocale();
    	dataSource = configuration.database.build(environment.metrics(), "mainds");
    	sql = SqlFactory.createSql(configuration.database);
		sql2o = new Sql2o(dataSource);

    	authenticator = new Authenticator(configuration.ldap, sql);
    	smtp = new KxSmtp();
    	executor = Executors.newFixedThreadPool(PropsUtil.getInteger(PropsKeys.SYS_EXECUTOR_THREADS_NUM));
    	scheduler = StdSchedulerFactory.getDefaultScheduler();
    	
		try(InputStream is = this.getClass().getResourceAsStream("/profile.json")){
			profile = mapper.readValue(is, Profile.class);
		}
		
		loadFilter(environment.getApplicationContext(), HttpsFilter.class);
		loadServlet(environment.getApplicationContext(), ProxyServlet.class);

		environment.jersey().register(CORSFilter.class);
    	environment.jersey().register(new AuthFilter(profile)); //singleton scope
    	environment.jersey().register(MultiPartFeature.class);	//request scope
    	environment.jersey().register(MetricListener.class);
    	environment.jersey().register(KxExceptionMapper.class);
    	
    	environment.jersey().register(FileApi.class);
    	environment.jersey().register(AppApi.class);
    	environment.jersey().register(AuthApi.class);
    	environment.jersey().register(new DbMngApi(dataSource));
    	environment.jersey().register(AdminApi.class);
    	environment.jersey().register(MetricApi.class);
		environment.jersey().register(PropertyApi.class);
		environment.jersey().register(LoggerApi.class);
    	environment.jersey().register(AuthView.class);
    	environment.jersey().register(FormView.class);
    	environment.jersey().register(new TransactionBinder());
    	
    	environment.lifecycle().addServerLifecycleListener(this);
    }

    private void loadServlet(ServletContextHandler context, Class<? extends Servlet> clazz) {

    	Map<String, String> initParas = KxApp.configuration.servlet.get(clazz.getName());
    	if (initParas == null || initParas.get(KxConsts.CONFIG_URL_PATTERN) == null)
    		return;
    	
    	ServletHolder holder = context.addServlet(clazz, initParas.get(KxConsts.CONFIG_URL_PATTERN));    	
    	for(String para : initParas.keySet())
    		if (!para.startsWith("__"))
    			holder.setInitParameter(para, initParas.get(para));
    }
    
    private void loadFilter(ServletContextHandler context, Class<? extends Filter> clazz) {

    	Map<String, String> initParas = KxApp.configuration.filter.get(clazz.getName());
    	if (initParas == null || initParas.get(KxConsts.CONFIG_URL_PATTERN) == null)
    		return;
    	
    	context.addFilter(new FilterHolder(clazz), initParas.get(KxConsts.CONFIG_URL_PATTERN), EnumSet.of(DispatcherType.REQUEST));    	
    }
    
	@Override
	public void serverStarted(Server arg0) {
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        long memory = runtime.totalMemory() - runtime.freeMemory();
        log.info("Kaixin started, Used memory in bytes: " + memory);
    }
	
}