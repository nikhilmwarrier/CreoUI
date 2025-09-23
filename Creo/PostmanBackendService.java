import java.util.Map;

public class PostmanBackendService {
    private final HttpClientService httpClientService;

    public PostmanBackendService() {
        this.httpClientService = new HttpClientService();
        DBHandle.Initialize();
    }

    public HttpResponse handleRequest(String url, String method, Map<String, String> headers, String body) {
        HttpRequestData requestData = new HttpRequestData(url, method);
        requestData.setHeaders(headers);
        requestData.setBody(body);

        int requestId = saveRequestToDatabase(requestData);

        HttpResponse httpResponse = httpClientService.executeRequest(requestData);

        if (requestId != -1) {
            saveResponseToDatabase(httpResponse, requestId);
        }
        return httpResponse;
    }

    private int saveRequestToDatabase(HttpRequestData requestData) {
        Request request = new Request(0, requestData.getMethod(), requestData.getUrl(),
                requestData.getHeaders().toString(), requestData.getBody(), "");
        return new RequestsDAO().insert(request);
    }

    private void saveResponseToDatabase(HttpResponse httpResponse, int requestId) {
        Response response = new Response(0, requestId, httpResponse.getStatusCode(),
                httpResponse.getHeaders().toString(), httpResponse.getBody(), httpResponse.getContentType(), "");
        new ResponsesDAO().insert(response);
    }
}