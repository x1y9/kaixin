package com.kaixin.core.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kaixin.core.app.KxApp;
import com.kaixin.core.app.KxConsts;
import com.kaixin.core.auth.AuthUtil;
import com.kaixin.core.db.DbHandle;
import com.kaixin.core.db.Transactional;
import com.kaixin.core.event.AdminEvent;
import com.kaixin.core.profile.Field;
import com.kaixin.core.profile.Model;
import com.kaixin.core.sql.Condition;
import com.kaixin.core.sql.Sql;
import com.kaixin.core.sql2o.Connection;
import com.kaixin.core.sql2o.Query;
import com.kaixin.core.util.*;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.*;
import java.io.OutputStream;
import java.sql.Clob;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

/*
 * admin后台接口
 */
@Path("/api/admin/{model}")
@Produces(value = MediaType.APPLICATION_JSON)
public class AdminApi {

	@Context private ContainerRequestContext reqContext;
	@Context private UriInfo uriInfo;

	private static AdminEvent callback; 

	@GET
	@Transactional
    public List<Map<String,Object>> list(@Context HttpServletResponse response,
    							  @PathParam("model") 	   String model,
    		@DefaultValue("id")   @QueryParam("_sort")      String sort,
            @DefaultValue("true") @QueryParam("_reverse")   boolean reverse,
            @DefaultValue("20")   @QueryParam("_number")    long number,
            @DefaultValue("0")    @QueryParam("_start")     long start,
            @DefaultValue("false")@QueryParam("_retTotal")  boolean retTotal,
            					  @QueryParam("_fields")    String fields,
             					  @QueryParam("_filters")   String filters) throws SQLException {
		DbHandle handle = DbHandle.transactionInstance();
		Sql sql = KxApp.sql;
		PermissionUtil.checkPermission(handle, PermissionUtil.getLoginUser(), model, KxConsts.VIEW_LIST, null);

		String scope = getScope(model, handle.getHandle());
		List<Object> bindValues = new ArrayList<Object>();
		List<Map> joinList = new ArrayList<Map>();
		List<String> selectList = new ArrayList<String>();
		List<Field> fieldList = new ArrayList<Field>();
		List<Integer> refManyList = new ArrayList<Integer>();
		genJoinAliasFieldList(model, fields, joinList, selectList, fieldList, refManyList);
		String aliasMe = sql.alias(0);
		Condition condition = filters2Condition(model, aliasMe, joinList2Map(joinList), filters, scope, bindValues);
		
		Query query = handle.query(sql
				.select(sql.alias(model, aliasMe))
				.field(selectList)
				.leftJoin(joinList)
				.where(condition)
				.order(sql.field(aliasMe,sort), !reverse)
				.limit(start, number));
		
		int position = 0;
		for (Object value : bindValues) {
			 query.setUnamedParameter(position++, value);
		}
		
		if (retTotal) {
			Query queryCount = handle.query(KxApp.sql
					.selectCount(sql.alias(model, aliasMe))
					.leftJoin(joinList)
					.where(condition));
			
			position = 0;
			for (Object value : bindValues) {
				queryCount.setUnamedParameter(position++, value);
			}				
			
			response.setHeader("X-Total-Count", "" + queryCount.executeScalar(Long.class));
		}
        return db2admin(model, fieldList,refManyList, query.executeAndFetchRows());
    }


