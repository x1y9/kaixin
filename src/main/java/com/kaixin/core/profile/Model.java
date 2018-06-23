package com.kaixin.core.profile;

import com.kaixin.core.app.KxConsts;

import java.util.*;

public class Model {
	private String name;
    private String label;
    private String icon;
	private List<Field> fields = new ArrayList<>();
	private Map<String,Field> fieldsMap;
	
	public String getName() {
		return name;
	}

	public List<Field> getFields() {
		return fields;
	}
	
    public String getLabel() {
        return label;
    }

    public String getIcon() {
        return icon;
    }
    
    synchronized public Map<String,Field> getFieldsMap() {
		if (fieldsMap == null) {
			fieldsMap = new LinkedHashMap<String,Field>();
			for (Field field : fields) {
				fieldsMap.put(field.getName(), field);
			}
		}
		return fieldsMap;
	}
	
   
	public Field getField(String name) {
		return getFieldsMap().get(name.toLowerCase());
	}

	public List<Map> getJsFieldsByView(String view) {
		List<Map> results = new ArrayList<Map>();
		for (Field field : fields) {
			if (field.isRemoveInView(view))
				continue;
			
			Map jsField = field.toJsField();
			if (field.isReadOnlyInView(view))
				jsField.put("readOnly", true);
			
			results.add(jsField);
		}

		if (KxConsts.VIEW_LIST.equals(view)) {
			for (int i = 0; i < results.size(); i++)
				if (i >= 8)
					results.get(i).put("show", false);
				else
					results.get(i).put("show", true);
		}

		return results;	
	}
	
	public Map<String,Map> getJsFieldsMapByView(String view) {
		Map<String,Map> results = new HashMap<String,Map>();
		for (Field field : fields) {
			if (field.isRemoveInView(view))
				continue;
			
			Map jsField = field.toJsField();
			if (field.isReadOnlyInView(view))
				jsField.put("readOnly", true);
			
			results.put((String)jsField.get("name"),jsField);
		}
		return results;	
	}	
}
