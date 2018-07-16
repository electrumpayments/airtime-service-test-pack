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

import io.electrum.airtime.api.IPurchaseResource;
import io.electrum.airtime.api.PurchaseResource;
import io.electrum.airtime.api.model.PurchaseConfirmation;
import io.electrum.airtime.api.model.PurchaseRequest;
import io.electrum.airtime.api.model.PurchaseReversal;
import io.electrum.airtime.handler.AirtimeMessageHandlerFactory;
import io.swagger.annotations.Api;
import io.swagger.annotations.Authorization;

@Path(PurchaseResource.PATH)
@Api(description = "the Airtime API", authorizations = { @Authorization("httpBasic") })
public class PurchaseResourceImpl extends PurchaseResource implements IPurchaseResource {
   static PurchaseResourceImpl instance = null;
   private static final Logger log = LoggerFactory.getLogger(AirtimeTestServer.class.getPackage().getName());

   @Override
   protected IPurchaseResource getResourceImplementation() {
      if (instance == null) {
         instance = new PurchaseResourceImpl();
      }
      return instance;
   }

   @Override
   public void confirmPurchase(
         PurchaseConfirmation purchaseConfirmation,
         SecurityContext securityContext,
         Request request,
         HttpHeaders httpHeaders,
         AsyncResponse asyncResponse,
         UriInfo uriInfo,
         HttpServletRequest httpServletRequest) {

   }

   @Override
   public void purchasePurchase(
         PurchaseRequest purchaseRequest,
         SecurityContext securityContext,
         Request request,
         HttpHeaders httpHeaders,
         AsyncResponse asyncResponse,
         UriInfo uriInfo,
         HttpServletRequest httpServletRequest) {
      log.info(String.format("%s %s", httpServletRequest.getMethod(), uriInfo.getPath()));
      log.debug(String.format("%s %s\n%s", httpServletRequest.getMethod(), uriInfo.getPath(), purchaseRequest));
      Response rsp =
            AirtimeMessageHandlerFactory.getPurchaseRequestHandler().handle(purchaseRequest, httpHeaders, uriInfo);
      log.debug(String.format("Entity returned:\n%s", rsp.getEntity()));

      asyncResponse.resume(rsp);
   }

   @Override
   public void reversePurchase(
         PurchaseReversal purchaseReversal,
         SecurityContext securityContext,
         Request request,
         HttpHeaders httpHeaders,
         AsyncResponse asyncResponse,
         UriInfo uriInfo,
         HttpServletRequest httpServletRequest) {

   }

   @Override
   public void getPurchaseStatus(
         String s,
         String s1,
         String s2,
         SecurityContext securityContext,
         Request request,
         HttpHeaders httpHeaders,
         AsyncResponse asyncResponse,
         UriInfo uriInfo,
         HttpServletRequest httpServletRequest) {

   }

}
