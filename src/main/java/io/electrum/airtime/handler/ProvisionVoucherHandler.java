package io.electrum.airtime.handler;

import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import io.electrum.airtime.api.model.ErrorDetail;
import io.electrum.airtime.api.model.VoucherRequest;
import io.electrum.airtime.api.model.VoucherResponse;
import io.electrum.airtime.server.AirtimeTestServerRunner;
import io.electrum.airtime.server.util.RequestKey;
import io.electrum.airtime.server.util.VoucherModelUtils;

public class ProvisionVoucherHandler extends BaseHandler {
   protected ProvisionVoucherHandler(HttpHeaders httpHeaders) {
      super(httpHeaders);
   }

   public Response handle(String voucherId, VoucherRequest request, UriInfo uriInfo) {
      try {
         Response rsp = VoucherModelUtils.validateVoucherRequest(request);
         if (rsp != null) {
            return rsp;
         }

         if (!VoucherModelUtils.isUuidConsistent(voucherId, request.getId())) {
            return Response.status(400).entity(buildVoucherRequestErrorResponse(voucherId, request)).build();
         }

         if (!request.getClient().getId().equals(username)) {
            return VoucherModelUtils.buildIncorrectUsernameErrorResponse(
                  request.getId(),
                  request.getClient(),
                  username,
                  ErrorDetail.RequestType.VOUCHER_REQUEST);
         }

         rsp = VoucherModelUtils.canProvisionVoucher(voucherId, username, password);
         if (rsp != null) {
            return rsp;
         }

         RequestKey key = new RequestKey(username, password, RequestKey.VOUCHERS_RESOURCE, voucherId);
         ConcurrentHashMap<RequestKey, VoucherRequest> provisionRecords =
               AirtimeTestServerRunner.getTestServer().getProvisionVoucherRecords();
         provisionRecords.put(key, request);

         VoucherResponse voucherRsp = VoucherModelUtils.voucherRspFromReq(request);
         ConcurrentHashMap<RequestKey, VoucherResponse> responseRecords =
               AirtimeTestServerRunner.getTestServer().getVoucherResponseRecords();
         responseRecords.put(key, voucherRsp);

         rsp = Response.created(uriInfo.getRequestUri()).entity(voucherRsp).build();

         return rsp;
      } catch (Exception e) {
         return logAndBuildException(e);
      }
   }

   private ErrorDetail buildVoucherRequestErrorResponse(String voucherId, VoucherRequest request) {
      return VoucherModelUtils
            .buildInconsistentIdErrorDetail(voucherId, request.getId(), null, ErrorDetail.RequestType.VOUCHER_REQUEST);
   }

   @Override
   protected String getRequestName() {
      return "VoucherProvision";
   }
}
