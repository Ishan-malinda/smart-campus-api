package com.smartcampus.resource;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.exception.SensorNotFoundException;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.store.DataStore;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.*;
import java.util.stream.Collectors;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    // -------------------------------------------------------
    // GET /api/v1/sensors → All sensors
    // GET /api/v1/sensors?type=CO2 → Filter by type
    // -------------------------------------------------------
    @GET
    public Response getAllSensors(@QueryParam("type") String type) {

        List<Sensor> allSensors = new ArrayList<>(DataStore.sensors.values());

        // Filter if type query param is provided
        if (type != null && !type.trim().isEmpty()) {
            allSensors = allSensors.stream()
                    .filter(s -> s.getType().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
        }

        return Response.ok(allSensors).build();
    }

    // -------------------------------------------------------
    // POST /api/v1/sensors → Create new sensor
    // Validate that roomId exists!
    // -------------------------------------------------------
    @POST
    public Response createSensor(Sensor sensor) {

        // Basic validation
        if (sensor.getId() == null || sensor.getId().trim().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Sensor ID is required");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(error).build();
        }

        // Duplicate check
        if (DataStore.sensors.containsKey(sensor.getId())) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Sensor with ID '" + sensor.getId() +
                    "' already exists");
            return Response.status(Response.Status.CONFLICT)
                    .entity(error).build();
        }

        // Check if roomId exists - 422 if not found
        if (sensor.getRoomId() == null || sensor.getRoomId().trim().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "roomId is required");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(error).build();
        }

        Room linkedRoom = DataStore.rooms.get(sensor.getRoomId());
        if (linkedRoom == null) {
            throw new LinkedResourceNotFoundException(sensor.getRoomId());
        }

        // Default status ACTIVE
        if (sensor.getStatus() == null || sensor.getStatus().trim().isEmpty()) {
            sensor.setStatus("ACTIVE");
        }

        // Save sensor
        DataStore.sensors.put(sensor.getId(), sensor);

        // Update Room's sensorIds list
        linkedRoom.getSensorIds().add(sensor.getId());

        return Response.status(Response.Status.CREATED)
                .entity(sensor).build();
    }

    // -------------------------------------------------------
    // GET /api/v1/sensors/{sensorId} → Single sensor
    // -------------------------------------------------------
    @GET
    @Path("/{sensorId}")
    public Response getSensorById(@PathParam("sensorId") String sensorId) {

        Sensor sensor = DataStore.sensors.get(sensorId);

        if (sensor == null) {
            throw new SensorNotFoundException(sensorId);
        }

        return Response.ok(sensor).build();
    }

    // -------------------------------------------------------
    // Sub-resource locator → /sensors/{sensorId}/readings
    // Delegate to SensorReadingResource for path /sensors/{sensorId}/readings
    // -------------------------------------------------------
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingsResource(
            @PathParam("sensorId") String sensorId) {

        // Check if sensor exists
        Sensor sensor = DataStore.sensors.get(sensorId);
        if (sensor == null) {
            throw new SensorNotFoundException(sensorId);
        }

        // Return SensorReadingResource instance - pass sensorId
        return new SensorReadingResource(sensorId);
    }
}