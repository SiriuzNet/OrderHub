package com.digitalpurr.orderhub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Start {
	private final static Logger LOGGER = LoggerFactory.getLogger(Start.class);
	
	public static void main(String[] args) {
		JettyServer server = new JettyServer();
		try {
			server.setup();
			server.start();
		} catch (Exception e) {
			LOGGER.error("Unable to start server", e);
		}
	}

}
