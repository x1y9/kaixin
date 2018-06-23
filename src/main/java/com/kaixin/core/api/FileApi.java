package com.kaixin.core.api;

import com.kaixin.core.app.KxConsts;
import com.kaixin.core.app.KxTemplate;
import com.kaixin.core.db.DbHandle;
import com.kaixin.core.db.Transactional;
import com.kaixin.core.util.*;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.File;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.file.FileSystems;
import java.text.SimpleDateFormat;
import java.util.*;


@Path("/api/file")
public class FileApi {

	private static final String FILE_SERVER_PATH = "data/";
	private static final String UPLOAD_PATH = "upload/";
	
	@Context private ContainerRequestContext reqContext;
	@Context private UriInfo uriInfo;
	
	/*
	 * upload接口扫描所有上传的附件，不需要约定form key
	 * 
	 */
	@POST
	@Path("upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public List<Map> uploadFile( FormDataMultiPart multipart) throws Exception{
		
		List<Map> files = new ArrayList<Map>();
		
		Map<String, List<FormDataBodyPart>> map = multipart.getFields();
		for (Map.Entry<String, List<FormDataBodyPart>> entry : map.entrySet()) {
	        for (FormDataBodyPart part : entry.getValue()) {
	        	try(InputStream uploadedInputStream = part.getValueAs(InputStream.class)) {
					FormDataContentDisposition fileDetail = part.getFormDataContentDisposition();
					String shortName = fileDetail.getFileName();
					if (GetterUtil.isEmpty(shortName))
						continue;

					//IE会发送全路径，经过jersey会变成错误的c:tempfile1.name,需要jersey解决，暂时规避
					if (shortName.indexOf(":") != -1)
						shortName = shortName.substring(shortName.indexOf(":") + 1);
					String fileName = new String(shortName.getBytes("ISO8859-1"), "UTF-8");

					String path = getUploadPath(fileName);
					java.nio.file.Path outputPath = FileSystems.getDefault().getPath(FILE_SERVER_PATH, path);
					java.nio.file.Files.copy(uploadedInputStream, outputPath);

					Map<String,String> file = new HashMap<String,String>();
					file.put("path", path);
					file.put("name", fileName);
					files.add(file);
				}
	        }
	    }
        return files;        
	}
	

	@GET
	@Path("download")
	public Response download( @Context HttpServletRequest request, @QueryParam("path") String path, @QueryParam("name") String name) throws Exception{
		File file = new File(FILE_SERVER_PATH + path);
		Response response = null;
		if (file.exists()) {
	      response = Response.ok(file)
	    		  			 .header("Content-Disposition", FileUtil.getContentDisposition(request, path, name))
	    		  			 .build();
	    } 
		else {
	      response = Response.status(404).entity("FILE NOT FOUND: " + path).type("text/plain").build();
	    }
	      
	    return response;
	}

	@GET
	@Path("get")
	public Response get( @Context HttpServletRequest request, @QueryParam("path") String path, @QueryParam("name") String name) throws Exception{
		File file = new File(FILE_SERVER_PATH + path);
		
		Response response = null;
		if (file.exists()) {
		  String mime = MimeUtil.getMimeTypeByFileName(name == null ? path : name,true);
          //不需要.header("Content-Disposition", "inline " + FileUtil.getContentDisposition(request, path, name))也可以在三种浏览器预览
	      response = Response.ok(file, mime)
	    		  			 .build();
	    } 
		else {
	      response = Response.status(404).entity("FILE NOT FOUND: " + path).type("text/plain").build();
	    }
	      
	    return response;
	}

	/*
	 * list返回文档列表  ??? 需要修改
	 */
	@GET
	@Path("list")
	@Transactional
	public KxTemplate list( @QueryParam("path") String path, @QueryParam("all") boolean showAll) throws Exception{
		if (path == null)
			path = "";

		PermissionUtil.checkPermission(DbHandle.transactionInstance(),
				(Map<String, Object>)ThreadLocalUtil.get(KxConsts.TL_LOGIN_USER), KxConsts.MODEL_FILE, KxConsts.VIEW_LIST, path);
		Map<String,Object> m = new HashMap<String,Object>();
		new File(FILE_SERVER_PATH).mkdirs();
		File dir = new File(FILE_SERVER_PATH + path);
		String rootPath = new File(FILE_SERVER_PATH).getCanonicalPath();
		
		if (dir.isDirectory() && dir.getCanonicalPath().startsWith(rootPath)) {
			listDir(m,dir,rootPath,showAll);
	    } 

	    return new KxTemplate("file-list.ftl", reqContext, uriInfo, m);
	}
	
	


	/*
	 * 
	 * 
	 * 私有函数
	 * 
	 * 
	 */
	
	private void listDir(Map<String, Object> model, File dir, String rootPath, boolean showAll ) throws Exception {
		ArrayList<Map> breads = new ArrayList<Map>();
		File parent = dir;
		while(parent != null && !parent.getCanonicalPath().equals(rootPath)) {
			Map<String,Object> bread = new HashMap<String,Object>();
			bread.put("name", parent.getName());
			bread.put("url", "/api/file/list?path=" + URLEncoder.encode(FileUtil.getRelativePath(parent,rootPath), "UTF-8") + "\"");
			breads.add(0, bread);
			parent=parent.getParentFile();
		}
		
		List<Map> files = new ArrayList<Map>();
		File[] subs = FileUtil.listFiles(dir);
		for (File sub : subs) {
			if (!showAll && isIgnoreFile(sub)) 
				continue;
			
			Map<String,Object> item = new HashMap<String,Object>();
			if (sub.isDirectory()) {
				item.put("name", sub.getName());
				item.put("url", "/api/file/list?path=" + URLEncoder.encode(FileUtil.getRelativePath(sub,rootPath), "UTF-8") + "\"");
				item.put("icon", FileUtil.getFileFaIcon(sub));
			}
			else {
				item.put("name", sub.getName());
				item.put("url", "/api/file/download?path=" + URLEncoder.encode(FileUtil.getRelativePath(sub,rootPath), "UTF-8") + "\"");
				item.put("size", sub.length());
				item.put("icon", FileUtil.getFileFaIcon(sub));
			}
			files.add(item);
		}
		model.put("files", files);		
		model.put("breads", breads);
	}
	
	private boolean isIgnoreFile(File sub) {
		if (sub.isHidden() || "Thumbs.db".equalsIgnoreCase(sub.getName()))
			return true;
		else
			return false;
	}
	


	private String getUploadPath(String name) {
		String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
		String level1 = timestamp.substring(0,4);
		String level2 = timestamp.substring(0,8);

		String rename = UUID.randomUUID().toString().replace("-","") + FileUtil.getExtensionWithDot(name);
		File candiFile = new File(FILE_SERVER_PATH + UPLOAD_PATH, level1 + "/" + level2 + "/" + rename);

		if (!candiFile.getParentFile().exists())
			candiFile.getParentFile().mkdirs();

		return UPLOAD_PATH + level1 + "/" + level2 + "/" + rename;

	}
	

	
}