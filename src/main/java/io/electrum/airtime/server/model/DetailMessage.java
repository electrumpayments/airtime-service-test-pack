package io.electrum.airtime.server.model;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.electrum.airtime.api.model.Institution;
import io.electrum.airtime.api.model.Merchant;
import io.electrum.airtime.api.model.Product;
import io.electrum.airtime.api.model.SlipData;
import io.electrum.airtime.api.model.Voucher;
import io.swagger.annotations.ApiModelProperty;

public class DetailMessage {

   private String pathId = null;
   private String voucherId = null;
   private String reversalId = null;
   private String confirmationId = null;
   private String voidId = null;
   private Product product = null;
   private String requestTime = null;
   private String reversalTime = null;
   private String confirmDate = null;
   private String voidDate = null;
   private Institution sender = null;
   private Merchant merchant = null;
   private Institution processor = null;
   private Institution vendor = null;
   private Voucher voucher = null;
   private SlipData slipData = null;
   private String freeString = null;
   private List<FormatError> formatErrors = null;

   /**
    * The randomly generated UUID identifying this voucher request, as defined for a variant 4 UUID in [RFC
    * 4122](https://tools.ietf.org/html/rfc4122). This must be the same as the voucherId path parameter.
    **/
   public DetailMessage pathId(String pathId) {
      this.pathId = pathId;
      return this;
   }

   @ApiModelProperty(value = "The randomly generated UUID identifying this voucher request, as defined for a variant 4 UUID in [RFC 4122](https://tools.ietf.org/html/rfc4122). This must be the same as the voucherId path parameter.")
   @JsonProperty("pathId")
   public String getPathId() {
      return pathId;
   }

   public void setPathId(String pathId) {
      this.pathId = pathId;
   }

   /**
    * The randomly generated UUID identifying this voucher request, as defined for a variant 4 UUID in [RFC
    * 4122](https://tools.ietf.org/html/rfc4122). This must be the same as the voucherId path parameter.
    **/
   public DetailMessage voucherId(String voucherId) {
      this.voucherId = voucherId;
      return this;
   }

   @ApiModelProperty(value = "The randomly generated UUID identifying this voucher request, as defined for a variant 4 UUID in [RFC 4122](https://tools.ietf.org/html/rfc4122). This must be the same as the voucherId path parameter.")
   @JsonProperty("voucherId")
   public String getVoucherId() {
      return voucherId;
   }

   public void setVoucherId(String voucherId) {
      this.voucherId = voucherId;
   }

   /**
    * The randomly generated UUID identifying this voucher reversal, as defined for a variant 4 UUID in [RFC
    * 4122](https://tools.ietf.org/html/rfc4122). This must be the same as the reversalId path parameter.
    **/
   public DetailMessage reversalId(String reversalId) {
      this.reversalId = reversalId;
      return this;
   }

   @ApiModelProperty(value = "The randomly generated UUID identifying this voucher reversal, as defined for a variant 4 UUID in [RFC 4122](https://tools.ietf.org/html/rfc4122). This must be the same as the reversalId path parameter.")
   @JsonProperty("reversalId")
   public String getReversalId() {
      return reversalId;
   }

   public void setReversalId(String reversalId) {
      this.reversalId = reversalId;
   }

   /**
    * The randomly generated UUID identifying this voucher confirmation, as defined for a variant 4 UUID in [RFC
    * 4122](https://tools.ietf.org/html/rfc4122). This must be the same as the confirmationId path parameter.
    **/
   public DetailMessage confirmationId(String confirmationId) {
      this.confirmationId = confirmationId;
      return this;
   }

   @ApiModelProperty(value = "The randomly generated UUID identifying this voucher confirmation, as defined for a variant 4 UUID in [RFC 4122](https://tools.ietf.org/html/rfc4122). This must be the same as the confirmationId path parameter.")
   @JsonProperty("confirmationId")
   public String getConfirmationId() {
      return confirmationId;
   }

   public void setConfirmationId(String confirmationId) {
      this.confirmationId = confirmationId;
   }

   /**
    * The randomly generated UUID identifying this voucher void, as defined for a variant 4 UUID in [RFC
    * 4122](https://tools.ietf.org/html/rfc4122). This must be the same as the voidId path parameter.
    **/
   public DetailMessage voidId(String voidId) {
      this.voidId = voidId;
      return this;
   }

   @ApiModelProperty(value = "The randomly generated UUID identifying this voucher void, as defined for a variant 4 UUID in [RFC 4122](https://tools.ietf.org/html/rfc4122). This must be the same as the voidId path parameter.")
   @JsonProperty("voidId")
   public String getVoidId() {
      return voidId;
   }

   public void setVoidId(String voidId) {
      this.voidId = voidId;
   }

   /**
    * The product for which the voucher should be provisioned.
    **/
   public DetailMessage product(Product product) {
      this.product = product;
      return this;
   }

   @ApiModelProperty(value = "The product for which the voucher should be provisioned.")
   @JsonProperty("product")
   public Product getProduct() {
      return product;
   }

   public void setProduct(Product product) {
      this.product = product;
   }

