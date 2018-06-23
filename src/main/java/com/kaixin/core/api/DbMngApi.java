package com.kaixin.core.api;

import com.kaixin.core.app.KxApp;
import com.kaixin.core.app.KxConsts;
import com.kaixin.core.app.KxTemplate;
import com.kaixin.core.db.Transactional;
import com.kaixin.core.sql.Sql;
import com.kaixin.core.util.GetterUtil;
import com.kaixin.core.util.SqlUtil;
import com.kaixin.core.util.ThreadLocalUtil;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.lang3.time.DateUtils;

import javax.sql.DataSource;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

/*
 * DdMng后台接口，提供基于数据库级别的crud，这个功能比较危险，应该在生产环境禁用
 * 
 * 一些数据类型说明：
 *  boolean: 编辑时可写true/false,1/0
 *  timestamp：jdbc的java.sql下有Date,Time,Timestamp,都是java.util.Date的子类，
 *   MySQL的DateTime和Timestamp虽然有时区上的差异，但对Jdbc来说都是Timestamp
 */

@Path("/api/dbmng")
@Produces(value = MediaType.APPLICATION_JSON)
public class DbMngApi {
	
	private DataSource dataSource;
	private QueryRunner runner;
	
	public DbMngApi(DataSource ds) {
		dataSource = ds;
		runner = new QueryRunner(ds);
	}


	@GET
	@Path("/_meta")
	@Transactional
	public List<String> meta() throws Exception
	{

		Map<String,Object> context = new HashMap<String,Object>();

		List<String> tables = new ArrayList<String>();
		try (Connection connection = KxApp.dataSource.getConnection()) {
			DatabaseMetaData md = connection.getMetaData();
			ResultSet rs = md.getTables(null, null, "%", new String[]{"TABLE"});
			while (rs.next()) {
				tables.add(rs.getString("TABLE_NAME").toLowerCase());
			}
		}

		return tables;
	}

	@GET
	@Path("/{model}")
	@Transactional
    public Map<String,Object> load(
    		@PathParam("model") 	String table,
			@QueryParam("sort")    	String sort,
			@QueryParam("order") 	String order,
			@QueryParam("start")   	String start,
			@QueryParam("end")     	String end) throws Exception 
    {
		
		Map<String, Object> result = new HashMap<String,Object>();
		
		long pageStart = GetterUtil.getLong(start, 0);
		long pageEnd = GetterUtil.getLong(end, 20);
		List<Map<String, Object>> data = runner.query(
				KxApp.sql.select(table)
				.order(sort,!"desc".equalsIgnoreCase(order))
				.limit(pageStart, pageEnd- pageStart).toSql(), new MapListMetaHandler());
		List<Map<String, Object>> adminData = db2admin(data, (List<Map>)ThreadLocalUtil.get(KxConsts.TL_DBMNG_LAST_QUERY_META));
		
		//增加一行用于新增记录
		adminData.add(new HashMap<String,Object>());
		result.put("rowData", adminData);				
        return result;
    }
	
	@GET
	@Path("/{model}/column")
    public Map<String,Object> getColumn(@PathParam("model") String table) throws Exception 
    {
		Map<String, Object> result = new HashMap<String,Object>();
		result.put("columnDefs", getTableColumns(table));		
        return result;
    }
	
