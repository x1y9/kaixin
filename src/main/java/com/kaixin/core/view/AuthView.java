package com.kaixin.core.view;


import com.google.common.base.Optional;
import com.kaixin.core.app.KxApp;
import com.kaixin.core.app.KxConsts;
import com.kaixin.core.auth.AuthUtil;
import com.kaixin.core.auth.BasicCredentials;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Date;
import java.util.Map;

/*
 * 认证相关Resource接口，前台是普通网页用这个，如果是SPA用AuthApi
 * 注意cookie的用法，必须这样才能在IE、firefox、chrome都工作
 */
@Path("/auth")
public class AuthView {

	
	@Path("/login")
	@POST
    public Response login(@FormParam("account") String account, @FormParam("password") String password) throws Exception {
		Optional<Map<String,Object>> result = KxApp.authenticator.authenticate(new BasicCredentials(account, password));
		if (result.isPresent()) {
			String token = AuthUtil.createToken(result.get());
			Date expire = new Date(new Date().getTime() + 86400*365000L);
			return Response.seeOther(new URI("/"))
				.cookie(new NewCookie(KxConsts.AUTH_TOKEN_NAME, token,"/", null, 1, null, 86400*365, expire,false,false))
				.build();
		}
		else {
			//最好这里不要写绝对url，还是回到当前url，这样这个模块更通用
			return Response.seeOther(new URI("/login?code=1"))
					.cookie(new NewCookie(KxConsts.AUTH_TOKEN_NAME, "", "/", null, null, 0 /*maxAge*/, false))
					.build();			
		}
    }
	
	@GET
	@Path("/logout")
	public Response logout() throws Exception {
		return Response.temporaryRedirect(new URI("/"))
				.cookie(new NewCookie(KxConsts.AUTH_TOKEN_NAME, "", "/", null, null, 0 /*maxAge*/, false))
				.build();
		
	}

}
