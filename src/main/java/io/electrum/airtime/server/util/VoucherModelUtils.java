package io.electrum.airtime.server.util;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.validation.ConstraintViolation;
import javax.ws.rs.core.Response;

import io.electrum.airtime.api.model.ErrorDetail;
import io.electrum.airtime.api.model.ErrorDetail.ErrorType;
import io.electrum.airtime.api.model.Product;
import io.electrum.airtime.api.model.VoucherConfirmation;
import io.electrum.airtime.api.model.VoucherRequest;
import io.electrum.airtime.api.model.VoucherResponse;
import io.electrum.airtime.server.AirtimeTestServerRunner;
import io.electrum.airtime.server.model.DetailMessage;
import io.electrum.vas.JsonUtil;
import io.electrum.vas.model.BasicReversal;

public class VoucherModelUtils extends AirtimeModelUtils {

   public static VoucherResponse voucherRspFromReq(VoucherRequest req) throws IOException {
      VoucherResponse voucherResponse =
            JsonUtil.deserialize(JsonUtil.serialize(req, VoucherRequest.class), VoucherResponse.class);

      updateWithRandomizedIdentifiers(voucherResponse);
      voucherResponse.setVoucher(createRandomizedVoucher());
      voucherResponse.setSlipData(createRandomizedSlipData());
      voucherResponse.setResponseProduct(req.getProduct().name("TalkALot").type(Product.ProductType.AIRTIME_FIXED));

      return voucherResponse;
   }

   public static ErrorDetail productNotRecognised(VoucherRequest req) {
      ErrorDetail errorDetail = new ErrorDetail().id(req.getId()).requestType(ErrorDetail.RequestType.VOUCHER_REQUEST);
      errorDetail.setErrorType(ErrorType.INVALID_PRODUCT);
      errorDetail.setErrorMessage("Unknown product");
      DetailMessage detailMessage = new DetailMessage();
      detailMessage.setFreeString("This MNO does not recognise the product requested.");
      detailMessage.setProduct(req.getProduct());
      detailMessage.setReceiver(req.getReceiver());
      return errorDetail;
   }

   public static void validateVoucherRequest(VoucherRequest voucherRequest, Set<ConstraintViolation<?>> violations) {
      violations.addAll(validate(voucherRequest));
      if (voucherRequest != null) {
         validateTransaction(voucherRequest, violations);
         violations.addAll(validate(voucherRequest.getProduct()));
         validateAmounts(violations, voucherRequest.getAmounts());
         violations.addAll(validate(voucherRequest.getTenders()));
         violations.addAll(validate(voucherRequest.getPaymentMethods()));
      }
   }

   public static Response validateVoucherRequest(VoucherRequest voucherRequest) {
      Set<ConstraintViolation<?>> violations = new HashSet<ConstraintViolation<?>>();
      validateVoucherRequest(voucherRequest, violations);
      ErrorDetail errorDetail = buildFormatErrorRsp(violations);
      if (errorDetail == null) {
         return null;
      }
      errorDetail.id(voucherRequest.getId()).requestType(ErrorDetail.RequestType.VOUCHER_REQUEST);
      return Response.status(400).entity(errorDetail).build();
   }

   public static Response validateVoucherReversal(BasicReversal reversal) {
      Set<ConstraintViolation<?>> violations = new HashSet<ConstraintViolation<?>>();
      validateBasicReversal(reversal, violations);
      ErrorDetail errorDetail = buildFormatErrorRsp(violations);
      if (errorDetail == null) {
         return null;
      }
      errorDetail.id(reversal.getId()).originalId(reversal.getRequestId()).requestType(
            ErrorDetail.RequestType.VOUCHER_REVERSAL);
      return Response.status(400).entity(errorDetail).build();
   }

   public static Response validateVoucherConfirmation(VoucherConfirmation confirmation) {
      Set<ConstraintViolation<?>> violations = new HashSet<ConstraintViolation<?>>();
      validateTenderAdvice(confirmation, violations);
      violations.addAll(validate(confirmation.getVoucher()));

      ErrorDetail errorDetail = buildFormatErrorRsp(violations);
      if (errorDetail == null) {
         return null;
      }
      errorDetail.id(confirmation.getId()).originalId(confirmation.getRequestId()).requestType(
            ErrorDetail.RequestType.VOUCHER_CONFIRMATION);
      return Response.status(400).entity(errorDetail).build();
   }

