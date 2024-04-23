package ch.uzh.ifi.hase.soprafs24.game.dallE;

import ch.uzh.ifi.hase.soprafs24.config.Config;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.json.JSONArray;
import java.util.concurrent.TimeUnit;

import io.github.cdimascio.dotenv.Dotenv;

public class DallE {
    
    private static final String API_URL = "https://api.openai.com/v1/images/generations";
    private static final MediaType JSON = MediaType.parse("application/json");
    private final OkHttpClient client;
    private String inputPhrase;
    private String imageUrl;

    public DallE() {
        this.client = new OkHttpClient().newBuilder()
                        .connectTimeout(30, TimeUnit.SECONDS) // Increase connect timeout if needed
                        .readTimeout(60, TimeUnit.SECONDS)    // Read timeout set to 60 seconds
                        .build();
    }

    public DallE(String inputPhrase){
        this.inputPhrase = inputPhrase;
        this.imageUrl = "";
        this.client = new OkHttpClient().newBuilder()
                        .connectTimeout(30, TimeUnit.SECONDS) // Increase connect timeout if needed
                        .readTimeout(60, TimeUnit.SECONDS)    // Read timeout set to 60 seconds
                        .build();
    }

    public String generatePicture(String inputPhrase) throws Exception{
        String jsonBody = String.format("{\"prompt\": \"%s\", \"n\": 1}", inputPhrase);
        RequestBody body = RequestBody.create(jsonBody, JSON);

        String apiKey = Config.getApiKey();

        if (apiKey == null || apiKey.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "API key not found");
        }

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

    public String getInputPhrase() {
        return inputPhrase;
    }

    public void setInputPhrase(String inputPhrase) {
        this.inputPhrase = inputPhrase;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}