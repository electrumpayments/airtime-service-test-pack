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
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.electrum.airtime.api.model.ErrorDetail;
import io.electrum.airtime.api.model.ErrorDetail.ErrorType;
import io.electrum.airtime.api.model.Product.ProductType;
import io.electrum.airtime.api.model.SlipData;
import io.electrum.airtime.api.model.Voucher;
import io.electrum.airtime.api.model.VoucherConfirmation;
import io.electrum.airtime.api.model.VoucherRequest;
import io.electrum.airtime.api.model.VoucherResponse;
import io.electrum.airtime.resource.impl.AirtimeTestServer;
import io.electrum.airtime.server.AirtimeTestServerRunner;
import io.electrum.airtime.server.model.DetailMessage;
import io.electrum.airtime.server.model.FormatError;
import io.electrum.vas.model.Amounts;
import io.electrum.vas.model.BasicReversal;
import io.electrum.vas.model.Institution;
import io.electrum.vas.model.Merchant;
import io.electrum.vas.model.Originator;
import io.electrum.vas.model.Tender;
import io.electrum.vas.model.ThirdPartyIdentifier;

public class VoucherModelUtils {
   private static List<String> redeemInstructions = new ArrayList<String>();
   private static List<String> messageLines = new ArrayList<String>();
   private static final Logger log = LoggerFactory.getLogger(AirtimeTestServer.class.getPackage().getName());
   static {
      redeemInstructions.add("To redeem your airtime");
      redeemInstructions.add("enter the USSD code below:");
      redeemInstructions.add("*999*<pin>#");
      messageLines.add("Use the Receiver reference");
      messageLines.add("number to query your network");
      messageLines.add("operator.");
   }

   public static VoucherResponse voucherRspFromReq(VoucherRequest req) {
      VoucherResponse rsp = new VoucherResponse();
      rsp.setOriginator(req.getOriginator());
      rsp.setTime(req.getTime());
      Institution receiver = req.getReceiver();
      List<ThirdPartyIdentifier> thirdPartyIds = req.getThirdPartyIdentifiers();
      if (thirdPartyIds == null) {
         new ArrayList<ThirdPartyIdentifier>();
      }
      rsp.setReceiver(req.getReceiver());
      rsp.setId(req.getId());
      Voucher voucher = new Voucher();
      voucher.setPin(RandomData.random09((int) ((Math.random() * 20) + 1)));
      voucher.setExpiryDate(new DateTime());
      voucher.setSerialNumber(RandomData.random09((int) ((Math.random() * 20) + 1)));
      voucher.setBatchNumber(RandomData.random09((int) ((Math.random() * 20) + 1)));
      voucher.setRedeemInstructions(redeemInstructions);
      rsp.setVoucher(voucher);
      SlipData slipData = new SlipData();
      slipData.setMessageLines(messageLines);
      slipData.setReceiverReference(RandomData.random09AZ((int) ((Math.random() * 20) + 1)));
      rsp.setSlipData(slipData);
      Institution settlementEntity = req.getSettlementEntity();
      if (settlementEntity == null) {
         settlementEntity = new Institution();
         settlementEntity.setId("33333333");
         settlementEntity.setName("TransactionsRUs");
      }
      thirdPartyIds.add(
            new ThirdPartyIdentifier().institutionId(settlementEntity.getId())
                  .transactionIdentifier(RandomData.random09AZ((int) ((Math.random() * 20) + 1))));
      thirdPartyIds.add(
            new ThirdPartyIdentifier().institutionId(receiver.getId())
                  .transactionIdentifier(RandomData.random09AZ((int) ((Math.random() * 20) + 1))));
      rsp.setSettlementEntity(settlementEntity);
      rsp.setThirdPartyIdentifiers(thirdPartyIds);
      rsp.setResponseProduct(req.getProduct().name("TalkALot").type(ProductType.AIRTIME_FIXED));
      return rsp;
   }

   public static ErrorDetail productNotRecognised(VoucherRequest req) {
      ErrorDetail errorDetail = new ErrorDetail();
      errorDetail.setErrorType(ErrorType.INVALID_PRODUCT);
      errorDetail.setErrorMessage("Unknown product");
      DetailMessage detailMessage = new DetailMessage();
      detailMessage.setFreeString("This MNO does not recognise the product requested.");
      detailMessage.setProduct(req.getProduct());
      detailMessage.setReceiver(req.getReceiver());
      return errorDetail;
   }

