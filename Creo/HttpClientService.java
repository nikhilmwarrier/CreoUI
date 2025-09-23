import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

public class HttpClientService {
    private final HttpClient httpClient;
    private final Duration timeout = Duration.ofSeconds(30);

    public HttpClientService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(timeout)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public HttpResponse executeRequest(HttpRequestData requestData) {
        long startTime = System.currentTimeMillis();
        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(requestData.getUrl()))
                    .timeout(timeout);

            addHeaders(requestBuilder, requestData.getHeaders());

            switch (requestData.getMethod().toUpperCase()) {
                case "GET" -> requestBuilder.GET();
                case "POST" -> requestBuilder.POST(createBodyPublisher(requestData));
                case "PUT" -> requestBuilder.PUT(createBodyPublisher(requestData));
                case "DELETE" -> requestBuilder.DELETE();
                default -> throw new IllegalArgumentException("Unsupported HTTP method: " + requestData.getMethod());
            }

            java.net.http.HttpResponse<String> response = httpClient.send(requestBuilder.build(), java.net.http.HttpResponse.BodyHandlers.ofString());
            return processResponse(response, startTime);

        } catch (Exception e) {
            return createErrorResponse(e, startTime);
        }
    }

    private HttpResponse processResponse(java.net.http.HttpResponse<String> response, long startTime) {
        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setStatusCode(response.statusCode());
        httpResponse.setUrl(response.uri().toString());
        httpResponse.setHeaders(response.headers().map());
        httpResponse.setContentType(getContentType(response.headers()));

        httpResponse.setBody(formatJsonResponse(response.body()));
        if (response.body() != null) {
            httpResponse.setBodySize(httpResponse.getBody().getBytes(StandardCharsets.UTF_8).length);
        } else {
            httpResponse.setBodySize(0);
        }

        httpResponse.setDuration(System.currentTimeMillis() - startTime);
        return httpResponse;
    }

    private HttpResponse createErrorResponse(Exception e, long startTime) {
        HttpResponse errorResponse = new HttpResponse();
        errorResponse.setError(true);
        errorResponse.setErrorMessage(e.getMessage());
        errorResponse.setBody("Error: " + e.getMessage());
        errorResponse.setStatusCode(-1);
        errorResponse.setDuration(System.currentTimeMillis() - startTime);
        return errorResponse;
    }

    private void addHeaders(HttpRequest.Builder builder, Map<String, String> headers) {
        if (headers != null) {
            headers.forEach(builder::header);
        }
    }

    private HttpRequest.BodyPublisher createBodyPublisher(HttpRequestData requestData) {
        String body = requestData.getBody();
        return (body == null || body.isEmpty())
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body);
    }

    private String getContentType(HttpHeaders headers) {
        return headers.firstValue("content-type").orElse("text/plain");
    }

    private String formatJsonResponse(String json) {
        if (json == null || json.trim().isEmpty()) {
            return "";
        }
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonElement jsonElement = JsonParser.parseString(json);
            return gson.toJson(jsonElement);
        } catch (Exception e) {
            return json;
        }
    }
}