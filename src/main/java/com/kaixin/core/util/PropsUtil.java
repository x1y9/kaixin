package com.kaixin.core.util;

import com.kaixin.core.app.KxConsts;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by zhongshu on 2017/1/7.
 */
public class PropsUtil {
    //不能通过get接口返回null判断是否有设置，因为get总是返回Default，只在没有default才返回null
    public static String getWithArg(String key, Object... args) {
        return GetterUtil.get(_get(String.format(key, args)), getDefault(key));
    }

    public static String get(String key) {
        return GetterUtil.get(_get(key), getDefault(key));
    }

    //下面这些接口不可能返回空了
    public static String getStringWithArg(String key, Object... args) {
        return GetterUtil.get(_get(String.format(key, args)), getDefaultString(key));
    }

    public static String getString(String key) {
        return GetterUtil.get(_get(key), getDefaultString(key));
    }
    public static boolean getBooleanWithArg(String key, Object... args) {
        return GetterUtil.getBoolean(_get(String.format(key, args)), getDefaultBoolean(key));
    }

    public static boolean getBoolean(String key) {
        return GetterUtil.getBoolean(_get(key), getDefaultBoolean(key));
    }

    public static int getIntegerWithArg(String key, Object... args) {
        return GetterUtil.getInteger(_get(String.format(key, args)), getDefaultInteger(key));
    }

    public static int getInteger(String key) {
        return GetterUtil.getInteger(_get(key), getDefaultInteger(key));
    }

    public static long getLongWithArg(String key, Object... args) {
        return GetterUtil.getLong(_get(String.format(key, args)), getDefaultLong(key));
    }

    public static long getLong(String key) {
        return GetterUtil.getLong(_get(key), getDefaultLong(key));
    }

    public static double getDoubleWithArg(String key, Object... args) {
        return GetterUtil.getDouble(_get(String.format(key, args)), getDefaultDouble(key));
    }

    public static double getDouble(String key) {
        return GetterUtil.getDouble(_get(key), getDefaultDouble(key));
    }

    public static String[] getArray(String key) {
        return getArray(key, ",");
    }

    public static String[] getArray(String key, String splitter) {
        return getString(key).split(splitter);
    }

    public static String[] getArrayWithArg(String key, String splitter, Object... args) {
        return getStringWithArg(key,args).split(splitter);
    }

//    public static Properties getProperties() {
//        Properties results = new Properties();
//        results.putAll(_orgProperties);
//        results.putAll(_setProperties);
//        return results;
//    }

    public static Map<String,String> getPropertiesMap(boolean onlyClient) {
        Map<String,String> map = new HashMap<>();
        for (String key : _propsMeta.keySet()) {
            if (!onlyClient || _propsMeta.get(key).forClient()) {
                map.put(key,  _propsMeta.get(key).defaultValue());
            }
        }
        MapUtil.mergePropertiesIntoMap(_orgProperties, map, false);
        MapUtil.mergePropertiesIntoMap(_setProperties, map, false);
        return map;
    }

    private static String _get(String key) {
        String value = _setProperties.getProperty(key);
        return value != null ? value : _orgProperties.getProperty(key);
    }


    //返回是否配置了某个property
    public static boolean contains(String var) {
        return _orgProperties.contains(var) || _setProperties.contains(var);
    }

    public static boolean contains(String var, Object... args) {
        String key = String.format(var, args);
        return _orgProperties.contains(key) || _setProperties.contains(key);
    }

    public static String getDefault(String key) {
        PropsProperty meta = _propsMeta.get(key);
        if (meta == null)
            return null;
        else
            return meta.defaultValue();
    }

    public static boolean getDefaultBoolean(String key) {
        return GetterUtil.getBoolean(getDefault(key),GetterUtil.DEFAULT_BOOLEAN);
    }

    public static int getDefaultInteger(String key) {
        return GetterUtil.getInteger(getDefault(key),GetterUtil.DEFAULT_INTEGER);
    }

    public static long getDefaultLong(String key) {
        return GetterUtil.getLong(getDefault(key),GetterUtil.DEFAULT_LONG);
    }

    public static String getDefaultString(String key) {
        return GetterUtil.get(getDefault(key),GetterUtil.DEFAULT_STRING);
    }

    public static double getDefaultDouble(String key) {
        return GetterUtil.getDouble(getDefault(key),GetterUtil.DEFAULT_DOUBLE);
    }

    /*
     * 这里value用null表示删除用户的设置，恢复缺省
     */
    public static String set(String key, String value) {
        return set(key, value, true);
    }

    public static String set(String key, String value, boolean save) {
        if (value == null)
            _setProperties.remove(key);
        else if (!value.equals(get(key)))
            _setProperties.setProperty(key, value);

        if (save)
            store();

        return get(key);
    }

    public static void store() {
        try {
            //USER_PROPERTIES有可能事先不存在，所以不能直接用getClassPathFile
            OutputStream os = new FileOutputStream(KxConsts.FILE_APP_PROPERTIES);
            _setProperties.store(os, "");
            IOUtils.closeQuietly(os);
        }catch(Exception e) {
            _log.error("save property error", e);
        }
    }

    protected PropsUtil() {
        try{

            //加载所有属性的注解，包括缺省值，是否需要重启等
            for (Field field: PropsKeys.class.getFields()) {
                PropsProperty annotation = field.getAnnotation(PropsProperty.class);
                if (annotation != null) {
                    String value = (String)field.get(null);
                    if (value != null) {
                        _propsMeta.put(value, annotation);
                    }
                }
            }

		     /*
		      * 先加载系统，在根据环境加载环境相关定义，再加载用户
		      */
            InputStream sysProp = this.getClass().getClassLoader().getResourceAsStream(KxConsts.RESOURCE_APP_PROPERTIES);
            if (sysProp != null) {
                _orgProperties.load(sysProp);
                sysProp.close();
            }

            if (new File(KxConsts.FILE_APP_PROPERTIES).exists()) {
                InputStream userProp = new FileInputStream(KxConsts.FILE_APP_PROPERTIES);
                if (userProp != null) {
                    _setProperties.load(userProp);
                    userProp.close();
                }
            }

        }catch(Exception e){
            _log.error("can not load properties file", e);
        }
    }

    private static Logger _log = LoggerFactory.getLogger(PropsUtil.class);
    private static Properties _orgProperties = new Properties();
    private static Properties _setProperties = new Properties();
    private static Map<String,PropsProperty> _propsMeta = new HashMap<>();

    private static PropsUtil _instance = new PropsUtil();
}
