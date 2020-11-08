package org.proxy;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.proxy.server.NonBlockingServlet;

public class Main {

    public static class JettyServer {

        public Server create() throws Exception {
            Server server = new Server();
            ServerConnector connector = new ServerConnector(server);
            connector.setPort(8090);
            server.setConnectors(new Connector[]{connector});
            ServletHandler servletHandler = new ServletHandler();
            servletHandler.addServletWithMapping(NonBlockingServlet.class, "/");
            server.setHandler(servletHandler);
            return server;
        }
    }

    public static void main(String[] arg) throws Exception {
        new JettyServer().create().start();
    }
}
