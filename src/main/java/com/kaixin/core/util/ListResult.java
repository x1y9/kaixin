package com.kaixin.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*
 * 前台list接口的返回参数
 */
public class ListResult {
	
	public Long total = 0L;
	
	/* echo 参数原封不动回传参数里的echo */
	public String echo = "";
	
	public List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
}
