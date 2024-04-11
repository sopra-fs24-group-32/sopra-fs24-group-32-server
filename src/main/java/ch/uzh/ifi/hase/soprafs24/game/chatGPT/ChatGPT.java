//package ch.uzh.ifi.hase.soprafs24.game.chatGPT;
//
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map;
//
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.RequestBody;
//import okhttp3.Response;
//import okhttp3.MediaType;
//
//import ch.uzh.ifi.hase.soprafs24.entity.Player;
//import io.github.cdimascio.dotenv.Dotenv;
//import org.json.JSONObject;
//
//public class ChatGPT {
//
//    public Map<Player, String> playerInputs = new HashMap<>();
//
//    public Map<Player, Integer> rateInputs(String originalTextPrompt, Map<Player, String> playerInputs) throws IOException {
//        Map<Player, Integer> ratings = new HashMap<>();
//
//        Dotenv dotenv = Dotenv.load();
//        String apiKey = dotenv.get("OPENAI_API_KEY"); // Assuming the correct environment variable name
//
//        OkHttpClient client = new OkHttpClient();
//        MediaType JSON = MediaType.get("application/json; charset=utf-8");
//
//        for (Map.Entry<Player, String> entry : playerInputs.entrySet()) {
//            String guessedInput = entry.getValue();
//            String prompt = String.format("Rate the similarity between these two sentences from 0 to 10: \"%s\" and \"%s\".", originalTextPrompt, guessedInput);
//            String jsonBody = "{\"model\": \"text-davinci-003\", \"prompt\": \"" + prompt + "\", \"temperature\": 0, \"max_tokens\": 50}";
//            RequestBody body = RequestBody.create(jsonBody, JSON);
//            Request request = new Request.Builder()
//                    .url("https://api.openai.com/v1/completions")
//                    .post(body)
//                    .addHeader("Authorization", "Bearer " + apiKey)
//                    .addHeader("Content-Type", "application/json")
//                    .build();
//
//            try (Response response = client.newCall(request).execute()) {
//                if (!response.isSuccessful()) {
//                    throw new RuntimeException("Failed to rate the similarity with ChatGPT: " + response);
//                }
//
//                // Assuming the API returns a straightforward response with the rating at the top level
//                JSONObject jsonResponse = new JSONObject(response.body().string());
//                String textResponse = jsonResponse.getJSONArray("choices").getJSONObject(0).getString("text");
//                // Extract integer rating from the textResponse (this step might require adjustment based on the actual response format)
//                int similarityRating = extractRatingFromResponse(textResponse);
//
//                ratings.put(entry.getKey(), similarityRating);
//            }
//        }
//
//        return ratings;
//    }
//
//    private int extractRatingFromResponse(String textResponse) {
//        // Extract the rating from the text response
//        // I need to know the exact format of the response to provide the correct implementation
//        return 0;
//    }
//}