package ch.uzh.ifi.hase.soprafs24.game.chatGPT;

import static org.mockito.ArgumentMatchers.any;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs24.config.Config;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import okhttp3.*;

@ExtendWith(MockitoExtension.class)
public class ChatGPTTest {

    @Mock
    private OkHttpClient mockClient;

    @Mock
    private Call mockCall; // Mock the Call object

    @Mock
    private Response mockResponse;

    @Test
    public void testConvertSimilarityScoreToPoints() throws Exception {
        ChatGPT chatGPT = new ChatGPT();
        User user = new User();

        chatGPT.convertSimilarityScoreToPoints(user, 0.8f);
        assertEquals(6, user.getPointsAwardedFromChatGPT());

        chatGPT.convertSimilarityScoreToPoints(user, 0.55f);
        assertEquals(4, user.getPointsAwardedFromChatGPT());

        chatGPT.convertSimilarityScoreToPoints(user, 0.3f);
        assertEquals(2, user.getPointsAwardedFromChatGPT());

        chatGPT.convertSimilarityScoreToPoints(user, 0.1f);
        assertEquals(0, user.getPointsAwardedFromChatGPT());
    }


    @Test
    void testRateInputs_Successful() throws IOException {
        // Setup
        ChatGPT chatGPT = new ChatGPT();
        String json = "{\"choices\": [{\"text\": \"0.8\"}]}";
        ResponseBody responseBody = ResponseBody.create(json, MediaType.get("application/json; charset=utf-8"));

        Mockito.lenient().when(mockClient.newCall(any(Request.class))).thenReturn(mockCall);
        Mockito.lenient().when(mockCall.execute()).thenReturn(mockResponse);
        Mockito.lenient().when(mockResponse.isSuccessful()).thenReturn(true);
        Mockito.lenient().when(mockResponse.body()).thenReturn(responseBody);

        // Act
        float score = chatGPT.rateInputs("A cat on a mat", "A cat on a mat");

        // Assert
        assertEquals(0.89f, score, 0.01, "Score should match the stubbed response.");
    }

    @Test
    void testRateInputs_ApiKeyIsNull() {
        try (MockedStatic<Config> mockedConfig = Mockito.mockStatic(Config.class)) {
            mockedConfig.when(Config::getApiKey).thenReturn(null);

            ChatGPT chatGPT = new ChatGPT();

            Exception exception = assertThrows(ResponseStatusException.class, 
                () -> chatGPT.rateInputs("A cat on a mat", "A cat on a mat"),
                "Expected rateInputs to throw, but it did not");

            assertTrue(exception.getMessage().contains("API key not found"));
        }
    }

    @Test
    void testRateInputs_ApiKeyIsEmpty() {
        try (MockedStatic<Config> mockedConfig = Mockito.mockStatic(Config.class)) {
            mockedConfig.when(Config::getApiKey).thenReturn("");

            ChatGPT chatGPT = new ChatGPT();

            Exception exception = assertThrows(ResponseStatusException.class, 
                () -> chatGPT.rateInputs("A cat on a mat", "A cat on a mat"),
                "Expected rateInputs to throw, but it did not");

            assertTrue(exception.getMessage().contains("API key not found"));
        }
    }

}
