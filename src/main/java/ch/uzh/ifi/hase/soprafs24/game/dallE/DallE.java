package ch.uzh.ifi.hase.soprafs24.game.dallE;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.json.JSONArray;


import io.github.cdimascio.dotenv.Dotenv;

public class DallE {
    
    private static final String API_URL = "https://api.openai.com/v1/images/generations";
    private static final MediaType JSON = MediaType.parse("application/json");
    private final OkHttpClient client = new OkHttpClient();

    public DallE() {
    }

    public String generatePicture(String inputPhrase) throws Exception{
        String jsonBody = String.format("{\"prompt\": \"%s\", \"n\": 1}", inputPhrase);
        RequestBody body = RequestBody.create(jsonBody, JSON);

        Dotenv dotenv = Dotenv.load();
        String apiKey = dotenv.get("DALL_E_API_KEY");

        Request request = new Request.Builder()
                    .url(API_URL)
                    .post(body)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .build();

        Response response = client.newCall(request).execute();

        if (!response.isSuccessful()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Failed to generate image with DALL-E");
        }

        String responseBody = response.body().string();
        JSONObject jsonObject = new JSONObject(responseBody);
        JSONArray dataArray = jsonObject.getJSONArray("data");
        JSONObject firstElement = dataArray.getJSONObject(0);
        String imageUrl = firstElement.getString("url");
        return imageUrl;
    }
}