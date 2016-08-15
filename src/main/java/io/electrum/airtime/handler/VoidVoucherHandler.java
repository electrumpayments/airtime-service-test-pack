package io.electrum.airtime.handler;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import io.electrum.airtime.api.model.VoucherVoid;
import io.electrum.airtime.server.TestServerRunner;
import io.electrum.airtime.server.util.RequestKey;
import io.electrum.airtime.server.util.VoucherModelUtils;

public class VoidVoucherHandler {
   public Response handle(UUID voucherId, UUID voidId, VoucherVoid voidAdv, HttpHeaders httpHeaders) {
      Response rsp = VoucherModelUtils.validateVoucherVoid(voidAdv);
      if (rsp != null) {
         return rsp;
      }
      rsp = VoucherModelUtils.isUuidConsistent(voidId, voidAdv);
      if (rsp != null) {
         return rsp;
      }
      String authString = VoucherModelUtils.getAuthString(httpHeaders.getHeaderString(HttpHeaders.AUTHORIZATION));
      String username = VoucherModelUtils.getUsernameFromAuth(authString);
      String password = VoucherModelUtils.getPasswordFromAuth(authString);
      rsp = VoucherModelUtils.canVoidVoucher(voucherId, username, password);
      if (rsp != null) {
         return rsp;
      }
      ConcurrentHashMap<RequestKey, VoucherVoid> voidRecords = TestServerRunner.getTestServer().getVoidRecords();
      RequestKey voidKey = new RequestKey(username, password, RequestKey.VOIDS_RESOURCE, voucherId.toString());
      // quietly overwrites any existing void
      voidRecords.put(voidKey, voidAdv);
      rsp = Response.accepted().build();
      return rsp;
   }
}
