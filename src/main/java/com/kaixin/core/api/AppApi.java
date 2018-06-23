package com.kaixin.core.api;

import com.kaixin.core.app.KxApp;
import com.kaixin.core.app.KxConsts;
import com.kaixin.core.db.Transactional;
import com.kaixin.core.util.PermissionUtil;
import com.kaixin.core.util.PropsUtil;
import com.kaixin.core.util.ThreadLocalUtil;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

@Path("/api/app")
@Produces(value = MediaType.APPLICATION_JSON)
public class AppApi {
	
	@GET
	@Path("/loginInfo")
	@Transactional
    public Map loginInfo() throws Exception {
		Map<String,Object> result = new HashMap<String,Object>();
		Map<String, Object> loginUser = (Map<String, Object>)ThreadLocalUtil.get(KxConsts.TL_LOGIN_USER);

		result.put("loginUser", loginUser);
		result.put("isAdmin", PermissionUtil.isLoginUserAdmin());
		result.put("permissions", PermissionUtil.getFieldsMapByUser(loginUser)); 
        return result;
    }

	@GET
	@Path("/bootup")
	@Transactional
	public Map bootup() throws Exception {
		Map<String,Object> result = new HashMap<String,Object>();
		Map<String, Object> loginUser = (Map<String, Object>)ThreadLocalUtil.get(KxConsts.TL_LOGIN_USER);

		result.put("profile", KxApp.profile);
		result.put("system", PropsUtil.getPropertiesMap(true));
		result.put("login", loginInfo());
		return result;
	}

	
}
