package com.kaixin.core.sql;

import io.dropwizard.db.DataSourceFactory;


public class SqlFactory {

	public static Sql createSql(DataSourceFactory dsf) throws Exception {
		
		String _driver = dsf.getDriverClass();
		String _url = dsf.getUrl();
		String _user = dsf.getUser();
		String _pass = dsf.getPassword();
		
		if (_url == null || _driver == null || _user == null || _pass == null)
			throw new Exception("jdbc parra error");
		
		if (_driver.contains("mysql")) {
			return  new MySql(_driver, _url, _user, _pass);
		}
		else if (_driver.contains("h2")){
			return new H2Sql(_driver, _url, _user, _pass);
		}
		else {
			throw new Exception("unsupported database type");
		}
	}

}