   public static Response canProvisionVoucher(String voucherId, String username, String password) {
      ConcurrentHashMap<RequestKey, VoucherRequest> provisionRecords =
            AirtimeTestServerRunner.getTestServer().getProvisionVoucherRecords();
      RequestKey requestKey = new RequestKey(username, password, RequestKey.VOUCHERS_RESOURCE, voucherId);
      VoucherRequest originalRequest = provisionRecords.get(requestKey);
      if (originalRequest != null) {
         ErrorDetail errorDetail =
               buildDuplicateErrorDetail(voucherId, null, ErrorDetail.RequestType.VOUCHER_REQUEST, originalRequest);

         DetailMessage detailMessage = (DetailMessage) errorDetail.getDetailMessage();
         detailMessage.setProduct(originalRequest.getProduct());
         ConcurrentHashMap<RequestKey, VoucherResponse> responseRecords =
               AirtimeTestServerRunner.getTestServer().getVoucherResponseRecords();
         VoucherResponse rsp = responseRecords.get(requestKey);
         if (rsp != null) {
            detailMessage.setVoucher(rsp.getVoucher());
         }
         return Response.status(400).entity(errorDetail).build();
      }

      ConcurrentHashMap<RequestKey, BasicReversal> reversalRecords =
            AirtimeTestServerRunner.getTestServer().getVoucherReversalRecords();
      RequestKey reversalKey = new RequestKey(username, password, RequestKey.REVERSALS_RESOURCE, voucherId);
      BasicReversal reversal = reversalRecords.get(reversalKey);
      if (reversal != null) {
         ErrorDetail errorDetail =
               buildErrorDetail(
                     voucherId,
                     "Voucher reversed.",
                     "Voucher reversal with String already processed with the associated fields.",
                     reversal.getId(),
                     ErrorDetail.RequestType.VOUCHER_REQUEST,
                     ErrorDetail.ErrorType.ACCOUNT_ALREADY_SETTLED);

         DetailMessage detailMessage = (DetailMessage) errorDetail.getDetailMessage();
         ConcurrentHashMap<RequestKey, VoucherResponse> responseRecords =
               AirtimeTestServerRunner.getTestServer().getVoucherResponseRecords();
         VoucherResponse rsp = responseRecords.get(requestKey);
         if (rsp != null) {
            detailMessage.setVoucher(rsp.getVoucher());
         }
         return Response.status(400).entity(errorDetail).build();
      }
      return null;
   }

   public static Response canReverseVoucher(String voucherId, String reversalId, String username, String password) {
      ErrorDetail errorDetail =
            new ErrorDetail().id(reversalId).originalId(voucherId).requestType(
                  ErrorDetail.RequestType.VOUCHER_REVERSAL);
      ConcurrentHashMap<RequestKey, VoucherRequest> provisionRecords =
            AirtimeTestServerRunner.getTestServer().getProvisionVoucherRecords();
      if (!isVoucherProvisioned(voucherId, provisionRecords, username, password)) {
         errorDetail.errorType(ErrorType.UNABLE_TO_LOCATE_RECORD).errorMessage("No voucher req.").detailMessage(
               new DetailMessage().freeString("No VoucherRequest located for given voucherId.").voucherId(voucherId));
         return Response.status(404).entity(errorDetail).build();
      }

      // check it's not confirmed
      ConcurrentHashMap<RequestKey, VoucherConfirmation> confirmationRecords =
            AirtimeTestServerRunner.getTestServer().getVoucherConfirmationRecords();
      RequestKey confirmKey =
            new RequestKey(username, password, RequestKey.CONFIRMATIONS_RESOURCE, voucherId.toString());
      VoucherConfirmation confirmation = confirmationRecords.get(confirmKey);
      if (confirmation != null) {
         errorDetail.errorType(ErrorType.ACCOUNT_ALREADY_SETTLED).errorMessage("Voucher confirmed.").detailMessage(
               new DetailMessage().freeString(
                     "The voucher cannot be reversed as it has already been confirmed with the associated details.")
                     .confirmationId(confirmation.getId())
                     .voucher(confirmation.getVoucher()));
         return Response.status(400).entity(errorDetail).build();
      }
      return null;
   }

