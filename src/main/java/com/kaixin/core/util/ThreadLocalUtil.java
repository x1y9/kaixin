package com.kaixin.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/*
 * 封装ThreadLocal变量的操作，所有的key必须名字唯一
 */
public class ThreadLocalUtil {
	
	public static Object get(String key) {
		Map<String, Object> hm = _threadMap.get();
		if (hm != null) {
			return hm.get(key);
		}
		return null;
	}
	
	public static void set(String key, Object value) {
		Map<String, Object> hm = _threadMap.get();
		if(hm != null) {
			hm.put(key, value);
		}
	}

	public static void setIfNull(String key, Object value) {
		Map<String, Object> hm = _threadMap.get();
		if(hm != null && hm.get(key) == null) {
			hm.put(key, value);
		}
	}

	public static void remove(String key) {
		Map<String, Object> hm = _threadMap.get();
		if(hm != null)
		{
			hm.remove(key);
		}
	}
	
	public static void clear() {
		Map<String, Object> hm = _threadMap.get();
		if(hm != null)
		{
			hm.clear();
		}
	}
	
	
	/*
	 * 存储定义
	 */
	private static final Logger _log = LoggerFactory.getLogger(ThreadLocalUtil.class);
	
	private static ThreadLocal<Map<String, Object>> _threadMap = new ThreadLocal<Map<String, Object>>() {
		@Override
	    protected Map<String, Object> initialValue() {
			return new HashMap<String,Object>();
		}		
	};

}
