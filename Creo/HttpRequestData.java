import java.util.HashMap;
import java.util.Map;

public class HttpRequestData {
    private String url;
    private String method;
    private Map<String, String> headers;
    private String body;

    public HttpRequestData(String url, String method) {
        this.url = url;
        this.method = method;
        this.headers = new HashMap<>();
    }

    // Getters and Setters
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public Map<String, String> getHeaders() { return headers; }
    public void setHeaders(Map<String, String> headers) { this.headers = headers; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
}