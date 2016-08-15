package io.electrum.airtime.server.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.internal.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.electrum.airtime.api.model.ErrorDetail;
import io.electrum.airtime.api.model.ErrorDetail.ErrorTypeEnum;
import io.electrum.airtime.api.model.Institution;
import io.electrum.airtime.api.model.Merchant;
import io.electrum.airtime.api.model.SlipData;
import io.electrum.airtime.api.model.Voucher;
import io.electrum.airtime.api.model.VoucherConfirmation;
import io.electrum.airtime.api.model.VoucherRequest;
import io.electrum.airtime.api.model.VoucherResponse;
import io.electrum.airtime.api.model.VoucherReversal;
import io.electrum.airtime.api.model.VoucherVoid;
import io.electrum.airtime.resource.impl.TestServer;
import io.electrum.airtime.server.TestServerRunner;
import io.electrum.airtime.server.model.DetailMessage;
import io.electrum.airtime.server.model.FormatError;

public class VoucherModelUtils {
   private static List<String> redeemInstructions = new ArrayList<String>();
   private static List<String> messageLines = new ArrayList<String>();
   private static final Logger log = LoggerFactory.getLogger(TestServer.class.getPackage().getName());
   static {
      redeemInstructions.add("To redeem your airtime");
      redeemInstructions.add("enter the USSD code below:");
      redeemInstructions.add("*999*<pin>#");
      messageLines.add("Use the vendor reference");
      messageLines.add("number to query your network");
      messageLines.add("operator.");
   }

   public static VoucherResponse voucherRspFromReq(VoucherRequest req) {
      VoucherResponse rsp = new VoucherResponse();
      rsp.setMerchant(req.getMerchant());
      rsp.setRequestTime(req.getRequestTime());
      Institution vendor = req.getVendor();
      vendor.setReference(RandomData.random09AZ((int) ((Math.random() * 20)+1)));
      rsp.setVendor(req.getVendor());
      rsp.setVoucherId(req.getVoucherId());
      Voucher voucher = new Voucher();
      voucher.setPin(RandomData.random09((int) ((Math.random() * 20)+1)));
      voucher.setSerialNumber(RandomData.random09((int) ((Math.random() * 20)+1)));
      voucher.setBatchNumber(RandomData.random09((int) ((Math.random() * 20)+1)));
      voucher.setRedeemInstructions(redeemInstructions);
      rsp.setVoucher(voucher);
      SlipData slipData = new SlipData();
      slipData.setMessageLines(messageLines);
      slipData.setVendorReference(RandomData.random09AZ((int) ((Math.random() * 20)+1)));
      rsp.setSlipData(slipData);
      Institution processor = req.getProcessor();
      if (processor == null) {
         processor = new Institution();
         processor.setId("33333333");
         processor.setName("TransactionsRUs");
      }
      processor.setReference(RandomData.random09AZ((int) ((Math.random() * 20)+1)));
      rsp.setProcessor(processor);
      return rsp;
   }

   public static ErrorDetail productNotRecognised(VoucherRequest req) {
      ErrorDetail errorDetail = new ErrorDetail();
      errorDetail.setErrorType(ErrorTypeEnum.INVALID_PRODUCT);
      errorDetail.setErrorMessage("Unknown product");
      DetailMessage detailMessage = new DetailMessage();
      detailMessage.setFreeString("This MNO does not recognise the product requested.");
      detailMessage.setProduct(req.getProduct());
      Institution vendor = req.getVendor();
      vendor.setReference(RandomData.random09AZ((int) (Math.random() * 21)));
      detailMessage.setVendor(req.getVendor());
      return errorDetail;
   }

   private static <T> Set<ConstraintViolation<T>> validate(T tInstance) {
      if(tInstance == null)
      {
         return new HashSet<ConstraintViolation<T>>();
      }
      Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
      Set<ConstraintViolation<T>> violations = validator.validate(tInstance);
      return violations;
   }

