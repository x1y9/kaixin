package com.kaixin.core.sql;

public class Statement {

	public static final String WHERE_ = "WHERE ";
	public static final String PARA = "?";
	public static final String _ASC = " ASC";
	public static final String _DESC = " DESC";
	public static final String SELECT_ = "SELECT ";
	public static final String UPDATE_ = "UPDATE ";
	public static final String INSERT_INTO_ = "INSERT INTO ";
	public static final String DELETE_ = "DELETE ";
	public static final String COUNT_ALL = "COUNT(*)";
	public static final String COUNT = "COUNT";
	public static final String FROM_ = "FROM ";
	public static final String _FROM_ = " FROM ";
	public static final String LEFT_JOIN_ = "LEFT JOIN ";
	public static final String INNER_JOIN_ = "INNER JOIN ";
	public static final String GROUP_BY_ = "GROUP BY ";
	public static final String ORDER_BY_ = "ORDER BY ";
	public static final String _ON_ = " ON ";
	public static final String LIMIT_ = "LIMIT ";
	public static final String COMMA_ = ", ";
	public static final String HAVING_ = "HAVING ";
	
	public String toSql() {
		return "ERROR";	
	}
}
