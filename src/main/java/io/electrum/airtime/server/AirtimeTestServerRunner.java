package io.electrum.airtime.server;

import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ScheduledExecutorScheduler;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.electrum.airtime.resource.impl.AirtimeTestServer;
public class AirtimeTestServerRunner {

   private static Logger log_logger = LoggerFactory.getLogger("io.electrum.airtime.server.log");
   
   private static AirtimeTestServer testServer;

   public static void main(String[] args) throws Exception {
      startAirtimeTestServer();
   }

   public static void startAirtimeTestServer() throws Exception {

      log_logger.info("---- STARTING AIRTIME SERVER ----");

      try {

         // === jetty.xml ===
         // Setup Threadpool
         QueuedThreadPool threadPool = new QueuedThreadPool();
         threadPool.setMaxThreads(500);

         // Server
         Server server = new Server(threadPool);

         // Scheduler
         server.addBean(new ScheduledExecutorScheduler());

         // HTTP Configuration
         HttpConfiguration http_config = new HttpConfiguration();
         http_config.setSecureScheme("https");
         http_config.setSecurePort(8082);
         http_config.setOutputBufferSize(32768);
         http_config.setRequestHeaderSize(8192);
         http_config.setResponseHeaderSize(8192);
         http_config.setSendServerVersion(true);
         http_config.setSendDateHeader(false);

         // Handler Structure
         HandlerCollection handlers = new HandlerCollection();
         ContextHandlerCollection contexts = new ContextHandlerCollection();
         handlers.setHandlers(new Handler[] { contexts, new DefaultHandler() });
         server.setHandler(handlers);

         // Extra options
         server.setDumpAfterStart(false);
         server.setDumpBeforeStop(false);
         server.setStopAtShutdown(true);

         // === jetty-http.xml ===
         ServerConnector http = new ServerConnector(server, new HttpConnectionFactory(http_config));
         http.setPort(8081);
         http.setIdleTimeout(30000);
         server.addConnector(http);

         ConstraintSecurityHandler sh = new ConstraintSecurityHandler();
         // sh.addConstraintMapping(cm);

         testServer = new AirtimeTestServer();
         ServletContainer servletContainer = new ServletContainer(testServer);
         ServletHolder servletHolder = new ServletHolder(servletContainer);
         ServletContextHandler context = new ServletContextHandler();
         context.setContextPath("/");
         context.addServlet(servletHolder, "/*");
         context.setHandler(sh);

         server.setHandler(context);
        
         // Start the server
         server.start();
         server.join();
      } catch (Exception e) {
         log_logger.error("Unable to start TestServer", e);
         throw e;
      }
   }
   
   public static AirtimeTestServer getTestServer()
   {
      return testServer;
   }
}
