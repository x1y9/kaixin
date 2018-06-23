package com.kaixin.core.sql;

import com.kaixin.core.util.StringUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*  SQL Insert 的封装
 * 
 * Insert into
 * FROM & JOINs   
 * (Fields)         
 * Values (Values)
 */

public class InsertStatement extends Statement {
	
	//考虑有多datasource的情况，不能用全局的sql
	private Sql sql;
	
	private List<String> fields = new ArrayList<String>();
	private String into;
	
	
	public InsertStatement(Sql sql, String into) {
		this.sql = sql;
		this.into = into;
	}

	public InsertStatement set(String field) {
		if (field != null)
			this.fields.add(field);
		
		return this;
	}

	public InsertStatement set(String... fields) {
		for (String field:fields)
			set(field);
		
		return this;
	}

	public InsertStatement set(Map<String,Object> fields) {
		for (String field: fields.keySet())
			this.fields.add(field);
		
		return this;
	}

	@Override
	public String toSql() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(INSERT_INTO_).append(into);
		
		sb.append(" (").append(StringUtils.join(fields, COMMA_)).append(")");
		sb.append(" VALUES ").append("(");
		sb.append(StringUtil.repeat(PARA, COMMA_, fields.size())).append(")");
		
		return sb.toString();		
	}
	 	
}
