package io.electrum.airtime.handler;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.electrum.airtime.api.model.VoucherVoid;
import io.electrum.airtime.resource.impl.AirtimeTestServer;
import io.electrum.airtime.server.AirtimeTestServerRunner;
import io.electrum.airtime.server.util.RequestKey;
import io.electrum.airtime.server.util.VoucherModelUtils;

public class VoidVoucherHandler {
   private static final Logger log = LoggerFactory.getLogger(AirtimeTestServer.class.getPackage().getName());
   public Response handle(UUID voucherId, UUID voidId, VoucherVoid voidAdv, HttpHeaders httpHeaders) {
      try
      {
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
         ConcurrentHashMap<RequestKey, VoucherVoid> voidRecords = AirtimeTestServerRunner.getTestServer().getVoidRecords();
         RequestKey voidKey = new RequestKey(username, password, RequestKey.VOIDS_RESOURCE, voucherId.toString());
         // quietly overwrites any existing void
         voidRecords.put(voidKey, voidAdv);
         rsp = Response.accepted().build();
         return rsp;
      }
      catch (Exception e)
      {
         log.debug("error processing VoucherProvision", e);
         Response rsp = Response.serverError().entity(e.getMessage()).build();
         return rsp;
      }
   }
}
