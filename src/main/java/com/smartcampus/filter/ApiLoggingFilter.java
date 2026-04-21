package com.smartcampus.filter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.util.logging.Logger;

@Provider
public class ApiLoggingFilter
        implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOGGER = Logger.getLogger(ApiLoggingFilter.class.getName());

    // -------------------------------------------------------
    // Fires on every incoming REQUEST
    // -------------------------------------------------------
    @Override
    public void filter(ContainerRequestContext requestContext)
            throws IOException {

        String method = requestContext.getMethod();
        String uri = requestContext.getUriInfo().getRequestUri().toString();

        LOGGER.info(">> INCOMING REQUEST  | Method: " + method +
                " | URI: " + uri);
    }

    // -------------------------------------------------------
    // Fires on every outgoing RESPONSE
    // -------------------------------------------------------
    @Override
    public void filter(ContainerRequestContext requestContext,
            ContainerResponseContext responseContext)
            throws IOException {

        int statusCode = responseContext.getStatus();
        String method = requestContext.getMethod();
        String uri = requestContext.getUriInfo()
                .getRequestUri().toString();

        LOGGER.info("<< OUTGOING RESPONSE | Method: " + method +
                " | URI: " + uri +
                " | Status: " + statusCode);
    }
}