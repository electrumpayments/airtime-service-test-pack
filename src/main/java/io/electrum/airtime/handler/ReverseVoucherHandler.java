package io.electrum.airtime.handler;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import io.electrum.airtime.api.model.VoucherReversal;
import io.electrum.airtime.server.TestServerRunner;
import io.electrum.airtime.server.util.RequestKey;
import io.electrum.airtime.server.util.VoucherModelUtils;

public class ReverseVoucherHandler {
   public Response handle(UUID voucherId, UUID reversalId, VoucherReversal reversal, HttpHeaders httpHeaders) {
      Response rsp = VoucherModelUtils.validateVoucherReversal(reversal);
      if (rsp != null) {
         return rsp;
      }
      rsp = VoucherModelUtils.isUuidConsistent(reversalId, reversal);
      if (rsp != null) {
         return rsp;
      }
      String authString = VoucherModelUtils.getAuthString(httpHeaders.getHeaderString(HttpHeaders.AUTHORIZATION));
      String username = VoucherModelUtils.getUsernameFromAuth(authString);
      String password = VoucherModelUtils.getPasswordFromAuth(authString);
      rsp = VoucherModelUtils.canReverseVoucher(voucherId, username, password);
      if (rsp != null) {
         if (rsp.getStatus() == 404) {
            ConcurrentHashMap<RequestKey, VoucherReversal> reversalRecords =
                  TestServerRunner.getTestServer().getReversalRecords();
            RequestKey reversalKey =
                  new RequestKey(username, password, RequestKey.REVERSALS_RESOURCE, voucherId.toString());
            // make sure to record the reversal in case we get the request late.
            reversalRecords.put(reversalKey, reversal);
         }
         return rsp;
      }
      ConcurrentHashMap<RequestKey, VoucherReversal> reversalRecords =
            TestServerRunner.getTestServer().getReversalRecords();
      RequestKey reversalKey = new RequestKey(username, password, RequestKey.REVERSALS_RESOURCE, voucherId.toString());
      // quietly overwrites any existing reversal
      reversalRecords.put(reversalKey, reversal);
      rsp = Response.accepted().build();
      return rsp;
   }
}
