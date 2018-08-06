package com.kaixin.core.api;

import com.google.common.base.Optional;
import com.kaixin.core.app.KxApp;
import com.kaixin.core.app.KxConsts;
import com.kaixin.core.auth.AuthUtil;
import com.kaixin.core.auth.BasicCredentials;
import com.kaixin.core.db.DbHandle;
import com.kaixin.core.db.TransactionInterceptor;
import com.kaixin.core.db.Transactional;
import com.kaixin.core.sql.Sql;
import com.kaixin.core.util.PropsKeys;
import com.kaixin.core.util.PropsUtil;
import com.kaixin.core.util.StringUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.time.DateUtils;

import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/*
 * 认证相关REST接口，前台是SPA用这个，如果是普通网页用AuthView
 * 注意恶心的cookie的用法，必须这样才能在IE、firefox、chrome都工作
 *
 * cookie，也可以交由js去设置，但是js需要angular1.4以后才能设置cookie的属性，
 * 不如server直接返回cookie可以更好的兼容。
 */

@Path("/api/auth")
@Produces(value = MediaType.APPLICATION_JSON)
public class AuthApi {

	@Context private ContainerRequestContext reqContext;
	@Context private UriInfo uriInfo;

	@POST
	@Path("/login")
	@Consumes(MediaType.APPLICATION_JSON)
    public Response login(Map<String, Object> para)  {
		BasicCredentials credential = new BasicCredentials((String)para.get("account"), (String)para.get("password"));
		final Optional<Map<String,Object>> result = KxApp.authenticator.authenticate(credential);
		if (result.isPresent()) {
			final String token = AuthUtil.createToken(result.get());
			Map<String,Object> entity = new HashMap<String,Object>(){{
				put(KxConsts.AUTH_TOKEN_NAME, token);
				put("user", result.get());
			}};
			Date expire = new Date(new Date().getTime() + 86400*365000L);
			return Response.ok().entity(entity)
					.cookie(new NewCookie(KxConsts.AUTH_TOKEN_NAME, token,"/", null, 1, null, 86400*365, expire,false,false)).build();
		}
		else {
			throw new RuntimeException(KxApp.locale.get("Account-or-password-error"));
		}
	}

	@GET
	@Path("/logout")
	public Response logout() throws Exception {
		return Response.ok()
				.cookie(new NewCookie(KxConsts.AUTH_TOKEN_NAME, "", "/", null, null, 0 /*maxAge*/, false))
				.build();

	}

	@POST
	@Path("/signup")
	@Transactional
	@Consumes(MediaType.APPLICATION_JSON)
	public void signup(Map<String, Object> para) throws Exception {
		if (!PropsUtil.getBoolean(PropsKeys.AUTH_SIGNUP_ENABLE))
			throw new RuntimeException(KxApp.locale.get("Signup-is-not-supported"));

		String account = (String)para.get("account");
		String password = (String)para.get("password");

		if (account == null || account.length() == 0 || password == null || password.length() == 0)
			throw new RuntimeException(KxApp.locale.get("Account-or-password-error"));

		Map<String, Object> user = DbHandle.transactionInstance()
				.query(KxApp.sql.select(KxConsts.TBL_USER)
						.where(KxConsts.COL_USER_ACCOUNT, Sql.EQ))
				.setUnamedParameter(0, account)
				.executeAndFetchFirstRow();

		if (user != null)
			throw new RuntimeException(KxApp.locale.get("Account-already-exist,-please-input-again."));

		DbHandle.transactionInstance()
				.query(KxApp.sql.insert(KxConsts.TBL_USER)
						.set(KxConsts.COL_USER_ACCOUNT)
						.set(KxConsts.COL_USER_NAME)
						.set(KxConsts.COL_USER_PASSWORD))
				.setUnamedParameter(0, account)
				.setUnamedParameter(1, new Date())
				.setUnamedParameter(2, AuthUtil.encodePassword(password))
				.executeUpdate();
	}

	@POST
	@Path("/forgot")
	@Transactional
	@Consumes(MediaType.APPLICATION_JSON)
	public void forgot(Map<String, Object> para) throws Exception {
		String account = (String)para.get("account");

		Map<String, Object> user = DbHandle.transactionInstance()
				.query(KxApp.sql.select(KxConsts.TBL_USER)
				.where(KxConsts.COL_USER_ACCOUNT, Sql.EQ))
				.setUnamedParameter(0, account)
				.executeAndFetchFirstRow();

		if (user == null)
			throw new RuntimeException(KxApp.locale.get("Email-invalid,-please-input-again."));

		//重置resetKey和resetTime
		String resetKey = RandomStringUtils.randomAlphanumeric(10);
		DbHandle.transactionInstance()
				.query(KxApp.sql.update(KxConsts.TBL_USER)
						.set(KxConsts.COL_USER_RESET_KEY)
						.set(KxConsts.COL_USER_RESET_TIME)
						.where(KxConsts.COL_USER_ACCOUNT, Sql.EQ))
				.setUnamedParameter(0, resetKey)
				.setUnamedParameter(1, new Date())
				.setUnamedParameter(2, account)
				.executeUpdate();

		//发送邮件
		String link = PropsUtil.get(PropsKeys.SYS_SERVER_URL) + "/forgot-reset?key=" + resetKey;
		KxApp.smtp.sendAsync(account,null, null, KxApp.locale.get("Reset-your-password"),
				KxApp.locale.get("Click-this-link-to-reset-your-password:") + "<br/>" +  StringUtil.url2Anchor(link));
	}

	@POST
	@Path("/reset")
	@Transactional
	@Consumes(MediaType.APPLICATION_JSON)
	public void reset(Map<String, Object> para) throws Exception {
		String resetKey = (String)para.get("resetKey");
		String password = (String)para.get("password");

		Map<String, Object> user = DbHandle.transactionInstance()
				.query(KxApp.sql.select(KxConsts.TBL_USER)
						.where(KxConsts.COL_USER_RESET_KEY, Sql.EQ))
				.setUnamedParameter(0, resetKey)
				.executeAndFetchFirstRow();

		if (user == null || password == null || password.length() == 0)
			throw new RuntimeException(KxApp.locale.get("Parameter-invalid"));

		Date resetTime = (Date)user.get(KxConsts.COL_USER_RESET_TIME);
		if ( resetTime == null || resetTime.before(DateUtils.addHours(new Date(), PropsUtil.getInteger(PropsKeys.AUTH_PASSWORD_RESET_TIMEOUT))))
			throw new RuntimeException(KxApp.locale.get("Password-reset-tiemeout"));

		//重置password和resetTime
		DbHandle.transactionInstance()
				.query(KxApp.sql.update(KxConsts.TBL_USER)
						.set(KxConsts.COL_USER_PASSWORD)
						.set(KxConsts.COL_USER_RESET_KEY, null)
						.where(KxConsts.COL_USER_RESET_KEY, Sql.EQ))
				.setUnamedParameter(0, AuthUtil.encodePassword(password))
				.setUnamedParameter(1, resetKey)
				.executeUpdate();

	}
}
