package com.kaixin.core.util;

import org.apache.commons.io.FilenameUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class FileUtil {
	
	private static Map<String,String> ext2icon = new HashMap<String,String>() {{
		put("pdf","fa-file-pdf-o");
		put("doc","fa-file-word-o");
		put("docx","fa-file-word-o");
		put("xls","fa-file-excel-o");
		put("xlsx","fa-file-excel-o");
		put("ppt","fa-file-powerpoint-o");
		put("pptx","fa-file-powerpoint-o");
		put("png","fa-file-image-o");
		put("jpg","fa-file-image-o");
		put("zip","fa-file-zip-o");
		put("rar","fa-file-zip-o");
	}};

	public static Object getFileFaIcon(File sub) {
		
		if (sub.isDirectory())
			return "fa-folder-o";
		else {
			String extension = FilenameUtils.getExtension(sub.getName()).toLowerCase();
			if (extension != null && ext2icon.containsKey(extension))
				return ext2icon.get(extension);
			else
				return "fa-file-o";
		}
	}
	
	public static File[] listFiles(File dir) {
		File[] subs = dir.listFiles();
		if (subs != null) {
			Arrays.sort(subs, new Comparator<File>() {
				@Override
				public int compare(File o1, File o2) {
					if (o1.isFile() && o2.isDirectory())
						return 1;
					else if (o2.isFile() && o1.isDirectory())
						return -1;
					else
						return o1.getName().compareTo(o2.getName());
				}
			});
		}
		return subs;
	}

	public static String getRelativePath(File sub, String rootPath) {
		try {
			return sub.getCanonicalPath().substring(rootPath.length() + 1);
		} catch (Exception e) {
			return "";
		}
	}

	public static String getExtensionWithDot(String name) {
		String extension = "";

		int i = name.lastIndexOf('.');
		int p = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
		if (i > p) {
			extension = name.substring(i);
		}
		return extension;
	}

	public static String getContentDisposition(HttpServletRequest request, String path, String name) {
		String pathName = new File(path).getName();
		return getContentDisposition(request, name == null ? pathName : name);
	}

	public static String getContentDisposition(HttpServletRequest request, String pathName) {
		String userAgent = request.getHeader("User-Agent").toLowerCase();

		String uriName = "file";
		try {
			uriName = new URI(null, null, pathName, null).toASCIIString();
		} catch (URISyntaxException e) {
		}

		if (userAgent.contains("msie")) {
			return "attachment; filename=" + uriName;
		}
		else {
			return "attachment; filename*=UTF-8''" + uriName;
		}
	}
}
