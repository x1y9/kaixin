package com.kaixin.core.sql;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/*  SQL Query 的封装
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

public class QueryStatement extends Statement{
	
	//考虑有多datasource的情况，不能用全局的sql
	private Sql sql;
	
	private List<String> fields = new ArrayList<String>();
	private String from;
	
	private List<String> joins = new ArrayList<String>();
	
	private Condition where;
	private List<String> groups = new ArrayList<String>();
	private String having;
	private List<String> orders = new ArrayList<String>();
	
	private Long offset;
	private Long pagesize;
	
	public QueryStatement(Sql sql, String from) {
		this.sql = sql;
		this.from = from;
	}

	public QueryStatement field(String field) {
		if (field != null)
			this.fields.add(field);
		
		return this;
	}
	
	public QueryStatement field(String... fields) {
		for (String field : fields)
			this.fields.add(field);
		
		return this;
	}
	
	public QueryStatement field(Collection<String> fields) {
		if (fields != null)
			this.fields.addAll(fields);
		
		return this;
	}	
	
	public QueryStatement from(String from) {
		this.from = from; 
		return this;
	}
	
	public QueryStatement leftJoin(String name, String leftField, String rightField) {
		if (name != null && leftField != null && rightField != null)
			joins.add(LEFT_JOIN_ + name + _ON_ + leftField + " = " + rightField);
		
		return this;
	}

	public QueryStatement leftJoin(List<Map> joinList) {
		for (Map joinMap : joinList)
			leftJoin(sql.alias((String)joinMap.get("model"),(String)joinMap.get("alias")),
					(String)joinMap.get("left"), (String)joinMap.get("right"));
		
		return this;
	}
	
	public QueryStatement innerJoin(String name, String leftField, String rightField) {
		if (name != null && leftField != null && rightField != null)
			joins.add(INNER_JOIN_ + name + _ON_ + leftField + " = " + rightField);
		
		return this;
	}

	public QueryStatement innerJoin(List<Map> joinList) {
		for (Map joinMap : joinList)
			innerJoin(sql.alias((String)joinMap.get("model"),(String)joinMap.get("alias")),
					(String)joinMap.get("left"), (String)joinMap.get("right"));
		
		return this;
	}

	public QueryStatement where(Condition condition) {
		if (condition != null)
			this.where = new Condition(condition); 
		
		return this;
	}

	public QueryStatement where(String clause) {
		return where(clause, null, 0, false);
	}
	
	public QueryStatement where(String field, String comparator) {
		return where(field, comparator, 0, false);
	}

	public QueryStatement where(String field, String comparator, boolean nullIgnore) {
		return where(field, comparator, 0, nullIgnore);
	}
	
	public QueryStatement where(String field, String comparator, int paras, boolean nullIgnore) {
		if (field != null)
			this.where = new Condition(field, comparator, paras, nullIgnore);
		
		return this;
	}

	public QueryStatement whereNot(Condition condition) {
		if (condition != null) {
			this.where = new Condition(condition);
			this.where.not();
		}
		return this;
	}

	public QueryStatement whereNot(String clause) {
		return whereNot(clause, null, 0, false);
	}
	
	public QueryStatement whereNot(String field, String comparator) {
		return whereNot(field, comparator, 0, false);	
	}

	public QueryStatement whereNot(String field, String comparator, boolean nullIgnore) {
		return whereNot(field, comparator, 0, nullIgnore);	
	}

	public QueryStatement whereNot(String field, String comparator, int paras, boolean nullIgnore) {
		if (field != null && comparator != null) {
			this.where = new Condition(field, comparator, paras, nullIgnore);
			this.where.not();
		}
		
		return this;
	}
	
	public QueryStatement and(Condition condition) {		
		this.where.and(condition);
		return this;
	}
	
	public QueryStatement and(String field) {
		this.where.and(field);
		return this;
	}

	public QueryStatement and(String field, String comparator) {
		this.where.and(field, comparator);
		return this;
	}

	public QueryStatement and(String field, String comparator, boolean nullIgnore) {
		this.where.and(field, comparator, nullIgnore);
		return this;
	}

	public QueryStatement and(String field, String comparator, int paras, boolean nullIgnore) {
		this.where.and(field, comparator, paras, nullIgnore);
		return this;
	}

	public QueryStatement or(Condition condition) {
		this.where.or(condition);
		return this;
	}

	public QueryStatement or(String field) {
		this.where.or(field);
		return this;
	}

	public QueryStatement or(String field, String comparator) {
		this.where.or(field, comparator);
		return this;
	}

	public QueryStatement or(String field, String comparator, boolean nullIgnore) {
		this.where.or(field, comparator, nullIgnore);
		return this;
	}

	public QueryStatement or(String field, String comparator, int paras, boolean nullIgnore) {
		this.where.or(field, comparator, paras, nullIgnore);
		return this;
	}
	
	public QueryStatement group(String para) {
		if (para != null)
			this.groups.add(para);
		
		return this;
	}
	
	public QueryStatement having(String having) {
		if (having != null)
			this.having = having;
		
		return this;
	}
	
	public QueryStatement order(String orderBy) {
		return order(orderBy, true);
	}
	
	public QueryStatement order(String orderBy, boolean asc) {
		if (orderBy != null && orderBy.length() > 0)
			this.orders.add(orderBy + (asc ?_ASC :_DESC) );
		
		return this;
	}
	
	public QueryStatement limit(long offset, long pagesize) {
		this.offset = offset;
		this.pagesize = pagesize;
		return this;
	}
	
	@Override
	public String toSql() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(SELECT_);
		
		if (fields != null && fields.size() > 0) {
			sb.append(StringUtils.join(fields, COMMA_)).append(" ");
		}
		else {
			sb.append(" * ");
		}
		
		sb.append(_FROM_).append(from).append(" ");
		
		if (joins != null && joins.size() > 0) {
			sb.append(StringUtils.join(joins, " ")).append(" ");
		}
		
		if (where != null) {
			sb.append(WHERE_).append(where.toSql(sql)).append(" ");
		}
		
		if (groups != null && groups.size() > 0) {
			sb.append(GROUP_BY_ + StringUtils.join(groups, COMMA_)).append(" ");
		}
		
		if (having != null) {
			sb.append(HAVING_ + having).append(" ");
		}
		
		if (orders != null && orders.size() > 0) {
			sb.append(ORDER_BY_).append(StringUtils.join(orders, COMMA_)).append(" ");
		}
		
		if (offset != null && pagesize != null) {
			sb.append(sql.limit(offset, pagesize)).append(" ");
		}
		
		return sb.toString();		
	}
	 	
}
