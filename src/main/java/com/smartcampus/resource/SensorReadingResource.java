package com.smartcampus.resource;

import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import com.smartcampus.store.DataStore;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.*;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;

    // Passed from SensorResource via constructor
    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    // -------------------------------------------------------
    // GET /api/v1/sensors/{sensorId}/readings
    // All historical readings for this sensor
    // -------------------------------------------------------
    @GET
    public Response getReadings() {

        // Get readings list - default to empty list if none exist
        List<SensorReading> sensorReadings = DataStore.readings
                .getOrDefault(sensorId, new ArrayList<>());

        Map<String, Object> response = new HashMap<>();
        response.put("sensorId", sensorId);
        response.put("totalReadings", sensorReadings.size());
        response.put("readings", sensorReadings);

        return Response.ok(response).build();
    }

    // -------------------------------------------------------
    // POST /api/v1/sensors/{sensorId}/readings
    // Add new reading + update parent sensor's currentValue!
    // -------------------------------------------------------
    @POST
    public Response addReading(SensorReading reading) {

        Sensor sensor = DataStore.sensors.get(sensorId);

        if (sensor == null) {
            throw new com.smartcampus.exception.SensorNotFoundException(sensorId);
        }

        // MAINTENANCE sensor - 403 Forbidden!
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(sensorId);
        }

        // Also block OFFLINE sensors
        if ("OFFLINE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(sensorId);
        }

        // Validate reading value
        if (reading == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Reading body is required");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(error).build();
        }

        // Auto-generate ID if not provided
        if (reading.getId() == null || reading.getId().trim().isEmpty()) {
            reading.setId(UUID.randomUUID().toString());
        }

        // Auto-set timestamp if not provided
        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        // Save reading to DataStore
        DataStore.readings
                .computeIfAbsent(sensorId, k -> new ArrayList<>())
                .add(reading);

        // ⭐ CRITICAL: Update parent sensor's currentValue!
        sensor.setCurrentValue(reading.getValue());

        // Success response
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Reading recorded successfully");
        response.put("sensorId", sensorId);
        response.put("reading", reading);
        response.put("sensorCurrentValue", sensor.getCurrentValue());

        return Response.status(Response.Status.CREATED)
                .entity(response).build();
    }
}