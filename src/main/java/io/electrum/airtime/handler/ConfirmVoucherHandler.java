package io.electrum.airtime.handler;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import io.electrum.airtime.api.model.VoucherConfirmation;
import io.electrum.airtime.server.TestServerRunner;
import io.electrum.airtime.server.util.RequestKey;
import io.electrum.airtime.server.util.VoucherModelUtils;

public class ConfirmVoucherHandler {
   public Response handle(
         UUID voucherId,
         UUID confirmationId,
         VoucherConfirmation confirmation,
         HttpHeaders httpHeaders) {
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
      rsp = VoucherModelUtils.canConfirmVoucher(voucherId, username, password);
      if (rsp != null) {
         return rsp;
      }
      ConcurrentHashMap<RequestKey, VoucherConfirmation> confimrationRecords =
            TestServerRunner.getTestServer().getConfirmationRecords();
      RequestKey confirmationsKey =
            new RequestKey(username, password, RequestKey.CONFIRMATIONS_RESOURCE, voucherId.toString());
      // quietly overwrites any existing confirmation
      confimrationRecords.put(confirmationsKey, confirmation);
      rsp = Response.accepted().build();
      return rsp;
   }
}
