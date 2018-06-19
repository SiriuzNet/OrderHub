package com.digitalpurr.orderhub;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

public class MessagingServlet extends WebSocketServlet {

	private static final long serialVersionUID = -1812566810996856553L;

	@Override
    public void configure(WebSocketServletFactory factory) {
        factory.register(MessagingAdapter.class);
    }
    
}