package io.electrum.airtime.handler;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.electrum.airtime.api.model.ErrorDetail;
import io.electrum.airtime.api.model.VoucherRequest;
import io.electrum.airtime.api.model.VoucherResponse;
import io.electrum.airtime.api.model.ErrorDetail.ErrorType;
import io.electrum.airtime.resource.impl.AirtimeTestServer;
import io.electrum.airtime.server.AirtimeTestServerRunner;
import io.electrum.airtime.server.model.DetailMessage;
import io.electrum.airtime.server.util.RequestKey;
import io.electrum.airtime.server.util.VoucherModelUtils;

public class ProvisionVoucherHandler {
   private static final Logger log = LoggerFactory.getLogger(AirtimeTestServer.class.getPackage().getName());
   public Response handle(UUID voucherId, VoucherRequest request, HttpHeaders httpHeaders, UriInfo uriInfo) {
      try
      {
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
         if(!request.getClient().getId().equals(username))
         {
            ErrorDetail errorDetail = new ErrorDetail().errorType(ErrorType.FORMAT_ERROR).errorMessage("Incorrect username");
            DetailMessage detailMessage = new DetailMessage();
            detailMessage.setFreeString("The HTTP Basic Authentication username ("+username+") is not the same as the value in the Client.Id field ("+request.getClient().getId()+").");
            detailMessage.setClient(request.getClient());
            errorDetail.setDetailMessage(detailMessage);
            rsp = Response.status(400).entity(errorDetail).build();
            return rsp;
         }
         String password = VoucherModelUtils.getPasswordFromAuth(authString);
         RequestKey key = new RequestKey(username, password, RequestKey.VOUCHERS_RESOURCE, voucherId.toString());
         rsp = VoucherModelUtils.canProvisionVoucher(voucherId, username, password);
         if (rsp != null) {
            return rsp;
         }
         ConcurrentHashMap<RequestKey, VoucherRequest> provisionRecords =
               AirtimeTestServerRunner.getTestServer().getProvisionRecords();
         provisionRecords.put(key, request);
         VoucherResponse voucherRsp = VoucherModelUtils.voucherRspFromReq(request);
         ConcurrentHashMap<RequestKey, VoucherResponse> responseRecords =
               AirtimeTestServerRunner.getTestServer().getResponseRecords();
         responseRecords.put(key, voucherRsp);
         rsp = Response.created(uriInfo.getRequestUri()).entity(voucherRsp).build();
         return rsp;
      }
      catch (Exception e)
      {
         log.debug("error processing VoucherProvision", e);
         for(StackTraceElement ste : e.getStackTrace())
         {
            log.debug(ste.toString());
         }
         Response rsp = Response.serverError().entity(e.getMessage()).build();
         return rsp;
      }
   }
}
