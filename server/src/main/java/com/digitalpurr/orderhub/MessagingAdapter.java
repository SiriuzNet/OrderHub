package com.digitalpurr.orderhub;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessagingAdapter extends WebSocketAdapter {
    
	private final static Logger LOGGER = LoggerFactory.getLogger(MessagingAdapter.class);
	private Long sessionId;
    
    @Override
    public void onWebSocketConnect(Session session) {
    	LOGGER.info("Websocket connect");
        super.onWebSocketConnect(session);
        try {
			sessionId = SessionManager.getInstance().createSession(session);
		} catch (Exception e) {
			LOGGER.error("Unable to open socket connection", e);
		}
    }
    @Override
    public void onWebSocketClose(int statusCode, String reason) {
    	LOGGER.info("Websocket close");
    	SessionManager.getInstance().dropSession(sessionId);
        this.sessionId = null;
        LOGGER.debug("Close connection "+statusCode+", "+reason);
        super.onWebSocketClose(statusCode, reason); 
    }
    @Override
    public void onWebSocketText(String message) {
    	LOGGER.trace("Recieved message: "+message);
        super.onWebSocketText(message);
        SessionManager.getInstance().getSessionData(sessionId).getRequestProcessor().run(message);
    }
    
}