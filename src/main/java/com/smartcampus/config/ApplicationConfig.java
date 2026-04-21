package com.smartcampus.config;

import jakarta.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;

@ApplicationPath("/api/v1")
public class ApplicationConfig extends ResourceConfig {

    public ApplicationConfig() {
        // Scan all resource classes within the com.smartcampus package
        packages("com.smartcampus");
        
        // Register Jackson feature for JSON support
        register(org.glassfish.jersey.jackson.JacksonFeature.class);
    }
}