	@GET
	@Path("/export")
	@Transactional
	public Response export(@Context HttpServletRequest request,
						   @PathParam("model") 	   String model,
						   @DefaultValue("id")   @QueryParam("_sort")      String sort,
						   @DefaultValue("true") @QueryParam("_reverse")   boolean reverse,
						   @QueryParam("_fields")    String fields,
						   @QueryParam("_filters")   String filters) throws SQLException {
		DbHandle handle = DbHandle.transactionInstance();
		Sql sql = KxApp.sql;
		PermissionUtil.checkPermission(handle, (Map<String, Object>)ThreadLocalUtil.get(KxConsts.TL_LOGIN_USER), model, KxConsts.VIEW_LIST, null);

		String scope = getScope(model, handle.getHandle());
		List<Object> bindValues = new ArrayList<Object>();
		List<Map> joinList = new ArrayList<Map>();
		List<String> selectList = new ArrayList<String>();
		final List<Field> fieldList = new ArrayList<Field>();
		List<Integer> refManyList = new ArrayList<Integer>();
		genJoinAliasFieldList(model, fields, joinList, selectList, fieldList, refManyList);
		String aliasMe = sql.alias(0);
		Condition condition = filters2Condition(model, aliasMe, joinList2Map(joinList), filters, scope, bindValues);

		Query query = handle.query(sql
				.select(sql.alias(model, aliasMe))
				.field(selectList)
				.leftJoin(joinList)
				.where(condition)
				.order(sql.field(aliasMe,sort), !reverse));

		int position = 0;
		for (Object value : bindValues) {
			query.setUnamedParameter(position++, value);
		}
		final List<Map<String,Object>> results= db2admin(model, fieldList,refManyList, query.executeAndFetchRows());

		StreamingOutput stream = new StreamingOutput() {
			public void write(OutputStream output) throws WebApplicationException {
				try {
					//表头
					String csvEncode = PropsUtil.get(PropsKeys.CSV_EXPORT_ENCODE);
					for(Field field : fieldList) {
						output.write(field.getLabel().getBytes(csvEncode));
						output.write(",".getBytes());
					}
					output.write("\r\n".getBytes());
					//记录
					for (Map<String,Object> line : results) {
						for(Field field : fieldList) {
							String value = StringEscapeUtils.escapeCsv(line.get(field.getName()) == null ? "" : line.get(field.getName()).toString()) +",";
							output.write(value.getBytes(csvEncode));
						}
						output.write("\r\n".getBytes());
					}
				} catch (Exception e) {
					throw new WebApplicationException(e);
				}
			}
		};
		String exportName = KxApp.profile.getModel(model).getLabel() + new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".csv";
		return Response.ok(stream).header("content-disposition", FileUtil.getContentDisposition(request, exportName)).build();
	}


	@GET
	@Path("/{id:[0-9]+?}")
	@Transactional
    public Map getById(@PathParam("model") String model,@PathParam("id") long id, @QueryParam("fields") String fields) throws SQLException {
		PermissionUtil.checkPermission(DbHandle.transactionInstance(), (Map<String, Object>)ThreadLocalUtil.get(KxConsts.TL_LOGIN_USER), model, KxConsts.VIEW_SHOW, null);
        return innerGetById(DbHandle.transactionInstance(), model, id, fields);
    }

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional
    public Map create(@PathParam("model") String model, Map<String,Object> entity) throws SQLException {
		
		adminEvent(model, AdminEvent.ACTION_CREATE, AdminEvent.STAGE_BEFORE, null, entity);
		DbHandle handle = DbHandle.transactionInstance();
		PermissionUtil.checkPermission(handle, (Map<String, Object>)ThreadLocalUtil.get(KxConsts.TL_LOGIN_USER), model, KxConsts.VIEW_CREATE, null);
		Map<String, Object> dbrow = admin2db(model, entity, null);
		Query query = handle.query(KxApp.sql.insert(model).set(dbrow),true);
		Object[] values = dbrow.values().toArray();
		for (int i=0; i < values.length; i++)
			query.setUnamedParameter(i, values[i]);

		long id = query.executeUpdate().getKey(Long.class);
		Map<String,Object> retEntity = innerGetById(handle, model, id, null);
		adminEvent(model, AdminEvent.ACTION_CREATE, AdminEvent.STAGE_AFTER, entity, retEntity);
		
		return retEntity;
    }

