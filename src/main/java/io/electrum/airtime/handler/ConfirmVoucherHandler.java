package io.electrum.airtime.handler;

import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.electrum.airtime.api.model.VoucherConfirmation;
import io.electrum.airtime.resource.impl.AirtimeTestServer;
import io.electrum.airtime.server.AirtimeTestServerRunner;
import io.electrum.airtime.server.util.RequestKey;
import io.electrum.airtime.server.util.VoucherModelUtils;
import io.electrum.vas.model.BasicAdviceResponse;

public class ConfirmVoucherHandler {
   private static final Logger log = LoggerFactory.getLogger(AirtimeTestServer.class.getPackage().getName());

   public Response handle(
         String voucherId,
         String confirmationId,
         VoucherConfirmation confirmation,
         HttpHeaders httpHeaders) {
      try {
         Response rsp = VoucherModelUtils.validateVoucherConfirmation(confirmation);
         if (rsp != null) {
            return rsp;
         }
         rsp = VoucherModelUtils.isUuidConsistent(confirmationId, confirmation);
         if (rsp != null) {
            return rsp;
         }
         String authString = VoucherModelUtils.getAuthString(httpHeaders.getHeaderString(HttpHeaders.AUTHORIZATION));
         String username = VoucherModelUtils.getUsernameFromAuth(authString);
         String password = VoucherModelUtils.getPasswordFromAuth(authString);
         rsp = VoucherModelUtils.canConfirmVoucher(voucherId, confirmationId, username, password);
         if (rsp != null) {
            return rsp;
         }
         ConcurrentHashMap<RequestKey, VoucherConfirmation> confimrationRecords =
               AirtimeTestServerRunner.getTestServer().getConfirmationRecords();
         RequestKey confirmationsKey =
               new RequestKey(username, password, RequestKey.CONFIRMATIONS_RESOURCE, voucherId.toString());
         // quietly overwrites any existing confirmation
         confimrationRecords.put(confirmationsKey, confirmation);
         rsp =
               Response.accepted(
                     new BasicAdviceResponse().id(confirmation.getId())
                           .requestId(confirmation.getRequestId())
                           .time(confirmation.getTime())
                           .transactionIdentifiers(confirmation.getThirdPartyIdentifiers()))
                     .build();
         return rsp;
      } catch (Exception e) {
         log.debug("error processing VoucherConfirmation", e);
         Response rsp = Response.serverError().entity(e.getMessage()).build();
         return rsp;
      }
   }
}
