package com.kaixin.core.app;

import com.kaixin.core.config.LdapConfiguration;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.Map;

/*
 * 对server.yml的封装，因为server.yml只在初始化时加载一次，
 * 所以如果有些配置是希望立即生效的，就不要做到这里，而应该放在数据库里。
 */
public class KxConfiguration extends Configuration {
	
	@Valid
    @NotNull
    public DataSourceFactory database = new DataSourceFactory();
	
	public LdapConfiguration ldap = new LdapConfiguration();

	public Map<String, Map<String, String>> view = Collections.emptyMap();
	public Map<String, Map<String, String>> filter = Collections.emptyMap();
	public Map<String, Map<String, String>> servlet = Collections.emptyMap();

	
	 
}