   public static void validateVoucherRequest(VoucherRequest voucherRequest, Set<ConstraintViolation<?>> violations) {
      violations.addAll(validate(voucherRequest));
      Merchant merchant = voucherRequest.getMerchant();
      violations.addAll(validate(merchant));
      if(merchant != null)
      {
         violations.addAll(validate(merchant.getInstitution()));
      }
      violations.addAll(validate(voucherRequest.getProcessor()));
      violations.addAll(validate(voucherRequest.getProduct()));
      violations.addAll(validate(voucherRequest.getRequestTime()));
      violations.addAll(validate(voucherRequest.getSender()));
      violations.addAll(validate(voucherRequest.getVendor()));
      violations.addAll(validate(voucherRequest.getVoucherId()));
   }

   public static Response validateVoucherRequest(VoucherRequest voucherRequest) {
      Set<ConstraintViolation<?>> violations = new HashSet<ConstraintViolation<?>>();
      validateVoucherRequest(voucherRequest, violations);
      return buildFormatErrorRsp(violations);
   }

   public static Response validateVoucherReversal(VoucherReversal voucherReversal) {
      Set<ConstraintViolation<?>> violations = new HashSet<ConstraintViolation<?>>();
      violations.addAll(validate(voucherReversal));
      violations.addAll(validate(voucherReversal.getMerchant()));
      violations.addAll(validate(voucherReversal.getMerchant().getInstitution()));
      violations.addAll(validate(voucherReversal.getProcessor()));
      violations.addAll(validate(voucherReversal.getReversalTime()));
      violations.addAll(validate(voucherReversal.getSender()));
      violations.addAll(validate(voucherReversal.getVendor()));
      violations.addAll(validate(voucherReversal.getReversalId()));
      validateVoucherRequest(voucherReversal.getVoucherRequest(), violations);
      return buildFormatErrorRsp(violations);
   }

   public static Response validateVoucherConfirmation(VoucherConfirmation voucherConfirmation) {
      Set<ConstraintViolation<?>> violations = new HashSet<ConstraintViolation<?>>();
      violations.addAll(validate(voucherConfirmation));
      violations.addAll(validate(voucherConfirmation.getMerchant()));
      violations.addAll(validate(voucherConfirmation.getMerchant().getInstitution()));
      violations.addAll(validate(voucherConfirmation.getProcessor()));
      violations.addAll(validate(voucherConfirmation.getConfirmDate()));
      violations.addAll(validate(voucherConfirmation.getSender()));
      violations.addAll(validate(voucherConfirmation.getVendor()));
      violations.addAll(validate(voucherConfirmation.getVoucher()));
      violations.addAll(validate(voucherConfirmation.getConfirmationId()));
      return buildFormatErrorRsp(violations);
   }

   public static Response validateVoucherVoid(VoucherVoid voucherVoid) {
      Set<ConstraintViolation<?>> violations = new HashSet<ConstraintViolation<?>>();
      violations.addAll(validate(voucherVoid));
      violations.addAll(validate(voucherVoid.getMerchant()));
      violations.addAll(validate(voucherVoid.getMerchant().getInstitution()));
      violations.addAll(validate(voucherVoid.getProcessor()));
      violations.addAll(validate(voucherVoid.getVoidDate()));
      violations.addAll(validate(voucherVoid.getSender()));
      violations.addAll(validate(voucherVoid.getVendor()));
      violations.addAll(validate(voucherVoid.getVoucher()));
      violations.addAll(validate(voucherVoid.getVoidId()));
      return buildFormatErrorRsp(violations);
   }

