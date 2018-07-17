package io.electrum.airtime.handler;

import static io.electrum.airtime.server.util.AirtimeModelUtils.buildAdviceResponseFromAdvice;

import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.electrum.airtime.api.model.PurchaseReversal;
import io.electrum.airtime.resource.impl.AirtimeTestServer;
import io.electrum.airtime.server.AirtimeTestServerRunner;
import io.electrum.airtime.server.util.AirtimeModelUtils;
import io.electrum.airtime.server.util.PurchaseModelUtils;
import io.electrum.airtime.server.util.RequestKey;

public class PurchaseReversalHandler {
   private static final Logger log = LoggerFactory.getLogger(AirtimeTestServer.class.getPackage().getName());

   public Response handle(PurchaseReversal purchaseReversal, HttpHeaders httpHeaders) {
      try {
         Response rsp = PurchaseModelUtils.validatePurchaseReversal(purchaseReversal);
         if (rsp != null) {
            return rsp;
         }

         String authString = AirtimeModelUtils.getAuthString(httpHeaders.getHeaderString(HttpHeaders.AUTHORIZATION));
         String username = AirtimeModelUtils.getUsernameFromAuth(authString);
         String password = AirtimeModelUtils.getPasswordFromAuth(authString);

         rsp = PurchaseModelUtils.canReversePurchase(purchaseReversal, username, password);
         if (rsp != null) {
            if (rsp.getStatus() == 404) {
               // make sure to record the purchaseReversal in case we get the request late.
               addPurchaseReversalToCache(purchaseReversal, username, password);
            }
            return rsp;
         }

         // quietly overwrites any existing purchaseReversal
         addPurchaseReversalToCache(purchaseReversal, username, password);

         rsp = Response.accepted(buildAdviceResponseFromAdvice(purchaseReversal)).build();

         return rsp;
      } catch (Exception e) {
         log.debug("error processing VoucherProvision", e);
         Response rsp = Response.serverError().entity(e.getMessage()).build();
         return rsp;
      }
   }

   private void addPurchaseReversalToCache(PurchaseReversal purchaseReversal, String username, String password) {
      ConcurrentHashMap<RequestKey, PurchaseReversal> reversalRecords =
            AirtimeTestServerRunner.getTestServer().getPurchaseReversalRecords();
      RequestKey reversalKey =
            new RequestKey(username, password, RequestKey.REVERSALS_RESOURCE, purchaseReversal.getRequestId());
      reversalRecords.put(reversalKey, purchaseReversal);
   }

}
