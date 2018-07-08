package com.digitalpurr.orderhub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitalpurr.orderhub.commons.BaseRequest;
import com.digitalpurr.orderhub.commons.LoginRequest;
import com.digitalpurr.orderhub.commons.RequestId;

public class RequestProcessor {
	private final static Logger LOGGER = LoggerFactory.getLogger(RequestProcessor.class);
	
	public void run(String message) {
		LOGGER.debug("got message: "+message);
		RequestId requestId = BaseRequest.deserializeRequestId(message);
		LOGGER.debug("Recognized request: "+requestId);
		switch(requestId) {
			case LOGIN: 
				LoginRequest loginRequest =	BaseRequest.deserializeRequest(message, LoginRequest.class);
				SessionManager.getInstance().onUserLogin(loginRequest);
				break;
		}
	}
}
