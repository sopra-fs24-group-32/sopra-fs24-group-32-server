package ch.uzh.ifi.hase.soprafs24.game.dallE;

import ch.uzh.ifi.hase.soprafs24.config.Config;
import okhttp3.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class DallETest<MockWebServer> {

    @Mock
    private OkHttpClient client;

    @Mock
    private Call call;

    @InjectMocks
    private DallE dallE;

    @Mock
    private Response mockResponse;

    @Mock
    private OkHttpClient mockClient;

    @Mock
    private Call mockCall;

    @Test
    public void generatePicture_WithInvalidApiKey_ShouldThrowException() {
        // Arrange

        try (MockedStatic<Config> mockedConfig = Mockito.mockStatic(Config.class)) {
            mockedConfig.when(Config::getApiKey).thenReturn(null);

            String inputPhrase = "A beautiful sunset over the mountains";

            // Act & Assert
            ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
                dallE.generatePicture(inputPhrase);
            });
            assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
            assertTrue(exception.getReason().contains("API key not found"));
        }
    }

    @Test
    void testGeneratePicture_Success() throws Exception {
        String imageUrl = dallE.generatePicture("A beautiful sunset");

        // Assert
        assertNotNull(imageUrl);
    }

    @Test
    public void generatePicture_WithFailedResponse_ShouldThrowException() throws Exception {
       // Arrange
       String inputPhrase = "adult content";
       // Act & Assert
       ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
           dallE.generatePicture(inputPhrase);
       });
       assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
       assertTrue(exception.getReason().contains("Failed to generate image with DALL-E"));
    }

    @Test
    public void testGetInputPhrase() {
        // Arrange
        String inputPhrase = "A beautiful sunset over the mountains";
        dallE.setInputPhrase(inputPhrase);

        // Act
        String result = dallE.getInputPhrase();

        // Assert
        assertEquals(inputPhrase, result);
    }

    @Test
    public void testSetInputPhrase() {
        // Arrange
        String inputPhrase = "A beautiful sunset over the mountains";

        // Act
        dallE.setInputPhrase(inputPhrase);

        // Assert
        assertEquals(inputPhrase, dallE.getInputPhrase());
    }

    @Test
    public void testGetImageUrl() {
        // Arrange
        String imageUrl = "http://example.com/image.png";
        dallE.setImageUrl(imageUrl);

        // Act
        String result = dallE.getImageUrl();

        // Assert
        assertEquals(imageUrl, result);
    }

    @Test
    public void testSetImageUrl() {
        // Arrange
        String imageUrl = "http://example.com/image.png";

        // Act
        dallE.setImageUrl(imageUrl);

        // Assert
        assertEquals(imageUrl, dallE.getImageUrl());
    }
}
