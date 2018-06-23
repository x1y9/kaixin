package com.kaixin.core.filter;

import com.kaixin.core.util.PropsKeys;
import com.kaixin.core.util.PropsUtil;

import java.io.IOException;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Response;



/*
 * 简单打开CORS
 */
@Priority(1)
public class CORSFilter implements ContainerRequestFilter, ContainerResponseFilter  {

	@Override
	public void filter(ContainerRequestContext request) throws IOException {
		
    	if (PropsUtil.getBoolean(PropsKeys.DEBUG_CORS_ENABLE) && request.getMethod().equalsIgnoreCase("OPTIONS")) {
    		Response.ResponseBuilder builder = Response.ok();
	        builder.header("Access-Control-Allow-Origin", "*");
	        //更好的写法这里应该property驱动
	        builder.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization, fusion-token");
	        builder.header("Access-Control-Allow-Credentials", "true");
	        builder.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
	        request.abortWith(builder.build());
    	}
	}
	
    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) throws IOException {
    
    	if (PropsUtil.getBoolean(PropsKeys.DEBUG_CORS_ENABLE) && !request.getMethod().equalsIgnoreCase("OPTIONS")) {
    		
    		response.getHeaders().add("Access-Control-Allow-Origin", "*");
    		response.getHeaders().add("Access-Control-Allow-Headers", "origin, content-type, accept, authorization, sdCountry, sdToken");
    		response.getHeaders().add("Access-Control-Allow-Credentials", "true");
    		response.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
    	}
        
    }


}
