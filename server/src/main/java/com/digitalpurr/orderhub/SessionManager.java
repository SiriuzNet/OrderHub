package com.digitalpurr.orderhub;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jetty.websocket.api.Session;

import com.digitalpurr.orderhub.commons.LoginRequest;

public class SessionManager {
	private final static SessionManager INSTANCE = new SessionManager();
	
	public final static SessionManager getInstance() {
		return INSTANCE;
	}
	
	private AtomicLong currentSessionId = new AtomicLong(0);
	private final static ConcurrentHashMap<Long, SessionData> SESSIONS = new ConcurrentHashMap<>();
	
	private SessionManager() {
		
	}
	
	public long createSession(Session session) throws Exception {
		long sessionId = currentSessionId.getAndIncrement();
		SessionData sessionData = new SessionData();
		sessionData.setSession(session);
		if (SESSIONS.putIfAbsent(sessionId, sessionData) != null) {
			throw new Exception("Unable to create session");
		}
		return sessionId;
	}
	
	public void dropSession(Long sessionId) {
		 SESSIONS.remove(sessionId);
	}
	
	public SessionData getSessionData(Long sessionId) {
		return SESSIONS.get(sessionId);
	}
	
	public void onUserLogin(LoginRequest loginRequest) {
		
	}
}
