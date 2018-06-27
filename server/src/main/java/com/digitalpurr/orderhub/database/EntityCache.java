package com.digitalpurr.orderhub.database;

import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityCache {
	private final static Logger LOGGER = LoggerFactory.getLogger(EntityCache.class);
	private final int USELESS_SIZE = 10;
	private final ConcurrentHashMap<String, EntityCacheObject> ENTITIES_CACHE = new ConcurrentHashMap<>();
	private final Queue<EntityCacheObject> USELESS = new CircularFifoQueue<>(USELESS_SIZE);
	private static long cacheSize = 0;
	
	public boolean containsKey(String key) {
		if (ENTITIES_CACHE.containsKey(key))
			return (ENTITIES_CACHE.get(key).getEntity() != null || ENTITIES_CACHE.get(key).getCompEntity() != null);
		return false;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(String key) {
		EntityCacheObject entityCacheObject = ENTITIES_CACHE.get(key);
		T entity = (T)entityCacheObject.getEntity();
		if (entity == null) {
			long size = com.carrotsearch.sizeof.RamUsageEstimator.sizeOf(entityCacheObject);
			LOGGER.debug("Entity ["+entityCacheObject.getPath()+"] size before: "+size);
			cacheSize -= size;
			entity = (T) PersistanceManager.deserialize(entityCacheObject.getCompEntity(), entityCacheObject.getEntityClass());
			entityCacheObject.setEntity(entity);
			entityCacheObject.setCompEntity(null);
			size = com.carrotsearch.sizeof.RamUsageEstimator.sizeOf(entityCacheObject);
			LOGGER.debug("Entity ["+entityCacheObject.getPath()+"] size after: "+size);
			cacheSize += size;
			return entity;
		} else {
			return entity;
		}
	}
	
	public EntityCacheObject put(String key, Object value) {
		EntityCacheObject newObj = ENTITIES_CACHE.get(key);
		EntityCacheObject obj = newObj;
		if (newObj == null) {
			newObj = new EntityCacheObject(key, value);
			obj = ENTITIES_CACHE.put(key, newObj);
		}
		cacheSize += com.carrotsearch.sizeof.RamUsageEstimator.sizeOf(newObj);
		return obj;
	}
	
	@SuppressWarnings({ "unused", "unchecked" })
	public String removeUseless() {
		long start = System.nanoTime();
		if (!USELESS.isEmpty()) {
			EntityCacheObject eco = (EntityCacheObject)USELESS.remove();
			long size = com.carrotsearch.sizeof.RamUsageEstimator.sizeOf(eco);
			LOGGER.debug("Entity ["+eco.getPath()+"] size before: "+size);
			cacheSize -= size;
			eco = eco.remove();
			if (eco.isEmpty()) {
				ENTITIES_CACHE.remove(eco.getPath());
				return eco.getPath();
			}
			size = com.carrotsearch.sizeof.RamUsageEstimator.sizeOf(eco);
			cacheSize += size;
			LOGGER.debug("Entity ["+eco.getPath()+"] size after: "+size+" in "+((System.nanoTime()-start)/1000000)+"ms");
			return eco.getPath();
		}
		long leastFrequent = Long.MAX_VALUE;
		for (EntityCacheObject eco : ENTITIES_CACHE.values()) {
			if ((eco.getEntity() != null || eco.getCompEntity() != null) && eco.getTotalUsages() <= leastFrequent) {
				USELESS.add(eco);
				leastFrequent = eco.getTotalUsages();
			}
		}
		if (USELESS.isEmpty()) {
			LOGGER.warn("No entity to remove. Total cache records is "+ENTITIES_CACHE.size());
			return null;
		}
		EntityCacheObject removedObj = (EntityCacheObject)USELESS.remove();
		long size = com.carrotsearch.sizeof.RamUsageEstimator.sizeOf(removedObj);
		LOGGER.debug("Entity ["+removedObj.getPath()+"] size before: "+size);
		cacheSize -= size;
		removedObj = removedObj.remove();
		if (removedObj.isEmpty()) {
			ENTITIES_CACHE.remove(removedObj.getPath());
			return removedObj.getPath();
		}
		size = com.carrotsearch.sizeof.RamUsageEstimator.sizeOf(removedObj);
		cacheSize += size;
		LOGGER.debug("Entity ["+removedObj.getPath()+"] size after: "+size+" in "+((System.nanoTime()-start)/1000000)+"ms");
		String removedPath = removedObj.getPath();
		return removedPath;
	}
	
	public long getCacheSizeInBytes() {
		return cacheSize;
	}
	
	public long recalculateCacheSize() {
		cacheSize = com.carrotsearch.sizeof.RamUsageEstimator.sizeOf(ENTITIES_CACHE);
		return cacheSize;
	}
	
	public ConcurrentHashMap<String, EntityCacheObject> getEntietiesCache() {
		return ENTITIES_CACHE;
	}
}
