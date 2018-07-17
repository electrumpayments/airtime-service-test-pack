package io.electrum.airtime.handler.purchase;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import io.electrum.airtime.handler.BaseHandler;

public class PurchaseStatusHandler extends BaseHandler {
   public PurchaseStatusHandler(HttpHeaders httpHeaders) {
      super(httpHeaders);
   }

   public Response handle(String provider, String purchaseReference, String originalMsgId) {
      try {
         if (originalMsgId == null) {

         } else {

         }
         return null;
         // Response rsp = PurchaseModelUtils.validatePurchaseReversal(purchaseReversal);
         // if (rsp != null) {
         // return rsp;
         // }
         //
         // rsp = PurchaseModelUtils.canReversePurchase(purchaseReversal, username, password);
         // if (rsp != null) {
         // if (rsp.getStatus() == 404) {
         // // make sure to record the purchaseReversal in case we get the request late.
         // addPurchaseReversalToCache(purchaseReversal);
         // }
         // return rsp;
         // }
         //
         // // quietly overwrites any existing purchaseReversal
         // addPurchaseReversalToCache(purchaseReversal);
         //
         // rsp = Response.accepted(buildAdviceResponseFromAdvice(purchaseReversal)).build();
         //
         // return rsp;
      } catch (Exception e) {
         return logAndBuildException(e);
      }
   }

   @Override
   protected String getRequestName() {
      return "Purchase Status";
   }
}