   private static <T> Set<ConstraintViolation<T>> validate(T tInstance) {
      if (tInstance == null) {
         return new HashSet<ConstraintViolation<T>>();
      }
      Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
      Set<ConstraintViolation<T>> violations = validator.validate(tInstance);
      return violations;
   }

   public static void validateVoucherRequest(VoucherRequest voucherRequest, Set<ConstraintViolation<?>> violations) {
      violations.addAll(validate(voucherRequest));
      if (voucherRequest != null) {
         violations.addAll(validate(voucherRequest.getClient()));
         violations.addAll(validate(voucherRequest.getId()));
         Originator originator = voucherRequest.getOriginator();
         violations.addAll(validate(originator));
         if (originator != null) {
            violations.addAll(validate(originator.getInstitution()));
            violations.addAll(validate(originator.getTerminalId()));
            Merchant merchant = originator.getMerchant();
            violations.addAll(validate(merchant));
            if (merchant != null) {
               violations.addAll(validate(merchant.getMerchantId()));
               violations.addAll(validate(merchant.getMerchantType()));
               violations.addAll(validate(merchant.getMerchantName()));
            }
         }
         violations.addAll(validate(voucherRequest.getProduct()));
         Amounts amounts = voucherRequest.getAmountss();
         violations.addAll(validate(amounts));
         if (amounts != null) {
            violations.addAll(validate(amounts.getRequestAmount()));
         }
         violations.addAll(validate(voucherRequest.getReceiver()));
         violations.addAll(validate(voucherRequest.getSettlementEntity()));
         violations.addAll(validate(voucherRequest.getThirdPartyIdentifiers()));
         violations.addAll(validate(voucherRequest.getTime()));
      }
   }

   public static Response validateVoucherRequest(VoucherRequest voucherRequest) {
      Set<ConstraintViolation<?>> violations = new HashSet<ConstraintViolation<?>>();
      validateVoucherRequest(voucherRequest, violations);
      return buildFormatErrorRsp(violations);
   }

   public static Response validateBasicReversal(BasicReversal BasicReversal) {
      Set<ConstraintViolation<?>> violations = new HashSet<ConstraintViolation<?>>();
      violations.addAll(validate(BasicReversal));
      violations.addAll(validate(BasicReversal.getId()));
      violations.addAll(validate(BasicReversal.getRequestId()));
      violations.addAll(validate(BasicReversal.getReversalReason()));
      violations.addAll(validate(BasicReversal.getThirdPartyIdentifiers()));
      violations.addAll(validate(BasicReversal.getTime()));
      return buildFormatErrorRsp(violations);
   }

