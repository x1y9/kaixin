package com.kaixin.core.db;

import com.kaixin.core.app.KxApp;
import com.kaixin.core.app.KxConsts;
import com.kaixin.core.sql.Statement;
import com.kaixin.core.sql2o.Connection;
import com.kaixin.core.sql2o.Query;
import com.kaixin.core.util.ThreadLocalUtil;

import java.io.Closeable;

/*
 * 对db进行封装，以支持更容易的替换db访问层
 * 两种方式获取DbHandle：
 *  1. 在@transaction方法里，可以取公用transactionInstance
 *  2. 从DBI重新手工创建一个出来manualInstance，这个要手动关闭
 */
public class DbHandle implements Closeable{

	private Connection connection;
	
	public DbHandle(Connection connection) {
		this.connection = connection;
	}
	
	/* transactionInstance 不需要关闭，不需要提交, 会在注解退出后自动处理  */
	public static DbHandle transactionInstance() {
		DbHandle handle = (DbHandle)ThreadLocalUtil.get(KxConsts.TL_TRANSACTION_HANDLE);
		if (handle == null)
			throw new RuntimeException("must use in @transaction method");
		
		return handle;		
	}
	
	/* manualInstance 需要手动关闭,或放在try块内 */
	public static DbHandle manualInstance()  {
		return new DbHandle(KxApp.sql2o.open());
	}
	
	public Connection getHandle() {
		return connection;
	}
	
	public Query query(Statement query) { return connection.createQuery(query.toSql()); }
	public Query query(Statement query,boolean returnKey) { return connection.createQuery(query.toSql(), returnKey); }

	

	@Override
	public void close() {
		if (connection != null)
			connection.close();
	}


}
