package ch.uzh.ifi.hase.soprafs24.config;
import io.github.cdimascio.dotenv.Dotenv;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

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
        String apiKey = null;
        if (dotenv != null) {
            try {
                apiKey = dotenv.get("DALL_E_API_KEY");
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        } else {
            InputStream inputStream = null;
            try {
                // Load the properties file
                File file = new File("app.yaml");
                if (file.exists()) {
                    inputStream = new FileInputStream(file);
                } else {
                }
                if (inputStream == null) {
                    throw new FileNotFoundException("app.yaml file not found in the root of the project directory");
                }

                Yaml yaml = new Yaml(new Constructor(Map.class));
                Map<String, Object> yamlProps = yaml.load(inputStream);

                // Assuming the API key is under env_variables in the YAML structure
                Map<String, String> envVariables = (Map<String, String>) yamlProps.get("env_variables");
                apiKey = envVariables.get("DALL_E_API_KEY");
            } catch (FileNotFoundException e) {
                System.err.println("app.yaml file not found: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Error reading app.yaml: " + e.getMessage());
            } finally {
                // Close the inputStream in a finally block to ensure it's always closed
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        System.err.println("Failed to close stream: " + e.getMessage());
                    }
                }
            }
        }
        return apiKey;
    }
}
