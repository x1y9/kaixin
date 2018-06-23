package com.kaixin.core.filter;

import com.kaixin.core.app.KxApp;
import com.kaixin.core.util.GetterUtil;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/*
 * Fusion的主servlet filter，注意静态文件也会进这个filter
 * 
 *  支持http到https转发,不需要配置web.xml,配置app.properties文件即可 
 *  如果需要按url转发，可以考虑增加一个正则表达式的配置
 */
public class HttpsFilter implements Filter {

	@Override
	public void init(FilterConfig arg0) throws ServletException {
	}

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
			throws IOException, ServletException {
		
		StringBuffer uri = ((HttpServletRequest) request).getRequestURL();
	    if (uri.toString().startsWith("http://")) {
	    	String uri2 = uri.substring("http://".length());
	    	String uriServer = uri2.substring(0, uri2.indexOf("/") == -1 ? uri2.length() : uri2.indexOf("/"));
	    	String uriPath = uri2.indexOf("/") == -1 ? "/" : uri2.substring(uri2.indexOf("/"));
	    	String uriDomain = uriServer.substring(0, uriServer.indexOf(":") == -1 ? uriServer.length() : uriServer.indexOf(":"));
	    	
	    	/* 使用配置的转发端口而不是使用配置的https端口，因为如果有反向proxy，转发端口可能不是https端口 */
	    	String httpsPort = GetterUtil.get(KxApp.configuration.filter.get(HttpsFilter.class.getName()).get("httpsPort"), "443");
	    	String location = "https://" + uriDomain + ":" + httpsPort + uriPath;
	    	if ("443".equals(httpsPort))
	    		location = "https://" + uriDomain + uriPath;
	        ((HttpServletResponse) response).sendRedirect(location);
	    } 
	    else {
        	chain.doFilter(request, response);
	    }
	}

}
