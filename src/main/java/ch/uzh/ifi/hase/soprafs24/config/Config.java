package ch.uzh.ifi.hase.soprafs24.config;
import io.github.cdimascio.dotenv.Dotenv;

public class Config {
    private static Dotenv dotenv;

    static {
        try {
            dotenv = Dotenv.load();
        } catch (Exception e) {
            dotenv = null;
        }
    }

    public static String getApiKey() {
        // Get API key from environment variable
        String apiKey = System.getenv("DALL_E_API_KEY");
        if (apiKey != null) {
            return apiKey;
        } else {
            System.out.println("API Key not found in environment variables.");
            return ""; // Handle the error appropriately
        }
    }

}

