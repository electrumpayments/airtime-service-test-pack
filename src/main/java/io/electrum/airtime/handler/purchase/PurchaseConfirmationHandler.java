package io.electrum.airtime.handler.purchase;

import static io.electrum.airtime.server.util.AirtimeModelUtils.buildAdviceResponseFromAdvice;

import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import io.electrum.airtime.api.PurchaseResource;
import io.electrum.airtime.api.model.PurchaseConfirmation;
import io.electrum.airtime.handler.BaseHandler;
import io.electrum.airtime.server.AirtimeTestServerRunner;
import io.electrum.airtime.server.util.PurchaseModelUtils;
import io.electrum.airtime.server.util.RequestKey;

public class PurchaseConfirmationHandler extends BaseHandler {
   public PurchaseConfirmationHandler(HttpHeaders httpHeaders) {
      super(httpHeaders);
   }

   public Response handle(PurchaseConfirmation purchaseConfirmation) {
      try {
         Response rsp;

         rsp = PurchaseModelUtils.canConfirmPurchase(purchaseConfirmation, username, password);
         if (rsp != null) {
            return rsp;
         }

         // quietly overwrites any existing purchaseConfirmation
         addPurchaseConfirmationToCache(purchaseConfirmation);

         rsp = Response.accepted(buildAdviceResponseFromAdvice(purchaseConfirmation)).build();

         return rsp;
      } catch (Exception e) {
         return logAndBuildException(e);
      }
   }

   private void addPurchaseConfirmationToCache(PurchaseConfirmation purchaseConfirmation) {
      ConcurrentHashMap<RequestKey, PurchaseConfirmation> confirmationRecords =
            AirtimeTestServerRunner.getTestServer().getPurchaseConfirmationRecords();
      RequestKey confirmationsKey =
            new RequestKey(
                  username,
                  password,
                  PurchaseResource.ConfirmPurchase.PURCHASE_CONFIRMATION,
                  purchaseConfirmation.getRequestId());
      // quietly overwrites any existing purchaseConfirmation
      confirmationRecords.put(confirmationsKey, purchaseConfirmation);
   }

   @Override
   protected String getRequestName() {
      return "Purchase Confirmation";
   }
}