	/*
	 * update应该是增量的，没有传的字段不修改
	 */
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/{id:[0-9]+?}")
	@Transactional
    public Map update(@PathParam("model") String model, Map<String,Object> entity) throws SQLException {
		long id = GetterUtil.getLong(entity.get("id"),-1L);	
		if (id <= 0)
			throw new RuntimeException("can not find id in entity");

		adminEvent(model, AdminEvent.ACTION_UPDATE, AdminEvent.STAGE_BEFORE, null, entity);
		DbHandle handle = DbHandle.transactionInstance();
		Sql sql = KxApp.sql;
		PermissionUtil.checkPermission(handle, (Map<String, Object>)ThreadLocalUtil.get(KxConsts.TL_LOGIN_USER), model, KxConsts.VIEW_EDIT, null);
		Map<String, Object> origin = handle.query(sql
				.select(model)
				.where(KxConsts.ID, sql.EQ))
				.setUnamedParameter(0, id)
				.executeAndFetchFirstRow();
		Map<String, Object> dbrow = admin2db(model, entity, origin);
		Query query = handle.query(sql
				.update(model)
				.set(dbrow)
				.where(KxConsts.ID, sql.EQ));
		Collection<Object> values1 = dbrow.values();
		Object[] values = dbrow.values().toArray();
		for (int i=0; i<values.length; i++)
			query.setUnamedParameter(i, values[i]);
		query.setUnamedParameter(values.length, id);
		query.executeUpdate();
		Map<String,Object> retEntity = innerGetById(handle, model, id, null);
		adminEvent(model, AdminEvent.ACTION_UPDATE, AdminEvent.STAGE_AFTER, entity, retEntity);
		
		return retEntity;
    }
	
	@DELETE
	@Path("/{id:[0-9]+?}")
	@Transactional
    public void delete(@PathParam("model") String model, @PathParam("id") int id) throws SQLException {

		DbHandle handle = DbHandle.transactionInstance();
		Sql sql = KxApp.sql;
		PermissionUtil.checkPermission(handle, (Map<String, Object>)ThreadLocalUtil.get(KxConsts.TL_LOGIN_USER), model, KxConsts.VIEW_DELETE, null);
		Map<String,Object> entity = innerGetById(handle, model, id, null);
		adminEvent(model, AdminEvent.ACTION_DELETE, AdminEvent.STAGE_BEFORE, null, entity);

		handle.query(sql.delete(model)
				.where(KxConsts.ID, sql.EQ))
				.setUnamedParameter(0, id)
				.executeUpdate();

		adminEvent(model, AdminEvent.ACTION_DELETE, AdminEvent.STAGE_AFTER, null, entity);
    }
	
	@DELETE
	@Transactional
    public void batchDelete(@PathParam("model") String model, @QueryParam("ids") String ids) throws SQLException {

		if (ids == null || ids.length() <=0)
			return;

		DbHandle handle = DbHandle.transactionInstance();
		Sql sql = KxApp.sql;
		PermissionUtil.checkPermission(handle, (Map<String, Object>)ThreadLocalUtil.get(KxConsts.TL_LOGIN_USER), model, KxConsts.VIEW_DELETE, null);
		adminEvent(model, AdminEvent.ACTION_DELETE, AdminEvent.STAGE_BEFORE, null, ids);

		String[] entityIds = ids.split(",");
		Query update = handle.query(sql.delete(model).where(KxConsts.ID, sql.IN, entityIds.length, false));
		int position = 0;
		for (String entityId : entityIds)
			update.setUnamedParameter(position++, GetterUtil.getLong(entityId,-1));
		update.executeUpdate();
		adminEvent(model, AdminEvent.ACTION_DELETE, AdminEvent.STAGE_AFTER, null, ids);
    }
		
	public static void setCallback(AdminEvent _callback) {
		callback = _callback;
	}
	
    /*
     * 下面是私有方法
     */

    private void adminEvent(String model, String action, String stage, Object orgEntity, Object entity) {
    	if (callback != null) {
    		callback.onAdminEvent(model, action, stage, orgEntity, entity);
		}

		if (stage == AdminEvent.STAGE_AFTER && action == AdminEvent.ACTION_CREATE) {
			String emails = PropsUtil.getString(PropsKeys.ADMIN_NOTIFICATION_EMAILS);
			if (!GetterUtil.isEmpty(emails)) {
				KxApp.smtp.sendAsync(null, emails, null,
						PropsUtil.getString(PropsKeys.ADMIN_NOTIFICATION_TITLE),
						GetterUtil.getString(((Map<String,Object>)entity).get(KxConsts.NAME),"..."));
			}
		}
	}

