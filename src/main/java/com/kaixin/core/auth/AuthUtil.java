package com.kaixin.core.auth;

import com.kaixin.core.app.KxConsts;
import com.kaixin.core.util.GetterUtil;
import com.kaixin.core.util.PropsKeys;
import com.kaixin.core.util.PropsUtil;
import com.kaixin.core.util.StringUtil;
import org.apache.commons.codec.digest.Md5Crypt;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

public class AuthUtil
{

	public static String createToken(Map<String,Object> user)
	{
		/* Expires in one hour */
		//long expires = System.currentTimeMillis() + 1000L * 60 * 60;
		/* never expire */
		long expires = 0;

		StringBuilder tokenBuilder = new StringBuilder();
		tokenBuilder.append(GetterUtil.get(user.get("id"), 0));
		tokenBuilder.append(":");
		try {
			tokenBuilder.append(StringUtil.toHex(GetterUtil.get(user.get(KxConsts.COL_USER_NAME), "").getBytes("UTF-8")));
		} catch (UnsupportedEncodingException e) {
		}
		tokenBuilder.append(":");
		tokenBuilder.append(expires);
		tokenBuilder.append(":");
		tokenBuilder.append(computeSignature(tokenBuilder.toString()));

		return tokenBuilder.toString();
	}


	public static String computeSignature(String token)
	{
		String toCheck = token + PropsUtil.get(PropsKeys.AUTH_TOKEN_SALT);

		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("No MD5 algorithm available!");
		}

		//token没有中文，这里不用管编码
		return StringUtil.toHex(digest.digest(toCheck.getBytes()));
	}


	public static long getUserIdFromToken(String authToken)
	{
		if (null == authToken) {
			return -1;
		}

		String[] parts = authToken.split(":");
		if (parts.length > 0)
			return GetterUtil.getLong(parts[0],-1);
		else 
			return 0;
	}

	public static String getUserNameFromToken(String authToken)
	{
		try {
			String[] parts = authToken.split(":");
			return StringUtil.fromHex(parts[1]);
		}catch(Exception e) {
			return null;
		}		
	}
	
	public static boolean validateToken(String authToken)
	{
		if (authToken == null)
			return false;
		
		String[] parts = authToken.split(":");
		
		if (parts.length != 4)
			return false;
		
		String userId = parts[0];
		String username = parts[1];
		long expires = Long.parseLong(parts[2]);
		String signature = parts[3];

		if (expires != 0 && expires < System.currentTimeMillis()) {
			return false;
		}

		String computed = computeSignature(parts[0] + ":" + parts[1] + ":" + parts[2] +":");
		return signature.equals(computed);
	}
	
	public static String encodePassword(String raw) {
		if (raw == null)
			return null;
		
		if(KxConsts.PASSWORD_ALGORITHM_APR1.equals(PropsUtil.get(PropsKeys.AUTH_PASSWORD_ALGORITHM))) {
			String salt = RandomStringUtils.randomAlphanumeric(4);
			return Md5Crypt.apr1Crypt(raw, salt);
		}
		else {
			MessageDigest digest;
			try {
				digest = MessageDigest.getInstance("MD5");
			} catch (NoSuchAlgorithmException e) {
				throw new IllegalStateException("No MD5 algorithm available!");
			}
	
			String org = raw + PropsUtil.get(PropsKeys.AUTH_PASSWORD_MD5_SALT);
			return StringUtil.toHex(digest.digest(org.getBytes()));
		}
	}
	
	public static boolean validatePassword(String raw, String saved) {
		if (raw == null || saved == null)
			return false;
		
		if(KxConsts.PASSWORD_ALGORITHM_APR1.equals(PropsUtil.get(PropsKeys.AUTH_PASSWORD_ALGORITHM))) {
			String expect = Md5Crypt.apr1Crypt(raw, saved);
			return expect.equals(saved);
		}
		else {
			String expect = encodePassword(raw);
			return expect.equals(saved);
		}
	}
	

}