package com.kaixin.core.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlMeta{

	public static final String TYPE_H2 = "h2";
	public static final String TYPE_MYSQL = "mysql";
	public static final String TYPE_ORACLE = "oracle";
	public static final String TYPE_HSQLDB = "hsqldb";
	
	public final static String DIALECT_H2 = "org.hibernate.dialect.H2Dialect";
	public final static String DIALECT_MYSQL = "org.hibernate.dialect.MySQL5InnoDBDialect"; //这里不用"org.hibernate.dialect.MySQLDialect"是因为它无法在ddl中加入on delete cascade
	public final static String DIALECT_ORACLE = "org.hibernate.dialect.Oracle10gDialect";

	private static final Logger _log = LoggerFactory.getLogger(SqlMeta.class);
	
	protected String cfgUrl;
	protected String cfgDriver;
	protected String cfgUser;
	protected String cfgPass;
	protected String dbType;
	protected String dbDialect;
	
	
	protected SqlMeta(String _driver, String _url, String _user, String _pass){
		cfgDriver = _driver;
		cfgUrl = _url;
		cfgUser = _user;
		cfgPass = _pass;
	}
	
	public String getCfgUrl() {
		return cfgUrl;
	}

	public String getCfgDriver() {
		return cfgDriver;
	}

	public String getCfgUser() {
		return cfgUser;
	}

	public String getCfgPass() {
		return cfgPass;
	}
	
	public void setDbType(String type) {
		dbType = type;
	}
	
	public String getDbType() {
		return dbType;
	}

	public void setDbDialect(String dialect) {
		dbDialect = dialect;
	}

	public String getDbDialect() {
		return dbDialect;
	}
	
	public boolean isH2() {
		return TYPE_H2.equalsIgnoreCase(getDbType());
	}
	
	public boolean isMysql() {
		return TYPE_MYSQL.equalsIgnoreCase(getDbType());
	}

	public boolean isOracle() {
		return TYPE_ORACLE.equalsIgnoreCase(getDbType());
	}
}
