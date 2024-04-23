package ch.uzh.ifi.hase.soprafs24.config;
import io.github.cdimascio.dotenv.Dotenv;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

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
        String apiKey = null;
        if (dotenv != null) {
            try {
                apiKey = System.getenv("DALL_E_API_KEY");
                System.out.println(apiKey);
            }
            catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        } else {
                try {
                    // Load the properties file (assuming app.yaml is actually app.properties format)
                    InputStream inputStream = new FileInputStream("../../../../../app.yaml");
                    Yaml yaml = new Yaml(new Constructor(Map.class));
                    Map<String, Object> yamlProps = yaml.load(inputStream);

                    // Assuming the API key is under env_variables in the YAML structure
                    Map<String, String> envVariables = (Map<String, String>) yamlProps.get("env_variables");
                    apiKey = envVariables.get("DALL_E_API_KEY");
                } catch (IOException e) {
                    System.err.println("Error loading properties file: " + e.getMessage());
                }
        }
        return apiKey;
    }
}