	private String getScope(String model, Connection connection) throws SQLException {
		Map<String, Object> scope = connection.createQuery(KxApp.sql
				.select(KxConsts.TBL_SCOPE)
				.where(KxConsts.COL_SCOPE_MODEL, Sql.EQ)
				.and(new Condition(KxConsts.COL_SCOPE_USERS, Sql.EQ)
						.or(KxConsts.COL_SCOPE_USERS, Sql.IS_NULL)
						.or(KxConsts.COL_SCOPE_USERS, Sql.LIKE)).toSql())
				.setUnamedParameter(0, model)
				.setUnamedParameter(1, "")
				.setUnamedParameter(2, "%[" + GetterUtil.get(ThreadLocalUtil.get(KxConsts.TL_LOGIN_USER_ID), -1) + "]%")
				.executeAndFetchFirstRow();
		return scope==null ? null : (String)scope.get(KxConsts.COL_SCOPE_FILTER);
	}

	private void genJoinAliasFieldList(String model, String fields, List<Map> joinList, List<String> selectList, List<Field> fieldList, List<Integer> refManyList) {
		Sql sql = KxApp.sql;
		int fieldNumber = 0;
		int aliasNumber = 0;
		
		//自己先作为一个alias
		String aliasMe = KxApp.sql.alias(aliasNumber++);
		List<Field> retFields = new ArrayList<Field>();
		if (fields != null && fields.length() > 0) {
			Set<String> hasFields = new HashSet<String>();
			for (String item : fields.split(",")) {
				Field itemField = KxApp.profile.getModel(model).getField(item);
				if ( itemField != null && !hasFields.contains(item)){
					retFields.add(itemField);
					hasFields.add(item);
				}
			}
			if (hasFields.contains(KxConsts.ID))
				retFields.add(0, KxApp.profile.getModel(model).getField(KxConsts.ID));
		}
		else {
			 retFields.addAll(KxApp.profile.getModel(model).getFields());
		}
		
		for(Field field : retFields) {			
			if (field.isReferenceSingle()) {
				String alias = KxApp.sql.alias(aliasNumber++);
				Map<String,String> joinMap = new HashMap<String,String>();
				joinMap.put("model", field.getTarget());
				joinMap.put("alias", alias);
				joinMap.put("left", sql.field(alias, KxConsts.ID));
				joinMap.put("right", sql.field(aliasMe ,field.getName()));
				joinList.add(joinMap);
				
				selectList.add(sql.alias(sql.field(alias,KxConsts.ID), "f" + fieldNumber+ "_" + KxConsts.ID));
				selectList.add(sql.alias(sql.field(alias,field.getTargetField()), "f" + fieldNumber + "_" + field.getTargetField()));
				fieldList.add(field);				
				fieldNumber++;
			}
			else {
				selectList.add(sql.alias(sql.field(aliasMe,field.getName()), "f" + fieldNumber));
				fieldList.add(field);
				if (field.isReferenceMany()) {
					refManyList.add(fieldNumber);
				}
				fieldNumber++;
			}
		}		
	}
	
