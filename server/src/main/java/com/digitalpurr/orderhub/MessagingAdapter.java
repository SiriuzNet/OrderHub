package com.digitalpurr.orderhub;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessagingAdapter extends WebSocketAdapter {
    
	private final static Logger LOGGER = LoggerFactory.getLogger(MessagingAdapter.class);
    private Session session;
    
    @Override
    public void onWebSocketConnect(Session session) {
    	LOGGER.info("Websocket connect");
        super.onWebSocketConnect(session); 
        
        this.session = session;
    }
    @Override
    public void onWebSocketClose(int statusCode, String reason) {
    	LOGGER.info("Websocket close");
        this.session = null;
        
        System.err.println("Close connection "+statusCode+", "+reason);
        
        super.onWebSocketClose(statusCode, reason); 
    }
    @Override
    public void onWebSocketText(String message) {
    	LOGGER.debug("Recieved message: "+message);
        super.onWebSocketText(message); 
    }
    
}