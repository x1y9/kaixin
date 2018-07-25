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
		PermissionUtil.checkPermission(handle, PermissionUtil.getLoginUser(), model, KxConsts.VIEW_LIST, null);
		String scope = getScope(model, handle.getHandle());
		Map<String,Object> result = AdminUtil.list(handle, model,fields,scope,filters,sort,reverse,start,number,retTotal);
		if (retTotal) {
			response.setHeader("X-Total-Count", "" + result.get("total"));
		}
		return (List<Map<String,Object>>)result.get("list");
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
		PermissionUtil.checkPermission(handle, (Map<String, Object>)ThreadLocalUtil.get(KxConsts.TL_LOGIN_USER), model, KxConsts.VIEW_LIST, null);
		String scope = getScope(model, handle.getHandle());

		Map<String,Object> exported = AdminUtil.export(handle, model,fields,scope,filters,sort,reverse);
		final List<Map<String,Object>> results = (List<Map<String,Object>>) exported.get("list");
		final List<Field> fieldList = (List<Field>) exported.get("fields");

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
        return AdminUtil.getById(DbHandle.transactionInstance(), model, id, fields);
    }

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional
    public Map create(@PathParam("model") String model, Map<String,Object> entity) throws SQLException {
		
		adminEvent(model, AdminEvent.ACTION_CREATE, AdminEvent.STAGE_BEFORE, null, entity);
		DbHandle handle = DbHandle.transactionInstance();
		PermissionUtil.checkPermission(handle, (Map<String, Object>)ThreadLocalUtil.get(KxConsts.TL_LOGIN_USER), model, KxConsts.VIEW_CREATE, null);
		Map<String,Object> retEntity = AdminUtil.create(handle, model, entity);
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
		PermissionUtil.checkPermission(handle, (Map<String, Object>)ThreadLocalUtil.get(KxConsts.TL_LOGIN_USER), model, KxConsts.VIEW_EDIT, null);
		Map<String,Object> retEntity = AdminUtil.update(handle, model, entity);
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

		Map<String,Object> entity = AdminUtil.getById(handle, model, id, null);
		adminEvent(model, AdminEvent.ACTION_DELETE, AdminEvent.STAGE_BEFORE, null, entity);
		AdminUtil.delete(handle, model, id);
		adminEvent(model, AdminEvent.ACTION_DELETE, AdminEvent.STAGE_AFTER, null, entity);
    }
	
	@DELETE
	@Transactional
    public void batchDelete(@PathParam("model") String model, @QueryParam("ids") String ids) throws SQLException {

		if (ids == null || ids.length() <=0)
			return;

		DbHandle handle = DbHandle.transactionInstance();
		PermissionUtil.checkPermission(handle, (Map<String, Object>)ThreadLocalUtil.get(KxConsts.TL_LOGIN_USER), model, KxConsts.VIEW_DELETE, null);
		adminEvent(model, AdminEvent.ACTION_DELETE, AdminEvent.STAGE_BEFORE, null, ids);
		AdminUtil.batchDelete(handle,model, ids);
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


}
