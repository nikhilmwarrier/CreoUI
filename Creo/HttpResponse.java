import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpResponse {
    private int statusCode;
    private String url;
    private Map<String, List<String>> headers;
    private String body;
    private String contentType;
    private long duration;
    private long bodySize;
    private boolean isError;
    private String errorMessage;

    public HttpResponse() {
        this.headers = new HashMap<>();
        this.isError = false;
    }

    // Getters and Setters
    public int getStatusCode() { return statusCode; }
    public void setStatusCode(int statusCode) { this.statusCode = statusCode; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public Map<String, List<String>> getHeaders() { return headers; }
    public void setHeaders(Map<String, List<String>> headers) { this.headers = headers; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public long getDuration() { return duration; }
    public void setDuration(long duration) { this.duration = duration; }
    public long getBodySize() { return bodySize; }
    public void setBodySize(long bodySize) { this.bodySize = bodySize; }
    public boolean isError() { return isError; }
    public void setError(boolean error) { isError = error; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public String getStatusText() {
        return switch (statusCode) {
            case 200 -> "OK";
            case 201 -> "Created";
            case 400 -> "Bad Request";
            case 404 -> "Not Found";
            case 500 -> "Internal Server Error";
            default -> "Unknown";
        };
    }
}