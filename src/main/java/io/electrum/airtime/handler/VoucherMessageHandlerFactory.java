package io.electrum.airtime.handler;

public class VoucherMessageHandlerFactory {
   public static ConfirmVoucherHandler getConfirmVoucherHandler() {
      return new ConfirmVoucherHandler();
   }

   public static ProvisionVoucherHandler getProvisionVoucherHandler() {
      return new ProvisionVoucherHandler();
   }

   public static ReverseVoucherHandler getReverseVoucherHandler() {
      return new ReverseVoucherHandler();
   }
}
