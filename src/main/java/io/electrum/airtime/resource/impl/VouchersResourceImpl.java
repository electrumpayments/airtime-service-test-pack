package io.electrum.airtime.resource.impl;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.ws.rs.Path;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.electrum.airtime.api.IVouchersResource;
import io.electrum.airtime.api.VouchersResource;
import io.electrum.airtime.api.model.VoucherConfirmation;
import io.electrum.airtime.api.model.VoucherRequest;
import io.electrum.airtime.api.model.VoucherReversal;
import io.electrum.airtime.api.model.VoucherVoid;
import io.electrum.airtime.handler.VoucherMessageHandlerFactory;
import io.swagger.annotations.Api;
import io.swagger.annotations.Authorization;

@Path("/airtime/v3/vouchers")
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
   public Response confirmVoucherImpl(
         UUID voucherId,
         UUID confirmationId,
         VoucherConfirmation confirmation,
         SecurityContext securityContext,
         HttpHeaders httpHeaders,
         UriInfo uriInfo,
         HttpServletRequest httpServletRequest) {
      log.info(String.format("%s %s", httpServletRequest.getMethod(), uriInfo.getPath()));
      log.debug(String.format("%s %s\n%s", httpServletRequest.getMethod(), uriInfo.getPath(), confirmation));
      Response rsp =
            VoucherMessageHandlerFactory.getConfirmVoucherHandler()
                  .handle(voucherId, confirmationId, confirmation, httpHeaders);
      log.debug(String.format("Entity returned:\n%s", rsp.getEntity()));
      return rsp;
   }

   @Override
   public Response provisionVoucherImpl(
         UUID voucherId,
         @Valid VoucherRequest request,
         SecurityContext securityContext,
         HttpHeaders httpHeaders,
         UriInfo uriInfo,
         HttpServletRequest httpServletRequest) {
      log.info(String.format("%s %s", httpServletRequest.getMethod(), uriInfo.getPath()));
      log.debug(String.format("%s %s\n%s", httpServletRequest.getMethod(), uriInfo.getPath(), request));
      Response rsp =
            VoucherMessageHandlerFactory.getProvisionVoucherHandler().handle(voucherId, request, httpHeaders, uriInfo);
      log.debug(String.format("Entity returned:\n%s", rsp.getEntity()));
      return rsp;
   }

   @Override
   public Response reverseVoucherImpl(
         UUID voucherId,
         UUID reversalId,
         VoucherReversal reversal,
         SecurityContext securityContext,
         HttpHeaders httpHeaders,
         UriInfo uriInfo,
         HttpServletRequest httpServletRequest) {
      log.info(String.format("%s %s", httpServletRequest.getMethod(), uriInfo.getPath()));
      log.debug(String.format("%s %s\n%s", httpServletRequest.getMethod(), uriInfo.getPath(), reversal));
      Response rsp =
            VoucherMessageHandlerFactory.getReverseVoucherHandler()
                  .handle(voucherId, reversalId, reversal, httpHeaders);
      log.debug(String.format("Entity returned:\n%s", rsp.getEntity()));
      return rsp;
   }

   @Override
   public Response voidVoucherImpl(
         UUID voucherId,
         UUID voidId,
         VoucherVoid voidAdv,
         SecurityContext securityContext,
         HttpHeaders httpHeaders,
         UriInfo uriInfo,
         HttpServletRequest httpServletRequest) {
      log.info(String.format("%s %s", httpServletRequest.getMethod(), uriInfo.getPath()));
      log.debug(String.format("%s %s\n%s", httpServletRequest.getMethod(), uriInfo.getPath(), voidAdv));
      Response rsp =
            VoucherMessageHandlerFactory.getVoidVoucherHandler().handle(voucherId, voidId, voidAdv, httpHeaders);
      log.debug(String.format("Entity returned:\n%s", rsp.getEntity()));
      return rsp;
   }
}
