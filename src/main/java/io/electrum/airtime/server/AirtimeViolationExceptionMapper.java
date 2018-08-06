package io.electrum.airtime.server;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.glassfish.jersey.server.model.Invocable;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import io.dropwizard.jersey.validation.ConstraintMessage;
import io.dropwizard.jersey.validation.JerseyViolationException;
import io.electrum.airtime.server.util.AirtimeModelUtils;

public class AirtimeViolationExceptionMapper implements ExceptionMapper<JerseyViolationException> {
   @Override
   public Response toResponse(JerseyViolationException exception) {
      final Invocable invocable = exception.getInvocable();
      final ImmutableList<String> errors =
            FluentIterable.from(exception.getConstraintViolations())
                  .transform(violation -> ConstraintMessage.getMessage(violation, invocable))
                  .toList();

      return Response.status(Response.Status.BAD_REQUEST).entity(AirtimeModelUtils.buildFormatErrorRsp(errors)).build();
   }
}