	private Condition filters2Condition(String modelName, String alias, Map<String,Map> joinMap,
										String urlFilters, String scopeFilter, List<Object> bindValues) {

		Condition condition = new Condition(Sql.ALWAYS_TRUE);
		Model model = KxApp.profile.getModel(modelName);
		Sql sql = KxApp.sql;
		String [] keySuffix = new String[]     {">=",   "<=",   "<>",    "=",    ">",    "<",    "%"};
		String [] keyComparator = new String[] {Sql.GE, Sql.LE, Sql.NEQ, Sql.EQ, Sql.GT, Sql.LT, Sql.LIKE};
		if(model == null)
			return null;
		
		try {
			Map<String,String> filtersMap = KxApp.mapper.readValue(urlFilters, Map.class);
			for (String key : filtersMap.keySet()) {

				String comparator = Sql.EQ;
				String fieldName = key;
				for (int i=0; i < keySuffix.length; i++) {
					if (key.endsWith(keySuffix[i])) {
						fieldName = key.substring(0, key.length() - keySuffix[i].length());
						comparator = keyComparator[i];
						break;
					}
				}

				String targetModelName = null;
				String targetFieldName = fieldName;
				Field field = null;
				if (fieldName.indexOf(".") != -1) {
					String refField = fieldName.split("\\.")[0];
					targetFieldName = fieldName.split("\\.")[1];
					field = model.getField(refField);
					if (field == null || GetterUtil.isEmpty(targetFieldName))
						continue;

					targetModelName = field.getTarget();
					Model targetModel = KxApp.profile.getModel(targetModelName);
					field = targetModel.getField(targetFieldName);
				}
				else {
					field = model.getField(fieldName);
				}

				if (field == null)
					continue;
				

				if (field.isLikeSearch())
					comparator = Sql.LIKE;

				if (targetModelName != null)
					condition = condition.and(
							sql.field((String)MapUtil.getCasscade(joinMap,targetModelName,"alias"), targetFieldName),
							comparator);
				else if (field.isReferenceSingle())  //??? 将来直接用 ref==2 应该理解为 ref.id == 2
					condition = condition.and(
						sql.field((String)MapUtil.getCasscade(joinMap,field.getTarget(),"alias"), field.getTargetField()),
						comparator);
				else if (field.isDate())
					condition = condition.and(sql.date2String(sql.field(alias,fieldName)), comparator);
				else
					condition = condition.and(sql.field(alias,fieldName), comparator);
				
				if (comparator.equals(Sql.LIKE))
					bindValues.add("%" + (String)filtersMap.get(fieldName) + "%");
				else
					bindValues.add(filtersMap.get(key));
			}
		}
		catch(Exception e){
		}
		
		//这里没有处理alias ???
		if (scopeFilter != null && !PermissionUtil.isLoginUserAdmin()) {
			scopeFilter = scopeFilter.replace(KxConsts.PATTERN_LOGINUSER_ID, 
					"" + GetterUtil.get(ThreadLocalUtil.get(KxConsts.TL_LOGIN_USER_ID), -1));
			condition = condition.and(scopeFilter);
		}
		
		return condition;
	}


	private Map<String,Object> innerGetById(DbHandle handle, String model, long id, String fields) throws SQLException {
		Sql sql = KxApp.sql;
		List<Map> joinList = new ArrayList<Map>();
		List<String> selectList = new ArrayList<String>();
		List<Field> fieldList = new ArrayList<Field>();
		List<Integer> refManyList = new ArrayList<Integer>();
		genJoinAliasFieldList(model, fields, joinList, selectList, fieldList, refManyList);
		
		String aliasMe = sql.alias(0);
		Query query = handle.query(sql
				.select(sql.alias(model, aliasMe))
				.field(selectList)
				.leftJoin(joinList)
				.where(sql.field(aliasMe, KxConsts.ID), Sql.EQ))
				.setUnamedParameter(0, id);
		
		Map<String, Object> entity = query.executeAndFetchFirstRow();
		List<Map<String,Object>> entities = new ArrayList<Map<String,Object>>();
		entities.add(entity);
		Map<String, Map> refManyCache = buildReferenManyCache(fieldList, refManyList, entities);
        return db2admin(model, fieldList, refManyList, refManyCache, entity);
    }
	
