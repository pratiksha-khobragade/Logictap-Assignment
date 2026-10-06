# Logictap-Assignment

# Logictap Call Service

A lightweight Java HTTP service developed for the Logictap Internship Hiring Task. The service receives call-ended events, validates requests, stores call records in memory, handles duplicate events safely, and provides an endpoint to retrieve stored call information.

## Features

- Accepts call-ended events through an HTTP endpoint
- Validates required request fields
- Stores call records using `ConcurrentHashMap`
- Prevents duplicate records using `call_id` as the idempotency key
- Supports retrieval of individual call records
- Returns appropriate HTTP status codes
- Includes an automated test for duplicate request handling
- Uses Java's built-in HTTP server and client APIs
- Requires no external frameworks or dependencies

## Project Structure

    Logictap-Call-Service/
    ├── CallRecord.java
    ├── CallServer.java
    ├── CallServerTest.java
    └── README.md

## Technology

- Java 21
- Java HTTP Server (`com.sun.net.httpserver`)
- Java HTTP Client
- JSON
- PowerShell
- ConcurrentHashMap

## Requirements

- Java 21 or later
- No external libraries or frameworks are required

## Running the Application

### 1. Compile the application

    javac --add-modules jdk.httpserver CallRecord.java CallServer.java

### 2. Start the server

    java --add-modules jdk.httpserver CallServer

The service will start at:

    http://localhost:8080

Expected output:

    Call service running on http://localhost:8080

## API

### POST /call-ended

Creates a call record.

Request:

    {
      "call_id": "abc123",
      "status": "answered",
      "duration_secs": 42
    }

Response:

    {
      "message": "Call processed successfully"
    }

Status: `200 OK`

### Duplicate `call_id`

If the same `call_id` is received more than once, the service returns a successful response but does not create another record.

The implementation uses:

    calls.putIfAbsent(callId, record);

`putIfAbsent()` provides an atomic insert-if-absent operation, making duplicate call events idempotent.

### GET /calls/{call_id}

Retrieves a stored call record.

Example:

    GET /calls/abc123

Response:

    {
      "call_id": "abc123",
      "status": "answered",
      "duration_secs": 42
    }

Status: `200 OK`

### Validation

A request without `call_id` is rejected.

Response:

    {
      "error": "call_id is required"
    }

Status: `400 Bad Request`

## Automated Test

`CallServerTest.java` verifies the duplicate-handling behavior by:

1. Sending a call-ended request.
2. Sending the same request again with the same `call_id`.
3. Retrieving the stored call.
4. Verifying that the expected record is available.

Compile the test:

    javac CallServerTest.java

Run the test while the server is running:

    java CallServerTest

Expected output:

    TEST PASSED: duplicate call_id was handled idempotently.

## Idempotency Design

For this assignment, `call_id` is used as the idempotency key because repeated events for the same call should not create multiple stored records.

The combination of `ConcurrentHashMap` and `putIfAbsent()` ensures that the check-and-insert operation is atomic.

In a production webhook system, I would additionally introduce a unique `event_id` for every webhook event and enforce uniqueness at the database level. This would provide stronger protection against duplicate webhook deliveries across multiple application instances.

## Design Notes

The service intentionally uses an in-memory data structure to keep the implementation small and dependency-free for the hiring task.

For a production implementation, the storage layer could be replaced with a persistent database, and request parsing could use a dedicated JSON library with stronger schema validation.

## Author

**Pratiksha Khobragade**

Developed as part of the Logictap Internship Hiring Task.
