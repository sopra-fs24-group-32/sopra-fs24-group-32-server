package ch.uzh.ifi.hase.soprafs24.config;
import io.github.cdimascio.dotenv.Dotenv;

public class Config {
    private static final Dotenv dotenv = Dotenv.load();

    public static String getApiKey() {
        // Check if running in production by looking for a specific environment variable
        String environment = System.getenv("ENV");

        if ("prod".equals(environment)) {
            // In production, use Secret Manager
            return SecretManagerAccess.getSecret("sopra-fs24-group-32-server", "dall_e_api_key");
        } else {
            // Locally, use dotenv
            return dotenv.get("DALL_E_API_KEY");
        }
    }
}

