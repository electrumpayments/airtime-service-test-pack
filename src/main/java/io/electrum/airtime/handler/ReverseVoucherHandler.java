package io.electrum.airtime.handler;

import static io.electrum.airtime.server.util.AirtimeModelUtils.buildAdviceResponseFromAdvice;

import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.electrum.airtime.api.model.ErrorDetail;
import io.electrum.airtime.resource.impl.AirtimeTestServer;
import io.electrum.airtime.server.AirtimeTestServerRunner;
import io.electrum.airtime.server.util.AirtimeModelUtils;
import io.electrum.airtime.server.util.RequestKey;
import io.electrum.airtime.server.util.VoucherModelUtils;
import io.electrum.vas.model.BasicReversal;

public class ReverseVoucherHandler {
   private static final Logger log = LoggerFactory.getLogger(AirtimeTestServer.class.getPackage().getName());

   public Response handle(String voucherId, String reversalId, BasicReversal reversal, HttpHeaders httpHeaders) {
      try {
         Response rsp = VoucherModelUtils.validateVoucherReversal(reversal);
         if (rsp != null) {
            return rsp;
         }

         if (!VoucherModelUtils.isUuidConsistent(reversalId, reversal.getId())) {
            return Response.status(400).entity(buildVoucherReversalErrorResponse(reversalId, reversal)).build();
         }

         String authString = AirtimeModelUtils.getAuthString(httpHeaders.getHeaderString(HttpHeaders.AUTHORIZATION));
         String username = AirtimeModelUtils.getUsernameFromAuth(authString);
         String password = AirtimeModelUtils.getPasswordFromAuth(authString);

         rsp = VoucherModelUtils.canReverseVoucher(voucherId, reversalId, username, password);
         if (rsp != null) {
            if (rsp.getStatus() == 404) {
               // make sure to record the reversal in case we get the request late.
               addVoucherReversalToCache(reversal, username, password);
            }
            return rsp;
         }

         // quietly overwrites any existing reversal
         addVoucherReversalToCache(reversal, username, password);

         rsp = Response.accepted(buildAdviceResponseFromAdvice(reversal)).build();

         return rsp;
      } catch (Exception e) {
         log.debug("error processing VoucherProvision", e);
         Response rsp = Response.serverError().entity(e.getMessage()).build();
         return rsp;
      }
   }

   private void addVoucherReversalToCache(BasicReversal basicReversal, String username, String password) {
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
}
