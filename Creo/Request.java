public class Request {
    private int ID;
    private String method, url, headers, body, timestamp;

    public Request(int ID, String method, String url, String headers, String body, String timestamp) {
        this.ID = ID;
        this.method = method;
        this.url = url;
        this.headers = headers;
        this.body = body;
        this.timestamp = timestamp;
    }

    // Getters
    public int getID() { return ID; }
    public String getMethod() { return method; }
    public String getUrl() { return url; }
    public String getHeaders() { return headers; }
    public String getBody() { return body; }
    public String getTimestamp() { return timestamp; }
}