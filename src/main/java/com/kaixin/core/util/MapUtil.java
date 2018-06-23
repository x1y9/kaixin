package com.kaixin.core.util;

import javax.ws.rs.core.MultivaluedMap;
import java.sql.Clob;
import java.util.*;

public class MapUtil {
	
	public static Map<String,String> multiMap2SingleMap(MultivaluedMap<String, String> multiMap) {
		Map<String,String> singleMap= new HashMap<String,String>();
		for (String key: multiMap.keySet()) {
			singleMap.put(key, multiMap.getFirst(key));
		}
		return singleMap;
	}

	public static Object getCasscade(Map map, String key1, String key2) {
		if (map == null || key1 == null || key2 == null)
			return null;
		
		if (map.get(key1) == null)
			return null;
		
		return ((Map)(map.get(key1))).get(key2);
	}
	
	public static Map<String,Object> convertClob(Map<String,Object> row) throws Exception{		
		Map<String,Object> result = new HashMap<String,Object>();
		if(row != null) {
			for (String key : row.keySet()) {
				Object value = row.get(key);
				
				if (value instanceof Clob) {
					result.put(key, SqlUtil.clob2String((Clob)value));
				}
				else {
					result.put(key, value);
				}
			}
		}
		return result;
	}

	public static Object searchMapList(List<Map> maps, String key, Object value, String retKey) {
		if(maps == null)
			return null;
		
		for (Map map : maps) {
			if (map.get(key).equals(value))
				return map.get(retKey);
		}
		return null;
	}

	public static Map<String,Object> mergeMap(Map<String,Object> src, Map<String,Object> tgt) {
		if (src == null)
			return null;
		
		if (tgt == null)
			return src;
		
		for (String key: tgt.keySet()) {
			src.put(key, tgt.get(key));
		}
		
		return src;
	}

	public static void mergePropertiesIntoMap(Properties props, Map map, boolean create) {
		if (map == null) {
			throw new IllegalArgumentException("Map must not be null");
		}

		if (props != null) {
			for (Enumeration en = props.propertyNames(); en.hasMoreElements();) {
				String key = (String) en.nextElement();
				if (create || map.containsKey(key)) {
					map.put(key, props.getProperty(key));
				}
			}
		}
	}
}
