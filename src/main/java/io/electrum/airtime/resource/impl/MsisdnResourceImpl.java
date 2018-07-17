package io.electrum.airtime.resource.impl;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.electrum.airtime.api.IMsisdnResource;
import io.electrum.airtime.api.MsisdnResource;
import io.electrum.airtime.handler.AirtimeMessageHandlerFactory;
import io.swagger.annotations.Api;
import io.swagger.annotations.Authorization;

@Path(MsisdnResource.PATH)
@Api(description = "the Airtime API", authorizations = { @Authorization("httpBasic") })
public class MsisdnResourceImpl extends MsisdnResource implements IMsisdnResource {
   static MsisdnResourceImpl instance = null;
   private static final Logger log = LoggerFactory.getLogger(AirtimeTestServer.class.getPackage().getName());

   @Override
   protected IMsisdnResource getResourceImplementation() {
      if (instance == null) {
         instance = new MsisdnResourceImpl();
      }
      return instance;
   }

   @Override
   public void lookupMsisdn(
         String msisdn,
         String operator,
         SecurityContext securityContext,
         Request request,
         HttpHeaders httpHeaders,
         AsyncResponse asyncResponse,
         UriInfo uriInfo,
         HttpServletRequest httpServletRequest) {
      log.info(String.format("%s %s", httpServletRequest.getMethod(), uriInfo.getPath()));
      log.debug(String.format("%s %s\n%s\n%s", httpServletRequest.getMethod(), uriInfo.getPath(), msisdn, operator));
      Response rsp = AirtimeMessageHandlerFactory.getMsisdnHandler(httpHeaders).handle(msisdn, operator);
      log.debug(String.format("Entity returned:\n%s", rsp.getEntity()));

      asyncResponse.resume(rsp);
   }

}
