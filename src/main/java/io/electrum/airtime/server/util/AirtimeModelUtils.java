package io.electrum.airtime.server.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.internal.util.Base64;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.electrum.airtime.api.model.ErrorDetail;
import io.electrum.airtime.api.model.Voucher;
import io.electrum.airtime.resource.impl.AirtimeTestServer;
import io.electrum.airtime.server.model.DetailMessage;
import io.electrum.airtime.server.model.FormatError;
import io.electrum.vas.model.Amounts;
import io.electrum.vas.model.Institution;
import io.electrum.vas.model.LedgerAmount;
import io.electrum.vas.model.Merchant;
import io.electrum.vas.model.Originator;
import io.electrum.vas.model.SlipData;
import io.electrum.vas.model.SlipLine;
import io.electrum.vas.model.ThirdPartyIdentifier;
import io.electrum.vas.model.Transaction;

public class AirtimeModelUtils {
   protected static final Logger log = LoggerFactory.getLogger(AirtimeTestServer.class.getPackage().getName());

   protected static List<String> redeemInstructions = new ArrayList<String>();
   protected static List<SlipLine> messageLines = new ArrayList<SlipLine>();

   static {
      redeemInstructions.add("To redeem your airtime");
      redeemInstructions.add("enter the USSD code below:");
      redeemInstructions.add("*999*<pin>#");
      messageLines.add(new SlipLine().text("For any queries please"));
      messageLines.add(new SlipLine().text("contact your network"));
      messageLines.add(new SlipLine().text("operator."));
   }

   static <T> Set<ConstraintViolation<T>> validate(T tInstance) {
      if (tInstance == null) {
         return new HashSet<ConstraintViolation<T>>();
      }
      Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
      Set<ConstraintViolation<T>> violations = validator.validate(tInstance);
      return violations;
   }

   protected static ErrorDetail buildFormatErrorRsp(Set<ConstraintViolation<?>> violations) {
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
            new ErrorDetail().errorType(ErrorDetail.ErrorType.FORMAT_ERROR)
                  .errorMessage("Bad formatting")
                  .detailMessage(new DetailMessage().formatErrors(formatErrors));
      return errorDetail;
   }

   protected static void validateTransaction(Transaction transaction, Set<ConstraintViolation<?>> violations) {
      violations.addAll(validate(transaction));
      if (transaction != null) {
         violations.addAll(validate(transaction.getId()));
         violations.addAll(validate(transaction.getTime()));
         validateOriginator(transaction.getOriginator(), violations);
         violations.addAll(validate(transaction.getClient()));
         violations.addAll(validate(transaction.getSettlementEntity()));
         violations.addAll(validate(transaction.getReceiver()));
         violations.addAll(validate(transaction.getThirdPartyIdentifiers()));
         violations.addAll(validate(transaction.getSlipData()));
         violations.addAll(validate(transaction.getBasketRef()));
         violations.addAll(validate(transaction.getTranType()));
         violations.addAll(validate(transaction.getSrcAccType()));
         violations.addAll(validate(transaction.getDestAccType()));
      }
   }

   private static void validateOriginator(Originator originator, Set<ConstraintViolation<?>> violations) {
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
   }

   protected static void validateAmounts(Set<ConstraintViolation<?>> violations, Amounts amounts) {
      violations.addAll(validate(amounts));
      if (amounts != null) {
         violations.addAll(validate(amounts.getRequestAmount()));
         violations.addAll(validate(amounts.getAdditionalAmounts()));
         violations.addAll(validate(amounts.getApprovedAmount()));
         violations.addAll(validate(amounts.getBalanceAmount()));
         violations.addAll(validate(amounts.getFeeAmount()));
      }
   }

   public static Response buildIncorrectUsernameErrorResponse(
         String objectId,
         Institution client,
         String username,
         ErrorDetail.RequestType requestType) {

      ErrorDetail errorDetail =
            buildErrorDetail(
                  objectId,
                  "Incorrect username",
                  "The HTTP Basic Authentication username (" + username
                        + ") is not the same as the value in the Client.Id field (" + client.getId() + ").",
                  null,
                  requestType,
                  ErrorDetail.ErrorType.FORMAT_ERROR);

      DetailMessage detailMessage = (DetailMessage) errorDetail.getDetailMessage();
      detailMessage.setClient(client);

      return Response.status(400).entity(errorDetail).build();
   }

