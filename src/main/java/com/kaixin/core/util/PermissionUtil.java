package com.kaixin.core.util;

import com.kaixin.core.app.KxApp;
import com.kaixin.core.app.KxConsts;
import com.kaixin.core.db.DbHandle;
import com.kaixin.core.profile.Model;
import com.kaixin.core.sql.Condition;
import com.kaixin.core.sql.Sql;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * 后台admin模块的权限校验模块
 * 1. 权限表的模型可以为多值，动作可以为多值（增删改查），用户为多值或空
 * 2. 字段为json，如果为空为缺省字段，否则按json来
 * 3. 如果当前用户为管理员，所有权限检查过掉
 * 4. 在权限表里逐条检查权限，有一条满足则满足
 * 5. 用户可以留空表示guest权限，如果backendAuth=true,相当于所有登录用户都有的权限。
 *    如果backendAuth=false，要相当于未登录用户和所有登录用户都有的权限
 * 6. 有些模型权限需要带path，则对path进行前缀匹配
 *
 * 几种常见权限情况
 * 1. 某个人维护特定表
 * 2. 某些人只读一些表，排除一些敏感字段，比如价格
 * 3. 某些人维护一些表，排除一些敏感字段，比如发布
 * 4. 多人共同维护一张表，各自修改各自的记录
 * 5. 工作流
 */
public class PermissionUtil {

	public static boolean isLoginUserAdmin() {
		Map<String,Object> loginUser = (Map<String,Object>)ThreadLocalUtil.get(KxConsts.TL_LOGIN_USER);
		if (loginUser != null)
			return GetterUtil.get(loginUser.get(KxConsts.COL_USER_IS_ADMIN), false);
		else
			return false;
	}
	
	public static long getLoginUserId() {
		return GetterUtil.getLong(ThreadLocalUtil.get(KxConsts.TL_LOGIN_USER_ID), -1);
	}
	
	public static Map getLoginUser() {
		return (Map)ThreadLocalUtil.get(KxConsts.TL_LOGIN_USER);
	}	

	public static List<Map<String, Object>> getPermissionsByUser(Map<String, Object> loginUser) throws Exception {
		if (PropsUtil.getBoolean(PropsKeys.AUTH_BACKEND_NEED_AUTH) && loginUser == null)
			return null;

		DbHandle handle = DbHandle.transactionInstance();
		List<Map<String, Object>> permissions = new ArrayList<Map<String, Object>>();
		if (loginUser != null) {				
			permissions = handle.query(KxApp.sql
						   .select(KxConsts.TBL_PERMISSION)
						   .where(KxConsts.COL_PERMISSION_USERS, Sql.LIKE)
						   .or(KxConsts.COL_PERMISSION_USERS, Sql.EQ)
						   .or(KxConsts.COL_PERMISSION_USERS, Sql.IS_NULL))
						   .setUnamedParameter(0, "[" + loginUser.get(KxConsts.ID) +"]")
						   .setUnamedParameter(1, "")
						   .executeAndFetchRows();
				
		}
		else {
			permissions = handle.query(KxApp.sql
					   .select(KxConsts.TBL_PERMISSION)
					   .where(KxConsts.COL_PERMISSION_USERS, Sql.EQ)
					   .or(KxConsts.COL_PERMISSION_USERS, Sql.IS_NULL))
					   .setUnamedParameter(0, "")
					   .executeAndFetchRows();
			
		}
		return permissions;
	}

	public static Map getPermissionsMapByUser(Map<String, Object> loginUser) throws Exception {
		List<Map<String, Object>> permissions = getPermissionsByUser(loginUser);
		if (permissions == null)
			return null;
		
		Map<String,Object> permissionMap = new HashMap<String,Object>();		
		for (Map<String, Object> p : permissions) {
			String pm = (String)p.get(KxConsts.COL_PERMISSION_MODEL);
			String pa = (String)p.get(KxConsts.COL_PERMISSION_ACTION);
			String pf = GetterUtil.get(p.get(KxConsts.COL_PERMISSION_FIELDS),"");
			if (pm != null && pa != null && pm.length() > 0 && pa.length() > 0) {
				Map<String,Object> pmModel = (Map<String,Object>)permissionMap.get(pm);
				if (pmModel == null) {
					pmModel = new HashMap<String,Object>();
					permissionMap.put(pm, pmModel);
				}
				pmModel.put(pa, pf);
			}
		}

		return permissionMap;
	}
	
	public static Map getFieldsMapByUser(Map<String, Object> loginUser) throws Exception {
		Map<String,Object> permissionMap = getPermissionsMapByUser(loginUser);
		Map<String,Object> modelFieldMap = new HashMap<String,Object>();
		if (permissionMap == null)
			return null;
		
		for (Model model : KxApp.profile.getModels()) {
			Map<String,Object> fieldMap = new HashMap<String,Object>();
			modelFieldMap.put(model.getName(), fieldMap);
			fieldMap.put(KxConsts.VIEW_LIST, getFieldsByModelViewPermssion(permissionMap, model.getName(), KxConsts.VIEW_LIST));
			fieldMap.put(KxConsts.VIEW_CREATE, getFieldsByModelViewPermssion(permissionMap, model.getName(), KxConsts.VIEW_CREATE));
			fieldMap.put(KxConsts.VIEW_EDIT, getFieldsByModelViewPermssion(permissionMap, model.getName(), KxConsts.VIEW_EDIT));
			fieldMap.put(KxConsts.VIEW_SHOW, getFieldsByModelViewPermssion(permissionMap, model.getName(), KxConsts.VIEW_SHOW));
		}
		
		return modelFieldMap;
	}
	
