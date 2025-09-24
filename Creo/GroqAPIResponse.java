import com.google.gson.Gson;
import java.util.List;

class GroqAPIResponse {
    String id;
    String object;
    long created;
    String model;
    List<Choice> choices;
    Usage usage;
    String system_fingerprint;
    XGroq x_groq;
    String service_tier;

    static class Choice {
        int index;
        Message message;
        String finish_reason;
    }

    static class Message {
        String role;
        String content;
    }

    static class Usage {
        double queue_time;
        double prompt_tokens;
        double prompt_time;
        double completion_tokens;
        double completion_time;
        double total_tokens;
        double total_time;
    }

    static class XGroq {
        String id;
    }


}
