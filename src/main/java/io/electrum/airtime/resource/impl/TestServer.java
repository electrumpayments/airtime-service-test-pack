package io.electrum.airtime.resource.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;

import io.electrum.airtime.api.model.VoucherConfirmation;
import io.electrum.airtime.api.model.VoucherRequest;
import io.electrum.airtime.api.model.VoucherResponse;
import io.electrum.airtime.api.model.VoucherReversal;
import io.electrum.airtime.api.model.VoucherVoid;
import io.electrum.airtime.server.util.RequestKey;

public class TestServer extends ResourceConfig {

   private ConcurrentHashMap<RequestKey, VoucherRequest> provisionRecords;
   private ConcurrentHashMap<RequestKey, VoucherResponse> responseRecords;
   private ConcurrentHashMap<RequestKey, VoucherReversal> reversalRecords;
   private ConcurrentHashMap<RequestKey, VoucherConfirmation> confirmationRecords;
   private ConcurrentHashMap<RequestKey, VoucherVoid> voidRecords;
   private static final PropertyNamingStrategy LOWER_CASE_WITH_HYPHEN_STRATEGY = new LowerCaseWitHyphenStrategy();
   private static final Logger log = LoggerFactory.getLogger(TestServer.class.getPackage().getName());

   public TestServer() {
      packages(TestServer.class.getPackage().getName());

      register(MyObjectMapperProvider.class);
      register(JacksonFeature.class);
      provisionRecords = new ConcurrentHashMap<RequestKey, VoucherRequest>();
      log.debug("Initing new TestServer");
      responseRecords = new ConcurrentHashMap<RequestKey, VoucherResponse>();
      reversalRecords = new ConcurrentHashMap<RequestKey, VoucherReversal>();
      confirmationRecords = new ConcurrentHashMap<RequestKey, VoucherConfirmation>();
      voidRecords = new ConcurrentHashMap<RequestKey, VoucherVoid>();
   }

   public ConcurrentHashMap<RequestKey, VoucherRequest> getProvisionRecords() {
      return provisionRecords;
   }

   public void setProvisionRecords(ConcurrentHashMap<RequestKey, VoucherRequest> provisionRecords) {
      this.provisionRecords = provisionRecords;
   }

   public ConcurrentHashMap<RequestKey, VoucherResponse> getResponseRecords() {
      return responseRecords;
   }

   public void setResponseRecords(ConcurrentHashMap<RequestKey, VoucherResponse> responseRecords) {
      this.responseRecords = responseRecords;
   }

   public ConcurrentHashMap<RequestKey, VoucherReversal> getReversalRecords() {
      return reversalRecords;
   }

   public void setReversalRecords(ConcurrentHashMap<RequestKey, VoucherReversal> reversalRecords) {
      this.reversalRecords = reversalRecords;
   }

   public ConcurrentHashMap<RequestKey, VoucherConfirmation> getConfirmationRecords() {
      return confirmationRecords;
   }

   public void setConfirmationRecords(ConcurrentHashMap<RequestKey, VoucherConfirmation> confirmationRecords) {
      this.confirmationRecords = confirmationRecords;
   }

   public ConcurrentHashMap<RequestKey, VoucherVoid> getVoidRecords() {
      return voidRecords;
   }

   public void setVoidRecords(ConcurrentHashMap<RequestKey, VoucherVoid> voidRecords) {
      this.voidRecords = voidRecords;
   }

   @Provider
   public static class MyObjectMapperProvider implements ContextResolver<ObjectMapper> {

      private final ObjectMapper mapper;
      private final DateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss:SSS");

      public MyObjectMapperProvider() {
         mapper = new ObjectMapper();
         mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
         mapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
         mapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
         mapper.setPropertyNamingStrategy(LOWER_CASE_WITH_HYPHEN_STRATEGY);
         mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
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
