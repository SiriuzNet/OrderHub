package com.digitalpurr.orderhub.database;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitalpurr.orderhub.database.entity.AbstractEntity;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import de.javakaffee.kryoserializers.EnumSetSerializer;

public class PersistanceManager {
	private final static Logger LOGGER = LoggerFactory.getLogger(PersistanceManager.class);
	private final static Kryo KRYO = new Kryo();
	private final static ConcurrentLinkedQueue<Object> ENTITIES_QUEUE = new ConcurrentLinkedQueue<>();
	private final static Map<String, Integer> ENTITIES_CAS = new ConcurrentHashMap<>();
	//private final static ConcurrentHashMap<String, Object> ENTITIES_CACHE = new ConcurrentHashMap<>();
	private final static EntityCache ENTITIES_CACHE = new EntityCache();

	private Configuration cfg;
	
	private static boolean noHeaders = true;
    private static int compressionLevel = 6;
    private static int maxWriteRetries = 5;
	private Thread persistanceThread;
	private long entityCacheSizeInBytes = 10000000;
	
	private static PersistanceManager INSTANCE;
	
	@SuppressWarnings("unused")
	private void init() {
		INSTANCE = this;
		KRYO.register(EnumSet.class, new EnumSetSerializer());		
		persistanceThread = new Thread() {
			@Override
			public void run() {
				while(true) {
					try {
						synchronized (persistanceThread) {
							if (ENTITIES_QUEUE.size() == 0)
								persistanceThread.wait();
							
							LOGGER.debug("Size of entity queue: "+ENTITIES_QUEUE.size());
							Object entity = ENTITIES_QUEUE.poll();
							if (entity != null) {
								LOGGER.debug("Saveing queued entity: "+ entity.getClass().getSimpleName());
								saveEntityNow(entity);
							}
						}
					} catch(Exception e) {
						LOGGER.error("Exception inside persistance thread", e);
					}
				}
			}
		};
		persistanceThread.setName("PersistMgr");
		persistanceThread.start();
	}
	
	public static PersistanceManager getInstance() {
		return INSTANCE;
	}

	public boolean saveEntity(Object entity) {
		synchronized (persistanceThread) {
			if (entity instanceof AbstractEntity) {
				AbstractEntity aEntity = (AbstractEntity) entity;
				Integer compareCas = ENTITIES_CAS.get(aEntity.getPersistancePath());
				if (compareCas != null && aEntity.getCas() != compareCas) {
					LOGGER.error("CAS doesn't match. Entity not persisted");
					return false;
				}
				aEntity.resetCas();
				ENTITIES_CAS.put(aEntity.getPersistancePath(), aEntity.getCas());
			}
			if (ENTITIES_QUEUE.contains(entity))
				return true;
			ENTITIES_QUEUE.add(entity);
			String path = getEntityPath(entity);
			//if (!ENTITIES_CACHE.containsKey(path))
			LOGGER.debug("Put to cache [{}]",path);
			ENTITIES_CACHE.put(path, entity);
				persistanceThread.notify();			
			
			return true;
		}
	}
	
	private void saveEntityNow(Object entity) {
		saveEntityNow(entity, null);
	}
	
	private synchronized void saveEntityNow(Object entity, String path) {
		int retry = 0;
		while (retry < maxWriteRetries) {
			Output output = null;
			try {
				if (retry > 0)
					Thread.sleep(retry*retry*10);
				if (path == null || path.isEmpty())
					path = getEntityPath(entity);
				Deflater deflater = new Deflater(compressionLevel, noHeaders);
				File file = new File((cfg.getPersistanceBasePath() + path).substring(0,(cfg.getPersistanceBasePath() + path).lastIndexOf(cfg.getFolderSeparator())));
				file.mkdirs();
				OutputStream outputStream = new DeflaterOutputStream(new FileOutputStream(cfg.getPersistanceBasePath() + path + ".dat"), deflater);
				output = new Output(outputStream, 1024);
				KRYO.writeObject(output, KRYO.copy(entity));
				output.flush();
				break;
			} catch (IOException e) {
				retry++;
				LOGGER.info("Error during saving entity. Retrying... [{}]", retry);
				if (retry >= maxWriteRetries) {
					LOGGER.error("Error during saving entity after final retry.", e);
				}
			} catch (InterruptedException e) {
				LOGGER.debug("Sleep interrupted while retrying to write entity");
			} finally {			
				output.close();
			}
		}
	}
	
