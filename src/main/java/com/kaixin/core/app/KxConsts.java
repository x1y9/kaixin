package com.kaixin.core.app;

/*
 * 一些常量，代码需要用，数据库的表和字段必须和这个一致
 */
public class KxConsts {
	public static final String ID ="id";
	public static final String NAME ="name";
	
	public static final String CONFIG_URL_PATTERN = "__urlPattern";
	
	public static final String AUTH_TOKEN_NAME = "fusion-token";

	public static final String TBL_USER = "user";	
	public static final String TBL_PERMISSION = "permission";
	public static final String TBL_SCOPE = "scope";
	public static final String TBL_FORM_CONFIG = "form_config";
	public static final String TBL_FORM_ENTITY = "form_entity";
	
	public static final String COL_USER_NAME = "name";
	public static final String COL_USER_ACCOUNT = "account";
	public static final String COL_USER_PASSWORD = "password";
	public static final Object COL_USER_IS_ADMIN = "isadmin";
	public static final String COL_USER_DISABLE = "disable";
	public static final String COL_USER_RESET_KEY = "resetKey";
	public static final String COL_USER_RESET_TIME = "resetTime";
	
	public static final String COL_PERMISSION_MODEL = "model";
	public static final String COL_PERMISSION_ACTION = "action";
	public static final String COL_PERMISSION_FIELDS = "fields";
	public static final String COL_PERMISSION_USERS = "users";
	public static final String COL_PERMISSION_MEMO = "memo";

	public static final String COL_SCOPE_MODEL = "model";
	public static final String COL_SCOPE_USERS = "users";
	public static final String COL_SCOPE_FILTER = "filter";
	
	public static final String COL_FORM_NAME = "name";
	public static final String COL_FORM_CONTENT = "content";
	public static final String COL_FORM_RESPONSE = "response";
	public static final String COL_FORM_NOTIFIER = "notifier";
	
	public static final String VIEW_DASHBOARD = "dashboard";
	public static final String VIEW_LIST = "list";
	public static final String VIEW_SHOW = "show";
	public static final String VIEW_CREATE = "create";
	public static final String VIEW_EDIT = "edit";
	public static final String VIEW_DELETE = "delete";
	public static final String VIEW_ALL = "*";
	
	public static final String MODEL_FILE =  "file"; /* 特殊的模型，用于文件目录权限 */
	public static final String MODEL_ALL =  "*";
			
	public static final String PATTERN_NOW = "${now}";
	public static final String PATTERN_LOGINUSER = "${loginUser}";
	public static final String PATTERN_LOGINUSER_ID = "${loginUserId}";

	public static final String CHOICE_PERMISSION_MODELS = "permissionModels";
	public static final String CHOICE_LABEL = "label";
	public static final String CHOICE_VALUE = "value";
	public static final String CHOICE_SEP = ",";

	public static final String REQ_CONTEXT_LOGIN_USER = "loginUser";

	public static final String TEMPLATE_VAR_URL_PARA = "urlPara";
	public static final String TEMPLATE_VAR_MESSAGE = "message";

	public static final String PASSWORD_ALGORITHM_MD5 = "md5";   //普通md5(加salt)
	public static final String PASSWORD_ALGORITHM_APR1 = "apr1"; //htpasswd文件用的加密算法

	public static final String MIME_UNKNOWN = "application/octet-stream";
	public static final String ENV_MODE_PRODUCT = "product";
	public static final String ENV_MODE_DEVELOP = "develop";

	public static final String ALL_LANGS[] = new String[] {"zh-CN", "en-US"};

	public static final String ENCODE_GBK = "gbk";
	public static final String ENCODE_UTF8 = "utf-8";

    public static final String FILE_APP_PROPERTIES = "app.properties";
	public static final String RESOURCE_APP_PROPERTIES = "app.properties";

	

	//ThreadLocal变量
	public static String TL_TRANSACTION_HANDLE = "transactionHandle";
	public static String TL_LOGIN_USER = "loginUser";
	public static String TL_LOGIN_USER_ID = "loginUserId";
	public static String TL_LOGIN_USER_NAME = "loginUserName";
	public static String TL_DBMNG_LAST_QUERY_META = "lastQueryMeta";

	

}
