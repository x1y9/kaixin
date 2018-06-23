package com.kaixin.core.util;

import com.kaixin.core.app.KxConsts;
import org.apache.commons.io.FilenameUtils;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/* 获取文件名对应的MIME 
 * 这个不简单，Java7的probeContentType严重依赖OS，所以经常会不准确
 * 其他的各种库都不太靠谱，还是手动+OS双重识别吧
 */

public class MimeUtil {

    private static final Map<String, String> fileExtensionMap;

    static {
        fileExtensionMap = new HashMap<String, String>();
        // MS Office
        fileExtensionMap.put("doc", "application/msword");
        fileExtensionMap.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        fileExtensionMap.put("xls", "application/vnd.ms-excel");
        fileExtensionMap.put("xlt", "application/vnd.ms-excel");
        fileExtensionMap.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        fileExtensionMap.put("ppt", "application/vnd.ms-powerpoint");
        fileExtensionMap.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        fileExtensionMap.put("swf", "application/x-shockwave-flash");
        fileExtensionMap.put("pdf", "application/pdf");
        
    }

    public static String getMimeTypeByFileName(String fileName, boolean useOS) {
    	String type = null;

    	String extension = FilenameUtils.getExtension(fileName);
        type = fileExtensionMap.get(extension);
        
        if (useOS && (type == null || type.length() == 0)) {
	    	try { type = Files.probeContentType(Paths.get(fileName));} catch (Exception e) {}
        }
        
        return type == null ? KxConsts.MIME_UNKNOWN : type;
    }
}
