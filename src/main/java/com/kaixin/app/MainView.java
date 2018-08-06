package com.kaixin.app;

import com.kaixin.core.app.KxTemplate;
import com.kaixin.core.util.PropsKeys;
import com.kaixin.core.util.PropsUtil;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriInfo;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * 这个view是按request-scope方式注册到jersey，这样的话class variable可以注入，
 * 大大减少每个方法的参数，缺点是没有办法使用带参数的构造函数，不能在初始化时注入一些参数。
 */

@Path("/")
@Produces(MediaType.TEXT_HTML)
public class MainView {
	
	@Context private ContainerRequestContext reqContext;
	@Context private UriInfo uriInfo;


	/*  如果想自动跳转，可以使能下面的view

	@GET
	public Response home() throws URISyntaxException {
		return Response.seeOther(new URI("/admin/")).build();
	}
	*/

	/*
	 * default 处理，根据url
	 *  看是否resource定义的页面，如果是直接返回resource页面
	 *  否则返回404页面（比返回404 code友好一些)
	 */
	
	@GET
	@Path("/{seg:.*}")
    public KxTemplate defaultAction(@PathParam("seg") List<PathSegment> segments) throws Exception
    {
		Map<String,Object> m = new HashMap<String,Object>();
		
		String path = StringUtils.join(segments, "/").toLowerCase();

		if (path.length() == 0 || path.endsWith("/"))
			path += "index";

		if (!path.endsWith(".ftl")) 
			path += ".ftl";
		
		URL resource = MainApplication.class.getResource(PropsUtil.get(PropsKeys.SYS_VIEW_PATH) + path);
		if (resource == null) //throw new WebApplicationException(404);
			path = "404.ftl";
		
		return new KxTemplate(path, reqContext, uriInfo, m);
    }
}
