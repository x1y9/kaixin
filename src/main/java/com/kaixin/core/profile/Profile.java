package com.kaixin.core.profile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Profile {
	
	private String name;
	private List<Model> models;
	private Map<String,Object> admin; /* 给webApp用，没有用pojo封装 */
	private Map<String,Model> modelsMap; /* 根据models自动生成一个map */

	
	public List<Model> getModels() {
		return models;
	}

	public String getName() {
		return name;
	}
	
	public Map<String,Object> getAdmin() {
		return admin;
	}
	
	synchronized public Map<String,Model> getModelsMap() {
		if (modelsMap == null) {
			modelsMap = new HashMap<String,Model>();
			for (Model model : models) {
				modelsMap.put(model.getName(), model);
			}
		}
		return modelsMap;
	}
	
	public Model getModel(String name) {
		if (name == null || name.length() == 0)
			return null;
		return getModelsMap().get(name);
	}
	
}
