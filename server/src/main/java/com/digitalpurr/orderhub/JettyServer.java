package com.digitalpurr.orderhub;

import java.util.Collections;

import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.security.Constraint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JettyServer {
	private final static Logger LOGGER = LoggerFactory.getLogger(JettyServer.class);
	private Server server;
    
    public void setup() {
    	LOGGER.info("Server setup...");
        server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(8080);
        server.addConnector(connector);
        
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setWelcomeFiles(new String[] { "index.html" });
        resourceHandler.setResourceBase("web/");
       
        LoginService loginService = new HashLoginService("Admin", "src/main/resources/realm.properties");
        server.addBean(loginService);
        
        ConstraintSecurityHandler security = new ConstraintSecurityHandler();
        
        Constraint constraint = new Constraint();
        constraint.setName("auth");
        constraint.setAuthenticate(true);
        constraint.setRoles(new String[] { "admin" });
        
        ConstraintMapping mapping = new ConstraintMapping();
        mapping.setPathSpec("/admin/*");
        mapping.setConstraint(constraint);
        
        security.setConstraintMappings(Collections.singletonList(mapping));
        security.setAuthenticator(new BasicAuthenticator());
        security.setLoginService(loginService);
        security.setHandler(resourceHandler);
        
        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        handler.setContextPath("/");
        
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { security, handler });
        
        server.setHandler(handlers);
        
        handler.addServlet(MessagingServlet.class, "/websocket");
    }
    
    public void start() throws Exception {
    	LOGGER.info("Server start...");
        server.start();
        //server.dump(System.err);
        server.join();
    }
}
