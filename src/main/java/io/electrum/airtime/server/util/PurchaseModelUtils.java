package io.electrum.airtime.server.util;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.validation.ConstraintViolation;
import javax.ws.rs.core.Response;

import io.electrum.airtime.api.PurchaseResource;
import io.electrum.airtime.api.model.ErrorDetail;
import io.electrum.airtime.api.model.Msisdn;
import io.electrum.airtime.api.model.Product;
import io.electrum.airtime.api.model.PurchaseConfirmation;
import io.electrum.airtime.api.model.PurchaseRequest;
import io.electrum.airtime.api.model.PurchaseResponse;
import io.electrum.airtime.api.model.PurchaseReversal;
import io.electrum.airtime.server.AirtimeTestServerRunner;
import io.electrum.airtime.server.model.DetailMessage;
import io.electrum.vas.JsonUtil;
import io.electrum.vas.model.BasicAdvice;
import io.electrum.vas.model.BasicReversal;

public class PurchaseModelUtils extends AirtimeModelUtils {

   public static PurchaseResponse purchaseRspFromReq(PurchaseRequest purchaseRequest) throws IOException {
      PurchaseResponse purchaseResponse =
            JsonUtil.deserialize(JsonUtil.serialize(purchaseRequest, PurchaseRequest.class), PurchaseResponse.class);

      updateWithRandomizedIdentifiers(purchaseResponse);
      purchaseResponse.setVoucher(createRandomizedVoucher());
      purchaseResponse.setSlipData(createRandomizedSlipData());
      purchaseResponse
            .setProduct(purchaseRequest.getProduct().name("TalkALot").type(Product.ProductType.AIRTIME_FIXED));
      purchaseResponse.setAmounts(createRandomizedAmounts());
      purchaseResponse.setMsisdn(buildMsisdn(purchaseRequest));

      return purchaseResponse;
   }

   private static Msisdn buildMsisdn(PurchaseRequest purchaseRequest) {
      Msisdn msisdn;
      if ((msisdn = purchaseRequest.getRecipientMsisdn()) == null) {
         msisdn = new Msisdn().msisdn(RandomData.random09(10)).country("ZA");
      }
      return msisdn;
   }

   public static Response validatePurchaseRequest(PurchaseRequest purchaseRequest) {
      Set<ConstraintViolation<?>> violations = new HashSet<ConstraintViolation<?>>();
      validatePurchaseRequest(purchaseRequest, violations);
      ErrorDetail errorDetail = buildFormatErrorRsp(violations);

      if (errorDetail == null) {
         return null;
      }
      errorDetail.id(purchaseRequest.getId()).requestType(ErrorDetail.RequestType.PURCHASE_REQUEST);
      return Response.status(400).entity(errorDetail).build();
   }

   public static Response validatePurchaseReversal(BasicReversal reversal) {
      Set<ConstraintViolation<?>> violations = new HashSet<ConstraintViolation<?>>();
      validateBasicReversal(reversal, violations);
      ErrorDetail errorDetail = buildFormatErrorRsp(violations);

      if (errorDetail == null) {
         return null;
      }
      errorDetail.id(reversal.getId()).originalId(reversal.getRequestId()).requestType(
            ErrorDetail.RequestType.PURCHASE_REVERSAL);
      return Response.status(400).entity(errorDetail).build();
   }

   public static Response validatePurchaseConfirmation(PurchaseConfirmation purchaseConfirmation) {
      Set<ConstraintViolation<?>> violations = new HashSet<ConstraintViolation<?>>();
      validateTenderAdvice(purchaseConfirmation, violations);
      ErrorDetail errorDetail = buildFormatErrorRsp(violations);
      if (errorDetail == null) {
         return null;
      }
      errorDetail.id(purchaseConfirmation.getId()).originalId(purchaseConfirmation.getRequestId()).requestType(
            ErrorDetail.RequestType.PURCHASE_CONFIRMATION);
      return Response.status(400).entity(errorDetail).build();
   }

