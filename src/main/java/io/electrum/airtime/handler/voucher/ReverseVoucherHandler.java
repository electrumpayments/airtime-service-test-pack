package io.electrum.airtime.handler.voucher;

import static io.electrum.airtime.server.util.AirtimeModelUtils.buildAdviceResponseFromAdvice;

import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import io.electrum.airtime.api.model.ErrorDetail;
import io.electrum.airtime.handler.BaseHandler;
import io.electrum.airtime.server.AirtimeTestServerRunner;
import io.electrum.airtime.server.util.RequestKey;
import io.electrum.airtime.server.util.VoucherModelUtils;
import io.electrum.vas.model.BasicReversal;

public class ReverseVoucherHandler extends BaseHandler {
   public ReverseVoucherHandler(HttpHeaders httpHeaders) {
      super(httpHeaders);
   }

   public Response handle(String voucherId, String reversalId, BasicReversal reversal) {
      try {
         Response rsp = VoucherModelUtils.validateVoucherReversal(reversal);
         if (rsp != null) {
            return rsp;
         }

         if (!VoucherModelUtils.isUuidConsistent(reversalId, reversal.getId())) {
            return Response.status(400).entity(buildVoucherReversalErrorResponse(reversalId, reversal)).build();
         }

         rsp = VoucherModelUtils.canReverseVoucher(voucherId, reversalId, username, password);
         if (rsp != null) {
            if (rsp.getStatus() == 404) {
               // make sure to record the reversal in case we get the request late.
               addVoucherReversalToCache(reversal);
            }
            return rsp;
         }

         // quietly overwrites any existing reversal
         addVoucherReversalToCache(reversal);

         rsp = Response.accepted(buildAdviceResponseFromAdvice(reversal)).build();

         return rsp;
      } catch (Exception e) {
         return logAndBuildException(e);
      }
   }

   private void addVoucherReversalToCache(BasicReversal basicReversal) {
      ConcurrentHashMap<RequestKey, BasicReversal> reversalRecords =
            AirtimeTestServerRunner.getTestServer().getVoucherReversalRecords();
      RequestKey reversalKey =
            new RequestKey(username, password, RequestKey.REVERSALS_RESOURCE, basicReversal.getRequestId());
      reversalRecords.put(reversalKey, basicReversal);
   }

   private ErrorDetail buildVoucherReversalErrorResponse(String reversalId, BasicReversal reversal) {
      return VoucherModelUtils.buildInconsistentIdErrorDetail(
            reversalId,
            reversal.getId(),
            reversal.getRequestId(),
            ErrorDetail.RequestType.VOUCHER_REVERSAL);
   }

   @Override
   protected String getRequestName() {
      return "Voucher Reversal";
   }
}
