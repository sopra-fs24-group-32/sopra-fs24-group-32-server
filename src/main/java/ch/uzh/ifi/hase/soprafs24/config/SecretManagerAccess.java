package ch.uzh.ifi.hase.soprafs24.config;
import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;

public class SecretManagerAccess {
    public static String getSecret(String projectId, String secretId) {
        // Initialize client that will be used to send requests. This client only needs to be created
        // once, and can be reused for multiple requests.
        try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
            SecretVersionName secretVersionName = SecretVersionName.of(projectId, secretId, "latest");
            // Access the secret version.
            AccessSecretVersionResponse response = client.accessSecretVersion(secretVersionName);
            // Return the payload.
            return response.getPayload().getData().toStringUtf8();
        } catch (Exception e) {
            System.out.println("Failed to access secret version: " + e);
            return "";
        }
    }
}