package com.kaixin.core.sql;

import com.kaixin.core.app.KxConsts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


/**
 * 
 * 对不同数据库及SQL进行封装，并在系统启动和关闭时执行相应操作。
 * 
 */
public class Sql {

	public static final String EQ = "=";
	public static final String EQ_SAFE = "EQ";
	public static final String NEQ = "<>";
	public static final String NEQ_SAFE = "NEQ";
	public static final String IS_NULL = "IS NULL";
	public static final String IS_NOT_NULL = "IS NOT NULL";
	public static final String LT = "<";
	public static final String GT = ">";
	public static final String LE = "<=";
	public static final String GE = ">=";
	public static final String LIKE = "LIKE";
	public static final String AND = "AND";
	public static final String OR = "OR";
	public static final String IN = "IN";
	public static final String NOT_IN = "NOT IN";
	public static final String BETWEEN = "BETWEEN";
	
	public static final String ALWAYS_TRUE = "1 > 0";
	public static final String ALWAYS_FALSE = "0 > 1";


	private static final Logger _log = LoggerFactory.getLogger(Sql.class);

	protected SqlMeta meta;

	public SqlMeta getMeta() {
		return meta;
	}

	protected Sql(String _driver, String _url, String _user, String _pass) {
		meta = new SqlMeta(_driver, _url, _user, _pass);
	}

	/*
	 * 执行数据库启动操作，不同的数据库重载后完成不同的启动操作
	 */
	public void onBoot() throws Exception {
	}

	public void onShutdown() throws Exception {
	}

	/*
	 * SQL条件 封装
	 */
	public String alwaysTrue() {
		return ALWAYS_TRUE;
	}

	public String alwaysFalse() {
		return ALWAYS_FALSE;
	}

	

	/*
	 * SQL子句
	 */

	//不同数据库limit语句有差异
	public String limit(long offset, long pagesize) {
		if (offset < 0 || pagesize < 0)
			return "";
		else
			return Statement.LIMIT_ + offset + Statement.COMMA_ + pagesize;
	}

	// 为字段或表生成别名子句，MySQL和H2支持用as或空格，Oracle只支持空格
	public String alias(String field, String alias) {
		return field + " " + alias;
	}
	
	//为aliasList的最后一个表生成别名
	public String alias(List<String> aliasList) {
		return alias(aliasList.size());
	}

	//为某个顺序号的表生成别名
	public String alias(int index) {
		return "a" + index;
	}

	public String field(String table, String column) {
		return table + "." + column;
	}

	public String escapeLike(String search) {
		// mysql和h2的like中的下划线，可以加反斜杠转义
		return "%" + search.toLowerCase().replace("_", "\\_") + "%";
	}
	/*
	 * SQL函数
	 */

	public String date2String(String column) {
		return "CONVERT(varchar(19)," + column + ",120)";
	}


	/*
	 * SQL语句封装，DSL方式
	 */

	public QueryStatement select(String table) {
		return new QueryStatement(this,table);
	}
	public QueryStatement selectCount(String table) {
		return new QueryStatement(this,table).field(Statement.COUNT_ALL);
	}
	public UpdateStatement update(String table) {
		return new UpdateStatement(this,table);
	}

	public DeleteStatement delete(String table) {
		return new DeleteStatement(this,table);
	}
	public InsertStatement insert(String table) {
		return new InsertStatement(this,table);
	}
	public QueryStatement selectById(String table) {
		return new QueryStatement(this,table).where(KxConsts.ID, Sql.EQ);
	}


	/*
	 * SQL参数封装，考虑可能的null过虑
	 */
	public Object[] para(boolean nullIgnore, Object... paras) {
		List<Object> list = new ArrayList<Object>();
		for (Object para : paras) {
			if (para instanceof Object[]) {
				for (Object sub : (Object[]) para) {
					if (sub != null || !nullIgnore)
						list.add(sub);
				}
			} else {
				if (para != null || !nullIgnore)
					list.add(para);
			}
		}
		return list.toArray(new Object[list.size()]);
	}

	/*
	 * 复杂的SQL参数封装，比如update语句，既有set区多个参数，又有where区多参数，需要使用paraMerge 比如paraMerge(
	 * para(false, v1, v2, v3), para(true, v4, v5, v6));
	 */
	public Object[] paraMerge(Object... paras) {
		return para(false, paras);
	}

	/*
	 * select语句和delete语句，一般是需要过滤null的，因为para都是用在where里，
	 * insert语句一般不需要过滤null，因为para用在values里
	 * update语句则不一定，因为可能有para在set里，也可能在where里
	 */

	public Object[] paraForSelect(Object... paras) {
		return para(true, paras);
	}

	public Object[] paraForInsert(Object... paras) {
		return para(false, paras);
	}

	public Object[] paraForDelete(Object... paras) {
		return para(true, paras);
	}



}
