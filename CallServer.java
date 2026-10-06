import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

public class CallServer {

    private static final ConcurrentHashMap<String, CallRecord> calls =
            new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {

        HttpServer server = HttpServer.create(
                new InetSocketAddress(8080), 0
        );

        server.createContext("/call-ended", CallServer::handleCallEnded);
        server.createContext("/calls", CallServer::handleGetCall);

        server.setExecutor(null);
        server.start();

        System.out.println("Call service running on http://localhost:8080");
    }

    private static void handleCallEnded(HttpExchange exchange) throws IOException {

        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }

        String body = new String(
                exchange.getRequestBody().readAllBytes(),
                StandardCharsets.UTF_8
        );

        String callId = getValue(body, "call_id");
        String status = getValue(body, "status");
        String durationValue = getValue(body, "duration_secs");

        if (callId == null || callId.isBlank()) {
            sendResponse(exchange, 400, "{\"error\":\"call_id is required\"}");
            return;
        }

        if (status == null || durationValue == null) {
            sendResponse(exchange, 400, "{\"error\":\"status and duration_secs are required\"}");
            return;
        }

        int durationSecs;

        try {
            durationSecs = Integer.parseInt(durationValue);
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "{\"error\":\"duration_secs must be a number\"}");
            return;
        }

        CallRecord record = new CallRecord(
                callId,
                status,
                durationSecs
        );

        // putIfAbsent makes the operation idempotent.
        // If the call_id already exists, the old record is kept.
        calls.putIfAbsent(callId, record);

        sendResponse(
                exchange,
                200,
                "{\"message\":\"Call processed successfully\"}"
        );
    }

    private static void handleGetCall(HttpExchange exchange) throws IOException {

        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String prefix = "/calls/";

        if (!path.startsWith(prefix) || path.length() <= prefix.length()) {
            sendResponse(exchange, 400, "{\"error\":\"call_id is required\"}");
            return;
        }

        String callId = path.substring(prefix.length());

        CallRecord record = calls.get(callId);

        if (record == null) {
            sendResponse(exchange, 404, "{\"error\":\"Call not found\"}");
            return;
        }

        sendResponse(exchange, 200, record.toJson());
    }

    private static String getValue(String json, String key) {

        String search = "\"" + key + "\"";
        int keyIndex = json.indexOf(search);

        if (keyIndex == -1) {
            return null;
        }

        int colonIndex = json.indexOf(":", keyIndex);

        if (colonIndex == -1) {
            return null;
        }

        int start = colonIndex + 1;

        while (start < json.length()
                && Character.isWhitespace(json.charAt(start))) {
            start++;
        }

        if (start >= json.length()) {
            return null;
        }

        if (json.charAt(start) == '"') {

            int end = json.indexOf("\"", start + 1);

            if (end == -1) {
                return null;
            }

            return json.substring(start + 1, end);
        }

        int end = start;

        while (end < json.length()
                && json.charAt(end) != ','
                && json.charAt(end) != '}') {
            end++;
        }

        return json.substring(start, end).trim();
    }

    private static void sendResponse(
            HttpExchange exchange,
            int statusCode,
            String response
    ) throws IOException {

        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set(
                "Content-Type",
                "application/json"
        );

        exchange.sendResponseHeaders(
                statusCode,
                responseBytes.length
        );

        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(responseBytes);
        }
    }
}