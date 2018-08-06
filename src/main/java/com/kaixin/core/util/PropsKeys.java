package com.kaixin.core.util;

/**
 * Created by zhongshu on 2017/1/7.
 */
public class PropsKeys {

    /*
     * 认证相关配置
     */

    @PropsProperty(type = "boolean", defaultValue = "true", needRestart = false, help = "是否使用cookie传递认证信息")
    public static final String AUTH_USE_COOKIE = "auth.use.cookie";

    @PropsProperty(type = "string", defaultValue = "md5", needRestart = false, help = "密码算法")
    public static final String AUTH_PASSWORD_ALGORITHM = "auth.password.algorithm";

    @PropsProperty(type = "string", defaultValue = "fusion", needRestart = false, help = "密码使用MD5算法时的盐值")
    public static final String AUTH_PASSWORD_MD5_SALT = "auth.password.md5.salt";

    @PropsProperty(type = "string", defaultValue = "fusion", needRestart = false, help = "认证token算法用的盐值")
    public static final String AUTH_TOKEN_SALT = "auth.token.salt";

    @PropsProperty(type = "integer", defaultValue = "2", needRestart = false, help = "密码重置的超时时间(小时)")
    public static final String AUTH_PASSWORD_RESET_TIMEOUT = "auth.password.reset.timeout";

    @PropsProperty(type = "boolean", defaultValue = "true", needRestart = false, forClient= true, help = "是否支持用户注册")
    public static final String AUTH_SIGNUP_ENABLE = "auth.signup.enable";

    @PropsProperty(type = "string", defaultValue = "sys/.*,api/dbmng/.*,api/logger/.*,api/metric/.*,api/property/.*", needRestart = false, help = "只能admin访问的url，逗号分隔")
    public static final String AUTH_ONLY_ADMIN_URLS = "auth.only.admin.urls";

    @PropsProperty(type = "string", defaultValue = "", needRestart = false, help = "登录后才可访问的url，逗号分隔")
    public static final String AUTH_ONLY_LOGIN_URLS = "auth.only.login.urls";

    @PropsProperty(type = "boolean", defaultValue = "true", needRestart = false, help = "进入后台是否需要认证")
    public static final String AUTH_BACKEND_NEED_AUTH = "auth.backend.need.auth";

    /*
	 * Metric配置
	 */
    @PropsProperty(type = "boolean", defaultValue = "true", needRestart = false, help = "是否统计sql查询的性能")
    public static final String METRIC_JDBC_QUERY_ENABLE = "metric.jdbc.query.enable";

    @PropsProperty(type = "boolean", defaultValue = "true", needRestart = false, help = "是否统计http请求的性能")
    public static final String METRIC_REST_REQUEST_ENABLE = "metric.rest.request.enable";

    @PropsProperty(type = "boolean", defaultValue = "true", needRestart = true, help = "是否使用高精度计时做metric测量")
    public static final String METRIC_USING_NANOTIME_ENABLE = "metric.using.nanotime.enable";

    @PropsProperty(type = "integer", defaultValue = "1024", needRestart = true, help = "统计数据snapshot记录条目数")
    public static final String METRIC_SNAPSHOT_SIZE = "metric.snapshot.size";

    @PropsProperty(type = "string", defaultValue = "gbk", needRestart = false, help = "CSV导出时的编码")
    public static final String CSV_EXPORT_ENCODE = "csv.export.encode";


    /*
	 * 系统配置
	 */
    @PropsProperty(type = "string", defaultValue = "/view/", needRestart = false, help = "系统view的基路径")
    public static final String SYS_VIEW_PATH = "sys.view.path";

    @PropsProperty(type = "integer", defaultValue = "20", needRestart = false, help = "系统executor线程数量")
    public static final String SYS_EXECUTOR_THREADS_NUM = "sys.executor.threads.num";

    @PropsProperty(type = "string", defaultValue = "zh-CN", needRestart = false, help = "系统缺省locale")
    public static final String SYS_LOCALE = "sys.locale";

    @PropsProperty(type = "string", defaultValue = "zh-CN,en-US", needRestart = true, help = "系统所有locale")
    public static final String SYS_ALL_LOCALES = "sys.all.locales";

    @PropsProperty(type = "string", defaultValue = "", needRestart = false, forClient = true, help = "系统的外部访问url")
    public static final String SYS_SERVER_URL = "sys.server.url";

    @PropsProperty(type = "string", defaultValue = "", needRestart = false, help = "系统的多个静态path(url)，逗号分隔")
    public static final String SYS_ASSERT_PATH = "sys.assert.path";

    @PropsProperty(type = "string", defaultValue = "debug", needRestart = true, help = "系统运行模式, product|debug")
    public static final String SYS_ENV = "sys.env";

    @PropsProperty(type = "boolean", defaultValue = "false", needRestart = false, help = "是否使能CORS")
    public static final String DEBUG_CORS_ENABLE = "debug.cors.enable";

    @PropsProperty(type = "boolean", defaultValue = "true", needRestart = false, help = "是否支持注册")
    public static final String SIGNUP_ENABLE = "signup.enable";

    /*
     *  邮件配置
     */
    @PropsProperty(type = "string", defaultValue = "", needRestart = false, help = "SMTP登录账号名")
    public static final String SMTP_USERNAME = "smtp.username";

    @PropsProperty(type = "string", defaultValue = "", needRestart = false, help = "SMTP登录密码")
    public static final String SMTP_PASSWORD = "smtp.password";

    @PropsProperty(type = "string", defaultValue = "", needRestart = false, help = "SMTP服务器地址")
    public static final String SMTP_HOST = "smtp.host";

    @PropsProperty(type = "int", defaultValue = "465", needRestart = false, help = "SMTP服务器端口号")
    public static final String SMTP_PORT = "smtp.port";

    @PropsProperty(type = "boolean", defaultValue = "true", needRestart = false, help = "SMTP协议是否加密")
    public static final String SMTP_SSL = "smtp.ssl";

    /*
     * admin
     */
    @PropsProperty(type = "string", defaultValue = "", needRestart = false, help = "ADMIN修改通知, 目前只在新建时通知")
    public static final String ADMIN_NOTIFICATION_EMAILS = "admin.notification.emails";

    @PropsProperty(type = "string", defaultValue = "修改通知", needRestart = false, help = "ADMIN修改通知标题")
    public static final String ADMIN_NOTIFICATION_TITLE = "admin.notification.title";

}
