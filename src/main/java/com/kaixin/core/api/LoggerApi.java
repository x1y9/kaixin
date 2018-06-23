package com.kaixin.core.api;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.kaixin.core.app.KxTemplate;
import com.kaixin.core.util.GetterUtil;
import com.kaixin.core.util.JsonException;
import com.kaixin.core.util.JsonResult;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.*;

/**
 * logback的动态配置，和log4j稍有不同
 */

@Path("/api/logger")
@Produces(value = MediaType.APPLICATION_JSON)
public class LoggerApi {


    /*
	 * 获取所有已配置级别的logger
	 */
    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Map> list() throws Exception {

        Map<String,Map> loggers = new TreeMap<>();

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        for (Logger logger : context.getLoggerList()) {
            Map<String,String> one = new HashMap<>();
            Level level = logger.getLevel();
            one.put("name", logger.getName());
            one.put("level",level == null ? "" : level.toString());
            loggers.put(logger.getName(), one);

        }

        return loggers.values();
    }

    /*
     * 对list返回数据进行增量修改
     */
    @POST
    @Path("/save")
    @Consumes(MediaType.APPLICATION_JSON)
    public JsonResult save(Map<String,Object> req) throws JsonException {

        if (req == null || req.get("data") == null)
            throw new JsonException("parameter error: req should contains data");

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        for (Map<String,Object> item: (List<Map>)(req.get("data"))) {
            String name = GetterUtil.get(item.get("name"), null);
            String value = GetterUtil.get(item.get("level"), null);
            if (name == null)
                continue;


            Logger logger = context.getLogger(name);
            if (logger == null)
                continue;

            //允许level为空
            Level level = Level.toLevel(value, null);
            logger.setLevel(level);
        }
        return new JsonResult();
    }

    @GET
    @Path("/get")
    public String get(@QueryParam("class") String clazz) throws JsonException {
        if (clazz == null)
            throw new JsonException("parameter error: class should not be empty");

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logger = context.getLogger(clazz);
        if (logger == null)
            throw new JsonException("logger not found");

        if (logger.getLevel() == null)
            return "";
        else
            return logger.getLevel().toString();
    }

    /*
     * 设置log级别，levelStr如果为空，则取消此logger设置级别
     */
    @POST
    @Path("/set")
    @Consumes(MediaType.APPLICATION_JSON)
    public JsonResult set(Map<String,Object> req) throws JsonException {

        String clazz = GetterUtil.get(req.get("class"),null);
        String levelStr = GetterUtil.get(req.get("level"), null);

        if (req == null || clazz == null)
            throw new JsonException("parameter error: class should not be empty");

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logger = context.getLogger(clazz);

        //允许level为空
        Level level = Level.toLevel(levelStr, null);

        if (logger == null)
            throw new JsonException("logger not found");

        logger.setLevel(level);
        return new JsonResult();
    }

}
