package com.digitalpurr.orderhub.database.entity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.digitalpurr.orderhub.database.PersistanceManager;



public class GenericEntity extends AbstractEntity {

	private Map<EntityAttribute, Object> attributes = new ConcurrentHashMap<EntityAttribute, Object>();
		
	public <T> T get(EntityAttribute attributeName) {
		@SuppressWarnings("unchecked")
		T result = (T) attributes.get(attributeName);
		return result; 
	}
	
	public GenericEntity set(EntityAttribute attributeName, Object value) {
		attributes.put(attributeName, value);
		return this;
	}
	
	public boolean isSet(EntityAttribute attributeName) {
		return attributes.containsKey(attributeName);
	}
	
	public GenericEntity delete(EntityAttribute attributeName) {
		attributes.remove(attributeName);
		return this;
	}
	
	public boolean save() {
		if (persistancePathSet) {
			PersistanceManager.getInstance().saveEntity(this);
			return true;
		}
		return false;
	}
	
	public static GenericEntity load(String path) {
		return PersistanceManager.getInstance().loadEntity(path, GenericEntity.class, true);
	}
	
	public String getAsPlainText() {
		String output = "Entity "+getPersistancePath()+":\n";
		output += "-----------------------------------------------------\n";
		for (EntityAttribute entityAttribute : attributes.keySet()) {
			output += entityAttribute.toString() + ": " + attributes.get(entityAttribute) + "\n";
		}
		return output;
	}
	
}
