package com.kaixin.core.view;

import com.kaixin.core.app.KxApp;
import com.kaixin.core.app.KxConsts;
import com.kaixin.core.app.KxTemplate;
import com.kaixin.core.db.DbHandle;
import com.kaixin.core.db.TransactionInterceptor;
import com.kaixin.core.db.Transactional;
import com.kaixin.core.sql.Sql;
import com.kaixin.core.sql2o.Connection;
import com.kaixin.core.util.SqlUtil;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.sql.Clob;
import java.util.HashMap;
import java.util.Map;

/*
 * 处理标准Form，比如在线申请试用之类, 每个Form有个唯一名字。
 * 相关配置保存在 form_config 表里
 * 所有实例保存在 form_entity 表里
 */

@Path("/form/{form}")
public class FormView {
	@Context private ContainerRequestContext reqContext;
	@Context private UriInfo uriInfo;
	
	/*
	 * 通用表单处理，记录到表单实例表，并发送邮件
	 */
	@POST
	@Path("submit")
	@Consumes("application/x-www-form-urlencoded")
	@Transactional
	public KxTemplate submit(@PathParam("form") String form, MultivaluedMap<String, String> entity) throws Exception{
		Map<String,Object> vm = new HashMap<String,Object>();
		

		Sql sql = KxApp.sql;

		//查询配置
		Map<String, Object> config = DbHandle.transactionInstance().query(
				sql.select(KxConsts.TBL_FORM_CONFIG)
				.where(KxConsts.COL_FORM_NAME, sql.EQ))
			.setUnamedParameter(0, form)
			.executeAndFetchFirstRow();

		if (config !=null) {
			//提交表单的处理
			vm.put(KxConsts.TEMPLATE_VAR_MESSAGE, SqlUtil.clob2String((Clob)config.get(KxConsts.COL_FORM_RESPONSE)));
		}

		//记录表单
		String content = "";
		if (entity != null)
			content = KxApp.mapper.writeValueAsString(entity);

		DbHandle.transactionInstance().query(sql
				.insert(KxConsts.TBL_FORM_ENTITY)
				.set(KxConsts.COL_FORM_NAME)
				.set(KxConsts.COL_FORM_CONTENT))
			.setUnamedParameter(0, form)
			.setUnamedParameter(1, content)
			.executeUpdate();

		//发送邮件
		if (config != null && config.get(KxConsts.COL_FORM_NOTIFIER) != null) {
			KxApp.smtp.sendAsync((String)config.get(KxConsts.COL_FORM_NOTIFIER), null, null, "表单提醒:" + form, content);
		}

		
		return new KxTemplate("form-submit.ftl", reqContext, uriInfo, vm);		
	}

	
}
