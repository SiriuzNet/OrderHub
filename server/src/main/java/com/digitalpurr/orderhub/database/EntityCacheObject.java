package com.digitalpurr.orderhub.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class EntityCacheObject {
	transient private final static Logger LOGGER = LoggerFactory.getLogger(EntityCacheObject.class);
	
	private String path;
	private Object entity;
	private byte[] compEntity;
	@SuppressWarnings("rawtypes")
	private Class entityClass;
	private long totalUsages;
	private long lastAccess;
	
	public EntityCacheObject(String path, Object entity) {
		this.path = path;
		this.entity = entity;
		this.entityClass = entity.getClass();
	}
	
	public long getTotalUsages() {
		return totalUsages;
	}
	
	public long getLastAccess() {
		return lastAccess;
	}
	
	public Object getEntity() {
		totalUsages++;
		lastAccess = System.currentTimeMillis();
		return entity;
	}

	public void setEntity(Object entity) {
		this.entity = entity;
	}
	
	public byte[] getCompEntity() {
		return compEntity;
	}

	public void setCompEntity(byte[] compEntity) {
		this.compEntity = compEntity;
	}

	@SuppressWarnings("rawtypes")
	public Class getEntityClass() {
		return entityClass;
	}

	public EntityCacheObject remove() {
		if (this.compEntity == null && this.entity != null) {
			this.compEntity = PersistanceManager.serialize(entity);
			LOGGER.debug("serialize");
		}
		if (this.compEntity != null && this.entity == null) {
			this.compEntity = null;
			LOGGER.debug("null");
		}
		this.entity = null;
		return this;
	}
	
	public String getPath() {
		return path;
	}
	
	public boolean isEmpty() {
		return (this.entity == null && this.compEntity == null);
	}
	
	public String getStatus() {
		return (entity==null?"e[N]":"e[E]")+":"+(compEntity==null?"c[N]":"c[E]");
	}
}
