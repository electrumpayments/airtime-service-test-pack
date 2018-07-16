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
import io.electrum.airtime.api.model.PurchaseRequest;
import io.electrum.airtime.api.model.PurchaseResponse;
import io.electrum.airtime.api.model.PurchaseReversal;
import io.electrum.airtime.server.AirtimeTestServerRunner;
import io.electrum.airtime.server.model.DetailMessage;
import io.electrum.vas.JsonUtil;
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

   protected static Msisdn buildMsisdn(PurchaseRequest purchaseRequest) {
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

   public static void validatePurchaseRequest(PurchaseRequest purchaseRequest, Set<ConstraintViolation<?>> violations) {
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
      PurchaseRequest originalRequest = purchaseRequestRecords.get(requestKey);
      if (originalRequest != null) {
         return buildDuplicateErrorResponse(purchaseRequestId, requestKey, originalRequest);
      }

      ConcurrentHashMap<RequestKey, PurchaseReversal> reversalRecords =
            AirtimeTestServerRunner.getTestServer().getPurchaseReversalRecords();
      RequestKey reversalKey =
            new RequestKey(username, password, PurchaseResource.ReversePurchase.REVERSE_PURCHASE, purchaseRequestId);
      BasicReversal reversal = reversalRecords.get(reversalKey);
      if (reversal != null) {
         return buildAlreadyReversedErrorResponse(purchaseRequestId, requestKey, reversal);
      }

      return null;
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

      DetailMessage detailMessage = (DetailMessage) errorDetail.getDetailMessage();
      ConcurrentHashMap<RequestKey, PurchaseResponse> responseRecords =
            AirtimeTestServerRunner.getTestServer().getPurchaseResponseRecords();
      PurchaseResponse rsp = responseRecords.get(requestKey);
      if (rsp != null) {
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

      ConcurrentHashMap<RequestKey, PurchaseResponse> responseRecords =
            AirtimeTestServerRunner.getTestServer().getPurchaseResponseRecords();
      PurchaseResponse rsp = responseRecords.get(requestKey);
      if (rsp != null) {
         detailMessage.setVoucher(rsp.getVoucher());
      }
      return Response.status(400).entity(errorDetail).build();
   }
}
