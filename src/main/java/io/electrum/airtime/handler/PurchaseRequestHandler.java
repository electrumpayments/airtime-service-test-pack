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
         if (!validUsername(purchaseRequest, username)) {
            return PurchaseModelUtils.buildIncorrectUsernameErrorResponse(
                  purchaseRequest.getId(),
                  purchaseRequest.getClient(),
                  username,
                  ErrorDetail.RequestType.PURCHASE_REQUEST);
         }

         String password = AirtimeModelUtils.getPasswordFromAuth(authString);
         RequestKey key =
               new RequestKey(username, password, PurchaseResource.Purchase.PURCHASE, purchaseRequest.getId());
         rsp = PurchaseModelUtils.canPurchasePurchaseRequest(purchaseRequest.getId(), username, password);
         if (rsp != null) {
            return rsp;
         }

         // store purchase request in db
         ConcurrentHashMap<RequestKey, PurchaseRequest> purchaseRequestRecords =
               AirtimeTestServerRunner.getTestServer().getPurchaseRequestRecords();
         purchaseRequestRecords.put(key, purchaseRequest);

         // generate purchase response with randomized fields and store in db
         PurchaseResponse purchaseResponse = PurchaseModelUtils.purchaseRspFromReq(purchaseRequest);
         ConcurrentHashMap<RequestKey, PurchaseResponse> responseRecords =
               AirtimeTestServerRunner.getTestServer().getPurchaseResponseRecords();
         responseRecords.put(key, purchaseResponse);

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

   private boolean validUsername(PurchaseRequest purchaseRequest, String username) {
      return purchaseRequest.getClient().getId().equals(username);
   }
}
