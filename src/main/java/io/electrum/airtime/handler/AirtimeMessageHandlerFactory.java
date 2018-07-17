package io.electrum.airtime.handler;

import javax.ws.rs.core.HttpHeaders;

public class AirtimeMessageHandlerFactory {
   public static ConfirmVoucherHandler getConfirmVoucherHandler(HttpHeaders httpHeaders) {
      return new ConfirmVoucherHandler(httpHeaders);
   }

   public static ProvisionVoucherHandler getProvisionVoucherHandler(HttpHeaders httpHeaders) {
      return new ProvisionVoucherHandler(httpHeaders);
   }

   public static ReverseVoucherHandler getReverseVoucherHandler(HttpHeaders httpHeaders) {
      return new ReverseVoucherHandler(httpHeaders);
   }

   public static PurchaseRequestHandler getPurchaseRequestHandler(HttpHeaders httpHeaders) {
      return new PurchaseRequestHandler(httpHeaders);
   }

   public static PurchaseReversalHandler getPurchaseReversalHandler(HttpHeaders httpHeaders) {
      return new PurchaseReversalHandler(httpHeaders);
   }

   public static PurchaseConfirmationHandler getPurchaseConfirmationHandler(HttpHeaders httpHeaders) {
      return new PurchaseConfirmationHandler(httpHeaders);
   }
}