	@POST
	@Path("/{model}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional
    public Map<String,Object> save(@PathParam("model") String table, Map<String,Object> req) throws Exception 
    {
		
		
		Map<String, Object> result = new HashMap<String,Object>();
		if (req==null || req.get("data") == null)
			return result;
		
		int updatedRows = 0;
		for(Map<String,Object> item: (List<Map>)req.get("data")) {
			long id = GetterUtil.getLong(item.get("id"), -1);
			Map<String,Object> row = admin2db(table, item);
			if (id == -1) {
				updatedRows += runner.update(KxApp.sql.insert(table).set(row).toSql(), row.values().toArray());
			}
			else {
				List<Object> paras = new ArrayList<Object>(row.values());
				paras.add(id);
				updatedRows += runner.update(KxApp.sql
						.update(table)
						.set(row)
						.where(KxConsts.ID, Sql.EQ).toSql(),
						paras.toArray());
			}	
		}
		result.put("updatedRows", updatedRows); 
		return result;
    }
	
	@POST
	@Path("/{model}/sql")
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional
    public Map<String,Object> sql(@PathParam("model") String table, Map<String,Object> req) throws Exception 
    {
		Map<String, Object> result = new HashMap<String,Object>();
		String statement = "";
		if (req != null && req.get("statement") != null)
			statement = ((String)req.get("statement")).trim();

		if(statement.toLowerCase().startsWith("select ")) {
			List<Map<String, Object>> data = runner.query(statement, new MapListMetaHandler());
			List<Map> columns  = (List<Map>) ThreadLocalUtil.get(KxConsts.TL_DBMNG_LAST_QUERY_META);
			result.put("columnDefs", columns);
			result.put("rowData", db2admin(data, columns));
			if (isReadonlySql(statement,table)) {
				result.put("readOnly", true);
			}
		}
		else {
			int updatedRows = runner.update(statement);
			result.put("rowData", new ArrayList<Map<String,Object>>());
			result.put("columnDefs", new ArrayList<Map<String,Object>>());
			result.put("updatedRows", updatedRows);
		}
		
		return result;		
    }

	@DELETE
	@Path("/{model}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional
    public Map<String,Object> delete(@PathParam("model") String table, Map<String,Object> req) throws Exception 
    {
		
		Map<String, Object> result = new HashMap<String,Object>();
		if (req==null || req.get("data") == null)
			return result;
		
		int updatedRows = 0;
		for(Map<String,Object> item: (List<Map>)req.get("data")) {
			long id = GetterUtil.getLong(item.get("id"), -1);
			if (id != -1) {
				updatedRows += runner.update(KxApp.sql.delete(table).where("id",Sql.EQ).toSql(),id);
			}
		}
		result.put("updatedRows", updatedRows);
		return result;
    }	

	/*
     * 下面是私有方法
     */
   
	private List<Map> getTableColumns(String table) throws Exception {
		
		List<Map<String,Object>> results = new ArrayList<Map<String,Object>>();
		runner.query("SELECT * FROM " + table + " WHERE 1<0", new MapListMetaHandler());
		return (List<Map>)ThreadLocalUtil.get(KxConsts.TL_DBMNG_LAST_QUERY_META);		
	}
	
	private Map<String,Map> getTableColumnsMap(String table) throws Exception {
		List<Map> results = getTableColumns(table);
		Map<String,Map> results2 = new HashMap<String,Map>();
		for (Map result : results)
			results2.put((String)result.get("field"), result);
		return results2;	
	}	
	
	private boolean isReadonlySql(String statement, String table) {
    	String[] splits = statement.replaceAll(";\\s*$", "").split("\\s+");
    	
    	if (splits.length > 3) {
    		if (splits[0].equals("select")
    			&& splits[1].equals("*")
    			&& splits[2].equals("from")
    			&& splits[3].equals(table.toLowerCase()))
    			return false;    			
    	}
    	return true;
	}


	private Map<String,Object> admin2db(String table, Map<String, Object> entity) throws Exception {
		Map<String,Object> result = new HashMap<String,Object>();		
		Map<String, Map> columns = getTableColumnsMap(table);
		for (String key : entity.keySet()) {
			Map<String,Object> column = columns.get(key);
			if (column == null)
				continue;

			Object value = entity.get(key);
			if ( (int)column.get("type") != Types.VARCHAR && (int)column.get("type") != Types.LONGNVARCHAR
			   &&(int)column.get("type") != Types.CLOB    && (int)column.get("type") != Types.LONGVARCHAR)
				value = GetterUtil.emptyToNull(value);
				
			if (value == null) {
				result.put(key, null);
			}
			else if ((int)column.get("type") == Types.DATE || (int)column.get("type") == Types.TIME || (int)column.get("type") == Types.TIMESTAMP ) {
				result.put(key, parseTime(value));
			}
			else if ((int)column.get("type") == Types.INTEGER || (int)column.get("type") == Types.BIGINT 
				  || (int)column.get("type") == Types.TINYINT) {
				result.put(key, Long.parseLong(value.toString()));
			}			
			else if ((int)column.get("type") == Types.BIT) {
				result.put(key, GetterUtil.getBoolean(value));
			}			
			else {
				result.put(key, value);
			}
		}
		return result;
	}
	


	/* 
	 * 处理数据库返回row到admin的转换
	 */
	private List<Map<String,Object>> db2admin(List<Map<String,Object>> map, List<Map> columns) throws Exception {
		List<Map<String,Object>> results = new ArrayList<Map<String,Object>>();
		
		for (Map<String,Object> item : map) {
			results.add(row2admin(item, columns));
		}
		return results;
	}

	private Map<String, Object> row2admin(Map<String, Object> row, List<Map> columns) {
		Map<String,Object> result = new HashMap<String,Object>();
		
		if(row != null) {
			for (String key : row.keySet()) {
				Object value = row.get(key);
				
				if (value instanceof Clob) {
					result.put(key.toLowerCase(), SqlUtil.clob2String((Clob)value));
				}
				else if (value instanceof java.sql.Timestamp){
					result.put(key.toLowerCase(),new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(value));
				}
				else if (value instanceof java.sql.Date){
					result.put(key.toLowerCase(),new SimpleDateFormat("yyyy/MM/dd").format(value));
				}
				else if (value instanceof java.sql.Time){
					result.put(key.toLowerCase(),new SimpleDateFormat("HH:mm:ss").format(value));
				}
				else {
					result.put(key.toLowerCase(), value);
				}
			}
		}
		return result;
	}
	
	/* 支持数据库的Date/Time/Datetime/Timestamp */
	private Date parseTime(Object value) throws ParseException {
		return DateUtils.parseDate((String)value, new String[]{
				"yyyy/MM/dd HH:mm:ss.SSS",
				"yyyy/MM/dd HH:mm:ss", 
				"yyyy/MM/dd",
				"HH:mm:ss"
				});
	}

	private class MapListMetaHandler extends MapListHandler {
		@Override
		public List<Map<String,Object>> handle(ResultSet rs) throws SQLException {
			List<Map<String, Object>> list = super.handle(rs);

			List<Map> columns = new ArrayList<Map>();
			ResultSetMetaData rsmd = rs.getMetaData();
			for (int i = 1 ; i <= rsmd.getColumnCount(); i++) {
				Map<String,Object> column = new HashMap<String,Object>();
				String name = rsmd.getColumnName(i).toLowerCase();
				column.put("field", name);
				column.put("headerName", name);
				column.put("type", rsmd.getColumnType(i));
				column.put("typeName", rsmd.getColumnTypeName(i));
				column.put("typeSize", rsmd.getColumnDisplaySize(i));
				column.put("headerTooltip",name + ":" + rsmd.getColumnTypeName(i) + "(" + rsmd.getColumnDisplaySize(i) + ")");
				if (!name.equalsIgnoreCase("id"))
					column.put("editable", true);

				if (rsmd.getColumnDisplaySize(i) > 255) {
					column.put("cellEditor", "largeText");
					Map<String,String> cellEditorParams = new HashMap<String,String>();
					cellEditorParams.put("maxLength", "" + rsmd.getColumnDisplaySize(i));
					column.put("cellEditorParams", cellEditorParams);
				}

				columns.add(column);
			}
			ThreadLocalUtil.set(KxConsts.TL_DBMNG_LAST_QUERY_META, columns);
			return list;
		}
	}
}







