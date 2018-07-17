package io.electrum.airtime.handler;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.electrum.airtime.resource.impl.AirtimeTestServer;
import io.electrum.airtime.server.util.AirtimeModelUtils;

public abstract class BaseHandler {
   private static final Logger log = LoggerFactory.getLogger(AirtimeTestServer.class.getPackage().getName());

   protected String username;
   protected String password;

   protected BaseHandler(HttpHeaders httpHeaders) {
      String authString = AirtimeModelUtils.getAuthString(httpHeaders.getHeaderString(HttpHeaders.AUTHORIZATION));
      this.username = AirtimeModelUtils.getUsernameFromAuth(authString);
      this.password = AirtimeModelUtils.getPasswordFromAuth(authString);
   }

   protected Response logAndBuildException(Exception e) {
      log.debug("error processing " + getRequestName(), e);
      for (StackTraceElement ste : e.getStackTrace()) {
         log.debug(ste.toString());
      }
      return Response.serverError().entity(e.getMessage()).build();
   }

   protected abstract String getRequestName();
}