   /**
    * The date and time of the request as recorded by the sender. The format shall be as defined for date-time in [RFC
    * 3339 section 5.6](https://tools.ietf.org/html/rfc3339#section-5.6). It is recommended that the optional
    * time-secfrac be included up to millisecond precision.
    **/
   public DetailMessage requestTime(String requestTime) {
      this.requestTime = requestTime;
      return this;
   }

   @ApiModelProperty(value = "The date and time of the request as recorded by the sender. The format shall be as defined for date-time in [RFC 3339 section 5.6](https://tools.ietf.org/html/rfc3339#section-5.6). It is recommended that the optional time-secfrac be included up to millisecond precision.")
   @JsonProperty("requestTime")
   public String getRequestTime() {
      return requestTime;
   }

   public void setRequestTime(String requestTime) {
      this.requestTime = requestTime;
   }

   /**
    * The date and time of the reversal as recorded by the sender. The format shall be as defined for date-time in [RFC
    * 3339 section 5.6](https://tools.ietf.org/html/rfc3339#section-5.6). It is recommended that the optional
    * time-secfrac be included up to millisecond precision.
    **/
   public DetailMessage reversalTime(String reversalTime) {
      this.reversalTime = reversalTime;
      return this;
   }

   @ApiModelProperty(value = "The date and time of the reversal as recorded by the sender. The format shall be as defined for date-time in [RFC 3339 section 5.6](https://tools.ietf.org/html/rfc3339#section-5.6). It is recommended that the optional time-secfrac be included up to millisecond precision.")
   @JsonProperty("reversalTime")
   public String getReversalTime() {
      return reversalTime;
   }

   public void setReversalTime(String reversalTime) {
      this.reversalTime = reversalTime;
   }

   /**
    * The date and time of the confirmation as recorded by the sender. The format shall be as defined for date-time in
    * [RFC 3339 section 5.6](https://tools.ietf.org/html/rfc3339#section-5.6). It is recommended that the optional
    * time-secfrac be included up to millisecond precision.
    **/
   public DetailMessage confirmDate(String confirmDate) {
      this.confirmDate = confirmDate;
      return this;
   }

   @ApiModelProperty(value = "The date and time of the confirmation as recorded by the sender. The format shall be as defined for date-time in [RFC 3339 section 5.6](https://tools.ietf.org/html/rfc3339#section-5.6). It is recommended that the optional time-secfrac be included up to millisecond precision.")
   @JsonProperty("confirmDate")
   public String getConfirmDate() {
      return confirmDate;
   }

   public void setConfirmDate(String confirmDate) {
      this.confirmDate = confirmDate;
   }

   /**
    * Information about the merchant who originated this request.
    **/
   public DetailMessage merchant(Merchant merchant) {
      this.merchant = merchant;
      return this;
   }

   /**
    * The date and time of the void as recorded by the sender. The format shall be as defined for date-time in [RFC 3339
    * section 5.6](https://tools.ietf.org/html/rfc3339#section-5.6). It is recommended that the optional time-secfrac be
    * included up to millisecond precision.
    **/
   public DetailMessage voidDate(String voidDate) {
      this.voidDate = voidDate;
      return this;
   }

   @ApiModelProperty(value = "The date and time of the void as recorded by the sender. The format shall be as defined for date-time in [RFC 3339 section 5.6](https://tools.ietf.org/html/rfc3339#section-5.6). It is recommended that the optional time-secfrac be included up to millisecond precision.")
   @JsonProperty("voidDate")
   public String getVoidDate() {
      return voidDate;
   }

   public void setVoidDate(String voidDate) {
      this.voidDate = voidDate;
   }

   @ApiModelProperty(value = "Information about the merchant who originated this request.")
   @JsonProperty("merchant")
   public Merchant getMerchant() {
      return merchant;
   }

   public void setMerchant(Merchant merchant) {
      this.merchant = merchant;
   }

   /**
    * Information about the sender of this request.
    **/
   public DetailMessage sender(Institution sender) {
      this.sender = sender;
      return this;
   }

   @ApiModelProperty(value = "Information about the sender of this request.")
   @JsonProperty("sender")
   public Institution getSender() {
      return sender;
   }

   public void setSender(Institution sender) {
      this.sender = sender;
   }

   /**
    * Information about the processor who should process this request.
    **/
   public DetailMessage processor(Institution processor) {
      this.processor = processor;
      return this;
   }

   @ApiModelProperty(value = "Information about the processor who should process this request.")
   @JsonProperty("processor")
   public Institution getProcessor() {
      return processor;
   }

   public void setProcessor(Institution processor) {
      this.processor = processor;
   }

   /**
    * Information about the vendor who should process this request.
    **/
   public DetailMessage vendor(Institution vendor) {
      this.vendor = vendor;
      return this;
   }

   @ApiModelProperty(value = "Information about the vendor who should process this request.")
   @JsonProperty("vendor")
   public Institution getVendor() {
      return vendor;
   }

   public void setVendor(Institution vendor) {
      this.vendor = vendor;
   }

   /**
    * The voucher provisioned if the vendor processed the request successfully.
    **/
   public DetailMessage voucher(Voucher voucher) {
      this.voucher = voucher;
      return this;
   }

