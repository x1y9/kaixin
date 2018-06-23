package com.kaixin.core.profile;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.kaixin.core.app.KxApp;
import com.kaixin.core.app.KxConsts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Field {
	//public static final String TYPE_REFERENCE_MANY ="ReferenceMany";
	public static final String TYPE_REFERENCE ="Reference";
	public static final String TYPE_STRING ="String";
	public static final String TYPE_PASSWORD ="Password";
	public static final String TYPE_TEXT ="Text";
	public static final String TYPE_LONG ="Long";
	public static final String TYPE_RICH ="RichText";
	public static final String TYPE_CHOICE ="Choice";
	public static final String TYPE_BOOLEAN = "Boolean";
	public static final String TYPE_FILE ="File";
	public static final String TYPE_DATE ="Date"; 
	public static final String TYPE_DATETIME ="DateTime";


	private String name;
	private String type;
	private String label;
	private String target;
	private String targetField;
	private String defaultValue;
	private boolean required;
	private boolean multiple;
	private Object attr;
	private Object choices;

	public String getName() {
		//为了确保兼容，都小写，客户端如果手工上传field，应该先把field小写
		return name.toLowerCase();
	}

	public String getType() {
		return type;
	}

	public String getLabel() {
		return label;
	}

	public boolean isRequired() {
		return required;
	}

	public boolean isMultiple() { return multiple; }

	public String getTarget() {
		if (target == null)
			return name;
		else
			return target;
	}

	public String getTargetField() {
		if (targetField != null)
			return targetField;
		else
			return "name";
	}

	@JsonProperty("default")
	public String getDefault() {
		return defaultValue;
	}

	@JsonProperty("default")
	public void setDefault(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	public Object getChoices() {
		return choices;
	}
	
	@JsonIgnore
	public List<Map> getConvertedChoices() {
		if (choices instanceof String && KxConsts.CHOICE_PERMISSION_MODELS.equals(choices)) {
			List<Map> results = new ArrayList<Map>();
			List<Model> models =  KxApp.profile.getModels();
			for (Model model : models) {
				Map<String,String> pm = new HashMap<String,String>();
				pm.put(KxConsts.CHOICE_LABEL, model.getLabel());
				pm.put(KxConsts.CHOICE_VALUE, model.getName());
				results.add(pm);
			}
			return results;
		}
		else if (choices instanceof List)
			return (List<Map>)choices;
		else
			return null;
	}
	
	@JsonIgnore
	public String getConvertedChoiceJson() {
		try {
			return KxApp.mapper.writeValueAsString(getConvertedChoices());
		} catch (JsonProcessingException e) {
			return "[]";
		} 
	}	
	
	public Object getAttr() {
		return attr;
	}

	@JsonIgnore
	public String getAttrJson() {
		try {
			return KxApp.mapper.writeValueAsString(attr);
		} catch (JsonProcessingException e) {
			return "{}";
		}
	}
	
	@JsonIgnore
	public boolean isReference() {
		return TYPE_REFERENCE.equals(type);
	}

	@JsonIgnore
	public boolean isReferenceMany() {
		return isReference() && isMultiple();
	}

	@JsonIgnore
	public boolean isReferenceSingle() {
		return isReference() && !isMultiple();
	}
	
	@JsonIgnore
	public boolean isPassword() {
		return TYPE_PASSWORD.equals(type);
	}

  	@JsonIgnore
	public boolean isChoice() {
		return TYPE_CHOICE.equals(type);
	}

  	@JsonIgnore
	public boolean isChoiceSingle() {
		return isChoice() && !isMultiple();
	}

  	@JsonIgnore
	public boolean isChoiceMultiple() {
		return isChoice() && isMultiple();
	}

	@JsonIgnore
	public boolean isDate() { return TYPE_DATE.equals(type) || TYPE_DATETIME.equals(type); 	}

	@JsonIgnore
	public boolean isLikeSearch() {
		if (isReference())
			return KxApp.profile.getModel(getTarget()).getField(getTargetField()).isLikeSearch();

		return TYPE_STRING.equalsIgnoreCase(type) || TYPE_TEXT.equalsIgnoreCase(type)
			|| TYPE_RICH.equalsIgnoreCase(type) || TYPE_FILE.equalsIgnoreCase(type)
			|| TYPE_DATE.equalsIgnoreCase(type) || TYPE_DATETIME.equalsIgnoreCase(type)
			|| TYPE_PASSWORD.equalsIgnoreCase(type);
	}

	public boolean isRemoveInView(String view) {
		if ((KxConsts.VIEW_CREATE.equals(view) || KxConsts.VIEW_EDIT.equals(view)) 
				&& KxConsts.ID.equalsIgnoreCase(name))
			return true;
		
		if ((KxConsts.VIEW_LIST.equals(view) || KxConsts.VIEW_DASHBOARD.equals(view)) 
				&& ( TYPE_PASSWORD.equalsIgnoreCase(type)))
			return true;

		return false;
	}
	
	public boolean isReadOnlyInView(String view) {
		return false;
	}

	public Map toJsField() {
		Map<String,Object> result = new HashMap<String,Object>();
		result.put("name",getName());
		result.put("label",getLabel());
		result.put("type",getType());
		result.put("choices", getConvertedChoices());
		result.put("default",getDefault());
		result.put("required",isRequired());
		result.put("multiple",isMultiple());
		if (isReference()) {
			result.put("target",getTarget());
			result.put("targetField", getTargetField());
		}
		return result;
	}



}
