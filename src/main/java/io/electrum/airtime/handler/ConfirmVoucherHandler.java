package io.electrum.airtime.handler;

import static io.electrum.airtime.server.util.AirtimeModelUtils.buildAdviceResponseFromAdvice;

import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import io.electrum.airtime.api.model.ErrorDetail;
import io.electrum.airtime.api.model.VoucherConfirmation;
import io.electrum.airtime.server.AirtimeTestServerRunner;
import io.electrum.airtime.server.util.RequestKey;
import io.electrum.airtime.server.util.VoucherModelUtils;

public class ConfirmVoucherHandler extends BaseHandler {
   protected ConfirmVoucherHandler(HttpHeaders httpHeaders) {
      super(httpHeaders);
   }

   public Response handle(String voucherId, String confirmationId, VoucherConfirmation confirmation) {
      try {
         Response rsp = VoucherModelUtils.validateVoucherConfirmation(confirmation);
         if (rsp != null) {
            return rsp;
         }

         if (!VoucherModelUtils.isUuidConsistent(confirmationId, confirmation.getId())) {
            return Response.status(400)
                  .entity(buildVoucherConfirmationErrorResponse(confirmationId, confirmation))
                  .build();
         }

         rsp = VoucherModelUtils.canConfirmVoucher(voucherId, confirmationId, username, password);
         if (rsp != null) {
            return rsp;
         }

         addVoucherConfirmationToCache(confirmation);

         rsp = Response.accepted(buildAdviceResponseFromAdvice(confirmation)).build();

         return rsp;
      } catch (Exception e) {
         return logAndBuildException(e);
      }
   }

   private ErrorDetail buildVoucherConfirmationErrorResponse(String confirmationId, VoucherConfirmation confirmation) {
      return VoucherModelUtils.buildInconsistentIdErrorDetail(
            confirmationId,
            confirmation.getId(),
            confirmation.getRequestId(),
            ErrorDetail.RequestType.VOUCHER_REVERSAL);
   }

   private void addVoucherConfirmationToCache(VoucherConfirmation confirmation) {
      ConcurrentHashMap<RequestKey, VoucherConfirmation> confirmationRecords =
            AirtimeTestServerRunner.getTestServer().getVoucherConfirmationRecords();
      RequestKey confirmationsKey =
            new RequestKey(username, password, RequestKey.CONFIRMATIONS_RESOURCE, confirmation.getRequestId());
      // quietly overwrites any existing confirmation
      confirmationRecords.put(confirmationsKey, confirmation);
   }

   @Override
   protected String getRequestName() {
      return "VoucherConfirmation";
   }
}
