package io.electrum.airtime.handler;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.electrum.airtime.resource.impl.AirtimeTestServer;
import io.electrum.vas.Utils;

public abstract class BaseHandler {
   private static final Logger log = LoggerFactory.getLogger(AirtimeTestServer.class.getPackage().getName());

   protected String username;
   protected String password;

   protected BaseHandler(HttpHeaders httpHeaders) {
      String authString = Utils.getBasicAuthString(httpHeaders.getHeaderString(HttpHeaders.AUTHORIZATION));
      this.username = Utils.getUsernameFromBasicAuth(authString);
      this.password = Utils.getPasswordFromBasicAuth(authString);
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
