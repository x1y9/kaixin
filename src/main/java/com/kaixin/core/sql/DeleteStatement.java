package com.kaixin.core.sql;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/*  SQL Delete 的封装
 * 
 * From
 * FROM & JOINs   determine & filter rows
 * WHERE          more filters on the rows
 */

public class DeleteStatement extends Statement {
	
	//考虑有多datasource的情况，不能用全局的sql
	private Sql sql;
	
	private String from;
	
	private List<String> joins = new ArrayList<String>();
	
	private Condition where;
	
	public DeleteStatement(Sql sql, String from) {
		this.sql = sql;
		this.from = from;
	}


	public DeleteStatement from(String from) {
		this.from = from; 
		return this;
	}
	
	public DeleteStatement leftJoin(String name, String leftField, String rightField) {
		if (name != null && leftField != null && rightField != null)
			joins.add(LEFT_JOIN_ + name + _ON_ + leftField + " = " + rightField);
		
		return this;
	}

	public DeleteStatement innerJoin(String name, String leftField, String rightField) {
		if (name != null && leftField != null && rightField != null)
			joins.add(INNER_JOIN_ + name + _ON_ + leftField + " = " + rightField);
		
		return this;
	}
	
	public DeleteStatement where(Condition condition) {
		if (condition != null)
			this.where = new Condition(condition); 
		return this;
	}

	public DeleteStatement where(String field) {		
		return where(field, null, 0, false);
	}
	
	public DeleteStatement where(String field, String comparator) {
		return where(field, comparator, 0, false);
	}

	public DeleteStatement where(String field, String comparator, boolean nullIgnore) {
		return where(field, comparator, 0, nullIgnore);
	}
	
	public DeleteStatement where(String field, String comparator, int paras, boolean nullIgnore) {
		if (field != null)
			this.where = new Condition(field, comparator, paras, nullIgnore);
		
		return this;
	}

	public DeleteStatement whereNot(Condition condition) {
		if (condition != null) {
			this.where = new Condition(condition);
			this.where.not();
		}
		
		return this;
	}

	public DeleteStatement whereNot(String field) {
		return whereNot(field, null, 0, false);
	}
	
	public DeleteStatement whereNot(String field, String comparator) {
		return whereNot(field, comparator, 0, false);	
	}

	public DeleteStatement whereNot(String field, String comparator, boolean nullIgnore) {
		return whereNot(field, comparator, 0, nullIgnore);
	}

	public DeleteStatement whereNot(String field, String comparator, int paras, boolean nullIgnore) {
		if (field != null) {
			this.where = new Condition(field, comparator, paras, nullIgnore);
			this.where.not();
		}
		
		return this;
	}
	
	public DeleteStatement and(Condition condition) {
		this.where.and(condition);
		return this;
	}
	
	public DeleteStatement and(String field) {
		this.where.and(field);
		return this;
	}

	public DeleteStatement and(String field, String comparator) {
		this.where.and(field, comparator);
		return this;
	}

	public DeleteStatement and(String field, String comparator, boolean nullIgnore) {
		this.where.and(field, comparator, nullIgnore);
		return this;
	}

	public DeleteStatement and(String field, String comparator, int paras, boolean nullIgnore) {
		this.where.and(field, comparator, paras, nullIgnore);
		return this;
	}

	public DeleteStatement or(Condition condition) {
		this.where.or(condition);
		return this;
	}

	public DeleteStatement or(String field) {
		this.where.or(field);
		return this;
	}

	public DeleteStatement or(String field, String comparator) {
		this.where.or(field, comparator);
		return this;
	}

	public DeleteStatement or(String field, String comparator, boolean nullIgnore) {
		this.where.or(field, comparator, nullIgnore);
		return this;
	}

	public DeleteStatement or(String field, String comparator, int paras, boolean nullIgnore) {
		this.where.or(field, comparator, paras, nullIgnore);
		return this;
	}
	
	@Override
	public String toSql() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(DELETE_);
		
		sb.append(_FROM_).append(from).append(" ");
		
		if (joins != null && joins.size() > 0) {
			sb.append(StringUtils.join(joins, " ")).append(" ");
		}
		
		if (where != null) {
			sb.append(WHERE_).append(where.toSql(sql)).append(" ");
		}
		
		return sb.toString();		
	}
	 	
}