	private Map<String,Object> admin2db(String modelName, Map<String, Object> entity, Map<String, Object> origin) {
		Map<String,Object> result = new HashMap<String,Object>();
		Model model = KxApp.profile.getModel(modelName);
		for (String key : entity.keySet()) {
			Object value = entity.get(key);
			Field field = model.getField(key);
			if (field == null)
				continue;

			if (field.isReferenceMany() && value instanceof List) {
				try {
					Set<Long> check = new HashSet<Long>(); //检查去重
					for (Object v : (List)value) {
						if (v instanceof Map)
						{
							long id = GetterUtil.getLong(((Map)v).get(KxConsts.ID), -1);
							if (id != -1)
								check.add(id);
						}
					}
					result.put(key, StringUtils.join(check, KxConsts.MULTIPLE_SEPERATOR));
				} catch (Exception e) {
				}
			}
			else if (field.isReferenceSingle() && value instanceof Map) {
				result.put(key, ((Map)value).get(KxConsts.ID));
			}
			else if (field.isReferenceSingle() && value instanceof List) {
				if (((List) value).size() > 0) {
					Map first = (Map)((List) value).get(0);
					result.put(key, first.get(KxConsts.ID));
				}
				else {
					result.put(key, null);
				}
			}
			else if (field.isChoiceSingle() && value instanceof Map) {
				result.put(key, ((Map)value).get(KxConsts.CHOICE_VALUE));
			}
			else if (field.isChoiceMultiple() && value instanceof List) {
				Set<String> check = new HashSet<String>(); //检查去重
				for (Object v : (List)value) {
					if (v instanceof Map)
					{
						String item = GetterUtil.getString(((Map)v).get(KxConsts.CHOICE_VALUE));
						if (!GetterUtil.isEmpty(item))
							check.add(item);
					}
					else if (v instanceof  String) {
						check.add((String)v);
					}
				}
				result.put(key, StringUtils.join(check, KxConsts.MULTIPLE_SEPERATOR));
			}
			else if (field.TYPE_PASSWORD.equalsIgnoreCase(field.getType()))
			{
				String password = (String) value;
				if (origin == null || (password != null && password.length() > 0 && !password.equals((String)origin.get(key))))
					result.put(key, AuthUtil.encodePassword((String)value));
			}
			else if (field.TYPE_DATETIME.equalsIgnoreCase(field.getType())|| field.TYPE_DATE.equalsIgnoreCase(field.getType()))
			{
				result.put(key, StringUtil.parseDateTime((String)value));
			}
			else if (field.TYPE_FILE.equalsIgnoreCase(field.getType()))
			{
				try {
					result.put(key, KxApp.mapper.writeValueAsString(value));
				} catch (JsonProcessingException e) {
					result.put(key, null);
				}
			}
			else {
				result.put(key, value);
			}
		}
		
		//创建时需要处理default值, 因为可能不在entity里，所以要遍历所有field
		if (origin == null) {
			for (Field field : model.getFields()) {
				String defaultValue = field.getDefault();
				if (defaultValue != null && result.get(field.getName()) == null) {
					result.put(field.getName(), transDefault(defaultValue));
				}
			}
			//新建对象无需id
			result.remove(KxConsts.ID);
		}
		
		return result;
	}
	
	private Object transDefault(String defaultValue) {
		if (defaultValue == null)
			return null;
		
		if (defaultValue.equals(KxConsts.PATTERN_NOW)) {
			return new Date();
		}
		else if (defaultValue.equals(KxConsts.PATTERN_LOGINUSER)) {
			return ThreadLocalUtil.get(KxConsts.TL_LOGIN_USER_ID);
		}
		else {
			return defaultValue;
		}
	}

	/* 
	 * 处理数据库返回row到ngadmin的转换
	 */
	private List<Map<String,Object>> db2admin(String model, List<Field> fieldList, List<Integer> refManyList, List<Map<String,Object>> mapList) throws SQLException {
		List<Map<String,Object>> results = new ArrayList<Map<String,Object>>();
		Map<String, Map> refManyCache = buildReferenManyCache(fieldList, refManyList, mapList);
		for (Map<String,Object> item : mapList) {
			results.add(db2admin(model, fieldList, refManyList, refManyCache, item));
		}
		return results;
	}