   private static void validatePurchaseRequest(
         PurchaseRequest purchaseRequest,
         Set<ConstraintViolation<?>> violations) {
      violations.addAll(validate(purchaseRequest));
      if (purchaseRequest != null) {
         validateTransaction(purchaseRequest, violations);
         violations.addAll(validate(purchaseRequest.getProduct()));
         validateAmounts(violations, purchaseRequest.getAmounts());
         violations.addAll(validate(purchaseRequest.getReceiver()));
         violations.addAll(validate(purchaseRequest.getSettlementEntity()));
         violations.addAll(validate(purchaseRequest.getThirdPartyIdentifiers()));
         violations.addAll(validate(purchaseRequest.getTime()));
      }
   }

   public static Response canPurchasePurchaseRequest(String purchaseRequestId, String username, String password) {
      ConcurrentHashMap<RequestKey, PurchaseRequest> purchaseRequestRecords =
            AirtimeTestServerRunner.getTestServer().getPurchaseRequestRecords();
      RequestKey requestKey = new RequestKey(username, password, PurchaseResource.Purchase.PURCHASE, purchaseRequestId);

      // Check if request already provisioned
      PurchaseRequest originalRequest = purchaseRequestRecords.get(requestKey);
      if (originalRequest != null) {
         return buildDuplicateErrorResponse(purchaseRequestId, requestKey, originalRequest);
      }

      // Check if request has already been reversed
      BasicReversal reversal = getPurchaseReversalFromCache(purchaseRequestId, username, password);
      if (reversal != null) {
         return buildAlreadyReversedErrorResponse(purchaseRequestId, requestKey, reversal);
      }

      return null;
   }

   public static Response canReversePurchase(BasicReversal reversal, String username, String password) {
      if (!isPurchaseRequestProvisioned(reversal.getRequestId(), username, password)) {
         ErrorDetail errorDetail = buildRequestNotFoundErrorDetail(reversal);
         return Response.status(404).entity(errorDetail).build();
      }

      // check that it's not confirmed
      PurchaseConfirmation confirmation = getPurchaseConfirmationFromCache(reversal.getRequestId(), username, password);
      if (confirmation != null) {
         ErrorDetail errorDetail =
               buildErrorDetail(
                     reversal.getId(),
                     "Purchase Request confirmed already.",
                     "The purchase cannot be reversed as it has already been confirmed with the associated details.",
                     reversal.getRequestId(),
                     ErrorDetail.RequestType.PURCHASE_REVERSAL,
                     ErrorDetail.ErrorType.ACCOUNT_ALREADY_SETTLED);

         DetailMessage detailMessage = (DetailMessage) errorDetail.getDetailMessage();
         detailMessage.setConfirmationId(confirmation.getId());

         return Response.status(400).entity(errorDetail).build();
      }

      return null;
   }

   public static Response canConfirmPurchase(
         PurchaseConfirmation purchaseConfirmation,
         String username,
         String password) {
      // check if purchase request was provisioned
      if (!isPurchaseRequestProvisioned(purchaseConfirmation.getRequestId(), username, password)) {
         ErrorDetail errorDetail = buildRequestNotFoundErrorDetail(purchaseConfirmation);
         return Response.status(404).entity(errorDetail).build();
      }

      // check that it's not reversed
      BasicReversal reversal = getPurchaseReversalFromCache(purchaseConfirmation.getRequestId(), username, password);
      if (reversal != null) {
         ErrorDetail errorDetail =
               buildErrorDetail(
                     reversal.getId(),
                     "Purchase Request reversed already.",
                     "The purchase cannot be confirmed as it has already been reversed with the associated details.",
                     reversal.getRequestId(),
                     ErrorDetail.RequestType.PURCHASE_CONFIRMATION,
                     ErrorDetail.ErrorType.ACCOUNT_ALREADY_SETTLED);
         return Response.status(400).entity(errorDetail).build();
      }

      return null;
   }

   private static ErrorDetail buildRequestNotFoundErrorDetail(BasicAdvice basicAdvice) {
      return buildErrorDetail(
            basicAdvice.getId(),
            "Original purchase request was not found.",
            "No PurchaseRequest located for given purchaseRequestId.",
            basicAdvice.getRequestId(),
            ErrorDetail.RequestType.PURCHASE_CONFIRMATION,
            ErrorDetail.ErrorType.UNABLE_TO_LOCATE_RECORD);
   }

