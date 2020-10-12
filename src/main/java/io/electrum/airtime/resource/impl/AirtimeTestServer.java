package io.electrum.airtime.resource.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;

import io.dropwizard.jersey.validation.DropwizardConfiguredValidator;
import io.dropwizard.jersey.validation.HibernateValidationFeature;
import io.dropwizard.jersey.validation.Validators;
import io.electrum.airtime.api.model.PurchaseConfirmation;
import io.electrum.airtime.api.model.PurchaseRequest;
import io.electrum.airtime.api.model.PurchaseResponse;
import io.electrum.airtime.api.model.PurchaseReversal;
import io.electrum.airtime.api.model.VoucherConfirmation;
import io.electrum.airtime.api.model.VoucherRequest;
import io.electrum.airtime.api.model.VoucherResponse;
import io.electrum.airtime.server.AirtimeViolationExceptionMapper;
import io.electrum.airtime.server.util.RequestKey;
import io.electrum.vas.model.BasicReversal;

public class AirtimeTestServer extends ResourceConfig {

   private ConcurrentHashMap<RequestKey, VoucherRequest> provisionVoucherRecords;
   private ConcurrentHashMap<RequestKey, VoucherResponse> voucherResponseRecords;
   private ConcurrentHashMap<RequestKey, BasicReversal> voucherReversalRecords;
   private ConcurrentHashMap<RequestKey, VoucherConfirmation> voucherConfirmationRecords;

   private ConcurrentHashMap<RequestKey, PurchaseRequest> purchaseRequestRecords;
   private ConcurrentHashMap<RequestKey, PurchaseReversal> purchaseReversalRecords;
   private ConcurrentHashMap<RequestKey, PurchaseConfirmation> purchaseConfirmationRecords;
   private ConcurrentHashMap<RequestKey, PurchaseResponse> purchaseResponseRecords;
   // This hashmap stores the relationship between purchase references and purchase request id's so a purchase reference
   // can be used to retrieve the correlated purchase request id
   private ConcurrentHashMap<RequestKey, String> purchaseReferenceRecords;
   private String smsApiKey = null;

   private static final Logger log = LoggerFactory.getLogger(AirtimeTestServer.class.getPackage().getName());

   public AirtimeTestServer(String smsApiKey) {
      packages(AirtimeTestServer.class.getPackage().getName());

      register(MyObjectMapperProvider.class);
      register(JacksonFeature.class);

      register(
            new HibernateValidationFeature(
                  new DropwizardConfiguredValidator(Validators.newValidatorFactory().getValidator())));
      register(new AirtimeViolationExceptionMapper());

      provisionVoucherRecords = new ConcurrentHashMap<>();
      log.debug("Initialising new TestServer");
      voucherResponseRecords = new ConcurrentHashMap<>();
      voucherReversalRecords = new ConcurrentHashMap<>();
      voucherConfirmationRecords = new ConcurrentHashMap<>();

      purchaseRequestRecords = new ConcurrentHashMap<>();
      purchaseResponseRecords = new ConcurrentHashMap<>();
      purchaseReversalRecords = new ConcurrentHashMap<>();
      purchaseConfirmationRecords = new ConcurrentHashMap<>();

      purchaseReferenceRecords = new ConcurrentHashMap<>();
      this.smsApiKey = smsApiKey;
   }

   public ConcurrentHashMap<RequestKey, VoucherRequest> getProvisionVoucherRecords() {
      return provisionVoucherRecords;
   }

   public void setProvisionVoucherRecords(ConcurrentHashMap<RequestKey, VoucherRequest> provisionVoucherRecords) {
      this.provisionVoucherRecords = provisionVoucherRecords;
   }

   public ConcurrentHashMap<RequestKey, VoucherResponse> getVoucherResponseRecords() {
      return voucherResponseRecords;
   }

