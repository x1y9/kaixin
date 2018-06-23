package com.kaixin.core.app;

import com.kaixin.core.module.KxLocale;
import com.kaixin.core.util.MapUtil;
import com.kaixin.core.util.PropsKeys;
import com.kaixin.core.util.PropsUtil;
import com.kaixin.core.util.ThreadLocalUtil;
import io.dropwizard.views.View;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.UriInfo;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/*
 * 对FTL模板的封装
 * 
 * 一般的, ftl模板文件的路径是相对这个class的,
 * 所以这里封装一下加上FusionApp.viewPath转换到绝对路径
 * 
 */
public class KxTemplate extends View{

	private final Map<String,Object> model;
	private final UriInfo uriInfo;
	
	public KxTemplate(String templateName, ContainerRequestContext rc, UriInfo uriInfo, Map model) {
		super(PropsUtil.get(PropsKeys.SYS_VIEW_PATH) + templateName, StandardCharsets.UTF_8);
		this.uriInfo = uriInfo;
		if (model == null) {
			this.model = new HashMap<String,Object>();
		}
		else {
			this.model = model;
		}
	}
	
	public Map<String,Object> getModel() {
		return model;
	}

	//freemarker不能直接访问public fields，所以各种参数都转换为map返回到模板中
	public Map<String,String> getSysProperties() {
		return PropsUtil.getPropertiesMap(false);
	}

	public Map<String,Object> getProfile() {
		return KxApp.mapper.convertValue(KxApp.profile, Map.class);
	}

	public KxLocale getLocale() {
		return KxApp.locale;
	}

	public Map<String,String> getReqQueryPara() {
		if (uriInfo == null)
			return null;
		else
			return MapUtil.multiMap2SingleMap(uriInfo.getQueryParameters());
	}

	public Map<String,Object> getLoginUser() {
		return (Map<String,Object>)ThreadLocalUtil.get(KxConsts.TL_LOGIN_USER);
	}

	public Long getLoginUserId() {
		return (Long)ThreadLocalUtil.get(KxConsts.TL_LOGIN_USER_ID);
	}

	public String getLoginUserName() {
		return (String)ThreadLocalUtil.get(KxConsts.TL_LOGIN_USER_NAME);
	}
}

