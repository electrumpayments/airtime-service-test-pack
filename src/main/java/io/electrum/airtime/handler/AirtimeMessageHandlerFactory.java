package io.electrum.airtime.handler;

public class AirtimeMessageHandlerFactory {
   public static ConfirmVoucherHandler getConfirmVoucherHandler() {
      return new ConfirmVoucherHandler();
   }

   public static ProvisionVoucherHandler getProvisionVoucherHandler() {
      return new ProvisionVoucherHandler();
   }

   public static ReverseVoucherHandler getReverseVoucherHandler() {
      return new ReverseVoucherHandler();
   }

   public static PurchaseRequestHandler getPurchaseRequestHandler() {
      return new PurchaseRequestHandler();
   }

   public static PurchaseReversalHandler getPurchaseReversalHandler() {
      return new PurchaseReversalHandler();
   }

}