	private final static String getEntityPath(Object entity) {
		String path = null;
		if (entity instanceof AbstractEntity)
			path = ((AbstractEntity) entity).getPersistancePath();
		if (path == null || path.isEmpty())
			path = entity.getClass().getSimpleName();
		return path;
	}
	
	public <T> T loadEntity(Class<T> entityClass) {
		return loadEntity(null, entityClass);
	}
	
	public <T> T loadEntity(String path, Class<T> entityClass) {
		return loadEntity(path, entityClass, true);
	}
	
	@SuppressWarnings("unchecked")
	public synchronized <T> T loadEntity(String path, Class<T> entityClass, boolean raportOnFail) {
		Input input = null;
		try {
			long start = System.nanoTime();
			if (path == null || path.isEmpty())
				path = entityClass.getSimpleName();
			if (ENTITIES_CACHE.containsKey(path)) {
				LOGGER.debug("Get from cache [{}]",path);
				return (T)ENTITIES_CACHE.get(path);
			}
			makeSpaceInCache();
			Inflater inflater = new Inflater(noHeaders);
			InputStream inputStream = new InflaterInputStream(new FileInputStream(cfg.getPersistanceBasePath() + path + ".dat"), inflater);
			input = new Input(inputStream, 1024);
			T result = KRYO.readObject(input, entityClass);
			ENTITIES_CACHE.put(path, result);
			if (result instanceof AbstractEntity) {
				ENTITIES_CAS.put(path, ((AbstractEntity)result).getCas());
			}
			LOGGER.debug("Entity ["+entityClass.getSimpleName()+"] loaded from file ["+path+"] in "+((System.nanoTime()-start)/1000000)+"ms");
			return result;
		} catch (FileNotFoundException e) {
			if (raportOnFail)
				LOGGER.warn("Error during loading entity ["+entityClass.getSimpleName()+"]: File not found ["+path+"]");
			return null;
		} finally {
			if (input != null)
				input.close();
		}
	}
	
	public static byte[] serialize(final Object entity) {
		try {
			long start = System.nanoTime();
			Output output = null;
			Deflater deflater = new Deflater(compressionLevel, noHeaders);
			ByteArrayOutputStream byteArrayOutput = new ByteArrayOutputStream();
			DeflaterOutputStream outputStream = new DeflaterOutputStream(byteArrayOutput, deflater);
			output = new Output(outputStream, 1024);
			KRYO.writeObject(output, entity);
			output.close();
			outputStream.close();
			byteArrayOutput.close();
			LOGGER.debug("Serialization time: "+((System.nanoTime()-start)/1000000)+"ms");
			return byteArrayOutput.toByteArray();
		} catch (IOException e) {
			LOGGER.error("Exception while serialization", e);
		}
		return null;
	}
	
	public static <T> T deserialize(byte[] entity, Class<T> entityClass) {
		try {
			long start = System.nanoTime();
			Inflater inflater = new Inflater(noHeaders);
			inflater.setInput(entity);
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream(entity.length);  
			byte[] buffer = new byte[1024];  
			while (!inflater.finished()) {  
				int count = inflater.inflate(buffer);  
				outputStream.write(buffer, 0, count);
			}  
			outputStream.close();  
			InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
			Input input = new Input(inputStream, 1024);
			T dObj = KRYO.readObject(input, entityClass);
			LOGGER.debug("Deserialization time: "+((System.nanoTime()-start)/1000000)+"ms");
			return dObj;
		} catch (Exception e) {
			LOGGER.error("Exception while deserialization", e);
		}
		return null;
	}
	
	public void makeSpaceInCache() {
		while (ENTITIES_CACHE.getCacheSizeInBytes() > entityCacheSizeInBytes) {
			String removedEntityPath = ENTITIES_CACHE.removeUseless();
			if (removedEntityPath == null)
				break;
			LOGGER.debug("Removing entity ["+removedEntityPath+"] from cache [cache = "+ENTITIES_CACHE.getCacheSizeInBytes()+"]");
		}
	}
	
	public long getCacheSizeInBytes() {
		return ENTITIES_CACHE.getCacheSizeInBytes();
	}
	
	public long recalculateCacheSize() {
		return ENTITIES_CACHE.recalculateCacheSize();
	}

	public long getEntityCacheSizeInBytes() {
		return entityCacheSizeInBytes;
	}

	public void setEntityCacheSizeInBytes(long entityCacheSizeInBytes) {
		this.entityCacheSizeInBytes = entityCacheSizeInBytes;
	}
	
	public static EntityCache getEntitiesCache() {
		return ENTITIES_CACHE;
	}
}
