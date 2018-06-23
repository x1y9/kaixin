package com.kaixin.core.sql;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*  SQL Update 的封装
 * 
 * SELECT
 * FIELDS
 * FROM & JOINs   determine & filter rows
 * WHERE          more filters on the rows
 * GROUP BY       combines those rows into groups
 * HAVING         filters groups
 * ORDER BY       arranges the remaining rows/groups
 * LIMIT          limits the results
 */

public class UpdateStatement extends Statement {
	
	//考虑有多datasource的情况，不能用全局的sql
	private Sql sql;
	
	private List<String> fields = new ArrayList<String>();
	private String table;
	
	private List<String> sets = new ArrayList<String>();
	private Condition where;
	
	public UpdateStatement(Sql sql, String table) {
		this.sql = sql;
		this.table = table;
	}

	public UpdateStatement field(String field) {
		if (field != null)
			this.fields.add(field);
		
		return this;
	}

	public UpdateStatement set(String field, String value) {
		if (field != null)
			this.sets.add(field + " = " + value);
		
		return this;
	}

	public UpdateStatement set(String field) {
		if (field != null)
			this.sets.add(field + " = ?");
		
		return this;
	}

	public UpdateStatement set(Map<String,Object> row) {
		if (row != null) {
			for (String field : row.keySet())
				this.sets.add(field + " = ?");
		}
		
		return this;
	}
	public UpdateStatement where(Condition condition) {
		if (condition != null)
			this.where = new Condition(condition);
		
		return this;
	}

	public UpdateStatement where(String field) {
		this.where = new Condition(field);
		return this;
	}
	
	public UpdateStatement where(String field, String comparator) {
		this.where = new Condition(field, comparator);
		return this;
	}

	public UpdateStatement where(String field, String comparator, boolean nullIgnore) {
		this.where = new Condition(field, comparator, nullIgnore);
		return this;
	}
	
	public UpdateStatement where(String field, String comparator, int paras, boolean nullIgnore) {
		if (field != null)
			this.where = new Condition(field, comparator, paras, nullIgnore);
		
		return this;
	}

	public UpdateStatement whereNot(Condition condition) {
		if (condition != null) {
			this.where = new Condition(condition);
			this.where.not();
		}
		
		return this;
	}

	public UpdateStatement whereNot(String field) {
		return whereNot(field, null, 0, false);
	}
	
	public UpdateStatement whereNot(String field, String comparator) {
		return whereNot(field, comparator, 0, false);
	}

	public UpdateStatement whereNot(String field, String comparator, boolean nullIgnore) {
		return whereNot(field, comparator, 0, nullIgnore);
	}

	public UpdateStatement whereNot(String field, String comparator, int paras, boolean nullIgnore) {
		if (field != null) {
			this.where = new Condition(field, comparator, paras, nullIgnore);
			this.where.not();
		}
		
		return this;
	}
	
	public UpdateStatement and(Condition condition) {
		this.where.and(condition);
		return this;
	}
	
	public UpdateStatement and(String field) {
		this.where.and(field);
		return this;
	}

	public UpdateStatement and(String field, String comparator) {
		this.where.and(field, comparator);
		return this;
	}

	public UpdateStatement and(String field, String comparator, boolean nullIgnore) {
		this.where.and(field, comparator, nullIgnore);
		return this;
	}

	public UpdateStatement and(String field, String comparator, int paras, boolean nullIgnore) {
		this.where.and(field, comparator, paras, nullIgnore);
		return this;
	}

	public UpdateStatement or(Condition condition) {
		this.where.or(condition);
		return this;
	}

	public UpdateStatement or(String field) {
		this.where.or(field);
		return this;
	}

	public UpdateStatement or(String field, String comparator) {
		this.where.or(field, comparator);
		return this;
	}

	public UpdateStatement or(String field, String comparator, boolean nullIgnore) {
		this.where.or(field, comparator, nullIgnore);
		return this;
	}

	public UpdateStatement or(String field, String comparator, int paras, boolean nullIgnore) {
		this.where.or(field, comparator, paras, nullIgnore);
		return this;
	}

	@Override
	public String toSql() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(UPDATE_).append(table).append(" SET ");
		sb.append(StringUtils.join(sets, ",")).append(" ");
		
		if (where != null) {
			sb.append(WHERE_).append(where.toSql(sql)).append(" ");
		}
		
		
		return sb.toString();		
	}
	 	
}