	// 废弃的实现
	private Map<String,Object> db2adminOLD(String model, List<Field> fieldList, List<Integer> refManyList, Map<String, Map> refManyCache, Map<String,Object> row) {
		Map<String,Object> result = new HashMap<String,Object>();
		if(KxApp.profile.getModel(model) == null)
			throw new RuntimeException("model not found");
		
		if(row != null) {
			for (String key : row.keySet()) {
				Object value = row.get(key);
				
				int subStart = key.indexOf("_");
				String fieldSn = key.substring(1, subStart == -1 ? key.length() : subStart);
				Field field = fieldList.get(GetterUtil.getInteger(fieldSn, -1));
				
				if (field == null) 
					continue;
				
				if (field.isReferenceMany()) {
					result.put(field.getName(), refMany2admin(field,value, refManyCache == null ? null : refManyCache.get(field.getName())));
				}
				else if (field.isReferenceSingle()){
					Map<String,Object> map = (Map)result.get(field.getName());
					if (map == null) {
						map = new HashMap<String,Object>();
						result.put(field.getName(), map);
					}
					map.put(key.substring(subStart + 1), value == null ? value : value.toString());
				}
				else if (field.isChoiceSingle() && value != null){
					Map<String,String> choice =  new HashMap<String,String>();
					String label = (String)MapUtil.searchMapList(field.getConvertedChoices(), KxConsts.CHOICE_VALUE, value, KxConsts.CHOICE_LABEL);
					if (GetterUtil.isEmpty(label)) {
						result.put(field.getName(), null);
					}
					else {
						choice.put(KxConsts.CHOICE_LABEL, label);
						choice.put(KxConsts.CHOICE_VALUE, (String) value);
						result.put(field.getName(), choice);
					}
				}
				else if (field.isChoiceMultiple() && value != null){
					List<Map<String,String>> choices = new ArrayList<>();
					for (String item : value.toString().split(KxConsts.MULTIPLE_SEPERATOR)) {
						String label = (String) MapUtil.searchMapList(field.getConvertedChoices(), KxConsts.CHOICE_VALUE, item, KxConsts.CHOICE_LABEL);
						if (!GetterUtil.isEmpty(label)) {
							Map<String, String> choice = new HashMap<String, String>();
							choice.put(KxConsts.CHOICE_LABEL, label);
							choice.put(KxConsts.CHOICE_VALUE, item);
							choices.add(choice);
						}
					}
					result.put(field.getName(), choices);
				}
				else if (value instanceof Clob) {
					result.put(field.getName(), SqlUtil.clob2String((Clob)value));
				}
				else if (Field.TYPE_DATETIME.equalsIgnoreCase(field.getType())){
					if (value != null)
						result.put(field.getName(),new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(value));
				}
				else if (Field.TYPE_DATE.equalsIgnoreCase(field.getType())){
					if (value != null)
						result.put(field.getName(),new SimpleDateFormat("yyyy-MM-dd").format(value));
				}
				else if (Field.TYPE_FILE.equalsIgnoreCase(field.getType())){
					result.put(field.getName(), new ArrayList<Map>());
					try { 
						result.put(field.getName(), KxApp.mapper.readValue((String)value, List.class));
					} catch (Exception e) {}
				}
				else {
					result.put(field.getName(), value);
				}
			}
		}
		return result;
	}

