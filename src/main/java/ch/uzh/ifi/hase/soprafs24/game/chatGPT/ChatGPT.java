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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        
        String promptInstructions = """
            How much similar are these two sentences from 0 to 1: \"%s\" and \"%s\" related to an online multiplayer drawing and guessing game. Only provide the similarity score value, nothing else.
            The first sentence provides a description that was used to generate an image via an AI-driven art tool (DALL-E), and the second sentence is a player's guess of that description. 
            **Scoring Guidelines**:
            - **1.0**: The guess perfectly matches the description.
            - **0.7-0.9**: The guess is very close to the description, capturing the main elements but possibly missing minor details.
            - **0.4-0.6**: The guess is partially accurate, sharing some key elements with the description but missing significant aspects or including incorrect details.
            - **0.1-0.3**: The guess has minimal relation to the description, with only slight overlaps in content.
            - **0.0**: The guess is completely unrelated to the description, with no overlapping elements.
            
            **Examples**:
            1. **Image Description**: "cat on the floor"
            **Guess**: "dog on the floor"
            **Score**: 0.0 
            **Reason**: Unrelated animal, incorrect guess
            
            2. **Image Description**: "professor explaining the course"
            **Guess**: "professor"
            **Score**: 0.7 
            **Reason**: Correct identification of the subject but missing the action
            
            3. **Image Description**: "cat"
            **Guess**: "cat"
            **Score**: 1.0
            **Reason**: Exact match
            
            This method should ensure a clear and precise assessment of how closely a player's guess aligns with the initial image description, helping maintain fairness and engagement in gameplay.
            **Your Response Guidelines**:
            - Again, provide only the similarity score value, nothing else. 
            - Do not include any additional information or context in your response.
            - Do not provide a range or interval of the similarity score, only a single value between 0 and 1.
            - Do not provide response values like 0.7-0.9 or 0.4-0.6, only a single value like 0.8 or 0.5.
        """;
        String prompt = String.format(promptInstructions, originalText, playerGuessed);
        
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

        Pattern pattern = Pattern.compile("\\b\\d+(\\.\\d+)?\\b");
        Matcher matcher = pattern.matcher(similarityRating);
        
        if (matcher.find()) {
            // Convert the first match to a float
            float similarityScore = Float.parseFloat(matcher.group());
            return similarityScore;
        } else {
            System.out.println("No float found in the string.");
            return 0.5f;
        }

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