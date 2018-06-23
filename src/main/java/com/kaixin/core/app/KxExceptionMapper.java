package com.kaixin.core.app;

import com.kaixin.core.profile.ErrorResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class KxExceptionMapper implements ExceptionMapper<Throwable> {
	private static final Logger _log = LoggerFactory.getLogger(KxExceptionMapper.class);

	@Override
	public Response toResponse(Throwable exception) {
		_log.error(exception.getMessage(), exception);
		
		if (exception instanceof WebApplicationException) {
			return ((WebApplicationException) exception).getResponse();
		}
		else {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).
						entity(new ErrorResult(exception.getMessage())).
						type(MediaType.APPLICATION_JSON).
						build();
		}
	}

}
