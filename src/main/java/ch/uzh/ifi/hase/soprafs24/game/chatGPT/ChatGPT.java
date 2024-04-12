package ch.uzh.ifi.hase.soprafs24.game.chatGPT;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import io.github.cdimascio.dotenv.Dotenv;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

public class ChatGPT {
    
    public Map<User, String> playerInputs;

    public ChatGPT() {
    }

    public Map<User, String> rateInputs(String originalTextPrompt, Map<User, String> playerInputs) throws IOException {
        Map<User, String> ratings = new HashMap<>();

        Dotenv dotenv = Dotenv.load();
        String apiKey = dotenv.get("DALL_E_API_KEY"); // Assuming the correct environment variable name

        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        ObjectMapper objectMapper = new ObjectMapper();

        for (Map.Entry<User, String> entry : playerInputs.entrySet()) {
            String guessedInput = entry.getValue();
            String prompt = String.format("How much similar are these two sentences from 0 to 100: \"%s\" and \"%s\".", originalTextPrompt, guessedInput);
        
            // Create the request payload as a Map and serialize it to JSON
            Map<String, Object> payload = new HashMap<>();
            payload.put("model", "gpt-3.5-turbo-instruct");
            payload.put("prompt", prompt);
            payload.put("temperature", 0);
            payload.put("max_tokens", 50);
            String jsonBody = objectMapper.writeValueAsString(payload); // Serialize the map to JSON string
        
            RequestBody body = RequestBody.create(jsonBody, JSON);
            Request request = new Request.Builder()
                .url("https://api.openai.com/v1/completions")
                .post(body)
                .addHeader("Authorization", "Bearer " + apiKey)
                .build();
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Failed to rate the similarity with ChatGPT: " + response.body().string());
            }

            // JSONObject jsonResponse = new JSONObject(response.body().string());
            // String textResponse = jsonResponse.getJSONArray("choices").toString();
            // float similarityRating = extractRatingFromResponse(textResponse);
            String textResponse = response.body().string();
                
            ratings.put(entry.getKey(), textResponse);
    }

    return ratings;
    }

    private String extractRatingFromResponse(String textResponse) {
        // Extract the rating from the text response
        // I need to know the exact format of the response to provide the correct implementation
        return textResponse;
    }
}
