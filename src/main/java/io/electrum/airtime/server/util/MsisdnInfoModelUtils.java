package io.electrum.airtime.server.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.core.Response;

import org.joda.time.DateTime;

import io.electrum.airtime.api.model.ErrorDetail;
import io.electrum.airtime.api.model.Msisdn;
import io.electrum.airtime.api.model.MsisdnInfoResponse;
import io.electrum.airtime.api.model.Product;
import io.electrum.airtime.api.model.Promotion;
import io.electrum.vas.model.Institution;
import io.electrum.vas.model.LedgerAmount;

public class MsisdnInfoModelUtils extends AirtimeModelUtils {

   protected static HashMap<String, List<Product>> msisdnProductPairing = new HashMap<>();
   public static final String MSISDN_EXAMPLE_ONE = "27749670999";
   public static final String MSISDN_EXAMPLE_TWO = "98987654356789";

   protected static HashMap<String, Promotion> msisdnPromotionPairing = new HashMap<>();

   static {
      List<Product> productList = new ArrayList<>();
      productList.add(createDataProduct());
      productList.add(createAirtimeProduct());
      msisdnProductPairing.put(MSISDN_EXAMPLE_ONE, productList);

      List<Product> productListTwo = new ArrayList<>(productList);
      productListTwo.add(createSmsProduct());
      msisdnProductPairing.put(MSISDN_EXAMPLE_TWO, productListTwo);

      msisdnPromotionPairing.put(MSISDN_EXAMPLE_ONE, createPromotion());
   }

   public static Response canMsisdnInfoRequest(String msisdn) {
      if (msisdn == null) {
         ErrorDetail errorDetail =
               buildErrorDetail(
                     null,
                     "Msisdn query parameter is not set.",
                     "Msisdn was not set but is a mandatory field for msisdn lookups.",
                     null,
                     ErrorDetail.RequestType.MSISDN_INFO_REQUEST,
                     ErrorDetail.ErrorType.FORMAT_ERROR);
         return Response.status(400).entity(errorDetail).build();
      }

      return null;
   }

   public static MsisdnInfoResponse msisdnInfoResponseFromMsisdn(String msisdn, String operator) {
      MsisdnInfoResponse msisdnInfoResponse = new MsisdnInfoResponse();

      msisdnInfoResponse.setMsisdn(buildRandomizedMsisdn(msisdn, operator));

      if (msisdnProductPairing.containsKey(msisdn)) {
         List<Product> productList = msisdnProductPairing.get(msisdn);
         msisdnInfoResponse.setAvaialbleProducts(productList.toArray(new Product[productList.size()]));
      }

      if (msisdnPromotionPairing.containsKey(msisdn)) {
         msisdnInfoResponse.setPromotion(msisdnPromotionPairing.get(msisdn));
      }

      return msisdnInfoResponse;
   }

   private static Msisdn buildRandomizedMsisdn(String msisdn, String operator) {
      return new Msisdn().msisdn(msisdn).operator(new Institution().id(RandomData.random09(5)).name(operator)).country(
            RandomData.randomAZ(3));
   }

   private static Promotion createPromotion() {
      return new Promotion().promotion("Example Promotion").startDate(new DateTime()).endDate(
            new DateTime().plusDays(Integer.parseInt(RandomData.random09(2))));
   }

   private static Product createDataProduct() {
      LedgerAmount[] productValues = new LedgerAmount[2];
      productValues[0] = new LedgerAmount().currency("710").amount(100L);
      productValues[1] = new LedgerAmount().currency("812").amount(50L);
      return new Product().name("Data 100mb")
            .type(Product.ProductType.DATA)
            .productId("89")
            .productValues(productValues)
            .wholesalePrice(new LedgerAmount().amount(40L).currency("710"))
            .recipientAmount(new LedgerAmount().amount(120L).currency("812"));
   }

   private static Product createAirtimeProduct() {
      LedgerAmount[] productValues = new LedgerAmount[2];
      productValues[0] = new LedgerAmount().currency("710").amount(29L);
      productValues[1] = new LedgerAmount().currency("212").amount(12L);
      return new Product().name("Airtime R29")
            .type(Product.ProductType.AIRTIME_FIXED)
            .productId("12")
            .productValues(productValues)
            .wholesalePrice(new LedgerAmount().amount(20L).currency("710"))
            .recipientAmount(new LedgerAmount().amount(25L).currency("212"));
   }

   private static Product createSmsProduct() {
      LedgerAmount[] productValues = new LedgerAmount[2];
      productValues[0] = new LedgerAmount().currency("710").amount(100L);
      productValues[1] = new LedgerAmount().currency("112").amount(39L);
      return new Product().name("Sms Bundle 100")
            .type(Product.ProductType.SMS_BUNDLE)
            .productId("76")
            .productValues(productValues)
            .wholesalePrice(new LedgerAmount().amount(25L).currency("710"))
            .recipientAmount(new LedgerAmount().amount(100L).currency("112"));
   }
}