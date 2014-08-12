package org.tiger.tools;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.webapp.WebAppClassLoader;
import org.eclipse.jetty.webapp.WebAppContext;

/*
 <!--jsp support for jetty, add the 2 following -->
        <dependency>
          <groupId>org.eclipse.jetty</groupId>
          <artifactId>jetty-server</artifactId>
          <version>7.6.12.v20130726</version>
          <scope>test</scope>
        </dependency>
        <dependency>
          <groupId>org.eclipse.jetty</groupId>
          <artifactId>jetty-webapp</artifactId>
          <version>7.6.12.v20130726</version>
          <scope>test</scope>
        </dependency>
	    <dependency>
	        <groupId>org.mortbay.jetty</groupId>
	        <artifactId>jsp-2.1</artifactId>
	        <version>6.1.14</version>
	        <type>jar</type>
	        <scope>test</scope>
	    </dependency>
	    <dependency>
	        <groupId>org.mortbay.jetty</groupId>
	        <artifactId>jsp-api-2.1</artifactId>
	        <version>6.1.14</version>
	        <type>jar</type>
	        <scope>test</scope>
	    </dependency>
	    <dependency>
			<groupId>javax.servlet.jsp</groupId>
			<artifactId>jsp-api</artifactId>
			<version>2.2</version>
			<scope>test</scope>
		</dependency> 
		<dependency>
            <groupId>org.apache.httpcomponents</groupId>
            <artifactId>httpclient</artifactId>
            <version>4.2.1</version>
        </dependency>
  
 */

public class EmbbedJettyServer {
    
    private static final String CONTEXT = "/sample";
    private static final int PORT = 8000;
    
    private static void reloadContext(Server server) throws Exception {
        WebAppContext context = (WebAppContext) server.getHandler();

        System.out.println("[INFO] Application reloading");
        context.stop();
        
        WebAppClassLoader classLoader = new WebAppClassLoader(context);
        classLoader.addClassPath("target/classes");
        context.setClassLoader(classLoader);

        context.start();

        System.out.println("[INFO] Application reloaded");
    }
    
	public static void main(String[] args) throws Exception {
		Server server = new Server();
		server.setStopAtShutdown(true);
		SelectChannelConnector connector = new SelectChannelConnector();
        connector.setPort(PORT);
        connector.setReuseAddress(false);
        server.setConnectors(new Connector[] { connector });
		
//		-Djava.net.preferIPv4Stack=true -DsingleApp=true
		System.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperty("singleApp", "true");

		WebAppContext context = new WebAppContext();
		context.setDescriptor("src/main/webapp/WEB-INF/web.xml");
		context.setResourceBase("src/main/webapp");
		context.setContextPath(CONTEXT);
		context.setParentLoaderPriority(true);

		server.setHandler(context);
		
		try {
            server.start();

            System.out.println("[INFO] Server running at http://localhost:" + PORT + CONTEXT);
//            System.out.println("[HINT] Hit Enter to reload the application quickly");

//            while (true) {
//                char c = (char) System.in.read();
//                if (c == '\n') {
//                    reloadContext(server);
//                }
//            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
	}

}
