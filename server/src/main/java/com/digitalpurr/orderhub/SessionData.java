package com.digitalpurr.orderhub;

import org.eclipse.jetty.websocket.api.Session;

public class SessionData {
	private Session session;
	private RequestProcessor requestProcessor;
	
	public Session getSession() {
		return session;
	}
	
	public void setSession(Session session) {
		this.session = session;
	}
	
	public RequestProcessor getRequestProcessor() {
		if (requestProcessor == null)
			requestProcessor = new RequestProcessor();
		return requestProcessor;
	}
}
