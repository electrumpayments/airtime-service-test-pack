package io.electrum.airtime.handler.purchase;

import static io.electrum.airtime.server.util.AirtimeModelUtils.buildErrorDetail;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import io.electrum.airtime.api.model.ErrorDetail;
import io.electrum.airtime.api.model.PurchaseResponse;
import io.electrum.airtime.handler.BaseHandler;
import io.electrum.airtime.server.util.PurchaseModelUtils;

public class PurchaseStatusHandler extends BaseHandler {
   public PurchaseStatusHandler(HttpHeaders httpHeaders) {
      super(httpHeaders);
   }

   public Response handle(String provider, String purchaseReference, String originalMsgId) {
      try {
         Response rsp = PurchaseModelUtils.validatePurchaseStatus(provider, purchaseReference, originalMsgId);
         if (rsp != null) {
            return rsp;
         }

         if (originalMsgId == null) {

         } else {
            rsp = PurchaseModelUtils.canPurchaseStatusWithMsgId(originalMsgId, username, password);
            if (rsp != null) {
               return rsp;
            }

            PurchaseResponse purchaseResponse =
                  PurchaseModelUtils.getPurchaseResponseFromCache(originalMsgId, username, password);
            if (purchaseResponse != null) {
               rsp = Response.accepted(purchaseResponse).build();
            } else {
               rsp = buildNoPurchaseRspFoundErrorResponse(originalMsgId);
            }

            return rsp;

         }
         return null;
      } catch (Exception e) {
         return logAndBuildException(e);
      }
   }

   private static Response buildNoPurchaseRspFoundErrorResponse(String purchaseRequestId) {
      ErrorDetail errorDetail =
            buildErrorDetail(
                  null,
                  "Purchase Response not found.",
                  "No Purchase Response could be found with associated msg id.",
                  purchaseRequestId,
                  ErrorDetail.RequestType.PURCHASE_STATUS_REQUEST,
                  ErrorDetail.ErrorType.UNABLE_TO_LOCATE_RECORD);

      return Response.status(400).entity(errorDetail).build();
   }

   @Override
   protected String getRequestName() {
      return "Purchase Status";
   }
}
