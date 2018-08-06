package io.electrum.airtime.resource.impl;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.electrum.airtime.api.IVouchersResource;
import io.electrum.airtime.api.VouchersResource;
import io.electrum.airtime.api.model.VoucherConfirmation;
import io.electrum.airtime.api.model.VoucherRequest;
import io.electrum.airtime.handler.AirtimeMessageHandlerFactory;
import io.electrum.vas.model.BasicReversal;
import io.swagger.annotations.Api;
import io.swagger.annotations.Authorization;

@Path("/airtime/v5/vouchers")
@Api(description = "the Airtime API", authorizations = { @Authorization("httpBasic") })
public class VouchersResourceImpl extends VouchersResource implements IVouchersResource {

   static VouchersResourceImpl instance = null;
   private static final Logger log = LoggerFactory.getLogger(AirtimeTestServer.class.getPackage().getName());

   @Override
   protected IVouchersResource getResourceImplementation() {
      if (instance == null) {
         instance = new VouchersResourceImpl();
      }
      return instance;
   }

   @Override
   public void provisionVoucherImpl(
         String voucherId,
         VoucherRequest body,
         SecurityContext securityContext,
         Request request,
         HttpHeaders httpHeaders,
         AsyncResponse asyncResponse,
         UriInfo uriInfo,
         HttpServletRequest httpServletRequest) {
      log.info(String.format("%s %s", httpServletRequest.getMethod(), uriInfo.getPath()));
      log.debug(String.format("%s %s\n%s", httpServletRequest.getMethod(), uriInfo.getPath(), body));
      Response rsp =
            AirtimeMessageHandlerFactory.getProvisionVoucherHandler(httpHeaders).handle(voucherId, body, uriInfo);
      log.debug(String.format("Entity returned:\n%s", rsp.getEntity()));

      asyncResponse.resume(rsp);
   }

   @Override
   public void reverseVoucherImpl(
         String voucherId,
         String reversalId,
         BasicReversal body,
         SecurityContext securityContext,
         Request request,
         HttpHeaders httpHeaders,
         AsyncResponse asyncResponse,
         UriInfo uriInfo,
         HttpServletRequest httpServletRequest) {
      log.info(String.format("%s %s", httpServletRequest.getMethod(), uriInfo.getPath()));
      log.debug(String.format("%s %s\n%s", httpServletRequest.getMethod(), uriInfo.getPath(), body));
      Response rsp =
            AirtimeMessageHandlerFactory.getReverseVoucherHandler(httpHeaders).handle(voucherId, reversalId, body);
      log.debug(String.format("Entity returned:\n%s", rsp.getEntity()));

      asyncResponse.resume(rsp);
   }

   @Override
   public void confirmVoucherImpl(
         String voucherId,
         String confirmationId,
         VoucherConfirmation body,
         SecurityContext securityContext,
         Request request,
         HttpHeaders httpHeaders,
         AsyncResponse asyncResponse,
         UriInfo uriInfo,
         HttpServletRequest httpServletRequest) {
      log.info(String.format("%s %s", httpServletRequest.getMethod(), uriInfo.getPath()));
      log.debug(String.format("%s %s\n%s", httpServletRequest.getMethod(), uriInfo.getPath(), body));
      Response rsp =
            AirtimeMessageHandlerFactory.getConfirmVoucherHandler(httpHeaders).handle(voucherId, confirmationId, body);
      log.debug(String.format("Entity returned:\n%s", rsp.getEntity()));

      asyncResponse.resume(rsp);
   }
}
