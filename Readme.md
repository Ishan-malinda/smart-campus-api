# 🏛️ Smart Campus Sensor & Room Management API

> **5COSC022W - Client-Server Architectures Coursework (2025/26)**  
> A RESTful API built with JAX-RS (Jersey) and an embedded Grizzly HTTP server for managing university campus rooms and IoT sensors.

---

## 📋 Table of Contents

- [API Overview](#api-overview)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [How to Build & Run](#how-to-build--run)
- [API Endpoints](#api-endpoints)
- [Sample curl Commands](#sample-curl-commands)
- [Report: Conceptual Questions & Answers](#report-conceptual-questions--answers)

---

## API Overview

The Smart Campus API provides a comprehensive backend service for the university's campus-wide infrastructure. It enables facilities managers and automated building systems to:

- **Manage Rooms** — Create, retrieve, and decommission campus rooms
- **Manage Sensors** — Register and monitor IoT sensors (Temperature, CO2, Occupancy, etc.)
- **Track Sensor Readings** — Maintain a full historical log of sensor measurements
- **Enforce Business Rules** — Prevent data orphans, validate references, and handle errors gracefully

All data is stored **in-memory** using `ConcurrentHashMap` and `ArrayList`. No database is used.

---

## Technology Stack

| Technology | Purpose |
|---|---|
| Java 11 | Core programming language |
| JAX-RS (Jakarta RESTful Web Services) | REST API framework |
| Jersey 3.1.3 | JAX-RS implementation |
| Grizzly HTTP Server | Embedded lightweight server |
| Jackson | JSON serialization/deserialization |
| Maven | Build and dependency management |

---

## Project Structure

```
smart-campus-api/
├── pom.xml
└── src/
    └── main/
        └── java/
            └── com/
                └── smartcampus/
                    ├── SmartCampusApp.java
                    ├── config/
                    │   └── ApplicationConfig.java
                    ├── model/
                    │   ├── Room.java
                    │   ├── Sensor.java
                    │   └── SensorReading.java
                    ├── store/
                    │   └── DataStore.java
                    ├── resource/
                    │   ├── DiscoveryResource.java
                    │   ├── RoomResource.java
                    │   ├── SensorResource.java
                    │   └── SensorReadingResource.java
                    ├── exception/
                    │   ├── ErrorResponse.java
                    │   ├── RoomNotFoundException.java
                    │   ├── RoomNotEmptyException.java
                    │   ├── LinkedResourceNotFoundException.java
                    │   ├── SensorNotFoundException.java
                    │   └── SensorUnavailableException.java
                    ├── mapper/
                    │   ├── RoomNotEmptyExceptionMapper.java
                    │   ├── RoomNotFoundExceptionMapper.java
                    │   ├── LinkedResourceNotFoundExceptionMapper.java
                    │   ├── SensorNotFoundExceptionMapper.java
                    │   ├── SensorUnavailableExceptionMapper.java
                    │   └── GlobalExceptionMapper.java
                    └── filter/
                        └── ApiLoggingFilter.java
```

---

## How to Build & Run

### Prerequisites

- Java 11 or higher installed
- Maven 3.6+ installed
- Git installed

### Step 1 — Clone the Repository

```bash
git clone https://github.com/Ishan-malinda/smart-campus-api.git
cd smart-campus-api
```

### Step 2 — Build the Project

```bash
mvn clean package
```

This will compile the project and package everything into a single runnable JAR file inside the `target/` directory.

### Step 3 — Run the Server

```bash
java -jar target/smart-campus-api-1.0-SNAPSHOT.jar
```

### Step 4 — Verify the Server is Running

Open your browser or use curl:

```bash
curl http://localhost:8080/api/v1
```

You should see a JSON response with API metadata and available resource links.

> The server runs on **http://localhost:8080** by default.  
> Press **ENTER** in the terminal to shut it down gracefully.

---

## API Endpoints

### Base URL: `http://localhost:8080/api/v1`

#### Discovery
| Method | Endpoint | Description |
|---|---|---|
| GET | `/` | API metadata and resource links |

#### Rooms
| Method | Endpoint | Description |
|---|---|---|
| GET | `/rooms` | Get all rooms |
| POST | `/rooms` | Create a new room |
| GET | `/rooms/{roomId}` | Get a specific room |
| DELETE | `/rooms/{roomId}` | Delete a room (fails if sensors exist) |

#### Sensors
| Method | Endpoint | Description |
|---|---|---|
| GET | `/sensors` | Get all sensors (supports `?type=` filter) |
| POST | `/sensors` | Register a new sensor |
| GET | `/sensors/{sensorId}` | Get a specific sensor |

#### Sensor Readings
| Method | Endpoint | Description |
|---|---|---|
| GET | `/sensors/{sensorId}/readings` | Get all readings for a sensor |
| POST | `/sensors/{sensorId}/readings` | Add a new reading (updates sensor's currentValue) |

#### Error Responses
| HTTP Code | Scenario |
|---|---|
| 400 Bad Request | Missing or invalid request fields |
| 403 Forbidden | Posting a reading to a MAINTENANCE/OFFLINE sensor |
| 404 Not Found | Room or Sensor ID does not exist |
| 409 Conflict | Deleting a room that still has sensors |
| 422 Unprocessable Entity | Sensor references a non-existent roomId |
| 500 Internal Server Error | Any unexpected server-side error |

---

## Sample curl Commands

### 1. Get API Discovery Info
```bash
curl -X GET http://localhost:8080/api/v1
```

**Expected Response:**
```json
{
  "name": "Smart Campus Sensor & Room Management API",
  "version": "1.0",
  "contact": "admin@smartcampus.ac.lk",
  "resources": {
    "rooms": "/api/v1/rooms",
    "sensors": "/api/v1/sensors"
  }
}
```

---

### 2. Create a Room
```bash
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{
    "id": "LIB-301",
    "name": "Library Quiet Study",
    "capacity": 50
  }'
```

**Expected Response (201 Created):**
```json
{
  "id": "LIB-301",
  "name": "Library Quiet Study",
  "capacity": 50,
  "sensorIds": []
}
```

---

### 3. Register a Sensor (with valid roomId)
```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{
    "id": "TEMP-001",
    "type": "Temperature",
    "status": "ACTIVE",
    "currentValue": 0.0,
    "roomId": "LIB-301"
  }'
```

**Expected Response (201 Created):**
```json
{
  "id": "TEMP-001",
  "type": "Temperature",
  "status": "ACTIVE",
  "currentValue": 0.0,
  "roomId": "LIB-301"
}
```

---

### 4. Post a Sensor Reading (updates currentValue automatically)
```bash
curl -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{
    "value": 23.5
  }'
```

**Expected Response (201 Created):**
```json
{
  "message": "Reading recorded successfully",
  "sensorId": "TEMP-001",
  "sensorCurrentValue": 23.5,
  "reading": {
    "id": "auto-generated-uuid",
    "timestamp": 1714000000000,
    "value": 23.5
  }
}
```

---

### 5. Get All Readings for a Sensor
```bash
curl -X GET http://localhost:8080/api/v1/sensors/TEMP-001/readings
```

**Expected Response (200 OK):**
```json
{
  "sensorId": "TEMP-001",
  "totalReadings": 1,
  "readings": [
    {
      "id": "auto-generated-uuid",
      "timestamp": 1714000000000,
      "value": 23.5
    }
  ]
}
```

---

### 6. Filter Sensors by Type
```bash
curl -X GET "http://localhost:8080/api/v1/sensors?type=Temperature"
```

**Expected Response (200 OK):**
```json
[
  {
    "id": "TEMP-001",
    "type": "Temperature",
    "status": "ACTIVE",
    "currentValue": 23.5,
    "roomId": "LIB-301"
  }
]
```

---

### 7. Test 409 — Delete a Room That Has Sensors
```bash
curl -X DELETE http://localhost:8080/api/v1/rooms/LIB-301
```

**Expected Response (409 Conflict):**
```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Room LIB-301 still has active sensors. Remove sensors before deleting.",
  "timestamp": 1714000000000
}
```

---

### 8. Test 422 — Register Sensor with Non-Existent roomId
```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{
    "id": "CO2-999",
    "type": "CO2",
    "status": "ACTIVE",
    "currentValue": 0.0,
    "roomId": "FAKE-999"
  }'
```

**Expected Response (422 Unprocessable Entity):**
```json
{
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "Referenced Room ID 'FAKE-999' does not exist in the system.",
  "timestamp": 1714000000000
}
```

---

### 9. Test 403 — Post Reading to MAINTENANCE Sensor
```bash
# First create a MAINTENANCE sensor
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{
    "id": "TEMP-002",
    "type": "Temperature",
    "status": "MAINTENANCE",
    "currentValue": 0.0,
    "roomId": "LIB-301"
  }'

# Then try to post a reading - this should fail
curl -X POST http://localhost:8080/api/v1/sensors/TEMP-002/readings \
  -H "Content-Type: application/json" \
  -d '{"value": 25.0}'
```

**Expected Response (403 Forbidden):**
```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "Sensor 'TEMP-002' is unavailable. It is currently under MAINTENANCE or OFFLINE.",
  "timestamp": 1714000000000
}
```

---

## Report: Conceptual Questions & Answers

---

### Part 1 — Service Architecture & Setup

#### Q1: Explain the default lifecycle of a JAX-RS Resource class. How does this impact in-memory data management?

By default, JAX-RS creates a **new instance of a resource class for every incoming HTTP request**. This is known as the **per-request lifecycle**. Each request gets its own fresh object, meaning instance variables are not shared between requests.

This architectural decision has a critical implication for in-memory data management: if data were stored as regular instance variables inside a resource class, it would be lost after every request because the object is discarded once the response is sent.

To work around this, a shared static data store (`DataStore.java`) is used with `static` fields. This ensures the same data structures persist across all requests regardless of how many resource instances are created. Furthermore, `ConcurrentHashMap` is used instead of a regular `HashMap` to prevent **race conditions** — a scenario where two requests simultaneously read and write to the same data structure, potentially causing data corruption or loss. `ConcurrentHashMap` is thread-safe by design, making it the appropriate choice for a multi-threaded server environment.

---

#### Q2: Why is HATEOAS considered a hallmark of advanced RESTful design? How does it benefit client developers?

**HATEOAS (Hypermedia as the Engine of Application State)** is a constraint of REST architecture where API responses include not just data, but also **navigational links** to related resources and actions. For example, a response to `GET /api/v1` includes links such as `"rooms": "/api/v1/rooms"` and `"sensors": "/api/v1/sensors"`.

This approach benefits client developers in several important ways. First, clients do not need to hard-code URLs — they can **discover endpoints dynamically** by following the links provided in responses. Second, if the server-side URL structure changes, clients that follow HATEOAS links are insulated from breaking changes. Third, it reduces the dependency on static external documentation, as the API itself communicates what actions are possible at any given state. This makes the API more **self-describing**, **resilient**, and **easier to integrate** for developers who are new to the system.

---

### Part 2 — Room Management

#### Q3: What are the implications of returning only IDs versus full room objects in a list response?

Returning only **IDs** reduces the payload size significantly, which conserves network bandwidth — particularly important when there are thousands of rooms. However, this forces the client to make **N additional HTTP requests** (one per room ID) to fetch the details it needs, which is known as the **N+1 problem**. This increases latency and server load.

Returning **full room objects** in a single response eliminates those additional round trips. The client receives all necessary data in one request, which is more efficient for rendering a list view. However, the response payload is much larger, which can be a concern for mobile clients or slow network connections.

The best practice is to return full objects for list endpoints when the dataset is reasonably sized, or implement **pagination** (e.g., `?page=1&size=20`) to balance bandwidth usage and client convenience.

---

#### Q4: Is the DELETE operation idempotent in your implementation? Justify your answer.

In the standard HTTP specification, DELETE is defined as **idempotent** — meaning multiple identical requests should produce the same server state as a single request.

However, in this implementation, **DELETE is not fully idempotent** in terms of the HTTP response code. The first DELETE request on a valid room returns **200 OK** and removes the room. A second identical DELETE request for the same room returns **404 Not Found** because the room no longer exists. The server state is the same (the room is gone), but the response codes differ.

This is a common and accepted implementation pattern. The server state remains consistent (no duplicate deletions, no data corruption), which satisfies the spirit of idempotency. The 404 on the second request is simply informing the client that the target resource no longer exists, which is accurate and appropriate behaviour.

---

### Part 3 — Sensor Operations & Linking

#### Q5: What are the technical consequences if a client sends data in a format other than application/json to an endpoint annotated with @Consumes(MediaType.APPLICATION_JSON)?

When a resource method is annotated with `@Consumes(MediaType.APPLICATION_JSON)`, JAX-RS uses **content negotiation** to match the incoming request's `Content-Type` header against what the method declares it can consume.

If a client sends a request with `Content-Type: text/plain` or `Content-Type: application/xml`, JAX-RS will determine that no resource method exists that can handle that media type. The runtime will automatically return an **HTTP 415 Unsupported Media Type** response without ever invoking the resource method. This is handled entirely by the JAX-RS framework — no additional code is needed by the developer to produce this error.

This mechanism enforces strict input contracts, ensuring that the server only processes data formats it is designed to handle, thereby preventing unexpected parsing errors or data corruption.

---

#### Q6: Why is @QueryParam superior to path-based filtering (e.g., /sensors/type/CO2) for filtering collections?

Using `@QueryParam` (e.g., `GET /api/v1/sensors?type=CO2`) is the more appropriate design for several reasons.

First, **query parameters are optional by nature**. When no `type` parameter is provided, the endpoint returns all sensors. This allows a single endpoint to serve both filtered and unfiltered requests without requiring separate route definitions.

Second, **path parameters imply resource identity**. A path like `/sensors/type/CO2` implies that `type/CO2` is a unique resource identifier, which is semantically misleading. The path structure should represent a resource hierarchy, not a filter operation.

Third, **query parameters are easily composable**. Multiple filters can be combined naturally (e.g., `?type=CO2&status=ACTIVE`) without creating complex and deeply nested URL paths. This makes the API more flexible and extensible for future filtering requirements.

---

### Part 4 — Deep Nesting with Sub-Resources

#### Q7: Discuss the architectural benefits of the Sub-Resource Locator pattern.

The **Sub-Resource Locator** pattern involves a resource method that, instead of returning a response directly, returns an instance of another resource class to handle the remainder of the request path. In this API, `SensorResource` contains a locator method for `/{sensorId}/readings` that delegates to `SensorReadingResource`.

This pattern provides significant architectural advantages. First, it enforces the **Single Responsibility Principle** — each resource class manages one logical concern. `SensorResource` handles sensor-level operations, while `SensorReadingResource` exclusively manages reading history. This separation makes each class easier to read, test, and maintain independently.

Second, it prevents resource classes from becoming excessively large. Without this pattern, a single `SensorResource` class would need to define every nested route (`/sensors/{id}/readings`, `/sensors/{id}/readings/{rid}`, etc.), quickly becoming unmanageable as the API grows.

Third, it enables **contextual initialisation** — the locator can validate the parent resource (e.g., checking the sensor exists) before delegating, ensuring the sub-resource always operates in a valid context.

---

### Part 5 — Advanced Error Handling & Logging

#### Q8: Why is HTTP 422 more semantically accurate than 404 when a referenced resource is missing inside a valid payload?

An HTTP **404 Not Found** response conventionally means that the **requested URL endpoint** does not exist on the server. It refers to the target of the HTTP request itself.

When a client sends a `POST /api/v1/sensors` request with a valid URL but includes a `roomId` in the body that does not exist in the system, the problem is not that the endpoint is missing — the endpoint `/api/v1/sensors` exists and is functioning correctly. The problem is that the **payload contains a reference to a resource that cannot be resolved**.

HTTP **422 Unprocessable Entity** is semantically more accurate in this context because it signals that the server understood the request format (valid JSON, correct endpoint), but the **semantic content** of the payload is invalid — specifically, it contains a broken foreign key reference. This gives the client a much clearer signal about what went wrong and where to look to fix it.

---

#### Q9: From a cybersecurity standpoint, what are the risks of exposing internal Java stack traces to API consumers?

Exposing raw Java stack traces in API responses presents several serious security vulnerabilities.

First, stack traces reveal the **internal package and class structure** of the application (e.g., `com.smartcampus.resource.RoomResource`). This gives attackers a detailed map of the codebase, helping them identify specific components to target.

Second, stack traces often expose the **versions of frameworks and libraries** in use (e.g., Jersey 3.1.3, Jackson 2.x). Attackers can cross-reference these versions against **known CVE databases** to identify unpatched vulnerabilities and craft targeted exploits.

Third, they may reveal **server file paths**, database query strings, or configuration details that provide further intelligence for attacks such as path traversal or SQL injection.

Fourth, detailed error messages can facilitate **reverse engineering** of business logic, helping attackers understand validation rules and craft inputs designed to bypass them.

The `GlobalExceptionMapper` in this implementation addresses all of these risks by intercepting every unhandled `Throwable`, logging the full stack trace **server-side only**, and returning a generic, non-descriptive `500 Internal Server Error` message to the client.

---

#### Q10: Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting Logger.info() statements inside every resource method?

Using JAX-RS filters for logging adheres to the **DRY (Don't Repeat Yourself)** principle and the concept of **Separation of Concerns**.

If logging statements were manually inserted into every resource method, any change to the logging format would require modifying every method across all resource classes — a maintenance burden that grows with the size of the API. It also increases the risk of inconsistent logging if a developer forgets to add the statement to a new method.

A JAX-RS filter annotated with `@Provider` is automatically applied to **every request and response** in the application without touching individual resource methods. This means logging behaviour is defined in exactly one place, is guaranteed to be consistent, and can be enabled, disabled, or modified without touching any business logic.

This approach also makes resource methods cleaner and more focused — they only contain code relevant to their core responsibility, improving readability and testability.

---

*End of Report*