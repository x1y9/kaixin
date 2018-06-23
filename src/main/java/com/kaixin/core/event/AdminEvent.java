package com.kaixin.core.event;


public interface AdminEvent {
	public static String ACTION_UPDATE = "update";
	public static String ACTION_CREATE = "create";
	public static String ACTION_DELETE = "delete";
	
	public static String STAGE_BEFORE = "before";
	public static String STAGE_AFTER = "after";
	
	public void onAdminEvent(String model, String action, String stage, Object orgEntity, Object entity);
}
