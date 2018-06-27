package com.digitalpurr.orderhub.database;

public class Configuration {
	private String persistanceBasePath = "";
	private String folderSeparator = "\\";
	
	public String getPersistanceBasePath() {
		return persistanceBasePath;
	}
	public void setPersistanceBasePath(String persistanceBasePath) {
		this.persistanceBasePath = persistanceBasePath;
	}
	public String getFolderSeparator() {
		return folderSeparator;
	}
	public void setFolderSeparator(String folderSeparator) {
		this.folderSeparator = folderSeparator;
	}
}
