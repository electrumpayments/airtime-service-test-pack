package io.electrum.airtime.handler;

import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.electrum.airtime.resource.impl.AirtimeTestServer;
import io.electrum.airtime.server.AirtimeTestServerRunner;
import io.electrum.airtime.server.util.RequestKey;
import io.electrum.airtime.server.util.VoucherModelUtils;
import io.electrum.vas.model.BasicAdviceResponse;
import io.electrum.vas.model.BasicReversal;

public class ReverseVoucherHandler {
   private static final Logger log = LoggerFactory.getLogger(AirtimeTestServer.class.getPackage().getName());

   public Response handle(String voucherId, String reversalId, BasicReversal reversal, HttpHeaders httpHeaders) {
      try {
         Response rsp = VoucherModelUtils.validateBasicReversal(reversal);
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
         rsp = VoucherModelUtils.canReverseVoucher(voucherId, reversalId, username, password);
         if (rsp != null) {
            if (rsp.getStatus() == 404) {
               ConcurrentHashMap<RequestKey, BasicReversal> reversalRecords =
                     AirtimeTestServerRunner.getTestServer().getReversalRecords();
               RequestKey reversalKey =
                     new RequestKey(username, password, RequestKey.REVERSALS_RESOURCE, voucherId.toString());
               // make sure to record the reversal in case we get the request late.
               reversalRecords.put(reversalKey, reversal);
            }
            return rsp;
         }
         ConcurrentHashMap<RequestKey, BasicReversal> reversalRecords =
               AirtimeTestServerRunner.getTestServer().getReversalRecords();
         RequestKey reversalKey =
               new RequestKey(username, password, RequestKey.REVERSALS_RESOURCE, voucherId.toString());
         // quietly overwrites any existing reversal
         reversalRecords.put(reversalKey, reversal);
         rsp =
               Response
                     .accepted(
                           new BasicAdviceResponse().id(reversal.getId())
                                 .requestId(reversal.getRequestId())
                                 .time(reversal.getTime())
                                 .transactionIdentifiers(reversal.getThirdPartyIdentifiers()))
                     .build();
         return rsp;
      } catch (Exception e) {
         log.debug("error processing VoucherProvision", e);
         Response rsp = Response.serverError().entity(e.getMessage()).build();
         return rsp;
      }
   }
}
