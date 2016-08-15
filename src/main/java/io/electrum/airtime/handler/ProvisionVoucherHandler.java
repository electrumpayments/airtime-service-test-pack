package io.electrum.airtime.handler;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import io.electrum.airtime.api.model.VoucherRequest;
import io.electrum.airtime.api.model.VoucherResponse;
import io.electrum.airtime.server.TestServerRunner;
import io.electrum.airtime.server.util.RequestKey;
import io.electrum.airtime.server.util.VoucherModelUtils;

public class ProvisionVoucherHandler {
   public Response handle(UUID voucherId, VoucherRequest request, HttpHeaders httpHeaders, UriInfo uriInfo) {
      Response rsp = VoucherModelUtils.validateVoucherRequest(request);
      if (rsp != null) {
         return rsp;
      }
      rsp = VoucherModelUtils.isUuidConsistent(voucherId, request);
      if (rsp != null) {
         return rsp;
      }
      String authString = VoucherModelUtils.getAuthString(httpHeaders.getHeaderString(HttpHeaders.AUTHORIZATION));
      String username = VoucherModelUtils.getUsernameFromAuth(authString);
      String password = VoucherModelUtils.getPasswordFromAuth(authString);
      RequestKey key = new RequestKey(username, password, RequestKey.VOUCHERS_RESOURCE, voucherId.toString());
      rsp = VoucherModelUtils.canProvisionVoucher(voucherId, username, password);
      if (rsp != null) {
         return rsp;
      }
      ConcurrentHashMap<RequestKey, VoucherRequest> provisionRecords =
            TestServerRunner.getTestServer().getProvisionRecords();
      provisionRecords.put(key, request);
      VoucherResponse voucherRsp = VoucherModelUtils.voucherRspFromReq(request);
      ConcurrentHashMap<RequestKey, VoucherResponse> responseRecords =
            TestServerRunner.getTestServer().getResponseRecords();
      responseRecords.put(key, voucherRsp);
      rsp = Response.created(uriInfo.getRequestUri()).entity(voucherRsp).build();
      return rsp;
   }
}
