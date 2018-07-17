package io.electrum.airtime.resource.impl;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
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
   public void confirmVoucherImpl(
         String requestId,
         String confirmationId,
         VoucherConfirmation confirmation,
         SecurityContext securityContext,
         Request request,
         HttpHeaders httpHeaders,
         AsyncResponse asyncResponse,
         UriInfo uriInfo,
         HttpServletRequest httpServletRequest) {
      log.info(String.format("%s %s", httpServletRequest.getMethod(), uriInfo.getPath()));
      log.debug(String.format("%s %s\n%s", httpServletRequest.getMethod(), uriInfo.getPath(), confirmation));
      Response rsp =
            AirtimeMessageHandlerFactory.getConfirmVoucherHandler(httpHeaders)
                  .handle(requestId, confirmationId, confirmation);
      log.debug(String.format("Entity returned:\n%s", rsp.getEntity()));

      asyncResponse.resume(rsp);
   }

   @Override
   public void provisionVoucherImpl(
         String requestId,
         @Valid VoucherRequest voucherRequest,
         SecurityContext securityContext,
         Request request,
         HttpHeaders httpHeaders,
         AsyncResponse asyncResponse,
         UriInfo uriInfo,
         HttpServletRequest httpServletRequest) {
      log.info(String.format("%s %s", httpServletRequest.getMethod(), uriInfo.getPath()));
      log.debug(String.format("%s %s\n%s", httpServletRequest.getMethod(), uriInfo.getPath(), voucherRequest));
      Response rsp =
            AirtimeMessageHandlerFactory.getProvisionVoucherHandler(httpHeaders)
                  .handle(requestId, voucherRequest, uriInfo);
      log.debug(String.format("Entity returned:\n%s", rsp.getEntity()));

      asyncResponse.resume(rsp);
   }

   @Override
   public void reverseVoucherImpl(
         String requestId,
         String reversalId,
         BasicReversal reversal,
         SecurityContext securityContext,
         Request request,
         HttpHeaders httpHeaders,
         AsyncResponse asyncResponse,
         UriInfo uriInfo,
         HttpServletRequest httpServletRequest) {
      log.info(String.format("%s %s", httpServletRequest.getMethod(), uriInfo.getPath()));
      log.debug(String.format("%s %s\n%s", httpServletRequest.getMethod(), uriInfo.getPath(), reversal));
      Response rsp =
            AirtimeMessageHandlerFactory.getReverseVoucherHandler(httpHeaders).handle(requestId, reversalId, reversal);
      log.debug(String.format("Entity returned:\n%s", rsp.getEntity()));

      asyncResponse.resume(rsp);
   }
}
