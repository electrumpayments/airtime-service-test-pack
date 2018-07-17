package io.electrum.airtime.handler;

import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import io.electrum.airtime.api.PurchaseResource;
import io.electrum.airtime.api.model.ErrorDetail;
import io.electrum.airtime.api.model.PurchaseRequest;
import io.electrum.airtime.api.model.PurchaseResponse;
import io.electrum.airtime.server.AirtimeTestServerRunner;
import io.electrum.airtime.server.util.PurchaseModelUtils;
import io.electrum.airtime.server.util.RequestKey;

public class PurchaseRequestHandler extends BaseHandler {
   protected PurchaseRequestHandler(HttpHeaders httpHeaders) {
      super(httpHeaders);
   }

   public Response handle(PurchaseRequest purchaseRequest, UriInfo uriInfo) {
      try {
         Response rsp = PurchaseModelUtils.validatePurchaseRequest(purchaseRequest);
         if (rsp != null) {
            return rsp;
         }

         if (!validUsername(purchaseRequest)) {
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
         addPurchaseRequestToCache(purchaseRequest);

         // generate purchase response with randomized fields and store in db
         PurchaseResponse purchaseResponse = PurchaseModelUtils.purchaseRspFromReq(purchaseRequest);

         addPurchaseResponseToCache(purchaseResponse);

         rsp = Response.created(uriInfo.getRequestUri()).entity(purchaseResponse).build();

         return rsp;
      } catch (Exception e) {
         return logAndBuildException(e);
      }
   }

   private void addPurchaseRequestToCache(PurchaseRequest purchaseRequest) {
      ConcurrentHashMap<RequestKey, PurchaseRequest> purchaseRequestRecords =
            AirtimeTestServerRunner.getTestServer().getPurchaseRequestRecords();
      RequestKey key = new RequestKey(username, password, PurchaseResource.Purchase.PURCHASE, purchaseRequest.getId());
      purchaseRequestRecords.put(key, purchaseRequest);
   }

   private void addPurchaseResponseToCache(PurchaseResponse purchaseResponse) {
      ConcurrentHashMap<RequestKey, PurchaseResponse> responseRecords =
            AirtimeTestServerRunner.getTestServer().getPurchaseResponseRecords();
      RequestKey key = new RequestKey(username, password, PurchaseResource.Purchase.PURCHASE, purchaseResponse.getId());
      responseRecords.put(key, purchaseResponse);
   }

   private boolean validUsername(PurchaseRequest purchaseRequest) {
      return purchaseRequest.getClient().getId().equals(username);
   }

   @Override
   protected String getRequestName() {
      return "Purchase Request";
   }
}
