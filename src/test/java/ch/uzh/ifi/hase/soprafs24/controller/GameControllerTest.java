package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.game.Game;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.ChatGPTPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GamePostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.service.GameService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import java.util.Optional;
import java.util.List;
import java.util.Collections;
import static org.mockito.ArgumentMatchers.anyLong;


import com.fasterxml.jackson.databind.ObjectMapper;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.messaging.simp.SimpMessagingTemplate;
@WebMvcTest(GameWebSocketController.class)
public class GameControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private UserService userService;

  @MockBean
  private GameService gameService;

  @MockBean
  private GameRepository gameRepository;

  @MockBean
  private UserRepository userRepository;

  @InjectMocks
  private UserController userController;

  @InjectMocks
   private GameWebSocketController gameController;

   @MockBean
   private SimpMessagingTemplate simpMessagingTemplate;


  @BeforeEach
  public void setUp(){
//     MockitoAnnotations.openMocks(this);
//     userRepository = mock(UserRepository.class);
//         userService = mock(UserService.class);
//         gameRepository = mock(GameRepository.class);
  }

    @Test
    public void joinLobbyAndIdDoesNotExists() throws Exception{
      String id = "1";
      String userToken = "random";

      doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND))
              .when(gameService)
              .joinLobby(id, userToken);

      MockHttpServletRequestBuilder postRequest = post("/join/lobby/{id}", id)
              .contentType(MediaType.APPLICATION_JSON)
              .content(userToken);

      mockMvc.perform(postRequest)
              .andExpect(status().isNotFound());

    }



    @Test
    public void createLobbyAndUserTokenIsNullOrEmpty() throws Exception {

        User emptyUser = new User();
        emptyUser.setUsername("owner");
        emptyUser.setUserToken("");
        
        User nullUser = new User();
        nullUser.setUsername("ownerNull");
        nullUser.setUserToken(null);

        String emptyToken = "{\"userToken\":\"\"}";
        String nullToken = "{\"userToken\":null}";

        GamePostDTO validGamePostDTO = new GamePostDTO();
        validGamePostDTO.setTimeLimit(20F);
        validGamePostDTO.setAmtOfRounds(10);
        validGamePostDTO.setMaxAmtUsers(10);

        when(userRepository.findByUserToken("")).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User does not exists"));
        when(userRepository.findByUserToken(null)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User does not exists"));
        
        given(gameService.createLobby(emptyToken, validGamePostDTO)).willThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "userToken is null or empty"));
        given(gameService.createLobby(nullToken, validGamePostDTO)).willThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "userToken is null or empty"));
        

        mockMvc.perform(post("/lobby/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(emptyToken))
                .andExpect(status().isBadRequest());
        

        mockMvc.perform(post("/lobby/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(nullToken))
                .andExpect(status().isBadRequest());
    }


    @Test
    public void createLobbyWithValidUserToken() throws Exception {
        String userToken = "valid_token";
        String username = "owner";
        long id = 1L;

        User user = new User();
        user.setUsername(username);
        user.setUserToken(userToken);

        GamePostDTO validGamePostDTO = new GamePostDTO();
        validGamePostDTO.setTimeLimit(20F);
        validGamePostDTO.setAmtOfRounds(10);
        validGamePostDTO.setMaxAmtUsers(10);

        Game lobby = new Game(id, username); // Assuming Lobby has an appropriate constructor

        given(userRepository.findByUserToken(userToken)).willReturn(user);
        given(gameService.createLobby(userToken, validGamePostDTO)).willReturn(lobby);

        mockMvc.perform(post("/lobby/create").header("userToken", userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(validGamePostDTO)))
                .andExpect(status().isCreated());
        }


        @Test
        public void updateLobbyWithValidParameters() throws Exception {
                Long id = 1L;
                String userToken = "validToken";


                GamePostDTO validGamePostDTO = new GamePostDTO();
                validGamePostDTO.setTimeLimit(20F);
                validGamePostDTO.setAmtOfRounds(10);
                validGamePostDTO.setMaxAmtUsers(10);

                Game lobby = new Game(id, "owner");

                Game updatedLobby = new Game(1L, "owner");
                updatedLobby.setTimeLimit(20F);
                updatedLobby.setAmtOfRounds(10);
                updatedLobby.setMaxAmtUsers(10);

                Optional<Game> optionalLobby = Optional.of(lobby);


                User user = new User();
                user.setUsername("owner");
                user.setUserToken(userToken);
                doReturn(user).when(userRepository).findByUserToken(userToken);


                doReturn(optionalLobby).when(gameRepository).findById(id);


                doReturn(updatedLobby).when(gameService).updateGameSettings(id, validGamePostDTO);


                mockMvc.perform(put("/lobby/update/{id}", id)
                        .header("userToken", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(validGamePostDTO)))
                        .andExpect(status().isOk());
        }

        @Test
        public void getAllLobbies_NoLobbiesFound_ReturnsEmptyList() throws Exception {
            when(gameService.getAllGames()).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/lobbies")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }


        @Test
        public void getLobbyById_NotFound_ReturnsNotFound() throws Exception {
            when(gameRepository.findById(anyLong())).thenReturn(Optional.empty());

            mockMvc.perform(get("/lobby/{id}", 1L))
                    .andExpect(status().isNotFound());
        }

    @Test
    public void playerLeaveTheGameShouldProcessCorrectly() throws Exception {
        Long gameId = 1L;
        String userToken = "{\"userToken\":\"valid-token\"}";
        Game game = new Game(gameId, "owner");
        User user = new User();
        user.setUsername("player1");
        user.setUserToken("valid-token");
        game.addPlayer(user);

        User user2 = new User();
        user2.setUsername("player2");
        user2.setUserToken("valid-token2");
        game.addPlayer(user2);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game)); 
        when(userRepository.findByUserToken("valid-token")).thenReturn(user);
        when(userRepository.findByUserToken("valid-token2")).thenReturn(user2);

        mockMvc.perform(post("/game/leave/{gameId}", gameId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(userToken))
                .andExpect(status().isOk());

        verify(gameService).playerLeaveGame(gameId, "valid-token");
   }

   @Test
    public void playerLeaveTheGameWhenUserTokenIsEmptyShouldReturnNotFound() throws Exception {
        Long gameId = 1L;
        String emptyUserToken = "{\"userToken\":\"\"}";

        mockMvc.perform(post("/game/leave/{gameId}", gameId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(emptyUserToken))
                .andExpect(status().isNotFound());
    }

    @Test
    public void playerLeaveTheGame_WhenGameIdIsInvalid_ShouldReturnNotFound() throws Exception {
        Long invalidGameId = 0L;
        String userToken = "{\"userToken\":\"valid-token\"}";

        mockMvc.perform(post("/game/leave/{invalidGameId}", invalidGameId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(userToken))
                .andExpect(status().isNotFound());
    }

    @Test
    public void playerLeaveTheGame_WhenGameNotFound_ShouldReturnNotFound() throws Exception {
        Long gameId = 1L;
        String userToken = "{\"userToken\":\"valid-token\"}";

        when(gameRepository.findById(gameId)).thenReturn(Optional.empty());

        mockMvc.perform(post("/game/leave/{gameId}", gameId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(userToken))
                .andExpect(status().isNotFound());
    }

    private String asJsonString(Object object) {
        try {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
        throw new RuntimeException(e);
        }
    }

    @Test
    public void evaluateGuessesByChatGPT_WhenGameIdIsInvalid_ShouldReturnNotFound() throws Exception {
        Long invalidGameId = 0L;
        Long invalidGameIdNull = null;
        String userToken = "{\"userToken\":\"valid-token\"}";

        ChatGPTPostDTO chatGPTPostDTO = new ChatGPTPostDTO();

        mockMvc.perform(put("/game/chatgpt/{invalidGameId}", invalidGameId)
                .header("userToken", userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(chatGPTPostDTO)))
                .andExpect(status().isNotFound());

        mockMvc.perform(put("/game/chatgpt/{invalidGameIdNull}", invalidGameIdNull)
                .header("userToken", userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(chatGPTPostDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void evaluateGuessesByChatGPT_WhenGameNotFound_ShouldReturnNotFound() throws Exception {
        Long gameId = 1L;
        String userToken = "{\"userToken\":\"valid-token\"}";

        ChatGPTPostDTO chatGPTPostDTO = new ChatGPTPostDTO();

        when(gameRepository.findById(gameId)).thenReturn(Optional.empty());

        mockMvc.perform(put("/game/chatgpt/{gameId}", gameId)
                .header("userToken", userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(chatGPTPostDTO)))
                .andExpect(status().isNotFound());

    }

    @Test
    public void evaluateGuessesByChatGPT_WhenUserTokenIsEmpty_ShouldReturnNotFound() throws Exception {
        Long gameId = 1L;
        String emptyUserToken = "{\"userToken\":\"\"}";
        Game game = new Game(gameId, "owner");

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        ChatGPTPostDTO chatGPTPostDTO = new ChatGPTPostDTO();

        mockMvc.perform(put("/game/chatgpt/{gameId}", gameId)
                .header("userToken", emptyUserToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(chatGPTPostDTO)))
                .andExpect(status().isNotFound());

    }

    @Test
    public void evaluateGuessesByChatGPT_WhenPlayerGuessedEmpty_ShouldReturnZeroPoint() throws Exception {
        Long gameId = 1L;
        String username = "owner";
        String userToken = "{\"userToken\":\"valid-token\"}";
        Game game = new Game(gameId, username);

        User user = new User();
        user.setUsername(username);
        game.addPlayer(user);

        ChatGPTPostDTO chatGPTPostDTO = new ChatGPTPostDTO();
        chatGPTPostDTO.setPlayerGuessed("");
        chatGPTPostDTO.setTimeGuessSubmitted(30);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(userRepository.findByUserToken("valid-token")).thenReturn(user);

        mockMvc.perform(put("/game/chatgpt/{gameId}", gameId)
                .header("userToken", userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(chatGPTPostDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.score").value(0));        
    }

    @Test
    public void generatePictureByDallE_WhenGameIdIsInvalid_ShouldReturnNotFound() throws Exception {
        Long invalidGameId = 0L;
        Long invalidGameIdNull = null;
        String textPrompt = "A picture of a cat";

        mockMvc.perform(post("/game/image/{invalidGameId}", invalidGameId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(textPrompt))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/game/image/{invalidGameIdNull}", invalidGameIdNull)
                .contentType(MediaType.APPLICATION_JSON)
                .content(textPrompt))
                .andExpect(status().isNotFound());
    }

    @Test
    public void generatePictureByDallE_WhenGameNotFound_ShouldReturnNotFound() throws Exception {
        Long gameId = 1L;
        String textPrompt = "A picture of a cat";

        when(gameRepository.findById(gameId)).thenReturn(Optional.empty());

        mockMvc.perform(post("/game/image/{gameId}", gameId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(textPrompt))
                .andExpect(status().isNotFound());
    }

    @Test
    public void generatePictureByDallE_WhenTextPromptIsEmptyOrNull_ShouldReturnBadRequest() throws Exception {
        Long gameId = 1L;
        Game game = new Game(gameId, "owner");

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        String emptyTextPrompt = "";

        mockMvc.perform(post("/game/image/{gameId}", gameId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(emptyTextPrompt))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void generatePictureByDallE_WhenTextPromptIsValid_ShouldReturnImageUrl() throws Exception {
        Long gameId = 1L;
        String textPrompt = "A picture of a cat";
        String imageUrl = "https://example.com/cat.jpg";

        Game game = new Game(gameId, "owner");

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(gameService.generatePictureDallE(textPrompt)).thenReturn(imageUrl);

        mockMvc.perform(post("/game/image/{gameId}", gameId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(textPrompt))
                .andExpect(status().isCreated());
    }

    @Test
    public void getPictureGeneratedByDallE_WhenGameIdIsInvalid_ShouldReturnNotFound() throws Exception {
        Long invalidGameId = 0L;
        Long invalidGameIdNull = null;

        mockMvc.perform(get("/game/image/{invalidGameId}", invalidGameId))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/game/image/{invalidGameIdNull}", invalidGameIdNull))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getPictureGeneratedByDallE_WhenGameNotFound_ShouldReturnNotFound() throws Exception {
        Long gameId = 1L;

        when(gameRepository.findById(gameId)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));

        mockMvc.perform(get("/game/image/{gameId}", gameId))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getPictureGeneratedByDallE_WhenGameFound_ShouldReturnImageUrl() throws Exception {
        Long gameId = 1L;
        String imageUrl = "https://example.com/cat.jpg";
        Game game = new Game(gameId, "owner");

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(gameService.getImageGeneratedByDallE(1L)).thenReturn(imageUrl);

        mockMvc.perform(get("/game/image/{gameId}", gameId))
                .andExpect(status().isOk());
    }

    @Test
    public void getPictureGeneratedByDallE_WhenGameFoundButNoImageGenerated_ShouldReturnNotFound() throws Exception {
        Long gameId = 1L;
        Game game = new Game(gameId, "owner");

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(gameService.getImageGeneratedByDallE(1L)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "No image generated"));

        mockMvc.perform(get("/game/image/{gameId}", gameId))
                .andExpect(status().isNotFound());
    }

    @Test
    public void evaluateGuessesByChatGPT_WhenGameFound_ShouldReturnScore() throws Exception {
        Long gameId = 1L;
        String userToken = "{\"userToken\":\"valid-token\"}";
        Game game = new Game(gameId, "owner");

        User user = new User();
        user.setUsername("owner");
        user.setUserToken("valid-token");
        game.addPlayer(user);
        game.setTimeLimit(30);

        ChatGPTPostDTO chatGPTPostDTO = new ChatGPTPostDTO();
        chatGPTPostDTO.setPlayerGuessed("A picture of a cat");
        chatGPTPostDTO.setTimeGuessSubmitted(10);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(userRepository.findByUserToken("valid-token")).thenReturn(user);
        when(gameService.evaluatePlayerGuessWithChatGPT(chatGPTPostDTO.getPlayerGuessed())).thenReturn(6);

        mockMvc.perform(put("/game/chatgpt/{gameId}", gameId)
                .header("userToken", userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(chatGPTPostDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.score").value(7));

    }

    @Test
    public void evaluateGuessesByChatGPT_WhenGameFoundGuessSubmittedLate_ShouldReturnScore() throws Exception {
        Long gameId = 1L;
        String userToken = "{\"userToken\":\"valid-token\"}";
        Game game = new Game(gameId, "owner");

        User user = new User();
        user.setUsername("owner");
        user.setUserToken("valid-token");
        game.addPlayer(user);
        game.setTimeLimit(30);

        int score = 6;

        ChatGPTPostDTO chatGPTPostDTO = new ChatGPTPostDTO();
        chatGPTPostDTO.setPlayerGuessed("A picture of a cat");
        chatGPTPostDTO.setTimeGuessSubmitted(25);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(userRepository.findByUserToken("valid-token")).thenReturn(user);
        when(gameService.evaluatePlayerGuessWithChatGPT(chatGPTPostDTO.getPlayerGuessed())).thenReturn(score);

        mockMvc.perform(put("/game/chatgpt/{gameId}", gameId)
                .header("userToken", userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(chatGPTPostDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.score").value(score));

    }

    @Test
    public void evaluateGuessesByChatGPT_WhenGameFoundEmptyGuess_ShouldReturnScoreZero() throws Exception {
        Long gameId = 1L;
        String userToken = "{\"userToken\":\"valid-token\"}";
        Game game = new Game(gameId, "owner");

        User user = new User();
        user.setUsername("owner");
        user.setUserToken("valid-token");
        game.addPlayer(user);
        game.setTimeLimit(30);

        int score = 0;

        ChatGPTPostDTO chatGPTPostDTO = new ChatGPTPostDTO();
        chatGPTPostDTO.setPlayerGuessed("");
        chatGPTPostDTO.setTimeGuessSubmitted(10);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(userRepository.findByUserToken("valid-token")).thenReturn(user);
        when(gameService.evaluatePlayerGuessWithChatGPT(chatGPTPostDTO.getPlayerGuessed())).thenReturn(score);

        mockMvc.perform(put("/game/chatgpt/{gameId}", gameId)
                .header("userToken", userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(chatGPTPostDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.score").value(score));

    }

    @Test
    public void evaluateGuessesByChatGPT_WhenGameFoundNullGuess_ShouldReturnScoreZero() throws Exception {
        Long gameId = 1L;
        String userToken = "{\"userToken\":\"valid-token\"}";
        Game game = new Game(gameId, "owner");

        User user = new User();
        user.setUsername("owner");
        user.setUserToken("valid-token");
        game.addPlayer(user);
        game.setTimeLimit(30);

        int score = 0;

        ChatGPTPostDTO chatGPTPostDTO = new ChatGPTPostDTO();
        chatGPTPostDTO.setPlayerGuessed(null);
        chatGPTPostDTO.setTimeGuessSubmitted(30);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(userRepository.findByUserToken("valid-token")).thenReturn(user);
        when(gameService.evaluatePlayerGuessWithChatGPT(chatGPTPostDTO.getPlayerGuessed())).thenReturn(score);

        mockMvc.perform(put("/game/chatgpt/{gameId}", gameId)
                .header("userToken", userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(chatGPTPostDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.score").value(score));

    }

    @Test
    public void startGame_WhenGameIdIsInvalid_ShouldReturnNotFound() throws Exception {
        Long invalidGameId = 0L;

        mockMvc.perform(post("/lobby/start/{invalidGameId}", invalidGameId))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void startGame_WhenGameNotFound_ShouldReturnNotFound() throws Exception {
        Long gameId = 1L;
        Game game = new Game(gameId, "owner");

        User user = new User();
        user.setUsername("owner");
        user.setUserToken("valid-token");

        String userToken = "{\"userToken\":\"valid-token\"}";

        when(gameRepository.findById(gameId)).thenReturn(Optional.empty());
        when(userRepository.findByUserToken("valid-token")).thenReturn(user);

        mockMvc.perform(post("/lobby/start/{gameId}", gameId)
                .header("userToken", userToken))
                .andExpect(status().isNotFound());
    }

    @Test
    public void startGame_WhenUserTokenIsEmpty_ShouldReturnNotFound() throws Exception {
        Long gameId = 1L;
        Game game = new Game(gameId, "owner");

        User user = new User();
        user.setUsername("owner");
        user.setUserToken("valid-token");

        String emptyUserToken = "{\"userToken\":\"\"}";

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(userRepository.findByUserToken("")).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User does not exists"));

        mockMvc.perform(post("/lobby/start/{gameId}", gameId)
                .header("userToken", emptyUserToken))
                .andExpect(status().isNotFound());
    }

    @Test
    public void startGame_WhenGameFound_ShouldReturnGameStarted() throws Exception {
        Long gameId = 1L;
        Game game = new Game(gameId, "owner");

        User user = new User();
        user.setUsername("owner");
        user.setUserToken("valid-token");

        String userToken = "{\"userToken\":\"valid-token\"}";

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(userRepository.findByUserToken("valid-token")).thenReturn(user);

        mockMvc.perform(post("/lobby/start/{gameId}", gameId)
                .header("userToken", userToken))
                .andExpect(status().isOk());

    }
}