package com.digitalpurr.orderhub.database.entity;

import java.util.concurrent.ThreadLocalRandom;


public class AbstractEntity {
	protected boolean persistancePathSet = false;
	protected String persistancePath = "";
	protected int cas;

	public String getPersistancePath() {
		return persistancePath;
	}

	public void setPersistancePath(String persistancePath) {
		this.persistancePath = persistancePath;
		this.persistancePathSet = true;
	}
	
	public int getCas() {
		return cas;
	}
	
	public void resetCas() {
		cas = ThreadLocalRandom.current().nextInt();
	}
}