   @ApiModelProperty(value = "The voucher provisioned if the vendor processed the request successfully.")
   @JsonProperty("voucher")
   public Voucher getVoucher() {
      return voucher;
   }

   public void setVoucher(Voucher voucher) {
      this.voucher = voucher;
   }

   /**
    * Data to be printed on the slip in addition to the voucher instructions.
    **/
   public DetailMessage slipData(SlipData slipData) {
      this.slipData = slipData;
      return this;
   }

   @ApiModelProperty(value = "Data to be printed on the slip in addition to the voucher instructions.")
   @JsonProperty("slipData")
   public SlipData getSlipData() {
      return slipData;
   }

   public void setSlipData(SlipData slipData) {
      this.slipData = slipData;
   }

   /**
    * Data to be printed on the slip in addition to the voucher instructions.
    **/
   public DetailMessage freeString(String freeString) {
      this.freeString = freeString;
      return this;
   }

   @ApiModelProperty(value = "Free string which may provide further information.")
   @JsonProperty("freeString")
   public String getFreeString() {
      return freeString;
   }

   public void setFreeString(String freeString) {
      this.freeString = freeString;
   }

   /**
    * Data to be printed on the slip in addition to the voucher instructions.
    **/
   public DetailMessage formatErrors(List<FormatError> formatErrors) {
      this.formatErrors = formatErrors;
      return this;
   }

   @ApiModelProperty(value = "List of incorrectly formatted fields and a description of the formatting error.")
   @JsonProperty("formatErrors")
   public List<FormatError> getFormatErrors() {
      return formatErrors;
   }

   public void setFormatErrors(List<FormatError> formatErrors) {
      this.formatErrors = formatErrors;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }
      DetailMessage detailMessage = (DetailMessage) o;
      return Objects.equals(pathId, detailMessage.pathId) && Objects.equals(voucherId, detailMessage.voucherId)
            && Objects.equals(reversalId, detailMessage.reversalId)
            && Objects.equals(confirmationId, detailMessage.confirmationId)
            && Objects.equals(voidId, detailMessage.voidId) && Objects.equals(product, detailMessage.product)
            && Objects.equals(requestTime, detailMessage.requestTime)
            && Objects.equals(reversalTime, detailMessage.reversalTime)
            && Objects.equals(confirmDate, detailMessage.confirmDate)
            && Objects.equals(voidDate, detailMessage.voidDate) && Objects.equals(merchant, detailMessage.merchant)
            && Objects.equals(sender, detailMessage.sender) && Objects.equals(processor, detailMessage.processor)
            && Objects.equals(vendor, detailMessage.vendor) && Objects.equals(voucher, detailMessage.voucher)
            && Objects.equals(slipData, detailMessage.slipData) && Objects.equals(freeString, detailMessage.freeString)
            && Objects.equals(formatErrors, detailMessage.formatErrors);
   }

   @Override
   public int hashCode() {
      return Objects.hash(
            pathId,
            voucherId,
            reversalId,
            confirmationId,
            voidId,
            product,
            requestTime,
            reversalTime,
            confirmDate,
            voidDate,
            merchant,
            sender,
            processor,
            vendor,
            voucher,
            slipData,
            freeString,
            formatErrors);
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("class DetailMessage {\n");

      sb.append("    pathId: ").append(toIndentedString(pathId)).append("\n");
      sb.append("    voucherId: ").append(toIndentedString(voucherId)).append("\n");
      sb.append("    reversalId: ").append(toIndentedString(reversalId)).append("\n");
      sb.append("    confirmationId: ").append(toIndentedString(confirmationId)).append("\n");
      sb.append("    voidId: ").append(toIndentedString(voidId)).append("\n");
      sb.append("    product: ").append(toIndentedString(product)).append("\n");
      sb.append("    requestTime: ").append(toIndentedString(requestTime)).append("\n");
      sb.append("    reversalTime: ").append(toIndentedString(reversalTime)).append("\n");
      sb.append("    confirmDate: ").append(toIndentedString(confirmDate)).append("\n");
      sb.append("    voidDate: ").append(toIndentedString(voidDate)).append("\n");
      sb.append("    merchant: ").append(toIndentedString(merchant)).append("\n");
      sb.append("    sender: ").append(toIndentedString(sender)).append("\n");
      sb.append("    processor: ").append(toIndentedString(processor)).append("\n");
      sb.append("    vendor: ").append(toIndentedString(vendor)).append("\n");
      sb.append("    voucher: ").append(toIndentedString(voucher)).append("\n");
      sb.append("    slipData: ").append(toIndentedString(slipData)).append("\n");
      sb.append("    freeString: ").append(toIndentedString(freeString)).append("\n");
      sb.append("    formatErrors: ").append(toIndentedString(formatErrors)).append("\n");
      sb.append("}");
      return sb.toString();
   }

   /**
    * Convert the given object to string with each line indented by 4 spaces (except the first line).
    */
   private String toIndentedString(Object o) {
      if (o == null) {
         return "null";
      }
      return o.toString().replace("\n", "\n    ");
   }
}