   public static Response canConfirmVoucher(String voucherId, String confirmationId, String username, String password) {
      ErrorDetail errorDetail =
            new ErrorDetail().id(confirmationId).originalId(voucherId).requestType(
                  ErrorDetail.RequestType.VOUCHER_CONFIRMATION);
      ConcurrentHashMap<RequestKey, VoucherRequest> provisionRecords =
            AirtimeTestServerRunner.getTestServer().getProvisionVoucherRecords();
      // check voucher was provisioned
      if (!isVoucherProvisioned(voucherId, provisionRecords, username, password)) {
         errorDetail.errorType(ErrorType.UNABLE_TO_LOCATE_RECORD).errorMessage("No voucher req.").detailMessage(
               new DetailMessage().freeString("No VoucherRequest located for given voucherId.").voucherId(voucherId));
         return Response.status(404).entity(errorDetail).build();
      }

      // check it's not reversed
      ConcurrentHashMap<RequestKey, BasicReversal> reversalRecords =
            AirtimeTestServerRunner.getTestServer().getVoucherReversalRecords();
      RequestKey reversalsKey = new RequestKey(username, password, RequestKey.REVERSALS_RESOURCE, voucherId.toString());
      BasicReversal reversal = reversalRecords.get(reversalsKey);
      if (reversal != null) {
         errorDetail.errorType(ErrorType.ACCOUNT_ALREADY_SETTLED).errorMessage("Voucher reversed.").detailMessage(
               new DetailMessage()
                     .freeString("Voucher provision has already been reversed with the associated details.")
                     .reversalId(reversal.getId()));
         return Response.status(400).entity(errorDetail).build();
      }
      return null;
   }

   public static Response canVoidVoucher(String voucherId, String voidId, String username, String password) {
      ErrorDetail errorDetail =
            new ErrorDetail().id(voidId).originalId(voucherId).requestType(ErrorDetail.RequestType.VOUCHER_VOID);
      ConcurrentHashMap<RequestKey, VoucherRequest> provisionRecords =
            AirtimeTestServerRunner.getTestServer().getProvisionVoucherRecords();
      // check voucher was provisioned
      if (!isVoucherProvisioned(voucherId, provisionRecords, username, password)) {
         errorDetail.errorType(ErrorType.UNABLE_TO_LOCATE_RECORD).errorMessage("No voucher req.").detailMessage(
               new DetailMessage().freeString("No VoucherRequest located for given voucherId.").voucherId(voucherId));
         return Response.status(404).entity(errorDetail).build();
      }

      // check it's not reversed
      ConcurrentHashMap<RequestKey, BasicReversal> reversalRecords =
            AirtimeTestServerRunner.getTestServer().getVoucherReversalRecords();
      RequestKey reversalsKey = new RequestKey(username, password, RequestKey.REVERSALS_RESOURCE, voucherId.toString());
      BasicReversal reversal = reversalRecords.get(reversalsKey);
      if (reversal != null) {
         errorDetail.errorType(ErrorType.ACCOUNT_ALREADY_SETTLED).errorMessage("Voucher reversed.").detailMessage(
               new DetailMessage()
                     .freeString("Voucher provision has already been reversed with the associated details.")
                     .reversalId(reversal.getId()));
         return Response.status(400).entity(errorDetail).build();
      }

      // check it's not confirmed
      ConcurrentHashMap<RequestKey, VoucherConfirmation> confirmationRecords =
            AirtimeTestServerRunner.getTestServer().getVoucherConfirmationRecords();
      RequestKey confirmKey =
            new RequestKey(username, password, RequestKey.CONFIRMATIONS_RESOURCE, voucherId.toString());
      VoucherConfirmation confirmation = confirmationRecords.get(confirmKey);
      if (confirmation != null) {
         errorDetail.errorType(ErrorType.ACCOUNT_ALREADY_SETTLED).errorMessage("Voucher confirmed.").detailMessage(
               new DetailMessage()
                     .freeString("Voucher provision has already been confirmed with the associated details.")
                     .confirmationId(confirmation.getId())
                     .voucher(confirmation.getVoucher()));
         return Response.status(400).entity(errorDetail).build();
      }
      return null;
   }

   public static boolean isVoucherProvisioned(
         String voucherId,
         ConcurrentHashMap<RequestKey, VoucherRequest> provisionRecords,
         String username,
         String password) {
      RequestKey provisionKey = new RequestKey(username, password, RequestKey.VOUCHERS_RESOURCE, voucherId.toString());
      log.debug(String.format("Searching for provision record under following key: %s", provisionKey.toString()));
      return provisionRecords.get(provisionKey) != null;
   }

}
