package com.kaixin.core.db;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/*
 * DbRunner完成对QueryRunner和Connection的简单封装，使用更方便。
 * 
 * insert接口和update接口的区别是insert可以返回生成的key
 * 通常情况下key为long，所以使用insert即可
 * 特殊情况下可以用insertWithRsHandle，传入自定义的handler
 * 
 * batch接口只提供了updateBatch，insert语句可以使用（只是不返回key）
 * 因为一般应用场景下insert batch时不再需要key了
 */
public class DbRunner {
	private QueryRunner runner;
	private Connection connection;
	private MapListHandler rsHandler;
	private ScalarHandler<Long> longKeyHandler;
	
	public DbRunner(Connection connection) {
		this.connection = connection;
		runner = new QueryRunner();
		rsHandler = new MapListHandler();
		longKeyHandler = new ScalarHandler<Long>();
	}
	
	public Connection getConnection() {
		return connection;
	}
	
	public List<Map<String,Object>> query(String sql, Object... params) throws SQLException {
		return runner.query(connection, sql, rsHandler, params);
	}
	
	public int update(String sql, Object... params) throws SQLException {
		return runner.update(connection, sql, params);
	}

	public int[] updateBatch(String sql, Object[][] params) throws SQLException {
		return runner.batch(connection, sql, params);
	}
	
	public Long insert(String sql, Object... params) throws SQLException {
		return runner.insert(connection, sql, longKeyHandler, params);
	}
	
	public <T> T insertWithRsHandler(String sql, ResultSetHandler<T> handler, Object... params) throws SQLException {
		return runner.insert(connection, sql, handler, params);
	}
	
	public void commit() throws SQLException {
		connection.commit();
	}

	public void rollback() throws SQLException {
		connection.rollback();
	}

}
