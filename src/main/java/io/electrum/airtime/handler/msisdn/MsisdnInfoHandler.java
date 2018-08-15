package io.electrum.airtime.handler.msisdn;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import io.electrum.airtime.api.model.MsisdnInfoResponse;
import io.electrum.airtime.handler.BaseHandler;
import io.electrum.airtime.server.util.MsisdnInfoModelUtils;

public class MsisdnInfoHandler extends BaseHandler {
   public MsisdnInfoHandler(HttpHeaders httpHeaders) {
      super(httpHeaders);
   }

   public Response handle(String msisdn, String operator) {
      try {
         Response rsp = MsisdnInfoModelUtils.canMsisdnInfoRequest(msisdn);
         if (rsp != null) {
            return rsp;
         }

         MsisdnInfoResponse msisdnInfoResponse = MsisdnInfoModelUtils.msisdnInfoResponseFromMsisdn(msisdn, operator);

         rsp = Response.accepted(msisdnInfoResponse).build();

         return rsp;
      } catch (Exception e) {
         return logAndBuildException(e);
      }
   }

   @Override
   protected String getRequestName() {
      return "Msisdn Info Request";
   }
}
