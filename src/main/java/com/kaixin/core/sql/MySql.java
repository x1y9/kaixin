package com.kaixin.core.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MySql extends Sql{

	private static final Logger _log = LoggerFactory.getLogger(MySql.class);

	
	public MySql(String _driver, String _url, String _user, String _pass) {
		super(_driver, _url, _user, _pass);
		
		meta.setDbType(SqlMeta.TYPE_MYSQL);
		meta.setDbDialect(SqlMeta.DIALECT_MYSQL);
	}

	
	/*
	 * SQL封装
	 */
	@Override
	public String limit(long offset, long pagesize) {
		if (offset < 0 || pagesize < 0)
			return "";
		else
			return "LIMIT " + pagesize + " OFFSET " + offset;
	}

	@Override
	public String date2String(String column) {
		return "DATE_FORMAT(" + column + ",'%Y-%m-%d %H:%i:%S')";
	}
}
