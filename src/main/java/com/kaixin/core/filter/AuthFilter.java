package com.kaixin.core.filter;

import com.kaixin.core.app.KxApp;
import com.kaixin.core.app.KxConsts;
import com.kaixin.core.auth.AuthUtil;
import com.kaixin.core.db.DbHandle;
import com.kaixin.core.profile.Profile;
import com.kaixin.core.sql.Sql;
import com.kaixin.core.util.*;
import org.apache.http.HttpStatus;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.util.Map;


/*
 * 主认证Filter，这个filter是jersey的filter，不是servlet filter
 */
public class AuthFilter implements ContainerRequestFilter {
	private Profile profile;

	public AuthFilter(Profile profile) {
		this.profile = profile;
	}

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {

		String path = requestContext.getUriInfo().getPath();
		String authToken = extractAuthTokenFromRequest(requestContext);

		ThreadLocalUtil.remove(KxConsts.TL_LOGIN_USER);
		ThreadLocalUtil.remove(KxConsts.TL_LOGIN_USER_ID);
		ThreadLocalUtil.remove(KxConsts.TL_LOGIN_USER_NAME);

		boolean onlyAdmin = StringUtil.matchPatterns(path, PropsUtil.getArray(PropsKeys.AUTH_ONLY_ADMIN_URLS));
		boolean onlyLogin = StringUtil.matchPatterns(path, PropsUtil.getArray(PropsKeys.AUTH_ONLY_LOGIN_URLS));
		boolean isAdmin = false;
		boolean isGuest = true;

		if (!AuthUtil.validateToken(authToken)) {
			
            //token不合法
			String url = requestContext.getUriInfo().getPath().toString();
			/* 看是否security url，如果是全部跳转
			if (!url.contains("auth/")) {
				try {
					requestContext.abortWith(Response.temporaryRedirect(new URI("/auth/login")).build());
				} catch (URISyntaxException e) {
				}
			}
			*/
        }
		else {
			
			try (DbHandle handle = DbHandle.manualInstance()) {
				Map<String, Object> loginUser = handle.query(KxApp.sql.select(KxConsts.TBL_USER)
						.where(KxConsts.ID, Sql.EQ))
						.setUnamedParameter(0, AuthUtil.getUserIdFromToken(authToken))
						.executeAndFetchFirstRow();
								
				if (loginUser != null) {
					isGuest = false;
					ThreadLocalUtil.set(KxConsts.TL_LOGIN_USER, loginUser);
					ThreadLocalUtil.set(KxConsts.TL_LOGIN_USER_ID, loginUser.get(KxConsts.ID));
					ThreadLocalUtil.set(KxConsts.TL_LOGIN_USER_NAME, loginUser.get(KxConsts.COL_USER_NAME));
					isAdmin = GetterUtil.getBoolean(loginUser.get(KxConsts.COL_USER_IS_ADMIN), false);
				}
			}
		}

		if ((isGuest && onlyLogin) || (!isAdmin && onlyAdmin))
			throw new WebApplicationException("unauthorized", HttpStatus.SC_UNAUTHORIZED);
	}

	private String extractAuthTokenFromRequest(ContainerRequestContext requestContext)
	{
		String authToken = null;
		
		MultivaluedMap<String, String> multivaluedMap = requestContext.getUriInfo().getQueryParameters();
		if(multivaluedMap != null)
		{
			authToken = multivaluedMap.getFirst(KxConsts.AUTH_TOKEN_NAME);
		}

		if (authToken == null) {
			authToken = requestContext.getHeaderString(KxConsts.AUTH_TOKEN_NAME);
		}
		
		if (authToken == null) {
			Map<String,Cookie> cookies = requestContext.getCookies();
			Cookie cookie = cookies.get(KxConsts.AUTH_TOKEN_NAME);
			if(cookie != null)
			{
				authToken = cookie.getValue();
			}
		}
		
		return authToken;
	}
}