   private static Response buildFormatErrorRsp(Set<ConstraintViolation<?>> violations) {
      if (violations.size() == 0) {
         return null;
      }
      List<FormatError> formatErrors = new ArrayList<FormatError>(violations.size());
      int i = 0;
      for (ConstraintViolation violation : violations) {
         System.out.println(i);
         formatErrors.add(
               new FormatError().msg(violation.getMessage())
                     .field(violation.getPropertyPath().toString())
                     .value(violation.getInvalidValue() == null ? "null" : violation.getInvalidValue().toString()));
         i++;
      }
      ErrorDetail errorDetail =
            new ErrorDetail().errorType(ErrorTypeEnum.FORMAT_ERROR)
                  .errorMessage("Bad formatting")
                  .detailMessage(new DetailMessage().formatErrors(formatErrors));
      return Response.status(400).entity(errorDetail).build();
   }

   public static Response isUuidConsistent(UUID uuid, VoucherRequest voucherReq) {
      Response rsp = null;
      String pathId = uuid.toString();
      String objectId = voucherReq.getVoucherId();
      ErrorDetail errorDetail = isUuidConsistent(pathId, objectId);
      if (errorDetail != null) {
         DetailMessage detailMessage = (DetailMessage) errorDetail.getDetailMessage();
         detailMessage.setVoucherId(objectId);
         rsp = Response.status(400).entity(errorDetail).build();
      }
      return rsp;
   }

   public static Response isUuidConsistent(UUID uuid, VoucherReversal voucherRev) {
      Response rsp = null;
      String pathId = uuid.toString();
      String objectId = voucherRev.getReversalId();
      ErrorDetail errorDetail = isUuidConsistent(pathId, objectId);
      if (errorDetail != null) {
         DetailMessage detailMessage = (DetailMessage) errorDetail.getDetailMessage();
         detailMessage.setReversalId(objectId);
         rsp = Response.status(400).entity(errorDetail).build();
      }
      return rsp;
   }

   public static Response isUuidConsistent(UUID uuid, VoucherConfirmation voucherConfirmation) {
      Response rsp = null;
      String pathId = uuid.toString();
      String objectId = voucherConfirmation.getConfirmationId();
      ErrorDetail errorDetail = isUuidConsistent(pathId, objectId);
      if (errorDetail != null) {
         DetailMessage detailMessage = (DetailMessage) errorDetail.getDetailMessage();
         detailMessage.setConfirmationId(objectId);
         rsp = Response.status(400).entity(errorDetail).build();
      }
      return rsp;
   }

   public static Response isUuidConsistent(UUID uuid, VoucherVoid voucherVoid) {
      Response rsp = null;
      String pathId = uuid.toString();
      String objectId = voucherVoid.getVoidId();
      ErrorDetail errorDetail = isUuidConsistent(pathId, objectId);
      if (errorDetail != null) {
         DetailMessage detailMessage = (DetailMessage) errorDetail.getDetailMessage();
         detailMessage.setVoidId(objectId);
         rsp = Response.status(400).entity(errorDetail).build();
      }
      return rsp;
   }

   public static ErrorDetail isUuidConsistent(String pathId, String objectId) {
      ErrorDetail errorDetail = null;
      if (!pathId.equals(objectId)) {
         errorDetail = new ErrorDetail().errorType(ErrorTypeEnum.FORMAT_ERROR).errorMessage("UUID inconsistent");
         DetailMessage detailMessage = new DetailMessage();
         detailMessage.setPathId(pathId);
         detailMessage.setFreeString("The ID path parameter is not the same as the object's ID.");
         errorDetail.setDetailMessage(detailMessage);
      }
      return errorDetail;
   }

