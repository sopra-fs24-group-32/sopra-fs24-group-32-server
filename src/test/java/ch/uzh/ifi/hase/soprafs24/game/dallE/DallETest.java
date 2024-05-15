package ch.uzh.ifi.hase.soprafs24.game.dallE;

import ch.uzh.ifi.hase.soprafs24.config.Config;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DallETest {

    @Mock
    private OkHttpClient client;

    @Mock
    private Call call;

    @InjectMocks
    private DallE dallE;

    private MockedStatic<Config> mockedConfig;

    @BeforeEach
    public void setUp() {
        mockedConfig = mockStatic(Config.class);
        mockedConfig.when(Config::getApiKey).thenReturn("valid-api-key");
    }

    @AfterEach
    public void tearDown() {
        mockedConfig.close();
    }

    //@Test
    //public void generatePicture_WithValidInput_ShouldReturnImageUrl() throws Exception {
    //    // Arrange
    //    String inputPhrase = "A beautiful sunset over the mountains";
    //    String expectedResponseBody = "{\"data\": [{\"url\": \"http://example.com/image.png\"}]}";
    //    Response response = new Response.Builder()
    //            .request(new Request.Builder().url("http://localhost/").build())
    //            .protocol(Protocol.HTTP_1_1)
    //            .code(200)
    //            .message("OK")
    //            .body(ResponseBody.create(expectedResponseBody, MediaType.parse("application/json")))
    //            .build();
//
    //    when(client.newCall(any(Request.class))).thenReturn(call);
    //    when(call.execute()).thenReturn(response);
//
    //    // Act
    //    String imageUrl = dallE.generatePicture(inputPhrase);
//
    //    // Assert
    //    assertEquals("http://example.com/image.png", imageUrl);
    //}

    @Test
    public void generatePicture_WithInvalidApiKey_ShouldThrowException() {
        // Arrange
        mockedConfig.when(Config::getApiKey).thenReturn(null); // Invalidate the API key

        String inputPhrase = "A beautiful sunset over the mountains";

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            dallE.generatePicture(inputPhrase);
        });
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertTrue(exception.getReason().contains("API key not found"));
    }

    //@Test
    //public void generatePicture_WithFailedResponse_ShouldThrowException() throws Exception {
    //    // Arrange
    //    String inputPhrase = "A beautiful sunset over the mountains";
    //    Response response = new Response.Builder()
    //            .request(new Request.Builder().url("http://localhost/").build())
    //            .protocol(Protocol.HTTP_1_1)
    //            .code(400)
    //            .message("Bad Request")
    //            .body(ResponseBody.create("Failed to generate image with DALL-E", MediaType.parse("application/json")))
    //            .build();
//
    //    when(client.newCall(any(Request.class))).thenReturn(call);
    //    when(call.execute()).thenReturn(response);
//
    //    // Act & Assert
    //    ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
    //        dallE.generatePicture(inputPhrase);
    //    });
    //    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    //    assertTrue(exception.getReason().contains("Failed to generate image with DALL-E"));
    //}

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
