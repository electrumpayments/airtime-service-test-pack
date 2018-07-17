package io.electrum.airtime.handler.purchase;

import static io.electrum.airtime.server.util.AirtimeModelUtils.buildAdviceResponseFromAdvice;

import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import io.electrum.airtime.api.PurchaseResource;
import io.electrum.airtime.api.model.PurchaseReversal;
import io.electrum.airtime.handler.BaseHandler;
import io.electrum.airtime.server.AirtimeTestServerRunner;
import io.electrum.airtime.server.util.PurchaseModelUtils;
import io.electrum.airtime.server.util.RequestKey;

public class PurchaseReversalHandler extends BaseHandler {
   public PurchaseReversalHandler(HttpHeaders httpHeaders) {
      super(httpHeaders);
   }

   public Response handle(PurchaseReversal purchaseReversal) {
      try {
         Response rsp = PurchaseModelUtils.validatePurchaseReversal(purchaseReversal);
         if (rsp != null) {
            return rsp;
         }

         rsp = PurchaseModelUtils.canReversePurchase(purchaseReversal, username, password);
         if (rsp != null) {
            if (rsp.getStatus() == 404) {
               // make sure to record the purchaseReversal in case we get the request late.
               addPurchaseReversalToCache(purchaseReversal);
            }
            return rsp;
         }

         // quietly overwrites any existing purchaseReversal
         addPurchaseReversalToCache(purchaseReversal);

         rsp = Response.accepted(buildAdviceResponseFromAdvice(purchaseReversal)).build();

         return rsp;
      } catch (Exception e) {
         return logAndBuildException(e);
      }
   }

   private void addPurchaseReversalToCache(PurchaseReversal purchaseReversal) {
      ConcurrentHashMap<RequestKey, PurchaseReversal> reversalRecords =
            AirtimeTestServerRunner.getTestServer().getPurchaseReversalRecords();
      RequestKey reversalKey =
            new RequestKey(
                  username,
                  password,
                  PurchaseResource.ReversePurchase.REVERSE_PURCHASE,
                  purchaseReversal.getRequestId());
      reversalRecords.put(reversalKey, purchaseReversal);
   }

   @Override
   protected String getRequestName() {
      return "Purchase Reversal";
   }
}
