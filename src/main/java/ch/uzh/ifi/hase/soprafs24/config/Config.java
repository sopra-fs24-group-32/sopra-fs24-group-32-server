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
        // Check if running in production by looking for a specific environment variable
        if (dotenv != null) {
            return dotenv.get("DALL_E_API_KEY");
        } else {
            return SecretManagerAccess.getSecret("sopra-fs24-group-32-server", "dall_e_api_key");
        }

    }
}