   public static Response canProvisionVoucher(UUID voucherId, String username, String password) {
      ConcurrentHashMap<RequestKey, VoucherRequest> provisionRecords =
            TestServerRunner.getTestServer().getProvisionRecords();
      RequestKey requestKey = new RequestKey(username, password, RequestKey.VOUCHERS_RESOURCE, voucherId.toString());
      VoucherRequest originalRequest = provisionRecords.get(requestKey);
      if (originalRequest != null) {
         ErrorDetail errorDetail =
               new ErrorDetail().errorType(ErrorTypeEnum.DUPLICATE_RECORD).errorMessage("Duplicate UUID.");
         DetailMessage detailMessage =
               new DetailMessage().freeString("Voucher request with UUID already processed with the associated fields.")
                     .voucherId(voucherId.toString())
                     .requestTime(originalRequest.getRequestTime())
                     .product(originalRequest.getProduct())
                     .vendor(originalRequest.getVendor());
         ConcurrentHashMap<RequestKey, VoucherResponse> responseRecords =
               TestServerRunner.getTestServer().getResponseRecords();
         VoucherResponse rsp = responseRecords.get(requestKey);
         if (rsp != null) {
            detailMessage.setVoucher(rsp.getVoucher());
         }
         errorDetail.setDetailMessage(detailMessage);
         return Response.status(400).entity(errorDetail).build();
      }

      ConcurrentHashMap<RequestKey, VoucherReversal> reversalRecords =
            TestServerRunner.getTestServer().getReversalRecords();
      RequestKey reversalKey = new RequestKey(username, password, RequestKey.REVERSALS_RESOURCE, voucherId.toString());
      VoucherReversal reversal = reversalRecords.get(reversalKey);
      if (reversal != null) {
         ErrorDetail errorDetail =
               new ErrorDetail().errorType(ErrorTypeEnum.VOUCHER_ALREADY_REVERSED).errorMessage("Voucher reversed.");
         DetailMessage detailMessage =
               new DetailMessage()
                     .freeString("Voucher reversal with UUID already processed with the associated fields.")
                     .reversalId(reversal.getReversalId())
                     .reversalTime(reversal.getReversalTime())
                     .product(reversal.getVoucherRequest().getProduct())
                     .vendor(reversal.getVendor());
         ConcurrentHashMap<RequestKey, VoucherResponse> responseRecords =
               TestServerRunner.getTestServer().getResponseRecords();
         VoucherResponse rsp = responseRecords.get(requestKey);
         if (rsp != null) {
            detailMessage.setVoucher(rsp.getVoucher());
         }
         errorDetail.setDetailMessage(detailMessage);
         return Response.status(400).entity(errorDetail).build();
      }
      return null;
   }

   public static Response canReverseVoucher(UUID voucherId, String username, String password) {
      ConcurrentHashMap<RequestKey, VoucherRequest> provisionRecords =
            TestServerRunner.getTestServer().getProvisionRecords();
      if (!isVoucherProvisioned(voucherId, provisionRecords, username, password)) {
         ErrorDetail errorDetail =
               new ErrorDetail().errorType(ErrorTypeEnum.UNABLE_TO_LOCATE_RECORD).errorMessage("No voucher req.");
         errorDetail.setDetailMessage(
               new DetailMessage().freeString("No VoucherRequest located for given voucherId.")
                     .voucherId(voucherId.toString()));
         return Response.status(404).entity(errorDetail).build();
      }

      // check it's not confirmed
      ConcurrentHashMap<RequestKey, VoucherConfirmation> confirmationRecords =
            TestServerRunner.getTestServer().getConfirmationRecords();
      RequestKey confirmKey =
            new RequestKey(username, password, RequestKey.CONFIRMATIONS_RESOURCE, voucherId.toString());
      VoucherConfirmation confirmation = confirmationRecords.get(confirmKey);
      if (confirmation != null) {
         ErrorDetail errorDetail =
               new ErrorDetail().errorType(ErrorTypeEnum.VOUCHER_ALREADY_CONFIRMED)
                     .errorMessage("Voucher confirmed.")
                     .detailMessage(
                           new DetailMessage()
                                 .freeString(
                                       "The voucher cannot be reversed as it has already been confirmed with the associated details.")
                                 .confirmationId(confirmation.getConfirmationId())
                                 .confirmDate(confirmation.getConfirmDate())
                                 .voucher(confirmation.getVoucher())
                                 .vendor(confirmation.getVendor()));
         return Response.status(400).entity(errorDetail).build();
      }

      // check it's not voided
      ConcurrentHashMap<RequestKey, VoucherVoid> voidRecords = TestServerRunner.getTestServer().getVoidRecords();
      RequestKey voidKey = new RequestKey(username, password, RequestKey.VOIDS_RESOURCE, voucherId.toString());
      VoucherVoid voidAdv = voidRecords.get(voidKey);
      if (voidAdv != null) {
         ErrorDetail errorDetail =
               new ErrorDetail().errorType(ErrorTypeEnum.VOUCHER_ALREADY_VOIDED)
                     .errorMessage("Voucher voided.")
                     .detailMessage(
                           new DetailMessage()
                                 .freeString(
                                       "The voucher cannot be reversed as it has already been voided with the associated details.")
                                 .voidId(voidAdv.getVoidId())
                                 .voidDate(voidAdv.getVoidDate())
                                 .voucher(voidAdv.getVoucher())
                                 .vendor(voidAdv.getVendor()));
         return Response.status(400).entity(errorDetail).build();
      }
      return null;
   }