	private Map<String,Object> db2admin(String model, List<Field> fieldList, List<Integer> refManyList, Map<String, Map> refManyCache, Map<String,Object> row) {
		Map<String,Object> result = new HashMap<String,Object>();
		if(KxApp.profile.getModel(model) == null)
			throw new RuntimeException("model not found");

		if(row != null) {
			for (int sn = 0; sn < fieldList.size(); sn++) {
				Object value = row.get("f" + sn);
				Field field = fieldList.get(sn);

				if (field.isReferenceMany()) {
					result.put(field.getName(), refMany2admin(field,value, refManyCache == null ? null : refManyCache.get(field.getName())));
				}
				else if (field.isReferenceSingle()){
					Object refID = row.get("f" + sn + "_id");
					if (refID == null) {
						result.put(field.getName(), null);
					}
					else {
						Map<String,Object> map = new HashMap<String,Object>();
						result.put(field.getName(), map);
						map.put(KxConsts.ID, refID);
						map.put(field.getTargetField(), row.get("f" + sn + "_" + field.getTargetField()));
					}
				}
				else if (field.isChoiceSingle() && value != null){
					Map<String,String> choice =  new HashMap<String,String>();
					String label = (String)MapUtil.searchMapList(field.getConvertedChoices(), KxConsts.CHOICE_VALUE, value, KxConsts.CHOICE_LABEL);
					if (GetterUtil.isEmpty(label)) {
						result.put(field.getName(), null);
					}
					else {
						choice.put(KxConsts.CHOICE_LABEL, label);
						choice.put(KxConsts.CHOICE_VALUE, (String) value);
						result.put(field.getName(), choice);
					}
				}
				else if (field.isChoiceMultiple() && value != null){
					List<Map<String,String>> choices = new ArrayList<>();
					for (String item : value.toString().split(KxConsts.MULTIPLE_SEPERATOR)) {
						String label = (String) MapUtil.searchMapList(field.getConvertedChoices(), KxConsts.CHOICE_VALUE, item, KxConsts.CHOICE_LABEL);
						if (!GetterUtil.isEmpty(label)) {
							Map<String, String> choice = new HashMap<String, String>();
							choice.put(KxConsts.CHOICE_LABEL, label);
							choice.put(KxConsts.CHOICE_VALUE, item);
							choices.add(choice);
						}
					}
					result.put(field.getName(), choices);
				}
				else if (value instanceof Clob) {
					result.put(field.getName(), SqlUtil.clob2String((Clob)value));
				}
				else if (Field.TYPE_DATETIME.equalsIgnoreCase(field.getType())){
					if (value != null)
						result.put(field.getName(),new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(value));
				}
				else if (Field.TYPE_DATE.equalsIgnoreCase(field.getType())){
					if (value != null)
						result.put(field.getName(),new SimpleDateFormat("yyyy-MM-dd").format(value));
				}
				else if (Field.TYPE_FILE.equalsIgnoreCase(field.getType())){
					result.put(field.getName(), new ArrayList<Map>());
					try {
						result.put(field.getName(), KxApp.mapper.readValue((String)value, List.class));
					} catch (Exception e) {}
				}
				else {
					result.put(field.getName(), value);
				}
			}
		}
		return result;
	}

	private Map<String, Map> buildReferenManyCache(List<Field> fieldList, List<Integer> refManyList, List<Map<String, Object>> mapList) throws SQLException {
		Map<String, Map> cache = new HashMap<String,Map>();
		Sql sql = KxApp.sql;
		for (Integer refManyNo : refManyList) {
			Field refManyField = fieldList.get(refManyNo);
			HashSet<String> idSet = new HashSet<String>();
			for (Map<String,Object> row : mapList) {
				idSet.addAll(Arrays.asList(GetterUtil.getString(row.get("f" + refManyNo),"").split(KxConsts.MULTIPLE_SEPERATOR)));
			}
			Query query = DbHandle.transactionInstance().query(sql
					.select(refManyField.getTarget())
					.field(KxConsts.ID)
					.field(refManyField.getTargetField())
					.where(KxConsts.ID, Sql.IN, idSet.size(), false));
			
			int position = 0;
			for (String id: idSet)
				query.setUnamedParameter(position++, GetterUtil.getLong(id,-1));
			
			Map<Long,Object> resultMap = new HashMap<Long,Object>();
			for (Map item : query.executeAndFetchRows())
				resultMap.put(GetterUtil.getLong(item.get(KxConsts.ID), -1), item);
			cache.put(refManyField.getName(), resultMap);
		}
		return cache;
	}
	
	private List<Map> refMany2admin(Field field, Object value, Map<Long,Map> cache) {
		
		List<Map> retList = new ArrayList<Map>();
		if (value != null) {				
			String[] splits = value.toString().split("[^0-9]+");
			for (String s : splits)
			{
				long id = GetterUtil.getInteger(s, -1);
				if (id != -1) {
					if (cache == null) {
						Map map = new HashMap<String,Object>();
						map.put(KxConsts.ID, id);
						retList.add(map);
					}
					else {
						if (cache.get(id) != null)
							retList.add(cache.get(id));						
					}
				}
			}
		}
		return retList;		
	}

	private Map<String,Map> joinList2Map(List<Map> joinList) {
		Map<String,Map> results = new HashMap<String,Map>();
		for (Map join : joinList) {
			results.put((String)join.get("model"), join);
		}
		return results;
	}
}
