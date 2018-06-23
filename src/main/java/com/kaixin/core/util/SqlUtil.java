package com.kaixin.core.util;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.sql.Clob;
import java.util.List;
import java.util.Map;

public class SqlUtil {
	
	public static void processClob(List<Map<String,Object>> rows) {
		for (Map<String,Object> row :rows) {
			processClob(row);
		}
	}

	public static void processClob(Map<String, Object> row) {
		for (String key: row.keySet()) {
			if (row.get(key) instanceof Clob) {
				row.put(key, clob2String((Clob)row.get(key)));
			}
		}
		
	}

	public static String clob2String(Clob object) 
	{
		try {
			InputStream as = object.getAsciiStream();
			InputStreamReader in = new InputStreamReader(as, "UTF-8");
			StringWriter w = new StringWriter();
			IOUtils.copy(in, w);
			IOUtils.closeQuietly(w);
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(as);
			return w.toString();
		}catch(Exception e) {
			return "";
		}
			
	}
	
	
	
}
