package io.electrum.airtime.server.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.electrum.vas.Utils;
import io.electrum.vas.model.Amounts;
import io.swagger.annotations.ApiModelProperty;

public class PurchaseDetailMessage extends DetailMessage {
   private Amounts amounts = null;

   public PurchaseDetailMessage(DetailMessage detailMessage) {
      this.pathId = detailMessage.pathId;
   }

   @ApiModelProperty(value = "Amounts")
   @JsonProperty("amounts")
   public Amounts getAmounts() {
      return amounts;
   }

   public void setAmounts(Amounts amounts) {
      this.amounts = amounts;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o)
         return true;
      if (!(o instanceof PurchaseDetailMessage))
         return false;
      if (!super.equals(o))
         return false;
      PurchaseDetailMessage that = (PurchaseDetailMessage) o;
      return Objects.equals(amounts, that.amounts);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), amounts);
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("class PurchaseDetailMessage {\n");

      sb.append("    pathId: ").append(Utils.toIndentedString(pathId)).append("\n");
      sb.append("    voucherId: ").append(Utils.toIndentedString(voucherId)).append("\n");
      sb.append("    reversalId: ").append(Utils.toIndentedString(reversalId)).append("\n");
      sb.append("    confirmationId: ").append(Utils.toIndentedString(confirmationId)).append("\n");
      sb.append("    voidId: ").append(Utils.toIndentedString(voidId)).append("\n");
      sb.append("    product: ").append(Utils.toIndentedString(product)).append("\n");
      sb.append("    requestTime: ").append(Utils.toIndentedString(requestTime)).append("\n");
      sb.append("    reversalTime: ").append(Utils.toIndentedString(reversalTime)).append("\n");
      sb.append("    confirmDate: ").append(Utils.toIndentedString(confirmDate)).append("\n");
      sb.append("    voidDate: ").append(Utils.toIndentedString(voidDate)).append("\n");
      sb.append("    originator: ").append(Utils.toIndentedString(originator)).append("\n");
      sb.append("    sender: ").append(Utils.toIndentedString(client)).append("\n");
      sb.append("    processor: ").append(Utils.toIndentedString(settlementEntity)).append("\n");
      sb.append("    vendor: ").append(Utils.toIndentedString(receiver)).append("\n");
      sb.append("    voucher: ").append(Utils.toIndentedString(voucher)).append("\n");
      sb.append("    slipData: ").append(Utils.toIndentedString(slipData)).append("\n");
      sb.append("    thirdPartyIdentifiers: ").append(Utils.toIndentedString(thirdPartyIdentifiers)).append("\n");
      sb.append("    freeString: ").append(Utils.toIndentedString(freeString)).append("\n");
      sb.append("    formatErrors: ").append(Utils.toIndentedString(formatErrors)).append("\n");
      sb.append("    amounts: ").append(Utils.toIndentedString(amounts)).append("\n");
      sb.append("}");
      return sb.toString();
   }
}
