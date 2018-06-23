package com.kaixin.core.util;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class JsonException extends WebApplicationException {
	
	public JsonException(String message) {
		this(message, 500);
	}
	
	public JsonException(String message, int code) {
        super(Response.status(code).entity(
        		new JsonResult(code, message)).type(MediaType.APPLICATION_JSON).build());
    }
}
