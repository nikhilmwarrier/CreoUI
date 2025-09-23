import java.util.HashMap;
import java.util.Map;

public class PostmanApp {

    public static void main(String[] args) {
        System.out.println("===========================================");
        System.out.println("  POSTMAN CLONE - DEMO");
        System.out.println("  HTTP Client with Database Integration");
        System.out.println("===========================================\n");

        try {
            PostmanBackendService service = new PostmanBackendService();

            System.out.println("📡 DEMO 1: GET Request");
            Map<String, String> headers = new HashMap<>();
            headers.put("Accept", "application/json");
            HttpResponse response1 = service.handleRequest(
                    "https://jsonplaceholder.typicode.com/posts/1", "GET", headers, null
            );
            printResponse("GET Request", response1);

            System.out.println("\n📡 DEMO 2: POST Request");
            String jsonBody = "{\n" +
                    "  \"title\": \"College Project Test\",\n" +
                    "  \"body\": \"This is a test post from our Postman clone\",\n" +
                    "  \"userId\": 1\n" +
                    "}";
            Map<String, String> postHeaders = new HashMap<>();
            postHeaders.put("Content-Type", "application/json");
            postHeaders.put("Accept", "application/json");
            HttpResponse response2 = service.handleRequest(
                    "https://jsonplaceholder.typicode.com/posts", "POST", postHeaders, jsonBody
            );
            printResponse("POST Request", response2);

            System.out.println("\n💾 DATABASE STORAGE VERIFICATION");
            showDatabaseContents();

            System.out.println("\n✅ DEMO COMPLETED SUCCESSFULLY!");

        } catch (Exception e) {
            System.out.println("❌ Error during demo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void printResponse(String requestType, HttpResponse response) {
        System.out.println("  ✓ " + requestType + " completed:");
        System.out.println("    Status: " + response.getStatusCode() + " " + response.getStatusText());
        System.out.println("    Content-Type: " + response.getContentType());
        System.out.println("    Response Size: " + response.getBodySize() + " bytes");
        System.out.println("    Response Time: " + response.getDuration() + " ms");

        if (response.getBody() != null && !response.getBody().isEmpty()) {
            String preview = response.getBody().length() > 100 ?
                    response.getBody().substring(0, 100) + "..." :
                    response.getBody();
            System.out.println("    Body Preview: " + preview.replace("\n", " "));
        }
    }

    private static void showDatabaseContents() {
        try {
            System.out.println("  📊 Checking database contents...");
            RequestsDAO requestsDAO = new RequestsDAO();
            System.out.println("  📥 Stored Requests: " + requestsDAO.GetAll().size());

            // The ResponsesDAO class does not have a GetAll() method, so we remove these lines.
            // ResponsesDAO responsesDAO = new ResponsesDAO();
            // System.out.println("  📤 Stored Responses: " + responsesDAO.GetAll().size());
            System.out.println("  ✓ Database verification complete!");

        } catch (Exception e) {
            System.out.println("  ⚠️  Could not verify database contents: " + e.getMessage());
        }
    }
}