   private static Response buildAlreadyReversedErrorResponse(
         String purchaseRequestId,
         RequestKey requestKey,
         BasicReversal reversal) {
      ErrorDetail errorDetail =
            buildErrorDetail(
                  purchaseRequestId,
                  "Purchase Request reversed.",
                  "Purchase reversal with String already processed with the associated fields.",
                  reversal.getId(),
                  ErrorDetail.RequestType.PURCHASE_REVERSAL,
                  ErrorDetail.ErrorType.ACCOUNT_ALREADY_SETTLED);

      PurchaseResponse rsp = getPurchaseResponseFromCache(requestKey);
      if (rsp != null) {
         DetailMessage detailMessage = (DetailMessage) errorDetail.getDetailMessage();
         detailMessage.setVoucher(rsp.getVoucher());
      }
      return Response.status(400).entity(errorDetail).build();
   }

   private static Response buildDuplicateErrorResponse(
         String purchaseRequestId,
         RequestKey requestKey,
         PurchaseRequest originalRequest) {
      ErrorDetail errorDetail =
            buildDuplicateErrorDetail(
                  purchaseRequestId,
                  null,
                  ErrorDetail.RequestType.PURCHASE_REQUEST,
                  originalRequest);

      DetailMessage detailMessage = (DetailMessage) errorDetail.getDetailMessage();
      detailMessage.setProduct(originalRequest.getProduct());

      PurchaseResponse rsp = getPurchaseResponseFromCache(requestKey);
      if (rsp != null) {
         detailMessage.setVoucher(rsp.getVoucher());
      }
      return Response.status(400).entity(errorDetail).build();
   }

   private static boolean isPurchaseRequestProvisioned(String purchaseRequestId, String username, String password) {
      return getPurchaseRequestFromCache(purchaseRequestId, username, password) != null;
   }

   private static PurchaseRequest getPurchaseRequestFromCache(
         String purchaseRequestId,
         String username,
         String password) {
      ConcurrentHashMap<RequestKey, PurchaseRequest> purchaseRequestRecords =
            AirtimeTestServerRunner.getTestServer().getPurchaseRequestRecords();
      RequestKey provisionKey =
            new RequestKey(username, password, PurchaseResource.Purchase.PURCHASE, purchaseRequestId);
      log.debug(
            String.format(
                  "Searching for purchase request provision record under following key: %s",
                  provisionKey.toString()));
      return purchaseRequestRecords.get(provisionKey);
   }

   private static PurchaseReversal getPurchaseReversalFromCache(
         String purchaseRequestId,
         String username,
         String password) {
      ConcurrentHashMap<RequestKey, PurchaseReversal> reversalRecords =
            AirtimeTestServerRunner.getTestServer().getPurchaseReversalRecords();
      RequestKey reversalKey =
            new RequestKey(username, password, PurchaseResource.ReversePurchase.REVERSE_PURCHASE, purchaseRequestId);
      return reversalRecords.get(reversalKey);
   }

   private static PurchaseConfirmation getPurchaseConfirmationFromCache(
         String purchaseRequestId,
         String username,
         String password) {
      ConcurrentHashMap<RequestKey, PurchaseConfirmation> purchaseConfirmationRecords =
            AirtimeTestServerRunner.getTestServer().getPurchaseConfirmationRecords();
      RequestKey confirmKey =
            new RequestKey(
                  username,
                  password,
                  PurchaseResource.ConfirmPurchase.PURCHASE_CONFIRMATION,
                  purchaseRequestId);
      return purchaseConfirmationRecords.get(confirmKey);
   }

   private static PurchaseResponse getPurchaseResponseFromCache(RequestKey purchaseRequestKey) {
      ConcurrentHashMap<RequestKey, PurchaseResponse> responseRecords =
            AirtimeTestServerRunner.getTestServer().getPurchaseResponseRecords();
      return responseRecords.get(purchaseRequestKey);
   }

   private static PurchaseResponse getPurchaseResponseFromCache(
         String purchaseRequestId,
         String username,
         String password) {
      RequestKey purchaseRequestKey =
            new RequestKey(username, password, PurchaseResource.Purchase.PURCHASE, purchaseRequestId);
      ConcurrentHashMap<RequestKey, PurchaseResponse> responseRecords =
            AirtimeTestServerRunner.getTestServer().getPurchaseResponseRecords();
      return responseRecords.get(purchaseRequestKey);
   }
}
