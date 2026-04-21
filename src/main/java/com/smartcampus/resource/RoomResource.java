package com.smartcampus.resource;

import com.smartcampus.exception.RoomNotFoundException;
import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.Room;
import com.smartcampus.store.DataStore;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    // -------------------------------------------------------
    // GET /api/v1/rooms → All rooms list
    // -------------------------------------------------------
    @GET
    public Response getAllRooms() {
        List<Room> allRooms = new ArrayList<>(DataStore.rooms.values());
        return Response.ok(allRooms).build();
    }

    // -------------------------------------------------------
    // POST /api/v1/rooms → Create new room
    // -------------------------------------------------------
    @POST
    public Response createRoom(Room room) {

        // Validate - id and name required
        if (room.getId() == null || room.getId().trim().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Room ID is required");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(error).build();
        }

        if (room.getName() == null || room.getName().trim().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Room name is required");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(error).build();
        }

        // Check duplicate ID
        if (DataStore.rooms.containsKey(room.getId())) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Room with ID '" + room.getId() + "' already exists");
            return Response.status(Response.Status.CONFLICT)
                    .entity(error).build();
        }

        // Save to DataStore
        DataStore.rooms.put(room.getId(), room);

        // Return 201 Created with the new room
        return Response.status(Response.Status.CREATED)
                .entity(room).build();
    }

    // -------------------------------------------------------
    // GET /api/v1/rooms/{roomId} → Single room
    // -------------------------------------------------------
    @GET
    @Path("/{roomId}")
    public Response getRoomById(@PathParam("roomId") String roomId) {

        Room room = DataStore.rooms.get(roomId);

        if (room == null) {
            throw new RoomNotFoundException(roomId);
        }

        return Response.ok(room).build();
    }

    // -------------------------------------------------------
    // DELETE /api/v1/rooms/{roomId} → Delete room
    // Business rule: cannot delete if sensors exist in room!
    // -------------------------------------------------------
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {

        Room room = DataStore.rooms.get(roomId);

        // Check if room exists
        if (room == null) {
            throw new RoomNotFoundException(roomId);
        }

        // Check if sensors are present - 409 Conflict if not empty
        if (room.getSensorIds() != null && !room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(roomId);
        }

        DataStore.rooms.remove(roomId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Room '" + roomId + "' successfully deleted");

        return Response.ok(response).build();
    }
}