	private static List<Map> getFieldsByModelViewPermssion(Map<String, Object> permissionMap, String model, String view) {
		//权限的定义支持通配符
		String fieldDesc = (String)MapUtil.getCasscade(permissionMap,model,view);
		if (fieldDesc == null)
			fieldDesc = (String)MapUtil.getCasscade(permissionMap,model,KxConsts.VIEW_ALL);
		if (fieldDesc == null)
			fieldDesc = (String)MapUtil.getCasscade(permissionMap,KxConsts.MODEL_ALL,view);		
		if (fieldDesc == null)
			fieldDesc = (String)MapUtil.getCasscade(permissionMap,KxConsts.MODEL_ALL,KxConsts.VIEW_ALL);
		
		if (fieldDesc == null) {			
			return isLoginUserAdmin() ? KxApp.profile.getModel(model).getJsFieldsByView(view) : null;
		}
		else {
			List<Map> defaultFields = KxApp.profile.getModel(model).getJsFieldsByView(view);
			Map<String,Map> defaultFieldsMap = KxApp.profile.getModel(model).getJsFieldsMapByView(view);
			return mergeJsFeilds(defaultFields, defaultFieldsMap, fieldDesc);
		}
	}

	/*
	 * desc是一个json，比如
	 *  ["name","content":{"readOnly":true},"type"]
	 *  这个list做了一个简化，如果元素是字符，就直接复制缺省(比如上面的name和type)，如果是Map，则merge
	 */
	private static List<Map> mergeJsFeilds(List<Map> defaultFields, Map<String,Map> defaultFieldsMap, String fieldDesc) {
		try {
			List<Object> descFields = KxApp.mapper.readValue(fieldDesc, List.class);
			List<Map> results = new ArrayList<Map>();
			for (Object field: descFields) {
				if (field instanceof String) {
					Map f = defaultFieldsMap.remove((String)field);
					if (f != null)
						results.add(f);
				} 
				else if (field instanceof Map) {
					Map f = defaultFieldsMap.remove(((Map)field).get("name"));
					if (f != null) 
						results.add(MapUtil.mergeMap(f, (Map)field));
				}
				
			}
			return results;
		}
		catch(Exception e) {
			return defaultFields;
		}
	}

	public static void checkPermission(DbHandle handle, Map<String, Object> user, String model, String action, String path) throws SQLException {
		
		/* 校验管理员 */
		if (user != null && GetterUtil.getBoolean(user.get(KxConsts.COL_USER_IS_ADMIN), false))
			return;

		if (user == null && PropsUtil.getBoolean(PropsKeys.AUTH_BACKEND_NEED_AUTH))
			throw new RuntimeException("user not login");

		/* 加载空用户权限 */
		Sql sql = KxApp.sql;
		List<Map<String, Object>> perms = new ArrayList<>(handle.query(
				sql.select(KxConsts.TBL_PERMISSION)
				   .where(new Condition(KxConsts.COL_PERMISSION_MODEL, sql.EQ)
				         			.or(KxConsts.COL_PERMISSION_MODEL, sql.EQ))
				   .and(new Condition(KxConsts.COL_PERMISSION_ACTION, sql.EQ)
				         		  .or(KxConsts.COL_PERMISSION_ACTION, sql.EQ))
				   .and(new Condition(KxConsts.COL_PERMISSION_USERS, sql.EQ)
						   		.or(KxConsts.COL_PERMISSION_USERS, sql.IS_NULL)))
				.setUnamedParameter(0, model)
				.setUnamedParameter(1, KxConsts.MODEL_ALL)
				.setUnamedParameter(2, action)
				.setUnamedParameter(3, KxConsts.VIEW_ALL)
				.setUnamedParameter(4, "") //sql里= ''，可以匹配' '等任意长度空字符串
				.executeAndFetchRows());
			
			
		/* 加载指定用户权限 */
		if(user != null) {
			perms.addAll(handle.query(
			sql.select(KxConsts.TBL_PERMISSION)
			    .where(new Condition(KxConsts.COL_PERMISSION_MODEL, sql.EQ)
			         			 .or(KxConsts.COL_PERMISSION_MODEL, sql.EQ))
			    .and(new Condition(KxConsts.COL_PERMISSION_ACTION, sql.EQ)
			         		   .or(KxConsts.COL_PERMISSION_ACTION, sql.EQ))
				.and(KxConsts.COL_PERMISSION_USERS, sql.LIKE))
				.setUnamedParameter(0, model)
				.setUnamedParameter(1, KxConsts.MODEL_ALL)
				.setUnamedParameter(2, action)
				.setUnamedParameter(3, KxConsts.VIEW_ALL)
				.setUnamedParameter(4, "%[" + user.get("id") + "]%")
				.executeAndFetchRows());
		}

		if(path == null) {
			/* 不带路径的模型 */
			if (perms.size() > 0)
				return;
		}
		else {
			/* 带路径的模型 */
			for(Map<String,Object> perm: perms) {
				if (path.startsWith((String)perm.get(KxConsts.COL_PERMISSION_MEMO)))
						return;
			}
		}
		
		throw new RuntimeException("permission denied");
	}

}
