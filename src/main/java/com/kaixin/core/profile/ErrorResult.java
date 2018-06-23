package com.kaixin.core.profile;

import javax.servlet.http.HttpServletResponse;

public class ErrorResult {
	public String message;
	public String detail;
	public int code;

	public ErrorResult(String message) {
		this(message, null, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	}

	public ErrorResult(String message, String detail, int code) {
		this.message = message;
		this.detail = detail;
		this.code = code;
	}
}
