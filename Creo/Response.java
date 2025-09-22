public class Response {
    private int id, requestID, statusCode;
    private String headers, body, contentType, timestamp;

    public Response(int id, int requestID, int statusCode, String headers, String body, String contentType, String timestamp) {
        this.id = id;
        this.requestID = requestID;
        this.statusCode = statusCode;
        this.headers = headers;
        this.body = body;
        this.contentType = contentType;
        this.timestamp = timestamp;
    }

    // Getters
    public int getID() { return id; }
    public int getRequestID() { return requestID; }
    public int getStatusCode() { return statusCode; }
    public String getHeaders() { return headers; }
    public String getBody() { return body; }
    public String getContentType() { return contentType; }
    public String getTimestamp() { return timestamp; }
}