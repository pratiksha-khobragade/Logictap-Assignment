import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class CallServerTest {

    public static void main(String[] args) throws Exception {

        HttpClient client = HttpClient.newHttpClient();

        String json = """
                {
                    "call_id": "test123",
                    "status": "answered",
                    "duration_secs": 30
                }
                """;

        // First request
        HttpRequest firstRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/call-ended"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> firstResponse =
                client.send(firstRequest, HttpResponse.BodyHandlers.ofString());

        // Duplicate request with the same call_id
        HttpRequest secondRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/call-ended"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> secondResponse =
                client.send(secondRequest, HttpResponse.BodyHandlers.ofString());

        // Retrieve the call
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/calls/test123"))
                .GET()
                .build();

        HttpResponse<String> getResponse =
                client.send(getRequest, HttpResponse.BodyHandlers.ofString());

        boolean passed =
                firstResponse.statusCode() == 200
                && secondResponse.statusCode() == 200
                && getResponse.statusCode() == 200
                && getResponse.body().contains("\"call_id\":\"test123\"");

        if (passed) {
            System.out.println(
                    "TEST PASSED: duplicate call_id was handled idempotently."
            );
        } else {
            System.out.println("TEST FAILED");
            System.out.println("First POST: " + firstResponse.statusCode());
            System.out.println("Second POST: " + secondResponse.statusCode());
            System.out.println("GET: " + getResponse.statusCode());
            System.out.println("GET body: " + getResponse.body());
        }
    }
}