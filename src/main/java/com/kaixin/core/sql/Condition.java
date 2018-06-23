package com.kaixin.core.sql;

import com.kaixin.core.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/*
 * SQL where 条件的封装
 * 对sql相等判断，增加了EQ_SAFE和NEQ_SAFE运算符，
 * 使用这两个做条件判断时则不需要根据参数是否null，写两个不同sql，但是bind参数时需要绑定两次
 *  public static final String EQ_SAFE = "EQ";   //兼容ISNULL和=,使用 x = ? OR (x IS NULL AND ? IS NULL)
 *	public static final String NEQ_SAFE = "NEQ"; //兼容ISNOTNULL和<>,使用 x <> ? OR (x IS NOT NULL AND ? IS NULL)

 * 举例：
    String parameter = null;
    String sql2 = handle.createQuery(sql.selectQuery("user")
				  		  .where("name",Condition.EQ_SAFE)
				  		.toSql())
						.bind(0, parameter)
						.bind(1, parameter)
						
	如果nullIgnore设为true，则value为null忽略这个条件，相当于 (x compare ? OR ? IS NULL)
	当然nullIgnore为true和EQ_SAFE/NEQ_SAFE 不能同时使用	  					
 */

public class Condition {

	
	//基本条件
	private boolean not = false;
	private String field;
	private String comparator;
	private int paras = 0;
	private boolean nullIgnore = false;
	
	//如果有左值，忽略上面的基本条件
	private Condition left;
	
	//右值(AND/OR条件)，可以为空
	private List<Condition> rights = new ArrayList<Condition>();
	private List<String> logicals = new ArrayList<String>();
	
	//用于只使用field，不用comparator的条件，所有条件都写在field里
	public Condition(String field) {
		this.field = field;
	}	

	public Condition(String field, String comparator) {
		this(field, comparator, 0, false);
	}

	public Condition(String field, String comparator, boolean nullIgnore) {
		this(field, comparator, 0, nullIgnore);
	}

	public Condition(String field, String comparator, int paras) {
		this(field, comparator, paras, false);
	}
	
	public Condition(String field, String comparator, int paras, boolean nullIgnore) {
		this.field = field;
		this.comparator = comparator;
		if (paras >= 0)
			this.paras = paras;
	
		this.nullIgnore = nullIgnore;		
	}
	
	public Condition(Condition left) {
		this.left = left;
	}
	
	public Condition and(Condition condition) {
		if (condition != null) {
			rights.add(condition);
			logicals.add(Sql.AND);
		}
		return this;
	}
	
	public Condition and(String field) {
		return and(field, null, 0, false);
	}	
	
	public Condition and(String field, String comparator) {
		return and(field, comparator, 0, false);
	}
	
	public Condition and(String field, String comparator, int paras) {
		return and(field, comparator, paras, false);
	}

	public Condition and(String field, String comparator, boolean nullIgnore) {
		return and(field, comparator, 0, nullIgnore);
	}

	public Condition and(String field, String comparator, int paras, boolean nullIgnore) {
		if (field != null) {
			rights.add(new Condition(field, comparator, paras, nullIgnore));
			logicals.add(Sql.AND);
		}
		return this;
	}
	
	public Condition or(Condition condition) {
		if (condition != null) {
			rights.add(condition);
			logicals.add(Sql.OR);
		}
		
		return this;
	}
	
	public Condition or(String field) {
		return or(field, null, 0, false);
	}	
	
	public Condition or(String field, String comparator) {
		return or(field, comparator, 0, false);
	}
	
	public Condition or(String field, String comparator, int paras) {
		return or(field, comparator, paras, false);
	}

	public Condition or(String field, String comparator, boolean nullIgnore) {
		return or(field, comparator, 0, nullIgnore);
	}

	public Condition or(String field, String comparator, int paras, boolean nullIgnore) {
		if (field != null) {
			rights.add(new Condition(field, comparator, paras, nullIgnore));
			logicals.add(Sql.OR);
		}
		
		return this;
	}
	
	public Condition not() {
		this.not = true;
		return this;
	}
	
	public String toSql(Sql sql) {
		StringBuilder sb = new StringBuilder();
		if (not)
			sb.append("NOT ");
		
		if (not || rights.size() > 0)
			sb.append("(");
		
		if (left != null)
			sb.append(left.toSql(sql));
		else
			sb.append(atomSql(sql));
		
		for (int i =0 ; i< rights.size(); i++) {
			sb.append(" " + logicals.get(i) + " ");
			sb.append(rights.get(i).toSql(sql));
		}
		
		if (not || rights.size() > 0)
			sb.append(")");
		
		return sb.toString();
	}
	
	private String atomSql(Sql sql) {
		if (field != null && comparator != null) {
			String ret = "";
			if (Sql.BETWEEN.equals(comparator))
				ret = field + " BETWEEN ? AND ?";
			else if (Sql.IN.equals(comparator)) {
				if (paras <= 0)
					ret = Sql.ALWAYS_FALSE;
				else
					ret = field + " IN (" + StringUtil.repeat("?", ",", paras) + ")";
			}
			else if (Sql.NOT_IN.equals(comparator)) {
				if (paras <= 0)
					ret = Sql.ALWAYS_TRUE;
				else
					ret = field + " NOT IN (" + StringUtil.repeat("?", ",", paras) + ")";
			}
			else if (Sql.EQ.equals(comparator))
				ret = field + " = ?";
			else if (Sql.NEQ.equals(comparator))
				ret = field + " <> ?";			
			else if (Sql.LT.equals(comparator))
				ret = field + " < ?";
			else if (Sql.GT.equals(comparator))
				ret = field + " > ?";			
			else if (Sql.LE.equals(comparator))
				ret = field + " <= ?";
			else if (Sql.GE.equals(comparator))
				ret = field + " >= ?";			
			else if (Sql.LIKE.equals(comparator))
				ret = field + " LIKE ?";			
			else if (Sql.EQ_SAFE.equals(comparator))
				ret = "(" + field + " = ? OR (" + field + " IS NULL AND ? IS NULL))";
			else if (Sql.NEQ_SAFE.equals(comparator))
				ret = "(" + field + " <> ? OR (" + field + " IS NOT NULL AND ? IS NULL))";
			else
				ret = field + " " + comparator;
			
			if (nullIgnore && !Sql.EQ_SAFE.equals(comparator) && !Sql.NEQ_SAFE.equals(comparator))
				ret = "(" + ret + " OR ? IS NULL)";
			
			return ret;
		}
		else if (field != null) {
			return "(" + field + ")";
		}
		
		return ""; //不应该进入这个分支
	}
}