   public static Response canConfirmVoucher(UUID voucherId, String username, String password) {
      ConcurrentHashMap<RequestKey, VoucherRequest> provisionRecords =
            TestServerRunner.getTestServer().getProvisionRecords();
      // check voucher was provisioned
      if (!isVoucherProvisioned(voucherId, provisionRecords, username, password)) {
         ErrorDetail errorDetail =
               new ErrorDetail().errorType(ErrorTypeEnum.UNABLE_TO_LOCATE_RECORD).errorMessage("No voucher req.");
         errorDetail.setDetailMessage(
               new DetailMessage().freeString("No VoucherRequest located for given voucherId.")
                     .voucherId(voucherId.toString()));
         return Response.status(404).entity(errorDetail).build();
      }

      // check it's not reversed
      ConcurrentHashMap<RequestKey, VoucherReversal> reversalRecords =
            TestServerRunner.getTestServer().getReversalRecords();
      RequestKey reversalsKey = new RequestKey(username, password, RequestKey.REVERSALS_RESOURCE, voucherId.toString());
      VoucherReversal reversal = reversalRecords.get(reversalsKey);
      if (reversal != null) {
         ErrorDetail errorDetail =
               new ErrorDetail().errorType(ErrorTypeEnum.VOUCHER_ALREADY_REVERSED).errorMessage("Voucher reversed.");
         errorDetail.setDetailMessage(
               new DetailMessage()
                     .freeString("Voucher provision has already been reversed with the associated details.")
                     .reversalId(reversal.getReversalId())
                     .reversalTime(reversal.getReversalTime())
                     .product(reversal.getVoucherRequest().getProduct())
                     .vendor(reversal.getVendor()));
         return Response.status(400).entity(errorDetail).build();
      }

      // check it's not voided
      ConcurrentHashMap<RequestKey, VoucherVoid> voidRecords = TestServerRunner.getTestServer().getVoidRecords();
      RequestKey voidKey = new RequestKey(username, password, RequestKey.VOIDS_RESOURCE, voucherId.toString());
      VoucherVoid voidAdv = voidRecords.get(voidKey);
      if (voidAdv != null) {
         ErrorDetail errorDetail =
               new ErrorDetail().errorType(ErrorTypeEnum.VOUCHER_ALREADY_VOIDED).errorMessage("Voucher voided.");
         errorDetail.setDetailMessage(
               new DetailMessage().freeString("Voucher provision has already been voided with the associated details.")
                     .voidId(voidAdv.getVoidId())
                     .voidDate(voidAdv.getVoidDate())
                     .voucher(voidAdv.getVoucher())
                     .vendor(voidAdv.getVendor()));
         return Response.status(400).entity(errorDetail).build();
      }
      return null;
   }

