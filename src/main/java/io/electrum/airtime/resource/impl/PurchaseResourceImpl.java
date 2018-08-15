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
      log.info(String.format("%s %s", httpServletRequest.getMethod(), uriInfo.getPath()));
      log.debug(String.format("%s %s\n%s", httpServletRequest.getMethod(), uriInfo.getPath(), purchaseConfirmation));
      Response rsp =
            AirtimeMessageHandlerFactory.getPurchaseConfirmationHandler(httpHeaders).handle(purchaseConfirmation);
      log.debug(String.format("Entity returned:\n%s", rsp.getEntity()));

      asyncResponse.resume(rsp);
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
            AirtimeMessageHandlerFactory.getPurchaseRequestHandler(httpHeaders).handle(purchaseRequest, uriInfo);
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
      log.info(String.format("%s %s", httpServletRequest.getMethod(), uriInfo.getPath()));
      log.debug(String.format("%s %s\n%s", httpServletRequest.getMethod(), uriInfo.getPath(), purchaseReversal));
      Response rsp = AirtimeMessageHandlerFactory.getPurchaseReversalHandler(httpHeaders).handle(purchaseReversal);
      log.debug(String.format("Entity returned:\n%s", rsp.getEntity()));

      asyncResponse.resume(rsp);
   }

   @Override
   public void getPurchaseStatus(
         String provider,
         String purchaseReference,
         String originalMsgId,
         SecurityContext securityContext,
         Request request,
         HttpHeaders httpHeaders,
         AsyncResponse asyncResponse,
         UriInfo uriInfo,
         HttpServletRequest httpServletRequest) {
      log.info(String.format("%s %s", httpServletRequest.getMethod(), uriInfo.getPath()));
      log.debug(
            String.format(
                  "%s %s\n%s\n%s\n%s",
                  httpServletRequest.getMethod(),
                  uriInfo.getPath(),
                  provider,
                  purchaseReference,
                  originalMsgId));
      Response rsp =
            AirtimeMessageHandlerFactory.getPurchaseStatusHandler(httpHeaders)
                  .handle(provider, purchaseReference, originalMsgId);
      log.debug(String.format("Entity returned:\n%s", rsp.getEntity()));

      asyncResponse.resume(rsp);
   }

}
