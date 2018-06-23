package com.kaixin.core.util;

import org.apache.commons.lang3.time.DateUtils;

import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

	public static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss";
	public static final String DATE_FORMAT_PATTERN_JS = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
	public static final String DATE_FORMAT_PATTERN_MS1 = "yyyy-MM-dd HH:mm:ss.S";
	public static final String DATE_FORMAT_PATTERN_MS3 = "yyyy-MM-dd HH:mm:ss.SSS";
	public static final String DATE_FORMAT_PATTERN_DOJO = "yyyy-MM-dd'T'HH:mm:ss.SSSX";
	public static final String DATE_FORMAT_PATTERN_DAY = "yyyy-MM-dd";
	public static final String DATE_FORMAT_PATTERN_HOUR = "yyyy-MM-dd HH";
	public static final String DATE_FORMAT_PATTERN_MIN = "yyyy-MM-dd HH:mm";
	public static final String DATE_FORMAT_PATTERN_MONTH1 = "yyyy-MM";
	public static final String DATE_FORMAT_PATTERN_MONTH2 = "yyyy.MM";
	
	public static String toHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
	
	public static String fromHex(String hex) throws UnsupportedEncodingException {
		return fromHex(hex, "UTF-8");
	}
	
	public static String fromHex(String hex, String encode) throws UnsupportedEncodingException {
		byte[] bytes = new byte[hex.length() / 2];
	    for (int i = 0; i < hex.length(); i+=2) {
	    	bytes[i / 2] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
	    }
	    return new String(bytes, encode);
	}
	
	public static String repeat(String s, String separator, int count) {
		if (count <=0)
			return "";
		
		StringBuilder sb = new StringBuilder((s.length() + separator.length()) * count);
		while (--count > 0) {
			sb.append(s).append(separator);
		}
		return sb.append(s).toString();
	}
	
	public static String repeat(Collection<String> strs, String separator) {
		int count = strs.size();
		if (count <=0)
			return "";
		
		StringBuilder sb = new StringBuilder();
		
		boolean first = true;
		for (String str : strs) {
			if (!first)
				sb.append(separator);
			sb.append(str);
		}
		
		return sb.toString();
	}

	public static String toSlug(String input) {
		String nowhitespace = Pattern.compile("[\\s]").matcher(input).replaceAll("-");
		String normalized = Normalizer.normalize(nowhitespace, Form.NFD);
		String slug = Pattern.compile("[^\\w-]").matcher(normalized).replaceAll("");
		return slug.toLowerCase(Locale.ENGLISH);
	}


	public static Date parseDate(String dateString) {
		String[] formatStrings = {"M/y", "M/d/y", "M-d-y"};
		try {
			return DateUtils.parseDate(dateString, formatStrings);
		} catch (Exception e) {
			return null;
		}
	}

	public static Date parseDateTime(String dateTime) {
		try {
			return DateUtils.parseDate(dateTime, new String[]{DATE_FORMAT_PATTERN, DATE_FORMAT_PATTERN_JS, DATE_FORMAT_PATTERN_MS1,
					DATE_FORMAT_PATTERN_MS3, DATE_FORMAT_PATTERN_DAY, DATE_FORMAT_PATTERN_HOUR,
					DATE_FORMAT_PATTERN_MIN,DATE_FORMAT_PATTERN_MONTH1,DATE_FORMAT_PATTERN_MONTH2,DATE_FORMAT_PATTERN_DOJO});
		} catch (Exception e) {
			return null;
		}
	}

    public static String url2Anchor(String url) {
    	return url2Anchor(url, url);
    }

	public static String url2Anchor(String url, String title) {
		return "<a href=\"" + url +"\">" + title + "</a>";
	}

	public static boolean matchPatterns(String src, String patterns) {
		if (patterns == null || patterns.length() == 0)
			return false;

		return matchPatterns(src, patterns.split("[,;]"));
	}

	public static boolean matchPatterns(String src, String[] patterns) {
		if(patterns != null && patterns.length > 0)
		{
			for(String pattern : patterns)
			{
				Matcher matcher = Pattern.compile(pattern).matcher(src);
				if(matcher.matches())
				{
					return true;
				}
			}
		}
		return false;
	}

}
