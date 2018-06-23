package com.kaixin.core.api;

import com.kaixin.core.app.KxTemplate;
import com.kaixin.core.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhongshu on 2017/1/8.
 */
@Path("/api/property")
@Produces(value = MediaType.APPLICATION_JSON)
public class PropertyApi {

    private static final Logger _log = LoggerFactory.getLogger(PropertyApi.class);


    /*
	 * 列举所有的property
	 */
    @GET
    @Path("/list")
    public List<Map> list() throws JsonException {
        List<Map> results = new ArrayList<>();
        for (Field field: PropsKeys.class.getFields()) {
            PropsProperty annotation = field.getAnnotation(PropsProperty.class);
            if (annotation != null) {
                try {
                    String key = (String)field.get(null);
                    //过滤掉参数化的key
                    if (key != null && key.length() > 0 && key.indexOf("%s") == -1) {
                        Map<String,Object> property = new HashMap();
                        String value = PropsUtil.get(key);
                        property.put("name", key);
                        property.put("value", value);
                        property.put("isModified", !annotation.defaultValue().equals(value));

                        property.put("type", annotation.type());
                        property.put("defaultValue", annotation.defaultValue());
                        property.put("needRestart", annotation.needRestart());
                        property.put("help", annotation.help());

                        results.add(property);
                    }
                } catch (Exception e) {
                }
            }
        }

        return results;
    }

    /*
     * 对list返回数据进行增量修改，注意这里有些特殊处理，为了支持某些属性不重启，需要手动调用某些特殊接口
     */
    @POST
    @Path("/save")
    public JsonResult save(Map<String,Object> req) throws JsonException {

        if (req == null || req.get("data") == null)
            throw new JsonException("parameter error: req should contains data");

        for (Map<String,Object> item: (List<Map>)(req.get("data"))) {
            String name = GetterUtil.get(item.get("name"), null);
            String value = GetterUtil.get(item.get("value"), null);
            if (name == null || value == null)
                continue;

            PropsUtil.set(name, value, false);
        }
        PropsUtil.store();

        return new JsonResult();
    }

    @GET
    @Path("/get")
    public JsonResult get(@QueryParam("key") String key) throws JsonException {
        if (key == null)
            throw new JsonException("parameter error: key should not be empty");

        String value = PropsUtil.get(key);

        return new JsonResult(value);
    }


    /*
     * 设置property，save参数是可选的，表示是否需要存库
     */
    @POST
    @Path("/set")
    @Consumes(MediaType.APPLICATION_JSON)
    public JsonResult set(Map<String,Object> req) throws JsonException {

        String key = GetterUtil.get(req.get("key"), null);
        String value = GetterUtil.get(req.get("value"), null);
        boolean save = GetterUtil.getBoolean(req.get("save"), true);

        if (req == null || key == null || value == null)
            throw new JsonException("parameter error: key and value should not be empty");

        PropsUtil.set(key, value, save);
        return new JsonResult();
    }
}
