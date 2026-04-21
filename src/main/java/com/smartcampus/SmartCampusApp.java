package com.smartcampus;

import com.smartcampus.config.ApplicationConfig;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;

import java.net.URI;
import java.util.logging.Logger;

public class SmartCampusApp {

    private static final Logger LOGGER = Logger.getLogger(SmartCampusApp.class.getName());

    // Server runs on this URL
    public static final String BASE_URI = "http://localhost:8080/";

    public static void main(String[] args) throws Exception {

        // Start the Grizzly embedded server
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(
                URI.create(BASE_URI),
                new ApplicationConfig());

        LOGGER.info("Smart Campus API started!");
        LOGGER.info("Available at: " + BASE_URI + "api/v1");
        LOGGER.info("Press ENTER to stop the server...");

        System.in.read(); // Keep server running until you press Enter
        server.shutdown();
    }
}