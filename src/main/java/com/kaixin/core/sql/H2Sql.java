package com.kaixin.core.sql;


import org.h2.tools.Server;

import java.sql.SQLException;

public class H2Sql extends Sql{

	public H2Sql(String _driver, String _url, String _user, String _pass) {
		super(_driver, _url, _user, _pass);
		meta.setDbType(SqlMeta.TYPE_H2);
		meta.setDbDialect(SqlMeta.DIALECT_H2);

		//不用8082以免和本地h2冲突
//		try {
//			Server.createWebServer("-webPort", "8071", "-tcpAllowOthers").start();
//			Server.createTcpServer("-tcpPort" ,"8073", "-tcpAllowOthers").start();
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}

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
		return "TO_CHAR(" + column + ",'YYYY-MM-DD HH24:MI:ss')";
	}
	
}
