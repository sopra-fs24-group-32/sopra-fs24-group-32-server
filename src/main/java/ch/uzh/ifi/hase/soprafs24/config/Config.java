package ch.uzh.ifi.hase.soprafs24.config;
import io.github.cdimascio.dotenv.Dotenv;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

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
        System.out.println(System.getProperty("java.class.path"));

        String apiKey = null;
        if (dotenv != null) {
            try {
                apiKey = System.getenv("DALL_E_API_KEY");
                System.out.println(apiKey);
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        } else {
            System.out.println("inputStream1");
            InputStream inputStream = null;
            try {
                // Load the properties file
                System.out.println("inputStream2");
                inputStream = Config.class.getClassLoader().getResourceAsStream("app.yaml");
                if (inputStream == null) {
                    throw new FileNotFoundException("app.yaml file not found in the root of the project directory");
                }
                System.out.println(inputStream);
                System.out.println("inputStream3");

                Yaml yaml = new Yaml(new Constructor(Map.class));
                Map<String, Object> yamlProps = yaml.load(inputStream);

                System.out.println(yamlProps);

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
        System.out.println(apiKey);
        return apiKey;
    }
}