   public static Response canVoidVoucher(UUID voucherId, String username, String password) {
      ConcurrentHashMap<RequestKey, VoucherRequest> provisionRecords =
            TestServerRunner.getTestServer().getProvisionRecords();
      // check voucher was provisioned
      if (!isVoucherProvisioned(voucherId, provisionRecords, username, password)) {
         ErrorDetail errorDetail =
               new ErrorDetail().errorType(ErrorTypeEnum.UNABLE_TO_LOCATE_RECORD).errorMessage("No voucher req.");
         errorDetail.setDetailMessage(
               new DetailMessage().freeString("No VoucherRequest located for given voucherId.")
                     .voucherId(voucherId.toString()));
         return Response.status(404).entity(errorDetail).build();
      }

      // check it's not reversed
      ConcurrentHashMap<RequestKey, VoucherReversal> reversalRecords =
            TestServerRunner.getTestServer().getReversalRecords();
      RequestKey reversalsKey = new RequestKey(username, password, RequestKey.REVERSALS_RESOURCE, voucherId.toString());
      VoucherReversal reversal = reversalRecords.get(reversalsKey);
      if (reversal != null) {
         ErrorDetail errorDetail =
               new ErrorDetail().errorType(ErrorTypeEnum.VOUCHER_ALREADY_REVERSED).errorMessage("Voucher reversed.");
         errorDetail.setDetailMessage(
               new DetailMessage()
                     .freeString("Voucher provision has already been reversed with the associated details.")
                     .reversalId(reversal.getReversalId())
                     .reversalTime(reversal.getReversalTime())
                     .product(reversal.getVoucherRequest().getProduct())
                     .vendor(reversal.getVendor()));
         return Response.status(400).entity(errorDetail).build();
      }

      // check it's not confirmed
      ConcurrentHashMap<RequestKey, VoucherConfirmation> confirmationRecords =
            TestServerRunner.getTestServer().getConfirmationRecords();
      RequestKey confirmKey =
            new RequestKey(username, password, RequestKey.CONFIRMATIONS_RESOURCE, voucherId.toString());
      VoucherConfirmation confirmation = confirmationRecords.get(confirmKey);
      if (confirmation != null) {
         ErrorDetail errorDetail =
               new ErrorDetail().errorType(ErrorTypeEnum.VOUCHER_ALREADY_CONFIRMED).errorMessage("Voucher confirmed.");
         errorDetail.setDetailMessage(
               new DetailMessage()
                     .freeString("Voucher provision has already been confirmed with the associated details.")
                     .confirmationId(confirmation.getConfirmationId())
                     .confirmDate(confirmation.getConfirmDate())
                     .voucher(confirmation.getVoucher())
                     .vendor(confirmation.getVendor()));
         return Response.status(400).entity(errorDetail).build();
      }
      return null;
   }

   public static boolean isVoucherProvisioned(
         UUID voucherId,
         ConcurrentHashMap<RequestKey, VoucherRequest> provisionRecords,
         String username,
         String password) {
      RequestKey provisionKey = new RequestKey(username, password, RequestKey.VOUCHERS_RESOURCE, voucherId.toString());
      log.debug(String.format("Searching for provision record under following key: %s", provisionKey.toString()));
      return provisionRecords.get(provisionKey) != null;
   }

   public static String getAuthString(String authHeader) {
      if (authHeader == null || authHeader.isEmpty() || !authHeader.startsWith("Basic "))
      {
         return null;
      }
      String credsSubstring = authHeader.substring("Basic ".length());
      String usernameAndPassword = Base64.decodeAsString(credsSubstring);
      return usernameAndPassword;
   }

   public static String getUsernameFromAuth(String authString) {
      String username = "null";
      if (authString != null && !authString.isEmpty()) {
         username = authString.substring(0, authString.indexOf(':'));
      }
      return username;
   }

   public static String getPasswordFromAuth(String authString) {
      String password = "null";
      if (authString != null && !authString.isEmpty()) {
         password = authString.substring(authString.indexOf(':') + 1);
      }
      return password;
   }
   
   public static void main (String [] args)
   {
      System.out.println(RandomData.random09(20));
      System.out.println(RandomData.random09((int) ((Math.random() * 20)+1)));
   }
}
