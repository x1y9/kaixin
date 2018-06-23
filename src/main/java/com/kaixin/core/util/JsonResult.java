package com.kaixin.core.util;

public class JsonResult {
	private static final int DEFAULT_CODE = 200;
	
	private int code;
	private String message;
	

	public JsonResult() {
		this("");
	}
	
	public JsonResult(String message) {
		this(DEFAULT_CODE, message);
	}

	public JsonResult(int code, String message) {
		this.code = code;
		this.message = message;
	}
	
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
	
}