   public static ErrorDetail buildInconsistentIdErrorDetail(
         String pathId,
         String objectId,
         String originalMsgId,
         ErrorDetail.RequestType requestType) {
      ErrorDetail errorDetail =
            buildErrorDetail(
                  objectId,
                  "String inconsistent",
                  "The ID path parameter is not the same as the object's ID.",
                  originalMsgId,
                  requestType,
                  ErrorDetail.ErrorType.FORMAT_ERROR);

      DetailMessage detailMessage = (DetailMessage) errorDetail.getDetailMessage();
      detailMessage.setPathId(pathId);

      return errorDetail;
   }

   protected static Voucher createRandomizedVoucher() {
      Voucher voucher = new Voucher();
      voucher.setPin(RandomData.random09((int) ((Math.random() * 20) + 1)));
      voucher.setExpiryDate(new DateTime());
      voucher.setSerialNumber(RandomData.random09((int) ((Math.random() * 20) + 1)));
      voucher.setBatchNumber(RandomData.random09((int) ((Math.random() * 20) + 1)));
      voucher.setRedeemInstructions(redeemInstructions);
      return voucher;
   }

   protected static SlipData createRandomizedSlipData() {
      SlipData slipData = new SlipData();
      slipData.setMessageLines(messageLines);
      return slipData;
   }

   protected static void updateWithRandomizedIdentifiers(Transaction transaction) {
      List<ThirdPartyIdentifier> thirdPartyIds = transaction.getThirdPartyIdentifiers();
      Institution settlementEntity = transaction.getSettlementEntity();
      if (settlementEntity == null) {
         settlementEntity = new Institution();
         settlementEntity.setId("33333333");
         settlementEntity.setName("TransactionsRUs");
      }

      Institution receiver = transaction.getReceiver();
      thirdPartyIds.add(
            new ThirdPartyIdentifier().institutionId(settlementEntity.getId())
                  .transactionIdentifier(RandomData.random09AZ((int) ((Math.random() * 20) + 1))));
      thirdPartyIds.add(
            new ThirdPartyIdentifier().institutionId(receiver.getId())
                  .transactionIdentifier(RandomData.random09AZ((int) ((Math.random() * 20) + 1))));
   }

   protected static Amounts createRandomizedAmounts() {
      return new Amounts().approvedAmount(createRandomizedAmount()).feeAmount(createRandomizedAmount());
   }

   private static LedgerAmount createRandomizedAmount() {
      return new LedgerAmount().currency("710")
            .amount(Long.parseLong(RandomData.random09((int) ((Math.random() * 2) + 1))));
   }

   public static ErrorDetail buildDuplicateErrorDetail(
         String objectId,
         String originalMsgId,
         ErrorDetail.RequestType requestType,
         Transaction transaction) {

      ErrorDetail errorDetail =
            buildErrorDetail(
                  objectId,
                  "Duplicate UUID.",
                  "Request with String already processed with the associated fields.",
                  originalMsgId,
                  requestType,
                  ErrorDetail.ErrorType.DUPLICATE_RECORD);

      DetailMessage detailMessage = (DetailMessage) errorDetail.getDetailMessage();
      detailMessage.setVoucherId(objectId);
      detailMessage.setRequestTime(transaction.getTime().toString());
      // detailMessage.setProduct(transaction.getProduct());
      detailMessage.setReceiver(transaction.getReceiver());

      return errorDetail;
   }

   public static ErrorDetail buildErrorDetail(
         String objectId,
         String errorMessage,
         String detailMessageFreeString,
         String originalMsgId,
         ErrorDetail.RequestType requestType,
         ErrorDetail.ErrorType errorType) {

      ErrorDetail errorDetail =
            new ErrorDetail().errorType(errorType)
                  .errorMessage(errorMessage)
                  .id(objectId)
                  .originalId(originalMsgId)
                  .requestType(requestType);

      DetailMessage detailMessage = new DetailMessage();
      detailMessage.setFreeString(detailMessageFreeString);
      detailMessage.setReversalId(originalMsgId);

      errorDetail.setDetailMessage(detailMessage);

      return errorDetail;
   }

   public static boolean isUuidConsistent(String pathId, String serviceId) {
      return pathId.equals(serviceId);
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
}