   public void setVoucherResponseRecords(ConcurrentHashMap<RequestKey, VoucherResponse> voucherResponseRecords) {
      this.voucherResponseRecords = voucherResponseRecords;
   }

   public ConcurrentHashMap<RequestKey, BasicReversal> getVoucherReversalRecords() {
      return voucherReversalRecords;
   }

   public void setVoucherReversalRecords(ConcurrentHashMap<RequestKey, BasicReversal> voucherReversalRecords) {
      this.voucherReversalRecords = voucherReversalRecords;
   }

   public ConcurrentHashMap<RequestKey, VoucherConfirmation> getVoucherConfirmationRecords() {
      return voucherConfirmationRecords;
   }

   public void setVoucherConfirmationRecords(
         ConcurrentHashMap<RequestKey, VoucherConfirmation> voucherConfirmationRecords) {
      this.voucherConfirmationRecords = voucherConfirmationRecords;
   }

   public ConcurrentHashMap<RequestKey, PurchaseRequest> getPurchaseRequestRecords() {
      return purchaseRequestRecords;
   }

   public void setPurchaseRequestRecords(ConcurrentHashMap<RequestKey, PurchaseRequest> purchaseRequestRecords) {
      this.purchaseRequestRecords = purchaseRequestRecords;
   }

   public ConcurrentHashMap<RequestKey, PurchaseResponse> getPurchaseResponseRecords() {
      return purchaseResponseRecords;
   }

   public void setPurchaseResponseRecords(ConcurrentHashMap<RequestKey, PurchaseResponse> purchaseResponseRecords) {
      this.purchaseResponseRecords = purchaseResponseRecords;
   }

   public ConcurrentHashMap<RequestKey, PurchaseReversal> getPurchaseReversalRecords() {
      return purchaseReversalRecords;
   }

   public void setPurchaseReversalRecords(ConcurrentHashMap<RequestKey, PurchaseReversal> purchaseReversalRecords) {
      this.purchaseReversalRecords = purchaseReversalRecords;
   }

   public ConcurrentHashMap<RequestKey, PurchaseConfirmation> getPurchaseConfirmationRecords() {
      return purchaseConfirmationRecords;
   }

   public void setPurchaseConfirmationRecords(
         ConcurrentHashMap<RequestKey, PurchaseConfirmation> purchaseConfirmationRecords) {
      this.purchaseConfirmationRecords = purchaseConfirmationRecords;
   }

   public ConcurrentHashMap<RequestKey, String> getPurchaseReferenceRecords() {
      return purchaseReferenceRecords;
   }

   public void setPurchaseReferenceRecords(ConcurrentHashMap<RequestKey, String> purchaseReferenceRecords) {
      this.purchaseReferenceRecords = purchaseReferenceRecords;
   }

   public String getSmsApiKey() {
      return smsApiKey;
   }

   public void setSmsApiKey(String smsApiKey) {
      this.smsApiKey = smsApiKey;
   }

   @Provider
   public static class MyObjectMapperProvider implements ContextResolver<ObjectMapper> {

      private final ObjectMapper mapper;
      private final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

      public MyObjectMapperProvider() {
         mapper = new ObjectMapper();
         // mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
         // mapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
         // mapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
         // mapper.setPropertyNamingStrategy(LOWER_CASE_WITH_HYPHEN_STRATEGY);
         // mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
         // DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
         // mapper.setDateFormat(DATE_FORMAT);
         mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
         mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
         mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
         mapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
         mapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
         mapper.registerModule(new JodaModule());
         DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
         mapper.setDateFormat(DATE_FORMAT);
      }

      @Override
      public ObjectMapper getContext(Class<?> type) {
         return mapper;
      }
   }

   @SuppressWarnings("serial")
   private static class LowerCaseWitHyphenStrategy extends PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy {
      @Override
      public String translate(String input) {
         String output = super.translate(input);
         return output == null ? null : output.replace('_', '-');
      }
   }
}
