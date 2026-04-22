package com.smartcampus.mapper;

import com.smartcampus.exception.ErrorResponse;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.logging.Logger;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable exception) {

        // 1. Check if it's a WebApplicationException (400, 404, 405, 415, etc.)
        // 2. Also check if the cause is a WebApplicationException (Jersey sometimes wraps them)
        Throwable target = exception;
        if (!(target instanceof WebApplicationException) && target.getCause() instanceof WebApplicationException) {
            target = target.getCause();
        }

        if (target instanceof WebApplicationException) {
            WebApplicationException wae = (WebApplicationException) target;
            Response r = wae.getResponse();
            
            // If the response already has an entity (like a default Jersey error), return it
            if (r.hasEntity()) {
                return r;
            }

            int status = r.getStatus();
            String errorTitle = "Request Error";
            String errorMessage = target.getMessage();

            // Specialize messages for common issues
            if (status == 415) {
                errorMessage = "Unsupported Media Type. Please ensure your 'Content-Type' header is set to 'application/json'.";
            } else if (status == 400) {
                errorMessage = "Bad Request. Please check your JSON syntax or required fields.";
            } else if (status == 405) {
                errorMessage = "Method Not Allowed. Check if you are using the correct HTTP verb (GET, POST, etc.) for this URL.";
            }

            return Response.status(status)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ErrorResponse(status, errorTitle, errorMessage))
                    .build();
        }

        // Log unexpected stack traces on the server side
        LOGGER.severe("Unexpected error caught by global handler: " + exception.getMessage());
        exception.printStackTrace(); // Useful for debugging

        // Show client only a generic message for real internal errors
        ErrorResponse error = new ErrorResponse(
                500,
                "Internal Server Error",
                "An unexpected error occurred. Please check your request format or contact the administrator.");

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }
}