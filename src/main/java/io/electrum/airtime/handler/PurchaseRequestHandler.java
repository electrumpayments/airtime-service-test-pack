package io.electrum.airtime.handler;

import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.electrum.airtime.api.PurchaseResource;
import io.electrum.airtime.api.model.ErrorDetail;
import io.electrum.airtime.api.model.PurchaseRequest;
import io.electrum.airtime.api.model.PurchaseResponse;
import io.electrum.airtime.resource.impl.AirtimeTestServer;
import io.electrum.airtime.server.AirtimeTestServerRunner;
import io.electrum.airtime.server.util.AirtimeModelUtils;
import io.electrum.airtime.server.util.PurchaseModelUtils;
import io.electrum.airtime.server.util.RequestKey;

public class PurchaseRequestHandler {
   private static final Logger log = LoggerFactory.getLogger(AirtimeTestServer.class.getPackage().getName());

   public Response handle(PurchaseRequest purchaseRequest, HttpHeaders httpHeaders, UriInfo uriInfo) {
      try {
         Response rsp = PurchaseModelUtils.validatePurchaseRequest(purchaseRequest);
         if (rsp != null) {
            return rsp;
         }

         String authString = AirtimeModelUtils.getAuthString(httpHeaders.getHeaderString(HttpHeaders.AUTHORIZATION));
         String username = AirtimeModelUtils.getUsernameFromAuth(authString);
         String password = AirtimeModelUtils.getPasswordFromAuth(authString);

         if (!validUsername(purchaseRequest, username)) {
            return PurchaseModelUtils.buildIncorrectUsernameErrorResponse(
                  purchaseRequest.getId(),
                  purchaseRequest.getClient(),
                  username,
                  ErrorDetail.RequestType.PURCHASE_REQUEST);
         }

         rsp = PurchaseModelUtils.canPurchasePurchaseRequest(purchaseRequest.getId(), username, password);
         if (rsp != null) {
            return rsp;
         }

         // store purchase request in db
         addPurchaseRequestToCache(purchaseRequest, username, password);

         // generate purchase response with randomized fields and store in db
         PurchaseResponse purchaseResponse = PurchaseModelUtils.purchaseRspFromReq(purchaseRequest);

         addPurchaseResponseToCache(purchaseResponse, username, password);

         rsp = Response.created(uriInfo.getRequestUri()).entity(purchaseResponse).build();

         return rsp;
      } catch (Exception e) {
         log.debug("error processing Purchase Request", e);
         for (StackTraceElement ste : e.getStackTrace()) {
            log.debug(ste.toString());
         }
         Response rsp = Response.serverError().entity(e.getMessage()).build();
         return rsp;
      }
   }

   private void addPurchaseRequestToCache(PurchaseRequest purchaseRequest, String username, String password) {
      ConcurrentHashMap<RequestKey, PurchaseRequest> purchaseRequestRecords =
            AirtimeTestServerRunner.getTestServer().getPurchaseRequestRecords();
      RequestKey key = new RequestKey(username, password, PurchaseResource.Purchase.PURCHASE, purchaseRequest.getId());
      purchaseRequestRecords.put(key, purchaseRequest);
   }

   private void addPurchaseResponseToCache(PurchaseResponse purchaseResponse, String username, String password) {
      ConcurrentHashMap<RequestKey, PurchaseResponse> responseRecords =
            AirtimeTestServerRunner.getTestServer().getPurchaseResponseRecords();
      RequestKey key = new RequestKey(username, password, PurchaseResource.Purchase.PURCHASE, purchaseResponse.getId());
      responseRecords.put(key, purchaseResponse);
   }

   private boolean validUsername(PurchaseRequest purchaseRequest, String username) {
      return purchaseRequest.getClient().getId().equals(username);
   }
}
