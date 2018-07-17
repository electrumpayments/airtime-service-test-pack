package io.electrum.airtime.handler;

import javax.ws.rs.core.HttpHeaders;

import io.electrum.airtime.handler.msisdn.MsisdnInfoHandler;
import io.electrum.airtime.handler.purchase.PurchaseConfirmationHandler;
import io.electrum.airtime.handler.purchase.PurchaseRequestHandler;
import io.electrum.airtime.handler.purchase.PurchaseReversalHandler;
import io.electrum.airtime.handler.purchase.PurchaseStatusHandler;
import io.electrum.airtime.handler.voucher.ConfirmVoucherHandler;
import io.electrum.airtime.handler.voucher.ProvisionVoucherHandler;
import io.electrum.airtime.handler.voucher.ReverseVoucherHandler;

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

   public static PurchaseStatusHandler getPurchaseStatusHandler(HttpHeaders httpHeaders) {
      return new PurchaseStatusHandler(httpHeaders);
   }

   public static MsisdnInfoHandler getMsisdnHandler(HttpHeaders httpHeaders) {
      return new MsisdnInfoHandler(httpHeaders);
   }
}
