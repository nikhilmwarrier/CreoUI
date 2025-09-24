import com.google.gson.Gson;

import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.util.Map;

public class AISummary {
    final String API_KEY = System.getenv("GROQ_API_KEY");

    PostmanBackendService backendService;

    AISummary (PostmanBackendService backendService) {
        this.backendService = backendService;
    }

    public String summarizeResponse(String responseString) {
        Map<String, String> headers = Map.of("Content-Type", "application/json", "Authorization", "Bearer " + API_KEY);
        String prompt = "The following is a response from an API call. Summarise all the key info here and present it in a neat, concise manner. Request: \n\n " + responseString;
        Gson gson = new Gson();
        String formattedPrompt = gson.toJson(prompt);

        String requestBody = """
                {
                  "model": "llama-3.3-70b-versatile",
                  "messages": [{
                      "role": "user",
                      "content":  %s
                  }]
                }
                """.formatted(formattedPrompt);

        HttpResponse response = this.backendService.handleRequest("https://api.groq.com/openai/v1/chat/completions", "POST", headers, requestBody);
        String body = response.getBody();

        GroqAPIResponse aiResponse = gson.fromJson(body, GroqAPIResponse.class);

        return convertMarkdownToHtml(aiResponse.choices.getFirst().message.content);

    }

    /**
     * Convert Markdown text to HTML using commonmark
     * @param markdown The Markdown text
     * @return HTML formatted string
     */
    public static String convertMarkdownToHtml(String markdown) {
        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdown);

        HtmlRenderer renderer = HtmlRenderer.builder().build();
        return renderer.render(document);
    }
}