   public static Response validateVoucherConfirmation(VoucherConfirmation voucherConfirmation) {
      Set<ConstraintViolation<?>> violations = new HashSet<ConstraintViolation<?>>();
      violations.addAll(validate(voucherConfirmation));
      violations.addAll(validate(voucherConfirmation.getId()));
      violations.addAll(validate(voucherConfirmation.getRequestId()));
      violations.addAll(validate(voucherConfirmation.getThirdPartyIdentifiers()));
      violations.addAll(validate(voucherConfirmation.getTime()));
      List<Tender> tenders = voucherConfirmation.getTenders();
      violations.addAll(validate(tenders));
      for (Tender tender : tenders) {
         violations.addAll(validate(tender));
      }
      violations.addAll(validate(voucherConfirmation.getVoucher()));
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
               new FormatError().msg(violation.getMessage()).field(violation.getPropertyPath().toString()).value(
                     violation.getInvalidValue() == null ? "null" : violation.getInvalidValue().toString()));
         i++;
      }
      ErrorDetail errorDetail =
            new ErrorDetail().errorType(ErrorType.FORMAT_ERROR)
                  .errorMessage("Bad formatting")
                  .detailMessage(new DetailMessage().formatErrors(formatErrors));
      return Response.status(400).entity(errorDetail).build();
   }

   public static Response isUuidConsistent(UUID uuid, VoucherRequest voucherReq) {
      Response rsp = null;
      String pathId = uuid.toString();
      UUID objectId = voucherReq.getId();
      ErrorDetail errorDetail = isUuidConsistent(pathId, objectId.toString());
      if (errorDetail != null) {
         DetailMessage detailMessage = (DetailMessage) errorDetail.getDetailMessage();
         detailMessage.setVoucherId(objectId);
         rsp = Response.status(400).entity(errorDetail).build();
      }
      return rsp;
   }

   public static Response isUuidConsistent(UUID uuid, BasicReversal voucherRev) {
      Response rsp = null;
      String pathId = uuid.toString();
      UUID objectId = voucherRev.getId();
      ErrorDetail errorDetail = isUuidConsistent(pathId, objectId.toString());
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
      UUID objectId = voucherConfirmation.getId();
      ErrorDetail errorDetail = isUuidConsistent(pathId, objectId.toString());
      if (errorDetail != null) {
         DetailMessage detailMessage = (DetailMessage) errorDetail.getDetailMessage();
         detailMessage.setConfirmationId(objectId);
         rsp = Response.status(400).entity(errorDetail).build();
      }
      return rsp;
   }

   public static ErrorDetail isUuidConsistent(String pathId, String objectId) {
      ErrorDetail errorDetail = null;
      if (!pathId.equals(objectId)) {
         errorDetail = new ErrorDetail().errorType(ErrorType.FORMAT_ERROR).errorMessage("UUID inconsistent");
         DetailMessage detailMessage = new DetailMessage();
         detailMessage.setPathId(pathId);
         detailMessage.setFreeString("The ID path parameter is not the same as the object's ID.");
         errorDetail.setDetailMessage(detailMessage);
      }
      return errorDetail;
   }

   public static Response canProvisionVoucher(UUID voucherId, String username, String password) {
      ConcurrentHashMap<RequestKey, VoucherRequest> provisionRecords =
            AirtimeTestServerRunner.getTestServer().getProvisionRecords();
      RequestKey requestKey = new RequestKey(username, password, RequestKey.VOUCHERS_RESOURCE, voucherId.toString());
      VoucherRequest originalRequest = provisionRecords.get(requestKey);
      if (originalRequest != null) {
         ErrorDetail errorDetail =
               new ErrorDetail().errorType(ErrorType.DUPLICATE_RECORD).errorMessage("Duplicate UUID.");
         DetailMessage detailMessage =
               new DetailMessage().freeString("Voucher request with UUID already processed with the associated fields.")
                     .voucherId(voucherId)
                     .requestTime(originalRequest.getTime().toString())
                     .product(originalRequest.getProduct())
                     .receiver(originalRequest.getReceiver());
         ConcurrentHashMap<RequestKey, VoucherResponse> responseRecords =
               AirtimeTestServerRunner.getTestServer().getResponseRecords();
         VoucherResponse rsp = responseRecords.get(requestKey);
         if (rsp != null) {
            detailMessage.setVoucher(rsp.getVoucher());
         }
         errorDetail.setDetailMessage(detailMessage);
         return Response.status(400).entity(errorDetail).build();
      }

      ConcurrentHashMap<RequestKey, BasicReversal> reversalRecords =
            AirtimeTestServerRunner.getTestServer().getReversalRecords();
      RequestKey reversalKey = new RequestKey(username, password, RequestKey.REVERSALS_RESOURCE, voucherId.toString());
      BasicReversal reversal = reversalRecords.get(reversalKey);
      if (reversal != null) {
         ErrorDetail errorDetail =
               new ErrorDetail().errorType(ErrorType.ACCOUNT_ALREADY_SETTLED).errorMessage("Voucher reversed.");
         DetailMessage detailMessage =
               new DetailMessage()
                     .freeString("Voucher reversal with UUID already processed with the associated fields.")
                     .reversalId(reversal.getId());
         ConcurrentHashMap<RequestKey, VoucherResponse> responseRecords =
               AirtimeTestServerRunner.getTestServer().getResponseRecords();
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
            AirtimeTestServerRunner.getTestServer().getProvisionRecords();
      if (!isVoucherProvisioned(voucherId, provisionRecords, username, password)) {
         ErrorDetail errorDetail =
               new ErrorDetail().errorType(ErrorType.UNABLE_TO_LOCATE_RECORD).errorMessage("No voucher req.");
         errorDetail.setDetailMessage(
               new DetailMessage().freeString("No VoucherRequest located for given voucherId.").voucherId(voucherId));
         return Response.status(404).entity(errorDetail).build();
      }

      // check it's not confirmed
      ConcurrentHashMap<RequestKey, VoucherConfirmation> confirmationRecords =
            AirtimeTestServerRunner.getTestServer().getConfirmationRecords();
      RequestKey confirmKey =
            new RequestKey(username, password, RequestKey.CONFIRMATIONS_RESOURCE, voucherId.toString());
      VoucherConfirmation confirmation = confirmationRecords.get(confirmKey);
      if (confirmation != null) {
         ErrorDetail errorDetail =
               new ErrorDetail().errorType(ErrorType.ACCOUNT_ALREADY_SETTLED)
                     .errorMessage("Voucher confirmed.")
                     .detailMessage(
                           new DetailMessage()
                                 .freeString(
                                       "The voucher cannot be reversed as it has already been confirmed with the associated details.")
                                 .confirmationId(confirmation.getId())
                                 .voucher(confirmation.getVoucher()));
         return Response.status(400).entity(errorDetail).build();
      }
      return null;
   }

   public static Response canConfirmVoucher(UUID voucherId, String username, String password) {
      ConcurrentHashMap<RequestKey, VoucherRequest> provisionRecords =
            AirtimeTestServerRunner.getTestServer().getProvisionRecords();
      // check voucher was provisioned
      if (!isVoucherProvisioned(voucherId, provisionRecords, username, password)) {
         ErrorDetail errorDetail =
               new ErrorDetail().errorType(ErrorType.UNABLE_TO_LOCATE_RECORD).errorMessage("No voucher req.");
         errorDetail.setDetailMessage(
               new DetailMessage().freeString("No VoucherRequest located for given voucherId.").voucherId(voucherId));
         return Response.status(404).entity(errorDetail).build();
      }

      // check it's not reversed
      ConcurrentHashMap<RequestKey, BasicReversal> reversalRecords =
            AirtimeTestServerRunner.getTestServer().getReversalRecords();
      RequestKey reversalsKey = new RequestKey(username, password, RequestKey.REVERSALS_RESOURCE, voucherId.toString());
      BasicReversal reversal = reversalRecords.get(reversalsKey);
      if (reversal != null) {
         ErrorDetail errorDetail =
               new ErrorDetail().errorType(ErrorType.ACCOUNT_ALREADY_SETTLED).errorMessage("Voucher reversed.");
         errorDetail.setDetailMessage(
               new DetailMessage()
                     .freeString("Voucher provision has already been reversed with the associated details.")
                     .reversalId(reversal.getId()));
         return Response.status(400).entity(errorDetail).build();
      }
      return null;
   }

   public static Response canVoidVoucher(UUID voucherId, String username, String password) {
      ConcurrentHashMap<RequestKey, VoucherRequest> provisionRecords =
            AirtimeTestServerRunner.getTestServer().getProvisionRecords();
      // check voucher was provisioned
      if (!isVoucherProvisioned(voucherId, provisionRecords, username, password)) {
         ErrorDetail errorDetail =
               new ErrorDetail().errorType(ErrorType.UNABLE_TO_LOCATE_RECORD).errorMessage("No voucher req.");
         errorDetail.setDetailMessage(
               new DetailMessage().freeString("No VoucherRequest located for given voucherId.").voucherId(voucherId));
         return Response.status(404).entity(errorDetail).build();
      }

      // check it's not reversed
      ConcurrentHashMap<RequestKey, BasicReversal> reversalRecords =
            AirtimeTestServerRunner.getTestServer().getReversalRecords();
      RequestKey reversalsKey = new RequestKey(username, password, RequestKey.REVERSALS_RESOURCE, voucherId.toString());
      BasicReversal reversal = reversalRecords.get(reversalsKey);
      if (reversal != null) {
         ErrorDetail errorDetail =
               new ErrorDetail().errorType(ErrorType.ACCOUNT_ALREADY_SETTLED).errorMessage("Voucher reversed.");
         errorDetail.setDetailMessage(
               new DetailMessage()
                     .freeString("Voucher provision has already been reversed with the associated details.")
                     .reversalId(reversal.getId()));
         return Response.status(400).entity(errorDetail).build();
      }

      // check it's not confirmed
      ConcurrentHashMap<RequestKey, VoucherConfirmation> confirmationRecords =
            AirtimeTestServerRunner.getTestServer().getConfirmationRecords();
      RequestKey confirmKey =
            new RequestKey(username, password, RequestKey.CONFIRMATIONS_RESOURCE, voucherId.toString());
      VoucherConfirmation confirmation = confirmationRecords.get(confirmKey);
      if (confirmation != null) {
         ErrorDetail errorDetail =
               new ErrorDetail().errorType(ErrorType.ACCOUNT_ALREADY_SETTLED).errorMessage("Voucher confirmed.");
         errorDetail.setDetailMessage(
               new DetailMessage()
                     .freeString("Voucher provision has already been confirmed with the associated details.")
                     .confirmationId(confirmation.getId())
                     .voucher(confirmation.getVoucher()));
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
      if (authHeader == null || authHeader.isEmpty() || !authHeader.startsWith("Basic ")) {
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

   public static void main(String[] args) {
      System.out.println(RandomData.random09(20));
      System.out.println(RandomData.random09((int) ((Math.random() * 20) + 1)));
   }
}
