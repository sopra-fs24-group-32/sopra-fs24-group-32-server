package ch.uzh.ifi.hase.soprafs24.game.chatGPT;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import ch.uzh.ifi.hase.soprafs24.config.Config;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;


import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class ChatGPT {
    
    public String playerGuessed;
    public String originalText;

    public ChatGPT() {
    }

    public float rateInputs(String originalText, String playerGuessed) throws IOException {

        String apiKey = Config.getApiKey();
        if (apiKey == null || apiKey.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "API key not found. Value: " + apiKey);
        }


        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        ObjectMapper objectMapper = new ObjectMapper();

        // String prompt = String.format("Only provide the similarity score value, nothing else. Take into account synonyms and if there are common words in both sentences. How much similar are these two sentences from 0 to 1: \"%s\" and \"%s\".", originalText, playerGuessed);
        String prompt = String.format("Only provide the similarity score, nothing else. how much similar are the two sentences on a scale from 0 to 1: \"%s\" and \"%s\".", originalText, playerGuessed);
        
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to rate the similarity with ChatGPT: " + response.body().string());
        }

        JSONObject jsonResponse = new JSONObject(response.body().string());
        JSONArray textResponse = jsonResponse.getJSONArray("choices");
        JSONObject textResponseObject = textResponse.getJSONObject(0);
        String similarityRating = textResponseObject.getString("text");

        return  Float.parseFloat(similarityRating);
    }

    public void convertSimilarityScoreToPoints(User user, float similarityScore) throws Exception {
        
        if (similarityScore < 0 || similarityScore > 1) {
            throw new IllegalArgumentException("Similarity score must be between 0 and 1.");
        }
        
        int pointsAwarded;
        if (similarityScore >= 0.75) {
            pointsAwarded = 6;
        } else if (similarityScore >= 0.5) {
            pointsAwarded = 4;
        } else if (similarityScore >= 0.25) {
            pointsAwarded = 2;
        } else {
            pointsAwarded = 0;
        }

        user.setPointsAwardedFromChatGPT(pointsAwarded);
        user.setSimilarityScore(similarityScore);
    }
}