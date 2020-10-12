package io.electrum.airtime.handler.purchase;

import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import io.electrum.airtime.api.PurchaseResource;
import io.electrum.airtime.api.model.ErrorDetail;
import io.electrum.airtime.api.model.PurchaseRequest;
import io.electrum.airtime.api.model.PurchaseResponse;
import io.electrum.airtime.handler.BaseHandler;
import io.electrum.airtime.server.AirtimeTestServerRunner;
import io.electrum.airtime.server.util.PurchaseModelUtils;
import io.electrum.airtime.server.util.RequestKey;
import io.electrum.vas.model.Institution;

public class PurchaseRequestHandler extends BaseHandler {

   private List<String> smsProducts = Arrays.asList("product1");

   public PurchaseRequestHandler(HttpHeaders httpHeaders) {
      super(httpHeaders);
   }

   public Response handle(PurchaseRequest purchaseRequest, UriInfo uriInfo) {
      try {
         Response rsp;

         if (!validUsername(purchaseRequest)) {
            return PurchaseModelUtils.buildIncorrectUsernameErrorResponse(
                  purchaseRequest.getId(),
                  purchaseRequest.getClient(),
                  username,
                  ErrorDetail.RequestType.PURCHASE_REQUEST);
         }

         rsp = PurchaseModelUtils.canPurchasePurchaseRequest(purchaseRequest.getId(), username, password);
         if (rsp != null) {
            return rsp;
         }

         // store purchase request in db
         addPurchaseRequestToCache(purchaseRequest);

         // generate purchase response with randomized fields and store in db
         PurchaseResponse purchaseResponse = PurchaseModelUtils.purchaseRspFromReq(purchaseRequest);

         addPurchaseResponseToCache(purchaseResponse);

         addPurchaseReferenceToCache(purchaseResponse);

         if (smsProducts.contains(purchaseRequest.getProduct().getProductId())
               && purchaseRequest.getRecipientMsisdn() != null) {
            sendSms(purchaseResponse);
         }

         rsp = Response.created(uriInfo.getRequestUri()).entity(purchaseResponse).build();

         return rsp;
      } catch (Exception e) {
         return logAndBuildException(e);
      }
   }

   private void sendSms(PurchaseResponse purchaseResponse) throws Exception {
      String url = "https://platform.clickatell.com/messages/http/send";
      String charset = StandardCharsets.UTF_8.name();
      String to = purchaseResponse.getMsisdn().getMsisdn();
      String content =
            String.format(
                  "Hello - Your airtime account has been topped up with R%s. Dial *123# to get your new airtime balance. Brought to you by %s.",
                  new BigDecimal(purchaseResponse.getAmounts().getApprovedAmount().getAmount()).movePointLeft(2).toString(),
                  purchaseResponse.getMsisdn().getOperator().getName());
      System.out.println(content);

      String query =
            String.format(
                  "apiKey=%s&to=%s&content=%s",
                  URLEncoder.encode(AirtimeTestServerRunner.getTestServer().getSmsApiKey(), charset),
                  URLEncoder.encode(to, charset),
                  URLEncoder.encode(content, charset));
      URLConnection connection = new URL(url + "?" + query).openConnection();
      connection.setRequestProperty("Accept-Charset", charset);
      InputStream response = new URL(url + "?" + query).openStream();
      try (Scanner scanner = new Scanner(response)) {
         String responseBody = scanner.useDelimiter("\\A").next();
         System.out.println(responseBody);
      }
   }

   private void addPurchaseRequestToCache(PurchaseRequest purchaseRequest) {
      ConcurrentHashMap<RequestKey, PurchaseRequest> purchaseRequestRecords =
            AirtimeTestServerRunner.getTestServer().getPurchaseRequestRecords();
      RequestKey key = new RequestKey(username, password, PurchaseResource.Purchase.PURCHASE, purchaseRequest.getId());
      purchaseRequestRecords.put(key, purchaseRequest);
   }

   private void addPurchaseResponseToCache(PurchaseResponse purchaseResponse) {
      ConcurrentHashMap<RequestKey, PurchaseResponse> responseRecords =
            AirtimeTestServerRunner.getTestServer().getPurchaseResponseRecords();
      RequestKey key = new RequestKey(username, password, PurchaseResource.Purchase.PURCHASE, purchaseResponse.getId());
      responseRecords.put(key, purchaseResponse);
   }

   /**
    * Extracts the settlement entity that was generated for the purchase response and uses the settlement entity as the
    * purchase reference. The <purchaseRef, purchase response Id> mapping is stored so later on, someone could use the
    * purchase reference alone to get the associated purchase response back.
    *
    * @param purchaseResponse
    *           - the just generated purchase response
    */
   private void addPurchaseReferenceToCache(PurchaseResponse purchaseResponse) {
      Institution purchaseReference = purchaseResponse.getSettlementEntity();

      ConcurrentHashMap<RequestKey, String> purchaseReferenceRecords =
            AirtimeTestServerRunner.getTestServer().getPurchaseReferenceRecords();
      RequestKey key = new RequestKey(username, password, RequestKey.PURCHASE_REF_RESOURCE, purchaseReference.getId());
      purchaseReferenceRecords.put(key, purchaseResponse.getId());
   }

   private boolean validUsername(PurchaseRequest purchaseRequest) {
      return purchaseRequest.getClient().getId().equals(username);
   }

   @Override
   protected String getRequestName() {
      return "Purchase Request";
   }
}
