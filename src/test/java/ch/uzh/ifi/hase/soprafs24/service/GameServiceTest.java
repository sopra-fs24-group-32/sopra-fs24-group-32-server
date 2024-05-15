package ch.uzh.ifi.hase.soprafs24.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.*;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.game.Game;
import ch.uzh.ifi.hase.soprafs24.game.chatGPT.ChatGPT;
import ch.uzh.ifi.hase.soprafs24.game.dallE.DallE;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GameGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GamePostDTO;

import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;


public class GameServiceTest {

    private GameService gameService;
    private UserService userService;
    private UserRepository userRepository;
    private GameRepository gameRepository;
    @Mock
    private ChatGPT chatGPT;
    @Mock
    private DallE dallE;

    @BeforeEach
    public void setup() {
        userRepository = mock(UserRepository.class);
        userService = mock(UserService.class);
        gameRepository = mock(GameRepository.class);
        gameService = new GameService(userRepository, gameRepository, userService, dallE, chatGPT);
    }

    @Test
    public void joinLobbyAndIdIsNullOrEmpty(){
        Exception exceptionIdIsNull = assertThrows(ResponseStatusException.class, () -> {
            gameService.joinLobby(null, "userToken");
        });

        String expectedMessage = "Lobby invitation code is null or empty";
        String actualMessage = exceptionIdIsNull.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        Exception exceptionIdIsEmpty = assertThrows(ResponseStatusException.class, () -> {
            gameService.joinLobby("", "userToken");
        });

        String expectedMessageWhenIdIsEmpty = "Lobby invitation code is null or empty";
        String actualMessageWhenIdIsEmpty = exceptionIdIsNull.getMessage();

        assertTrue(actualMessageWhenIdIsEmpty.contains(expectedMessageWhenIdIsEmpty));

    }

    @Test void joinLobbyAndUserTokenIsNullOrEmpty(){
        Exception exceptionUserTokenIsNull = assertThrows(ResponseStatusException.class, () -> {
            gameService.joinLobby("1", null);
        });

        String expectedMessage = "UserToken is null or empty";
        String actualMessage = exceptionUserTokenIsNull.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        Exception exceptionUserTokenIsEmpty = assertThrows(ResponseStatusException.class, () -> {
            gameService.joinLobby("1", "");
        });

        String expectedMessageWhenUserTokenIsEmpty = "UserToken is null or empty";
        String actualMessageWhenUserTokenIsEmpty = exceptionUserTokenIsEmpty.getMessage();

        assertTrue(actualMessageWhenUserTokenIsEmpty.contains(expectedMessageWhenUserTokenIsEmpty));
    }

    @Test
    public void joinGameAndLobbyDoesNotExist() throws Exception {
        User lobbyOwner = new User();
        lobbyOwner.setUserToken("userToken");
        lobbyOwner.setUsername("lobbyOwner");

        String userToken = "{\"userToken\":\"userToken\"}";

        when(userRepository.findByUserToken(userToken)).thenReturn(lobbyOwner);
        // Lobby lobby = gameService.createLobby("userToken");
        when(gameRepository.findByLobbyInvitationCode("-1")).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Lobby with id -1 does not exist"));

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            gameService.joinLobby("-1", userToken);
        });

        String expectedMessage = "Lobby with id -1 does not exist";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void joinLobbyButGameHasAlreadyStarted() throws Exception {
        User lobbyOwner = new User();
        lobbyOwner.setUserToken("userToken");
        lobbyOwner.setUsername("lobbyOwner");

        User newUser = new User();
        newUser.setUserToken("userToken2");
        newUser.setUsername("user2");

        String userToken = "userToken";
        String userToken2 = "{\"userToken\":\"userToken2\"}";


        when(userRepository.findByUserToken("userToken")).thenReturn(lobbyOwner);
        when(userRepository.findByUserToken("userToken2")).thenReturn(newUser);

        GamePostDTO validGamePostDTO = new GamePostDTO();
        validGamePostDTO.setTimeLimit(20F);
        validGamePostDTO.setAmtOfRounds(10);
        validGamePostDTO.setMaxAmtUsers(10);

        Game lobby = gameService.createLobby(userToken, validGamePostDTO);
        User player1 = new User();
        lobby.addPlayer(player1);;
        lobby.setTimeLimit(15f);
        lobby.startGame();
        boolean I = lobby.gameHasStarted();
        String lobbyInvitationCode = lobby.getLobbyInvitationCode();

        when(gameRepository.findByLobbyInvitationCode(lobbyInvitationCode)).thenReturn(lobby);

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            gameService.joinLobby(lobbyInvitationCode, userToken2);
        });

        String expectedMessage = "Game has already started";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void joinLobby_WithGameNull_ShouldThrowException() throws Exception {
        User lobbyOwner = new User();
        lobbyOwner.setUserToken("userToken");
        lobbyOwner.setUsername("lobbyOwner");

        String userToken = "{\"userToken\":\"userToken\"}";

        when(userRepository.findByUserToken(userToken)).thenReturn(lobbyOwner);
        when(gameRepository.findByLobbyInvitationCode("1")).thenReturn(null);

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            gameService.joinLobby("1", userToken);
        });

        String expectedMessage = "Lobby with invitation code: " + "1";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

    }

    @Test
    public void testFindByIdWhenExists() {
        Game expectedLobby = new Game(1L, "TestOwner");
        when(gameRepository.findById(1L)).thenReturn(Optional.of(expectedLobby)); // Mocking the behavior

        Optional<Game> foundLobby = gameRepository.findById(1L);
        assertTrue(foundLobby.isPresent()); // Check if the Optional is not empty
        assertEquals(expectedLobby, foundLobby.get()); // Compare the expected object with the actual one inside the Optional
    }

    @Test
    public void testFindByIdWhenNotExists() {
        Optional<Game> result = gameRepository.findById(22L);
        assertTrue(result.isEmpty());
    }


    @Test
    public void testCreateLobby() throws Exception {

        User lobbyOwner = new User();
        lobbyOwner.setUserToken("userToken");
        lobbyOwner.setUsername("lobbyOwner");

        String userToken = "userToken";

        when(userRepository.findByUserToken("userToken")).thenReturn(lobbyOwner);

        GamePostDTO validGamePostDTO = new GamePostDTO();
        validGamePostDTO.setTimeLimit(20F);
        validGamePostDTO.setAmtOfRounds(10);
        validGamePostDTO.setMaxAmtUsers(10);

        Game lobby = gameService.createLobby(userToken, validGamePostDTO);

        // Assert
        assertNotNull(lobby);
        assertEquals(1, lobby.getUsers().size());
        assertTrue(lobby.getUsers().stream().anyMatch(user -> user.getUsername().equals("lobbyOwner")));
    }


    @Test
    public void testUpdateGame() throws Exception {

        User lobbyOwen = new User();
        lobbyOwen.setUserToken("ownerToken");
        lobbyOwen.setUsername("owner");
        lobbyOwen.setId(1L);

        Game lobby = new Game();
        lobby.setId(1L);
        lobby.setLobbyOwner("owner");

        when(userRepository.findByUserToken("ownerToken")).thenReturn(lobbyOwen);
        when(gameRepository.findById(1L)).thenReturn(Optional.of(lobby));

        GamePostDTO gamePostDTO = new GamePostDTO();
        gamePostDTO.setTimeLimit(15.0f);
        gamePostDTO.setAmtOfRounds(7);
        gamePostDTO.setMaxAmtUsers(12);

        Game updatedLobby = gameService.updateGameSettings(lobby.getId(), gamePostDTO);

        // Assert
        assertNotNull(updatedLobby);
        assertEquals(15.0f, updatedLobby.getTimeLimit());
        assertEquals(7, updatedLobby.getAmtOfRounds());
        assertEquals(12, updatedLobby.getMaxAmtUsers());
    }

    @Test
    public void testUpdateGameIllegalArgument_NumberOfRounds() throws Exception {

        User lobbyOwen = new User();
        lobbyOwen.setUserToken("ownerToken");
        lobbyOwen.setUsername("owner");

        Game lobby = new Game();
        lobby.setId(1L);
        lobby.setLobbyOwner("owner");

        when(userRepository.findByUserToken("ownerToken")).thenReturn(lobbyOwen);
        when(gameRepository.findById(1L)).thenReturn(Optional.of(lobby));

        GamePostDTO gamePostDTO = new GamePostDTO();
        gamePostDTO.setTimeLimit(15.0f);
        gamePostDTO.setAmtOfRounds(0);      //illegal number of rounds
        gamePostDTO.setMaxAmtUsers(12);

        assertThrows(IllegalArgumentException.class, () -> {
            gameService.updateGameSettings(lobby.getId(), gamePostDTO);
        }, "There must be at least one round.");
    }

    @Test
    public void testUpdateGameIllegalArgument_TimeLimit() throws Exception {

        User lobbyOwen = new User();
        lobbyOwen.setUserToken("ownerToken");
        lobbyOwen.setUsername("owner");

        Game lobby = new Game();
        lobby.setId(1L);
        lobby.setLobbyOwner("owner");

        when(userRepository.findByUserToken("ownerToken")).thenReturn(lobbyOwen);
        when(gameRepository.findById(1L)).thenReturn(Optional.of(lobby));

        GamePostDTO gamePostDTO = new GamePostDTO();
        gamePostDTO.setTimeLimit(0.0f);      //illegal time limit T=0
        gamePostDTO.setAmtOfRounds(7);
        gamePostDTO.setMaxAmtUsers(12);

        GamePostDTO gamePostDTO2 = new GamePostDTO();
        gamePostDTO2.setTimeLimit(101.0f);      //illegal time limit T=101
        gamePostDTO2.setAmtOfRounds(7);
        gamePostDTO2.setMaxAmtUsers(12);

        assertThrows(IllegalArgumentException.class, () -> {
            gameService.updateGameSettings(lobby.getId(), gamePostDTO);
        }, "Time limit must be between 5 seconds and 100 Seconds.");

        assertThrows(IllegalArgumentException.class, () -> {
            gameService.updateGameSettings(lobby.getId(), gamePostDTO2);
        }, "Time limit must be between 5 seconds and 100 Seconds.");

    }

    @Test
    public void testUpdateGameIllegalArgument_MaxAmtUsers() throws Exception {

        User lobbyOwen = new User();
        lobbyOwen.setUserToken("ownerToken");
        lobbyOwen.setUsername("owner");

        Game lobby = new Game();
        lobby.setId(1L);
        lobby.setLobbyOwner("owner");

        when(userRepository.findByUserToken("ownerToken")).thenReturn(lobbyOwen);
        when(gameRepository.findById(1L)).thenReturn(Optional.of(lobby));

        GamePostDTO gamePostDTO = new GamePostDTO();
        gamePostDTO.setTimeLimit(15.0f);
        gamePostDTO.setAmtOfRounds(7);
        gamePostDTO.setMaxAmtUsers(1);      //illegal number of max users = 1

        assertThrows(IllegalArgumentException.class, () -> {
            gameService.updateGameSettings(lobby.getId(), gamePostDTO);
        }, "There must be at least two players.");

    }

    @Test
    public void testJoinGame() throws Exception {

        User lobbyOwen = new User();
        lobbyOwen.setUserToken("ownerToken");
        lobbyOwen.setUsername("owner");

        User user = new User();
        user.setUserToken("userToken");
        user.setUsername("username");

        String ownerToken = "ownerToken";
        String userToken = "{\"userToken\":\"userToken\"}";

        when(userRepository.findByUserToken("ownerToken")).thenReturn(lobbyOwen);
        when(userRepository.findByUserToken("userToken")).thenReturn(user);

        GamePostDTO validGamePostDTO = new GamePostDTO();
        validGamePostDTO.setTimeLimit(20F);
        validGamePostDTO.setAmtOfRounds(10);
        validGamePostDTO.setMaxAmtUsers(10);

        Game lobby = gameService.createLobby(ownerToken, validGamePostDTO);

        when(gameRepository.findByLobbyInvitationCode(lobby.getLobbyInvitationCode())).thenReturn(lobby);

        gameService.joinLobby(lobby.getLobbyInvitationCode(), userToken);

        // Assert
        assertNotNull(lobby);
        assertEquals(2, lobby.getUsers().size());
        assertTrue(lobby.getUsers().stream().anyMatch(u -> u.getUsername().equals("username")));
    }


    @Test
    public void playerLeaveTheGame_ShouldRemovePlayerSuccessfully() throws Exception {
        User lobbyOwner = new User();
        lobbyOwner.setUserToken("userToken");
        lobbyOwner.setUsername("lobbyOwner");

        User player1 = new User();
        player1.setUserToken("userToken2");
        player1.setUsername("player1");

        User player2 = new User();
        player2.setUserToken("userToken3");
        player2.setUsername("player2");

        Game game = new Game();
        Long gameId = 1L;
        game.setId(gameId);
        game.setLobbyOwner("lobbyOwner");
        game.addPlayer(player1);
        game.addPlayer(player2);
        game.addPlayer(lobbyOwner);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(userRepository.findByUserToken("userToken")).thenReturn(lobbyOwner);
        when(userRepository.findByUserToken("userToken2")).thenReturn(player1);
        when(userRepository.findByUserToken("userToken3")).thenReturn(player2);

        gameService.playerLeaveGame(gameId, player2.getUserToken());
        // Assert
        assertNotNull(game);
        assertEquals(2, game.getUsers().size());
    }

    @Test
    public void playerLeaveGame_ShouldThrowExceptionWhenUserEmptyOrNull() throws Exception {
        User lobbyOwner = new User();
        lobbyOwner.setUserToken("userToken");
        lobbyOwner.setUsername("lobbyOwner");

        Game game = new Game();
        Long gameId = 1L;
        game.setId(gameId);
        game.setLobbyOwner("lobbyOwner");
        game.addPlayer(lobbyOwner);

        User player1EmptyToken = new User();
        player1EmptyToken.setUserToken("");
        player1EmptyToken.setUsername("player1");

        User player2NullToken = new User();
        player2NullToken.setUserToken(null);
        player2NullToken.setUsername("player2");



        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(userRepository.findByUserToken("userToken")).thenReturn(lobbyOwner);
        when(userRepository.findByUserToken(player1EmptyToken.getUserToken())).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User does not exist"));
        when(userRepository.findByUserToken(player2NullToken.getUserToken())).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User does not exist"));

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            gameService.playerLeaveGame(gameId, player1EmptyToken.getUserToken());
        });

        String expectedMessage = "UserToken is null or empty";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void playerLeaveGame_WithUserTokenNullOrEmpty_ShouldThrowException() throws Exception {
        User lobbyOwner = new User();
        lobbyOwner.setUserToken("userToken");
        lobbyOwner.setUsername("lobbyOwner");

        Game game = new Game();
        Long gameId = 1L;
        game.setId(gameId);
        game.setLobbyOwner("lobbyOwner");
        game.addPlayer(lobbyOwner);

        User player1EmptyToken = new User();
        player1EmptyToken.setUserToken("");
        player1EmptyToken.setUsername("player1");

        User player2NullToken = new User();
        player2NullToken.setUserToken(null);
        player2NullToken.setUsername("player2");

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(userRepository.findByUserToken("userToken")).thenReturn(lobbyOwner);
        when(userRepository.findByUserToken("userToken2")).thenReturn(player1EmptyToken);
        when(userRepository.findByUserToken("userToken3")).thenReturn(player2NullToken);

        Exception exception1 = assertThrows(ResponseStatusException.class, () -> {
            gameService.playerLeaveGame(gameId, player1EmptyToken.getUserToken());
        });

        Exception exception2 = assertThrows(ResponseStatusException.class, () -> {
            gameService.playerLeaveGame(gameId, player2NullToken.getUserToken());
        });

        String expectedMessage = "UserToken is null or empty";
        String actualMessage1 = exception1.getMessage();
        String actualMessage2 = exception2.getMessage();

        assertTrue(actualMessage1.contains(expectedMessage));
        assertTrue(actualMessage2.contains(expectedMessage));
    }

    @Test
    public void playerLeaveGame_ShouldThrowExceptionWhenGameIdIsNullOrZerro() throws Exception {
        User lobbyOwner = new User();
        lobbyOwner.setUserToken("userToken");
        lobbyOwner.setUsername("lobbyOwner");

        Game game = new Game();
        Long gameId = 1L;
        game.setId(gameId);
        game.setLobbyOwner("lobbyOwner");
        game.addPlayer(lobbyOwner);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(userRepository.findByUserToken("userToken")).thenReturn(lobbyOwner);

        Exception exception1 = assertThrows(ResponseStatusException.class, () -> {
            gameService.playerLeaveGame(null, "userToken");
        });

        Exception exception2 = assertThrows(ResponseStatusException.class, () -> {
            gameService.playerLeaveGame(0L, "userToken");
        });

        String expectedMessage = "Game ID is null or zero";
        String actualMessage1 = exception1.getMessage();
        String actualMessage2 = exception2.getMessage();

        assertTrue(actualMessage1.contains(expectedMessage));
        assertTrue(actualMessage2.contains(expectedMessage));
    }

    @Test
    public void playerLeaveGame_ShouldThrowExceptionWhenGameDoesNotExist() throws Exception {
        User lobbyOwner = new User();
        lobbyOwner.setUserToken("userToken");
        lobbyOwner.setUsername("lobbyOwner");

        Game game = new Game();
        Long invalidGameId = 22L;
        game.setId(1L);
        game.setLobbyOwner("lobbyOwner");
        game.addPlayer(lobbyOwner);


        when(gameRepository.findById(invalidGameId)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Lobby not found"));
        when(userRepository.findByUserToken("userToken")).thenReturn(lobbyOwner);

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            gameService.playerLeaveGame(invalidGameId, "userToken");
        });

        String expectedMessage = "Lobby not found";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void playerLeaveGame_ShouldThrowExceptionWhenUserDoesNotExist() throws Exception {
        User lobbyOwner = new User();
        lobbyOwner.setUserToken("userToken");
        lobbyOwner.setUsername("lobbyOwner");

        Game game = new Game();
        Long gameId = 1L;
        game.setId(gameId);
        game.setLobbyOwner("lobbyOwner");
        game.addPlayer(lobbyOwner);

        User player1 = new User();
        player1.setUserToken("userToken2");
        player1.setUsername("player1");

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(userRepository.findByUserToken("userToken")).thenReturn(lobbyOwner);
        when(userRepository.findByUserToken("invalidToken")).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User with sent userToken does not exist"));

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            gameService.playerLeaveGame(gameId, "invalidToken");
        });

        String expectedMessage = "User with sent userToken does not exist";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void playerLeaveGame_ShouldThrowExceptionWhenUserIsNotInGame() throws Exception {
        User lobbyOwner = new User();
        lobbyOwner.setUserToken("userToken");
        lobbyOwner.setUsername("lobbyOwner");

        Game game = new Game();
        Long gameId = 1L;
        game.setId(gameId);
        game.setLobbyOwner("lobbyOwner");
        game.addPlayer(lobbyOwner);

        User player1 = new User();
        player1.setUserToken("userToken2");
        player1.setUsername("player1");

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(userRepository.findByUserToken("userToken")).thenReturn(lobbyOwner);
        when(userRepository.findByUserToken("userToken2")).thenReturn(player1);

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            gameService.playerLeaveGame(gameId, "userToken2");
        });

        String expectedMessage = "User not found in the game";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void playerLeaveGame_WithFindUserByTokenNull_ShouldThrowException() throws Exception {
        User lobbyOwner = new User();
        lobbyOwner.setUserToken("userToken");
        lobbyOwner.setUsername("lobbyOwner");

        Game game = new Game();
        Long gameId = 1L;
        game.setId(gameId);
        game.setLobbyOwner("lobbyOwner");
        game.addPlayer(lobbyOwner);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(userRepository.findByUserToken("userToken")).thenReturn(lobbyOwner);
        when(userRepository.findByUserToken("userToken2")).thenReturn(null);

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            gameService.playerLeaveGame(gameId, "userToken2");
        });

        String expectedMessage = "User with sent userToken does not exist";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    // @Test
    // public void playerLeaveGame_ShouldThrowExceptionWhenUserIsLobbyOwner() throws Exception {
    //     User lobbyOwner = new User();
    //     lobbyOwner.setUserToken("userToken");
    //     lobbyOwner.setUsername("lobbyOwner");

    //     Game game = new Game();
    //     Long gameId = 1L;
    //     game.setId(gameId);
    //     game.setLobbyOwner("lobbyOwner");
    //     game.addPlayer(lobbyOwner);

    //     when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
    //     when(userRepository.findByUserToken("userToken")).thenReturn(lobbyOwner);

    //     Exception exception = assertThrows(ResponseStatusException.class, () -> {
    //         gameService.playerLeaveGame(gameId, "userToken");
    //     });

    //     String expectedMessage = "Cannot remove the lobby owner from the game";
    //     String actualMessage = exception.getMessage();

    //     assertTrue(actualMessage.contains(expectedMessage));
    // }

    @Test
    public void generatePictureWithDallE_WithValidPrompt_ShouldReturnImageUrl() throws Exception {
        // Setup
        String inputPhrase =  "{\"description\":\"A picture of a cat\"}";
        String expectedUrl = "https://example.com/cat.jpg";

        DallE dallE = mock(DallE.class);
        GameService gameService = new GameService(userRepository, gameRepository, userService, dallE, chatGPT); 

        when(dallE.generatePicture("A picture of a cat")).thenReturn(expectedUrl);

        String imageUrl = gameService.generatePictureDallE(inputPhrase);

        assertNotNull(imageUrl);
        assertEquals(expectedUrl, imageUrl);
        verify(dallE).generatePicture("A picture of a cat");
    }

    @Test
    public void generatePictureWithDallE_WithEmptyPrompt_ShouldThrowException() throws Exception {
        String emptyPrompt =  "{\"description\":\"\"}";
        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            gameService.generatePictureDallE(emptyPrompt);
        });

        String expectedMessage = "Text prompt provided by the player is null or empty";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void generatePictureWithDallE_WithNullPrompt_ShouldThrowException() throws Exception {
        String nullPrompt =  "{\"description\":null}";
        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            gameService.generatePictureDallE(nullPrompt);
        });

        String expectedMessage = "Text prompt provided by the player is null or empty";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    // @Test
    // public void evaluatePlayerGuessWithChatGPT_WithValidInputs_ShouldReturnSimilarityScore() throws Exception {
    //     String originalText = "cat on the floor";
    //     String playerGuessed = "cat on the ground";
    //     float chatGPTResult = 0.85f;
    //     int expectedPoints = 6;
    //     String userToken = "userToken";

    //     User user = new User();
    //     user.setUserToken(userToken);
    //     user.setUsername("username");

    //     DallE dallE = mock(DallE.class);
    //     ChatGPT chatGPT = mock(ChatGPT.class);

    //     when(dallE.getInputPhrase()).thenReturn(originalText);
    //     when(userRepository.findByUserToken(userToken)).thenReturn(user);
    //     when(chatGPT.rateInputs(originalText, playerGuessed)).thenReturn(chatGPTResult);
    //     chatGPT.convertSimilarityScoreToPoints(user, chatGPTResult);
    //     GameService gameService = new GameService(userRepository, gameRepository, userService, dallE, chatGPT);
    //     gameService.evaluatePlayerGuessWithChatGPT(userToken, playerGuessed);
    //     assertEquals(expectedPoints, user.getScore());
    // }

    @Test
    public void evaluatePlayerGuessWithChatGPT_WithIncorrectGuess_ShouldReturnZero() throws Exception {
        String originalText = "cat on the floor";
        String playerGuessed = "dog on the floor";
        float chatGPTResult = 0.45f;
        int expectedPoints = 0;
        String userToken = "userToken";

        User user = new User();
        user.setUserToken(userToken);
        user.setUsername("username");

        DallE dallE = mock(DallE.class);
        ChatGPT chatGPT = mock(ChatGPT.class);

        when(dallE.getInputPhrase()).thenReturn(originalText);
        when(userRepository.findByUserToken(userToken)).thenReturn(user);
        when(chatGPT.rateInputs(originalText, playerGuessed)).thenReturn(chatGPTResult);
        chatGPT.convertSimilarityScoreToPoints(user, chatGPTResult);

        GameService gameService = new GameService(userRepository, gameRepository, userService, dallE, chatGPT);
        gameService.evaluatePlayerGuessWithChatGPT(userToken, playerGuessed);
        assertEquals(expectedPoints, user.getScore());
    }

    // @Test
    // public void evaluatePlayerGuessWithChatGPT_WithSimilarGuess_ShouldReturnFour() throws Exception {
    //     String originalText = "cat on the floor";
    //     String playerGuessed = "cat on the ground";
    //     float chatGPTResult = 0.70f;
    //     int expectedPoints = 4;
    //     String userToken = "userToken";

    //     User user = new User();
    //     user.setUserToken(userToken);
    //     user.setUsername("username");

    //     DallE dallE = mock(DallE.class);
    //     ChatGPT chatGPT = mock(ChatGPT.class);

    //     when(dallE.getInputPhrase()).thenReturn(originalText);
    //     when(userRepository.findByUserToken(userToken)).thenReturn(user);
    //     when(chatGPT.rateInputs(originalText, playerGuessed)).thenReturn(chatGPTResult);
    //     chatGPT.convertSimilarityScoreToPoints(user, chatGPTResult);

    //     GameService gameService = new GameService(userRepository, gameRepository, userService, dallE, chatGPT);
    //     gameService.evaluatePlayerGuessWithChatGPT(userToken, playerGuessed);
    //     assertEquals(expectedPoints, user.getScore());
    // }

    @Test
    public void evaluatePlayerGuessWithChatGPT_WithEmptyOrNullPlayerGuess_ShouldReturnZero() throws Exception {
        String originalText = "cat on the floor";
        String emptyPlayerGuessed = "";
        String nullPlayerGuessed = null;

        String userToken = "userToken";
        String userToken2 = "userToken2";

        User user = new User();
        user.setUserToken(userToken);
        user.setUsername("username");

        User user2 = new User();
        user2.setUserToken(userToken2);
        user2.setUsername("username2");

        int expectedNullPoints = 0;
        int expectedEmptyPoints = 0;

        DallE dallE = mock(DallE.class);

        when(dallE.getInputPhrase()).thenReturn(originalText);
        when(userRepository.findByUserToken(userToken)).thenReturn(user);
        when(userRepository.findByUserToken(userToken2)).thenReturn(user2);

        GameService gameService = new GameService(userRepository, gameRepository, userService, dallE, chatGPT);

        gameService.evaluatePlayerGuessWithChatGPT(userToken, emptyPlayerGuessed);
        gameService.evaluatePlayerGuessWithChatGPT(userToken2, nullPlayerGuessed);

        assertEquals(expectedEmptyPoints, user.getScore());
        assertEquals(expectedNullPoints, user2.getScore());
    }

    @Test
    public void evaluatePlayerGuessWithChatGPT_WithEmptyOrNullOriginalText_ShouldThrowException() throws Exception {
        String emptyOriginalText = "";
        String nullOriginalText = null;
        String playerGuessed = "cat on the ground";

        String userToken = "userToken";
        String userToken2 = "userToken2";

        User user = new User();
        user.setUserToken(userToken);
        user.setUsername("username");

        User user2 = new User();
        user2.setUserToken(userToken2);
        user2.setUsername("username2");

        DallE dallE = mock(DallE.class);
        ChatGPT chatGPT = mock(ChatGPT.class);

        when(dallE.getInputPhrase()).thenReturn(emptyOriginalText);
        when(userRepository.findByUserToken(userToken)).thenReturn(user);
        when(userRepository.findByUserToken(userToken2)).thenReturn(user2);

        GameService gameService = new GameService(userRepository, gameRepository, userService, dallE, chatGPT);

        Exception exceptionEmpty = assertThrows(ResponseStatusException.class, () -> {
            gameService.evaluatePlayerGuessWithChatGPT(userToken, playerGuessed);
        });

        when(dallE.getInputPhrase()).thenReturn(nullOriginalText);

        Exception exceptionNull = assertThrows(ResponseStatusException.class, () -> {
            gameService.evaluatePlayerGuessWithChatGPT(userToken2, playerGuessed);
        });

        String expectedMessage = "Image description is null or empty";
        String actualMessageEmpty = exceptionEmpty.getMessage();
        String actualMessageNull = exceptionNull.getMessage();

        assertTrue(actualMessageEmpty.contains(expectedMessage));
        assertTrue(actualMessageNull.contains(expectedMessage));
    }

    @Test
    public void getNextPictureGenerator_WithInvalidGameId_ShouldThrowException() {
        Long invalidGameId = 22L;
        when(gameRepository.findById(invalidGameId)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Lobby not found"));

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            gameService.getNextPictureGenerator(invalidGameId);
        });

        String expectedMessage = "Lobby not found";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void getNextPictureGenerator_WithGameIdAndGameExists_ShouldReturnNextPlayer() throws Exception {
        Game game = new Game();
        Long gameId = 1L;
        game.setId(gameId);
        game.setLobbyOwner("lobbyOwner");

        User player1 = new User();
        player1.setUserToken("userToken1");
        player1.setUsername("player1");

        User player2 = new User();
        player2.setUserToken("userToken2");
        player2.setUsername("player2");

        game.addPlayer(player1);
        game.addPlayer(player2);

        Set<String> remaininPictureGenerators = new HashSet<>();
        remaininPictureGenerators.add("player1");
        remaininPictureGenerators.add("player2");
        game.setRemaininPictureGenerators(remaininPictureGenerators);
        game.setGameStarted(true);
        game.setAmtOfRounds(3);

        // game.s
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        String selectedPlayer = gameService.getNextPictureGenerator(gameId);

        assertNotNull(selectedPlayer);
        verify(gameRepository).findById(gameId);
        verify(gameRepository).save(game);
        verify(gameRepository).flush();
    }

    @Test
    public void getNextPictureGenerator_WithGameIdAndAllPlayersSelected_ShouldThrowException() throws Exception {
        Game game = new Game();
        Long gameId = 1L;
        game.setId(gameId);
        game.setLobbyOwner("lobbyOwner");

        User player1 = new User();
        player1.setUserToken("userToken1");
        player1.setUsername("player1");

        User player2 = new User();
        player2.setUserToken("userToken2");
        player2.setUsername("player2");

        Set<String> remaininPictureGenerators = new HashSet<>();
        remaininPictureGenerators.add("player1");
        remaininPictureGenerators.add("player2");
        game.setRemaininPictureGenerators(remaininPictureGenerators);

        game.setAmtOfRounds(1);
        game.addPlayer(player2);
        game.addPlayer(player1);
        game.setGameStarted(true);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        String selectedPlayer1 = gameService.getNextPictureGenerator(gameId);
        String selectedPlayer2 = gameService.getNextPictureGenerator(gameId);

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            gameService.getNextPictureGenerator(gameId);
        });

        String expectedMessage = "All rounds have been played";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
        assertNotNull(selectedPlayer1);
        assertNotNull(selectedPlayer2);
    }

    @Test
    public void getNextPictureGenerator_WithGameIdNullorZerro_ShouldThrowException() throws Exception {
        Long invalidGameId = 0L;
        when(gameRepository.findById(invalidGameId)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Lobby not found"));

        Exception exception1 = assertThrows(ResponseStatusException.class, () -> {
            gameService.getNextPictureGenerator(null);
        });

        Exception exception2 = assertThrows(ResponseStatusException.class, () -> {
            gameService.getNextPictureGenerator(0L);
        });

        String expectedMessage = "Game ID is null";

        String actualMessage1 = exception1.getMessage();
        String actualMessage2 = exception2.getMessage();

        assertTrue(actualMessage1.contains(expectedMessage));
        assertTrue(actualMessage2.contains(expectedMessage));

    }

    @Test
    public void getNextPictureGenerator_WithNextGeneratorNull_ShouldThrowException() throws Exception {
        Game game = new Game();
        Long gameId = 1L;
        game.setId(gameId);
        game.setLobbyOwner("lobbyOwner");

        User player1 = new User();
        player1.setUserToken("userToken1");
        player1.setUsername("player1");

        User player2 = new User();
        player2.setUserToken("userToken2");
        player2.setUsername("player2");

        Set<String> remaininPictureGenerators = new HashSet<>();
        remaininPictureGenerators.add("player1");
        remaininPictureGenerators.add("player2");
        game.setRemaininPictureGenerators(remaininPictureGenerators);

        game.setAmtOfRounds(1);
        game.addPlayer(player2);
        game.addPlayer(player1);
        game.setGameStarted(true);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        String selectedPlayer1 = gameService.getNextPictureGenerator(gameId);
        remaininPictureGenerators.remove(selectedPlayer1);
        String selectedPlayer2 = gameService.getNextPictureGenerator(gameId);
        remaininPictureGenerators.remove(selectedPlayer2);

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            gameService.getNextPictureGenerator(gameId);
        });
        String expectedMessage = "All rounds have been played. Game is over.";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
        assertNotNull(selectedPlayer1);
        assertNotNull(selectedPlayer2);
    }

    @Test
    public void hostRemovePlayerFromLobby_WithValidGameIdAndTokens_ShouldRemovePlayerSuccessfully() throws Exception {
        User lobbyOwner = new User();
        lobbyOwner.setUserToken("userToken");
        lobbyOwner.setUsername("lobbyOwner");

        User player1 = new User();
        player1.setUserToken("userToken2");
        player1.setUsername("player1");

        User player2 = new User();
        player2.setUserToken("userToken3");
        player2.setUsername("player2");

        Game game = new Game();
        Long gameId = 1L;
        game.setId(gameId);
        game.setLobbyOwner("lobbyOwner");
        game.addPlayer(player1);
        game.addPlayer(player2);
        game.addPlayer(lobbyOwner);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(userRepository.findByUserToken("userToken")).thenReturn(lobbyOwner);
        when(userRepository.findByUserToken("userToken2")).thenReturn(player1);
        when(userRepository.findByUserToken("userToken3")).thenReturn(player2);

        gameService.hostRemovePlayerFromLobby(gameId, lobbyOwner.getUserToken(), player2.getUserToken());
        // Assert
        assertNotNull(game);
        assertEquals(2, game.getUsers().size());
    }

    @Test
    public void hostRemovePlayerFromLobby_WithPlayerEmptyOrNullToken_ShouldThrowException() throws Exception {
        User lobbyOwner = new User();
        lobbyOwner.setUserToken("userToken");
        lobbyOwner.setUsername("lobbyOwner");

        Game game = new Game();
        Long gameId = 1L;
        game.setId(gameId);
        game.setLobbyOwner("lobbyOwner");
        game.addPlayer(lobbyOwner);

        User player1EmptyToken = new User();
        player1EmptyToken.setUserToken("");
        player1EmptyToken.setUsername("player1");

        User player2NullToken = new User();
        player2NullToken.setUserToken(null);
        player2NullToken.setUsername("player2");

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(userRepository.findByUserToken("userToken")).thenReturn(lobbyOwner);
        when(userRepository.findByUserToken(player1EmptyToken.getUserToken())).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User does not exist"));
        when(userRepository.findByUserToken(player2NullToken.getUserToken())).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User does not exist"));

        Exception exception1 = assertThrows(ResponseStatusException.class, () -> {
            gameService.hostRemovePlayerFromLobby(gameId, lobbyOwner.getUserToken(), player1EmptyToken.getUserToken());
        });

        Exception exception2 = assertThrows(ResponseStatusException.class, () -> {
            gameService.hostRemovePlayerFromLobby(gameId, lobbyOwner.getUserToken(), player2NullToken.getUserToken());
        });

        String expectedMessage = "UserToken is null or empty";
        String actualMessage1 = exception1.getMessage();
        String actualMessage2 = exception2.getMessage();

        assertTrue(actualMessage1.contains(expectedMessage));
        assertTrue(actualMessage2.contains(expectedMessage));
    }

    @Test
    public void hostRemovePlayerFromLobby_WithGameIdIsNullOrZerro_ShouldThrowException() throws Exception {
        User lobbyOwner = new User();
        lobbyOwner.setUserToken("userToken");
        lobbyOwner.setUsername("lobbyOwner");

        User player = new User();
        player.setUserToken("userToken2");
        player.setUsername("player");

        Game game = new Game();
        Long gameId = 1L;
        game.setId(gameId);
        game.setLobbyOwner("lobbyOwner");
        game.addPlayer(lobbyOwner);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(userRepository.findByUserToken("userToken")).thenReturn(lobbyOwner);
        when(userRepository.findByUserToken("userToken2")).thenReturn(player);

        Exception exception1 = assertThrows(ResponseStatusException.class, () -> {
            gameService.hostRemovePlayerFromLobby(null, "userToken", "userToken2");
        });

        Exception exception2 = assertThrows(ResponseStatusException.class, () -> {
            gameService.hostRemovePlayerFromLobby(0L, "userToken", "userToken2");
        });

        String expectedMessage = "Game ID is null or zero";
        String actualMessage1 = exception1.getMessage();
        String actualMessage2 = exception2.getMessage();

        assertTrue(actualMessage1.contains(expectedMessage));
        assertTrue(actualMessage2.contains(expectedMessage));
    }

    @Test
    public void hostRemovePlayerFromLobby_WithGameDoesNotExist_ShouldThrowException() throws Exception {
        User lobbyOwner = new User();
        lobbyOwner.setUserToken("userToken");
        lobbyOwner.setUsername("lobbyOwner");

        User player = new User();
        player.setUserToken("userToken2");
        player.setUsername("player");

        Game game = new Game();
        Long invalidGameId = 22L;
        game.setId(1L);
        game.setLobbyOwner("lobbyOwner");
        game.addPlayer(lobbyOwner);

        when(gameRepository.findById(invalidGameId)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Lobby not found"));
        when(userRepository.findByUserToken("userToken")).thenReturn(lobbyOwner);
        when(userRepository.findByUserToken("userToken2")).thenReturn(player);

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            gameService.hostRemovePlayerFromLobby(invalidGameId, "userToken", "userToken2");
        });

        String expectedMessage = "Lobby not found";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void hostRemovePlayerFromLobby_WithUserDoesNotExist_ShouldThrowException() throws Exception {
        User lobbyOwner = new User();
        lobbyOwner.setUserToken("userToken");
        lobbyOwner.setUsername("lobbyOwner");

        User player = new User();
        player.setUserToken("userToken2");
        player.setUsername("player");

        Game game = new Game();
        Long gameId = 1L;
        game.setId(gameId);
        game.setLobbyOwner("lobbyOwner");
        game.addPlayer(lobbyOwner);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(userRepository.findByUserToken("userToken")).thenReturn(lobbyOwner);
        when(userRepository.findByUserToken("invalidToken")).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User with sent userToken does not exist"));

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            gameService.hostRemovePlayerFromLobby(gameId, "userToken", "invalidToken");
        });

        String expectedMessage = "User with sent userToken does not exist";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void hostRemovePlayerFromLobby_WithUserIsNotInGame_ShouldThrowException() throws Exception {
        User lobbyOwner = new User();
        lobbyOwner.setUserToken("userToken");
        lobbyOwner.setUsername("lobbyOwner");

        User player = new User();
        player.setUserToken("userToken2");
        player.setUsername("player");

        Game game = new Game();
        Long gameId = 1L;
        game.setId(gameId);
        game.setLobbyOwner("lobbyOwner");
        game.addPlayer(lobbyOwner);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(userRepository.findByUserToken("userToken")).thenReturn(lobbyOwner);
        when(userRepository.findByUserToken("userToken2")).thenReturn(player);

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            gameService.hostRemovePlayerFromLobby(gameId, "userToken", "userToken2");
        });

        String expectedMessage = "User not found in the game";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void hostRemovePlayerFromLobby_WithUserTryToRemoveLobbyOwner_ShouldThrowException() throws Exception {
        User lobbyOwner = new User();
        lobbyOwner.setUserToken("userToken");
        lobbyOwner.setUsername("lobbyOwner");

        User player = new User();
        player.setUserToken("userToken2");
        player.setUsername("player");

        Game game = new Game();
        Long gameId = 1L;
        game.setId(gameId);
        game.setLobbyOwner("lobbyOwner");
        game.addPlayer(lobbyOwner);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(userRepository.findByUserToken("userToken")).thenReturn(lobbyOwner);
        when(userRepository.findByUserToken("userToken2")).thenReturn(player);

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            gameService.hostRemovePlayerFromLobby(gameId, "userToken2", "userToken");
        });

        String expectedMessage = "Only host is allowed to remove player from lobby";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void hostRemovePlayerFromLobby_WithPlayerTryRejoin_ShouldThrowException() throws Exception {
        User lobbyOwner = new User();
        lobbyOwner.setUserToken("userToken");
        lobbyOwner.setUsername("lobbyOwner");

        User player = new User();
        player.setUserToken("userToken2");
        player.setUsername("player");

        Game game = new Game();
        Long gameId = 1L;
        game.setId(gameId);
        game.setLobbyOwner("lobbyOwner");
        game.addPlayer(lobbyOwner);
        game.addPlayer(player);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(userRepository.findByUserToken("userToken")).thenReturn(lobbyOwner);
        when(userRepository.findByUserToken("userToken2")).thenReturn(player);

        gameService.hostRemovePlayerFromLobby(gameId, "userToken", "userToken2");

        // player tries to rejoin, should throw exception
        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            game.addPlayer(player);
        });
        String expectedMessage = "You have been removed from the lobby by the host. Sorry, you cannot rejoin.";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void hostRemovePlayerFromLobby_WithHostTokenNullOrEmpty_ShouldThrowException() throws Exception {
        User lobbyOwner = new User();
        lobbyOwner.setUserToken("userToken");
        lobbyOwner.setUsername("lobbyOwner");

        User player = new User();
        player.setUserToken("userToken2");
        player.setUsername("player");

        Game game = new Game();
        Long gameId = 1L;
        game.setId(gameId);
        game.setLobbyOwner("lobbyOwner");
        game.addPlayer(lobbyOwner);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(userRepository.findByUserToken("userToken")).thenReturn(lobbyOwner);
        when(userRepository.findByUserToken("userToken2")).thenReturn(player);

        Exception exception1 = assertThrows(ResponseStatusException.class, () -> {
            gameService.hostRemovePlayerFromLobby(gameId, "", "userToken2");
        });

        Exception exception2 = assertThrows(ResponseStatusException.class, () -> {
            gameService.hostRemovePlayerFromLobby(gameId, null, "userToken2");
        });

        String expectedMessage = "Host Token is null or empty";
        String actualMessage1 = exception1.getMessage();
        String actualMessage2 = exception2.getMessage();

        assertTrue(actualMessage1.contains(expectedMessage));
        assertTrue(actualMessage2.contains(expectedMessage));
    }

    @Test
    public void hostRemovePlayerFromLobby_WithFindUserByTokenNull_ShouldThrowException() throws Exception {
        User lobbyOwner = new User();
        lobbyOwner.setUserToken("userToken");
        lobbyOwner.setUsername("lobbyOwner");

        User player = new User();
        player.setUserToken("userToken2");
        player.setUsername("player");

        Game game = new Game();
        Long gameId = 1L;
        game.setId(gameId);
        game.setLobbyOwner("lobbyOwner");
        game.addPlayer(lobbyOwner);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(userRepository.findByUserToken("userToken")).thenReturn(lobbyOwner);
        when(userRepository.findByUserToken("userToken2")).thenReturn(null);

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            gameService.hostRemovePlayerFromLobby(gameId, "userToken", "userToken2");
        });

        String expectedMessage = "User with sent userToken does not exist";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void hostRemovePlayerFromLobby_WithFindHostByTokenNull_ShouldThrowException() throws Exception {
        User lobbyOwner = new User();
        lobbyOwner.setUserToken("userToken");
        lobbyOwner.setUsername("lobbyOwner");

        User player = new User();
        player.setUserToken("userToken2");
        player.setUsername("player");

        Game game = new Game();
        Long gameId = 1L;
        game.setId(gameId);
        game.setLobbyOwner("lobbyOwner");
        game.addPlayer(lobbyOwner);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(userRepository.findByUserToken("userToken")).thenReturn(null);
        when(userRepository.findByUserToken("userToken2")).thenReturn(player);

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            gameService.hostRemovePlayerFromLobby(gameId, "userToken", "userToken2");
        });

        String expectedMessage = "Host with sent hostToken does not exist";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void deleteLobby_WithValidInputs_ShouldReturnSuccessMessage() throws Exception {
        User lobbyOwner = new User();
        lobbyOwner.setUserToken("userToken");
        lobbyOwner.setUsername("lobbyOwner");

        Game game = new Game();
        Long gameId = 1L;
        game.setId(gameId);
        game.setLobbyOwner("lobbyOwner");

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(userRepository.findByUserToken("userToken")).thenReturn(lobbyOwner);

        gameService.deleteLobby(gameId, "userToken");

        verify(gameRepository).delete(game);
        when(gameRepository.findById(gameId)).thenReturn(Optional.empty());
    }

    @Test
    public void deleteLobby_WithInvalidGameId_ShouldThrowException() throws Exception {
        Long invalidGameId = 22L;
        when(gameRepository.findById(invalidGameId)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Lobby not found"));

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            gameService.deleteLobby(invalidGameId, "userToken");
        });

        String expectedMessage = "Lobby not found";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void deleteLobby_WithInvalidUserToken_ShouldThrowException() throws Exception {
        User lobbyOwner = new User();
        lobbyOwner.setUserToken("userToken");
        lobbyOwner.setUsername("lobbyOwner");

        Game game = new Game();
        Long gameId = 1L;
        game.setId(gameId);
        game.setLobbyOwner("lobbyOwner");

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(userRepository.findByUserToken("userToken")).thenReturn(lobbyOwner);
        when(userRepository.findByUserToken("invalidToken")).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User with sent userToken does not exist"));

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            gameService.deleteLobby(gameId, "invalidToken");
        });

        String expectedMessage = "User with sent userToken does not exist";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void deleteLobby_WithUserNotLobbyOwner_ShouldThrowException() throws Exception {
        User lobbyOwner = new User();
        lobbyOwner.setUserToken("userToken");
        lobbyOwner.setUsername("lobbyOwner");

        User player = new User();
        player.setUserToken("userToken2");
        player.setUsername("player");

        Game game = new Game();
        Long gameId = 1L;
        game.setId(gameId);
        game.setLobbyOwner("lobbyOwner");
        game.addPlayer(player);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(userRepository.findByUserToken("userToken")).thenReturn(lobbyOwner);
        when(userRepository.findByUserToken("userToken2")).thenReturn(player);

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            gameService.deleteLobby(gameId, "userToken2");
        });

        String expectedMessage = "Only host is allowed to delete lobby";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void deleteLobby_WithGameIdNullOrZero_ShouldThrowException() throws Exception {
        Long gameId = 0L;
        Long gameIdNull = null;

        Exception exception1 = assertThrows(ResponseStatusException.class, () -> {
            gameService.deleteLobby(gameId, "userToken");
        });

        Exception exception2 = assertThrows(ResponseStatusException.class, () -> {
            gameService.deleteLobby(gameIdNull, "userToken");
        });

        String expectedMessage = "Game ID is null or zero";
        String actualMessage1 = exception1.getMessage();
        String actualMessage2 = exception2.getMessage();

        assertTrue(actualMessage1.contains(expectedMessage));
        assertTrue(actualMessage2.contains(expectedMessage));
    }

    @Test
    public void deleteLobby_WithUserTokenNullOrEmpty_ShouldThrowException() throws Exception {
        User lobbyOwner = new User();
        lobbyOwner.setUserToken("userToken");
        lobbyOwner.setUsername("lobbyOwner");

        Game game = new Game();
        Long gameId = 1L;
        game.setId(gameId);
        game.setLobbyOwner("lobbyOwner");

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(userRepository.findByUserToken("userToken")).thenReturn(lobbyOwner);

        Exception exception1 = assertThrows(ResponseStatusException.class, () -> {
            gameService.deleteLobby(gameId, "");
        });

        Exception exception2 = assertThrows(ResponseStatusException.class, () -> {
            gameService.deleteLobby(gameId, null);
        });

        String expectedMessage = "UserToken is null or empty";
        String actualMessage1 = exception1.getMessage();
        String actualMessage2 = exception2.getMessage();

        assertTrue(actualMessage1.contains(expectedMessage));
        assertTrue(actualMessage2.contains(expectedMessage));

    }

    @Test
    public void deleteLobby_WithFindUserByTokenNull_ShouldThrowException() throws Exception {
        User lobbyOwner = new User();
        lobbyOwner.setUserToken("userToken");
        lobbyOwner.setUsername("lobbyOwner");

        Game game = new Game();
        Long gameId = 1L;
        game.setId(gameId);
        game.setLobbyOwner("lobbyOwner");

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(userRepository.findByUserToken("userToken")).thenReturn(lobbyOwner);
        when(userRepository.findByUserToken("invalidToken")).thenReturn(null);

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            gameService.deleteLobby(gameId, "invalidToken");
        });

        String expectedMessage = "User with sent userToken does not exist";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void gameIsFinishedLeaveLobby_WithValidInputs_ShouldReturnNothing() throws Exception {
        User lobbyOwner = new User();
        lobbyOwner.setUserToken("userToken");
        lobbyOwner.setUsername("lobbyOwner");

        User player1 = new User();
        player1.setUserToken("userToken2");
        player1.setUsername("player1");

        User player2 = new User();
        player2.setUserToken("userToken3");
        player2.setUsername("player2");

        Game game = new Game();
        Long gameId = 1L;
        game.setId(gameId);
        game.setLobbyOwner("lobbyOwner");
        game.addPlayer(player1);
        game.addPlayer(player2);
        game.addPlayer(lobbyOwner);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(userRepository.findByUserToken("userToken")).thenReturn(lobbyOwner);
        when(userRepository.findByUserToken("userToken2")).thenReturn(player1);
        when(userRepository.findByUserToken("userToken3")).thenReturn(player2);

        gameService.gameIsFinishedLeaveLobby(gameId, "userToken");
        gameService.gameIsFinishedLeaveLobby(gameId, "userToken2");

        // check remaining players size in the game
        assertEquals(1, game.getUsers().size());
    }

    @Test
    public void gameIsFinishedLeaveLobby_WitUserNotInTheLobby_ShouldThrowExpection() throws Exception {
        User lobbyOwner = new User();
        lobbyOwner.setUserToken("userToken");
        lobbyOwner.setUsername("lobbyOwner");

        User player1 = new User();
        player1.setUserToken("userToken2");
        player1.setUsername("player1");

        User player2 = new User();
        player2.setUserToken("userToken3");
        player2.setUsername("player2");

        Game game = new Game();
        Long gameId = 1L;
        game.setId(gameId);
        game.setLobbyOwner("lobbyOwner");
        game.addPlayer(player1);
        game.addPlayer(lobbyOwner);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(userRepository.findByUserToken("userToken")).thenReturn(lobbyOwner);
        when(userRepository.findByUserToken("userToken2")).thenReturn(player1);
        when(userRepository.findByUserToken("userToken3")).thenReturn(player2);

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            gameService.gameIsFinishedLeaveLobby(gameId, "userToken3");
        });

        String expectedMessage = "User is not in the specified lobby";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void gameIsFinishedLeaveLobby_WithUserTokenNullOrEmpty_ShouldThrowException() throws Exception {
        User lobbyOwner = new User();
        lobbyOwner.setUserToken("userToken");
        lobbyOwner.setUsername("lobbyOwner");

        Game game = new Game();
        Long gameId = 1L;
        game.setId(gameId);
        game.setLobbyOwner("lobbyOwner");
        game.addPlayer(lobbyOwner);

        User player = new User();
        player.setUserToken("userToken2");
        player.setUsername("player");
        game.addPlayer(player);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(userRepository.findByUserToken("userToken")).thenReturn(lobbyOwner);
        when(userRepository.findByUserToken("userToken2")).thenReturn(player);

        Exception exception1 = assertThrows(ResponseStatusException.class, () -> {
            gameService.gameIsFinishedLeaveLobby(gameId, "");
        });

        Exception exception2 = assertThrows(ResponseStatusException.class, () -> {
            gameService.gameIsFinishedLeaveLobby(gameId, null);
        });

        String expectedMessage = "User token is null or empty";
        String actualMessage1 = exception1.getMessage();
        String actualMessage2 = exception2.getMessage();

        assertTrue(actualMessage1.contains(expectedMessage));
        assertTrue(actualMessage2.contains(expectedMessage));
    }

    @Test
    public void gameIsFinishedLeaveLobby_WithLobbyIdNull_ShouldThrowException() throws Exception {
        Long gameId = 0L;
        Long gameIdNull = null;

        Exception exception1 = assertThrows(ResponseStatusException.class, () -> {
            gameService.gameIsFinishedLeaveLobby(gameId, "userToken");
        });

        Exception exception2 = assertThrows(ResponseStatusException.class, () -> {
            gameService.gameIsFinishedLeaveLobby(gameIdNull, "userToken");
        });

        String expectedMessage = "Lobby ID is null";
        String actualMessage1 = exception1.getMessage();
        String actualMessage2 = exception2.getMessage();

        assertTrue(actualMessage1.contains(expectedMessage));
        assertTrue(actualMessage2.contains(expectedMessage));
    }

//    @Test
//    public void playerLeaveCurrentLobby_WithValidUserToken_ShouldBeSucessfull() throws Exception {
//        User lobbyOwner = new User();
//        lobbyOwner.setUserToken("userToken");
//        lobbyOwner.setUsername("lobbyOwner");
//
//        User player1 = new User();
//        player1.setUserToken("userToken2");
//        player1.setUsername("player1");
//
//        User player2 = new User();
//        player2.setUserToken("userToken3");
//        player2.setUsername("player2");
//
//        Game game = new Game();
//        Long gameId = 1L;
//        game.setId(gameId);
//        game.setLobbyOwner("lobbyOwner");
//        game.addPlayer(player1);
//        game.addPlayer(player2);
//        game.addPlayer(lobbyOwner);
//
//        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
//        when(userRepository.findByUserToken("userToken")).thenReturn(lobbyOwner);
//        when(userRepository.findByUserToken("userToken2")).thenReturn(player1);
//        when(userRepository.findByUserToken("userToken3")).thenReturn(player2);
//
//        gameService.playerLeaveCurrentLobby("userToken2");
//
//        // check remaining players size in the game
//        assertEquals(2, game.getUsers().size());
//    }

    @Test
    public void playerLeaveCurrentLobby_WithInvalidUserToken_ShouldThrowException() throws Exception {
        User lobbyOwner = new User();
        lobbyOwner.setUserToken("userToken");
        lobbyOwner.setUsername("lobbyOwner");

        User player1 = new User();
        player1.setUserToken("userToken2");
        player1.setUsername("player1");

        Game game = new Game();
        Long gameId = 1L;
        game.setId(gameId);
        game.setLobbyOwner("lobbyOwner");
        game.addPlayer(player1);
        game.addPlayer(lobbyOwner);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(userRepository.findByUserToken("userToken")).thenReturn(lobbyOwner);
        when(userRepository.findByUserToken("userToken2")).thenReturn(player1);

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            gameService.playerLeaveCurrentLobby("invalidToken");
        });

        String expectedMessage = "User with userToken has not been found";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testGetLastImageDescription_WhenDescriptionIsSet_ShouldReturnDescription() {
        String expectedDescription = "A beautiful sunset over the ocean";
        DallE dallE = mock(DallE.class);
        when(dallE.getInputPhrase()).thenReturn(expectedDescription);

        UserRepository userRepository = mock(UserRepository.class);
        GameRepository gameRepository = mock(GameRepository.class);
        UserService userService = mock(UserService.class);
        ChatGPT chatGPT = mock(ChatGPT.class);

        GameService gameService = new GameService(userRepository, gameRepository, userService, dallE, chatGPT);

        String actualDescription = gameService.getLastImageDescription();

        assertEquals(expectedDescription, actualDescription, "The returned image description should match the expected value.");
    }

    @Test
    public void testGetLastImageDescription_WhenDescriptionIsNotSet_ShouldReturnEmptyString() {
        String expectedDescription = "";
        DallE dallE = mock(DallE.class);
        when(dallE.getInputPhrase()).thenReturn(expectedDescription);

        UserRepository userRepository = mock(UserRepository.class);
        GameRepository gameRepository = mock(GameRepository.class);
        UserService userService = mock(UserService.class);
        ChatGPT chatGPT = mock(ChatGPT.class);

        GameService gameService = new GameService(userRepository, gameRepository, userService, dallE, chatGPT);

        String actualDescription = gameService.getLastImageDescription();

        assertEquals(expectedDescription, actualDescription, "The returned image description should match the expected value.");
    }

    @Test
    public void getGame_WithValidGameId_ShouldReturnGame() {
        Game game = new Game();
        Long gameId = 1L;
        game.setId(gameId);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        GameGetDTO actualGame = gameService.getGame(gameId);

        assertEquals(game.getId(), actualGame.getId());
        assertNotNull(actualGame);

    }

    @Test
    public void getGame_WithInvalidGameId_ShouldThrowException() {
        Long invalidGameId = 22L;
        when(gameRepository.findById(invalidGameId)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Lobby not found"));

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            gameService.getGame(invalidGameId);
        });

        String expectedMessage = "Lobby not found";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void getGame_WithGameIdNullOrZero_ShouldThrowException() {
        Long gameId = 0L;
        Long gameIdNull = null;

        Exception exception1 = assertThrows(ResponseStatusException.class, () -> {
            gameService.getGame(gameId);
        });

        Exception exception2 = assertThrows(ResponseStatusException.class, () -> {
            gameService.getGame(gameIdNull);
        });

        String expectedMessage = "Game ID is null";
        String actualMessage1 = exception1.getMessage();
        String actualMessage2 = exception2.getMessage();

        assertTrue(actualMessage1.contains(expectedMessage));
        assertTrue(actualMessage2.contains(expectedMessage));
    }

    @Test
    public void getAllGames_ShouldReturnAllGames() {
        Game game1 = new Game();
        Long gameId1 = 1L;
        game1.setId(gameId1);

        Game game2 = new Game();
        Long gameId2 = 2L;
        game2.setId(gameId2);

        List<Game> games = new ArrayList<>();
        games.add(game1);
        games.add(game2);

        when(gameRepository.findAll()).thenReturn(games);

        List<Game> actualGames = gameService.getAllGames();

        assertEquals(2, actualGames.size());
        assertEquals(game1.getId(), actualGames.get(0).getId());
        assertEquals(game2.getId(), actualGames.get(1).getId());
    }

    @Test
    public void getAllGames_WithNoGames_ShouldReturnEmptyList() {
        List<Game> games = new ArrayList<>();

        when(gameRepository.findAll()).thenReturn(games);

        List<Game> actualGames = gameService.getAllGames();

        assertEquals(0, actualGames.size());
    }

    @Test
    public void getAllUsers() {
        User user1 = new User();
        user1.setUserToken("userToken1");
        user1.setUsername("user1");

        User user2 = new User();
        user2.setUserToken("userToken2");
        user2.setUsername("user2");

        List<User> users = new ArrayList<>();
        users.add(user1);
        users.add(user2);

        when(userRepository.findAll()).thenReturn(users);

        List<User> actualUsers = gameService.getAllUsers();

        assertEquals(2, actualUsers.size());
        assertEquals(user1.getUserToken(), actualUsers.get(0).getUserToken());
        assertEquals(user2.getUserToken(), actualUsers.get(1).getUserToken());
    }

    @Test
    public void getAllUsers_WithNoUsers_ShouldReturnEmptyList() {
        List<User> users = new ArrayList<>();

        when(userRepository.findAll()).thenReturn(users);

        List<User> actualUsers = gameService.getAllUsers();

        assertEquals(0, actualUsers.size());
    }

    @Test
    public void startGame_WhenGameFound_ShouldReturnGameStarted() throws Exception {
        User lobbyOwner = new User();
        lobbyOwner.setUserToken("userToken");
        lobbyOwner.setUsername("lobbyOwner");

        User player1 = new User();
        player1.setUserToken("userToken2");
        player1.setUsername("player1");

        User player2 = new User();
        player2.setUserToken("userToken3");
        player2.setUsername("player2");

        Game game = new Game();
        Long gameId = 1L;
        game.setId(gameId);
        game.setLobbyOwner("lobbyOwner");
        game.addPlayer(player1);
        game.addPlayer(player2);
        game.addPlayer(lobbyOwner);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(userRepository.findByUserToken("userToken")).thenReturn(lobbyOwner);

        gameService.startGameLobby(gameId);

        assertTrue(game.isGameStarted());
    }

    @Test
    public void startGame_WhenGameNotFound_ShouldThrowException() throws Exception {
        Long invalidGameId = 22L;
        when(gameRepository.findById(invalidGameId)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Lobby not found"));

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            gameService.startGameLobby(invalidGameId);
        });

        String expectedMessage = "Lobby not found";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

    }

    @Test
    public void startGame_WhenGameIdNullOrZero_ShouldThrowException() throws Exception {
        Long gameId = 0L;
        Long gameIdNull = null;

        Exception exception1 = assertThrows(ResponseStatusException.class, () -> {
            gameService.startGameLobby(gameId);
        });

        Exception exception2 = assertThrows(ResponseStatusException.class, () -> {
            gameService.startGameLobby(gameIdNull);
        });

        String expectedMessage = "Game ID is null";
        String actualMessage1 = exception1.getMessage();
        String actualMessage2 = exception2.getMessage();

        assertTrue(actualMessage1.contains(expectedMessage));
        assertTrue(actualMessage2.contains(expectedMessage));
    }

    @Test
    public void createLobby_WithInvalidTimeLlimits_ShouldThrowExceptions() throws Exception {
        User user = new User();
        user.setUserToken("userToken");
        user.setUsername("user");

        GamePostDTO gamePostDTO = new GamePostDTO();
        gamePostDTO.setTimeLimit(4.0f);
        gamePostDTO.setAmtOfRounds(10);

        GamePostDTO gamePostDTO2 = new GamePostDTO();
        gamePostDTO2.setTimeLimit(-1.0f);
        gamePostDTO2.setAmtOfRounds(10);

        GamePostDTO gamePostDTO3 = new GamePostDTO();
        gamePostDTO3.setTimeLimit(101.0f);
        gamePostDTO3.setAmtOfRounds(10);

        when(userRepository.findByUserToken("userToken")).thenReturn(user);

        assertThrows(IllegalArgumentException.class, () -> {
            gameService.createLobby(user.getUserToken(), gamePostDTO);
        }, "Time limit must be between 5 seconds and 100 Seconds.");

        assertThrows(IllegalArgumentException.class, () -> {
            gameService.createLobby(user.getUserToken(), gamePostDTO2);
        }, "Time limit must be between 5 seconds and 100 Seconds.");

        assertThrows(IllegalArgumentException.class, () -> {
            gameService.createLobby(user.getUserToken(), gamePostDTO3);
        }, "Time limit must be between 5 seconds and 100 Seconds.");
    }

    @Test
    public void createLobby_WithInvalidAmtOfRounds_ShouldThrowExceptions() throws Exception {
        User user = new User();
        user.setUserToken("userToken");
        user.setUsername("user");

        GamePostDTO gamePostDTO = new GamePostDTO();
        gamePostDTO.setTimeLimit(10.0f);
        gamePostDTO.setAmtOfRounds(0);

        GamePostDTO gamePostDTO2 = new GamePostDTO();
        gamePostDTO2.setTimeLimit(10.0f);
        gamePostDTO2.setAmtOfRounds(-1);

        when(userRepository.findByUserToken("userToken")).thenReturn(user);

        assertThrows(IllegalArgumentException.class, () -> {
            gameService.createLobby(user.getUserToken(), gamePostDTO);
        }, "There must be at least one round.");

        assertThrows(IllegalArgumentException.class, () -> {
            gameService.createLobby(user.getUserToken(), gamePostDTO2);
        }, "There must be at least one round.");
    }

    @Test
    public void createLobby_WithInvalidMaxOfPlayers_ShouldThrowExceptions() throws Exception {
        User user = new User();
        user.setUserToken("userToken");
        user.setUsername("user");

        GamePostDTO gamePostDTO = new GamePostDTO();
        gamePostDTO.setTimeLimit(10.0f);
        gamePostDTO.setAmtOfRounds(10);
        gamePostDTO.setMaxAmtUsers(1);

        GamePostDTO gamePostDTO2 = new GamePostDTO();
        gamePostDTO2.setTimeLimit(10.0f);
        gamePostDTO2.setAmtOfRounds(10);
        gamePostDTO2.setMaxAmtUsers(-11);

        when(userRepository.findByUserToken("userToken")).thenReturn(user);

        assertThrows(IllegalArgumentException.class, () -> {
            gameService.createLobby(user.getUserToken(), gamePostDTO);
        }, "The maximum number of users cannot be less than 2.");

        assertThrows(IllegalArgumentException.class, () -> {
            gameService.createLobby(user.getUserToken(), gamePostDTO2);
        }, "The maximum number of users cannot be less than 2.");
    }

    @Test
    public void createLobby_WithValidInputs_ShouldReturnGame() throws Exception {
        User user = new User();
        user.setUserToken("userToken");
        user.setUsername("user");

        GamePostDTO gamePostDTO = new GamePostDTO();
        gamePostDTO.setTimeLimit(10.0f);
        gamePostDTO.setAmtOfRounds(10);
        gamePostDTO.setMaxAmtUsers(4);

        when(userRepository.findByUserToken("userToken")).thenReturn(user);

        Game actualGame = gameService.createLobby(user.getUserToken(), gamePostDTO);

        assertNotNull(actualGame);
        assertEquals(user.getUsername(), actualGame.getLobbyOwner());
        assertEquals(10.0f, actualGame.getTimeLimit());
        assertEquals(10, actualGame.getAmtOfRounds());
        assertEquals(4, actualGame.getMaxAmtUsers());
    }

    @Test
    public void createLobby_WithUserTokenNullOrEmpty_ShouldThrowException() throws Exception {
        User user = new User();
        user.setUserToken("userToken");
        user.setUsername("user");

        GamePostDTO gamePostDTO = new GamePostDTO();
        gamePostDTO.setTimeLimit(10.0f);
        gamePostDTO.setAmtOfRounds(10);
        gamePostDTO.setMaxAmtUsers(4);

        when(userRepository.findByUserToken("userToken")).thenReturn(user);

        Exception exception1 = assertThrows(ResponseStatusException.class, () -> {
            gameService.createLobby("", gamePostDTO);
        });

        Exception exception2 = assertThrows(ResponseStatusException.class, () -> {
            gameService.createLobby(null, gamePostDTO);
        });

        String expectedMessage = "User does not exist";
        String actualMessage1 = exception1.getMessage();
        String actualMessage2 = exception2.getMessage();

        assertTrue(actualMessage1.contains(expectedMessage));
        assertTrue(actualMessage2.contains(expectedMessage));
    }

    @Test
    public void createLobby_WithFoundUserByTokenNull_ShouldThrowException() throws Exception {
        User user = new User();
        user.setUserToken("userToken");
        user.setUsername("user");

        GamePostDTO gamePostDTO = new GamePostDTO();
        gamePostDTO.setTimeLimit(10.0f);
        gamePostDTO.setAmtOfRounds(10);
        gamePostDTO.setMaxAmtUsers(4);

        when(userRepository.findByUserToken("userToken")).thenReturn(null);

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            gameService.createLobby("userToken", gamePostDTO);
        });

        String expectedMessage = "User does not exist";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }


    @Test
    public void resetDallEsImageURL_WhenCalled_ShouldReturnSuccessMessage() {
        DallE dallE = mock(DallE.class);
        UserRepository userRepository = mock(UserRepository.class);
        GameRepository gameRepository = mock(GameRepository.class);
        UserService userService = mock(UserService.class);
        ChatGPT chatGPT = mock(ChatGPT.class);

        GameService gameService = new GameService(userRepository, gameRepository, userService, dallE, chatGPT);

        gameService.resetDallEsImageURL();
        verify(dallE).setImageUrl("");
        verifyNoMoreInteractions(dallE);
    }

    @Test
    public void resetDallEsImageURL_WhenExceptionThrown_ShouldThrowException() {
        DallE dallE = mock(DallE.class);
        UserRepository userRepository = mock(UserRepository.class);
        GameRepository gameRepository = mock(GameRepository.class);
        UserService userService = mock(UserService.class);
        ChatGPT chatGPT = mock(ChatGPT.class);

        GameService gameService = new GameService(userRepository, gameRepository, userService, dallE, chatGPT);

        doThrow(new RuntimeException()).when(dallE).setImageUrl("");

        Exception exception = assertThrows(RuntimeException.class, () -> {
            gameService.resetDallEsImageURL();
        });

        assertNotNull(exception);
    }

    @Test
    public void getImageGeneratedByDallE_WhenCalled_ShouldReturnImage() {
        DallE dallE = mock(DallE.class);
        UserRepository userRepository = mock(UserRepository.class);
        GameRepository gameRepository = mock(GameRepository.class);
        UserService userService = mock(UserService.class);
        ChatGPT chatGPT = mock(ChatGPT.class);

        GameService gameService = new GameService(userRepository, gameRepository, userService, dallE, chatGPT);

        gameService.getImageGeneratedByDallE();
        verify(dallE).getImageUrl();
        verifyNoMoreInteractions(dallE);
    }

    @Test
    public void leaveLobby_WithValidLobbyIdAndUserToken_ShouldReturnSuccessMessage() throws Exception {
        User user = new User();
        user.setUserToken("userToken");
        user.setUsername("user");

        Game game = new Game();
        Long gameId = 1L;
        game.setId(gameId);
        game.setLobbyOwner("user");
        game.addPlayer(user);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(userRepository.findByUserToken("userToken")).thenReturn(user);

        gameService.leaveLobby(gameId, "userToken");

        verify(gameRepository).save(game);
        verify(userRepository).save(user);
        assertEquals(0, game.getUsers().size());
    }

    @Test
    public void leaveLobby_WithInvalidLobbyId_ShouldThrowException() throws Exception {
        Long invalidGameId = 22L;
        User user = new User();
        user.setUserToken("userToken");
        user.setUsername("user");

        when(userRepository.findByUserToken("userToken")).thenReturn(user);
        when(gameRepository.findById(invalidGameId)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Lobby with ID " + invalidGameId + " does not exist"));

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            gameService.leaveLobby(invalidGameId, "userToken");
        });

        String expectedMessage = "Lobby with ID " + invalidGameId + " does not exist";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void leaveLobby_WithInvalidUserToken_ShouldThrowException() throws Exception {
        User user = new User();
        user.setUserToken("userToken");
        user.setUsername("user");

        Game game = new Game();
        Long gameId = 1L;
        game.setId(gameId);
        game.setLobbyOwner("user");
        game.addPlayer(user);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(userRepository.findByUserToken("userToken")).thenReturn(null);

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            gameService.leaveLobby(gameId, "userToken");
        });

        String expectedMessage = "User does not exist";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void leaveLobby_WithLobbyIdNull_ShouldThrowException() throws Exception {
        Long gameId = 0L;
        Long gameIdNull = null;

        User user = new User();
        user.setUserToken("userToken");
        user.setUsername("user");

        when(userRepository.findByUserToken("userToken")).thenReturn(user);
        when(gameRepository.findById(gameId)).thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lobby ID is null"));

        Exception exception1 = assertThrows(ResponseStatusException.class, () -> {
            gameService.leaveLobby(gameId, "userToken");
        });

        Exception exception2 = assertThrows(ResponseStatusException.class, () -> {
            gameService.leaveLobby(gameIdNull, "userToken");
        });

        String expectedMessage = "Lobby ID is null";
        String actualMessage1 = exception1.getMessage();
        String actualMessage2 = exception2.getMessage();
        System.out.println("actualMessage1: " + actualMessage1);

        assertTrue(actualMessage1.contains(expectedMessage));
        assertTrue(actualMessage2.contains(expectedMessage));
    }

    @Test
    public void leaveLobby_WithGameNotFound_ShouldThrowException() throws Exception {
        Long gameId = 1L;
        User user = new User();
        user.setUserToken("userToken");
        user.setUsername("user");

        when(userRepository.findByUserToken("userToken")).thenReturn(user);
        when(gameRepository.findById(gameId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            gameService.leaveLobby(gameId, "userToken");
        });

        String expectedMessage = "Lobby with ID " + gameId + " does not exist";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void leaveLobby_WithUserNotInTheLobby_ShouldThrowException() throws Exception {
        User user = new User();
        user.setUserToken("userToken");
        user.setUsername("user");

        User user2 = new User();
        user2.setUserToken("userToken2");
        user2.setUsername("user2");

        Game game = new Game();
        Long gameId = 1L;
        game.setId(gameId);
        game.setLobbyOwner("user");
        game.addPlayer(user);

        when(userRepository.findByUserToken("userToken")).thenReturn(user);
        when(userRepository.findByUserToken("userToken2")).thenReturn(user2);
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            gameService.leaveLobby(gameId, "userToken2");
        });

        String expectedMessage = "User is not in the specified lobby";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void gameIsFinishedLeaveLobby_WithUserNotExist_ShouldThrowException() throws Exception {
        User user = new User();
        user.setUserToken("userToken");
        user.setUsername("user");

        Game game = new Game();
        Long gameId = 1L;
        game.setId(gameId);
        game.setLobbyOwner("user");
        game.addPlayer(user);

        when(userRepository.findByUserToken("userToken")).thenReturn(user);
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            gameService.gameIsFinishedLeaveLobby(gameId, "userToken2");
        });

        String expectedMessage = "User does not exist";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    // @Test
    // public void gameIsFinishedLeaveLobby_WithLobbyNotPresent_ShouldThrowException() throws Exception {
    //     Long gameId = 1L;
    //     User user = new User();
    //     user.setUserToken("userToken");
    //     user.setUsername("user");

    //     when(userRepository.findByUserToken("userToken")).thenReturn(user);
    //     when(gameRepository.findById(gameId)).thenReturn(Optional.empty());

    //     Exception exception = assertThrows(ResponseStatusException.class, () -> {
    //         gameService.gameIsFinishedLeaveLobby(gameId, "userToken");
    //     });

    //     String expectedMessage = "Lobby not found";
    //     String actualMessage = exception.getMessage();
    //     assertTrue(actualMessage.contains(expectedMessage));
    // }

    @Test
    public void leaveLobby_WithUserTokenNullOrEmpty_ShouldReturnBadRequest() throws Exception {
        User user = new User();
        user.setUserToken("userToken");
        user.setUsername("user");

        Game game = new Game();
        Long gameId = 1L;
        game.setId(gameId);
        game.setLobbyOwner("user");
        game.addPlayer(user);

        when(userRepository.findByUserToken("userToken")).thenReturn(user);
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        Exception exception1 = assertThrows(ResponseStatusException.class, () -> {
            gameService.leaveLobby(gameId, "");
        });

        Exception exception2 = assertThrows(ResponseStatusException.class, () -> {
            gameService.leaveLobby(gameId, null);
        });

        String expectedMessage = "User token is null or empty";
        String actualMessage1 = exception1.getMessage();
        String actualMessage2 = exception2.getMessage();

        assertTrue(actualMessage1.contains(expectedMessage));
        assertTrue(actualMessage2.contains(expectedMessage));
    }
/*
    //TEST LEAVE LOBBY FUNCTIONALITY
    @Test
    public void playerLeaveCurrentLobby() throws Exception {
        Game game = createGame(3);
        User userToRemove = game.getUsers().get(1);

        when(userRepository.findByUserToken(userToRemove.getUserToken())).thenReturn(userToRemove);
        when(gameRepository.findById(game.getId())).thenReturn(Optional.of(game));

        gameService.playerLeaveCurrentLobby(userToRemove.getUserToken());

        assert !game.getUsers().contains(userToRemove);
        assert userToRemove.getGame() == null;
        assert userRepository.findByUserToken(userToRemove.getUserToken()) == userToRemove;
    }

    //ToDo: test  getImageGeneratedByDallE extensively!!
    //ToDo: create integration tests for repositories?

 */

    @Test
    void updateAmtOfGuessesInvalideId(){
        Long id = 0L;

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            gameService.updateAmtOfGuesses(id);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void updateAmtOfGuessesLobbyDoesNotExists() throws Exception{
        Long id = 1L;

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            gameService.updateAmtOfGuesses(id);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());

    }

    @Test
    void updateAmtOfGuessesAllPlayersHaveGuessed(){
        Game game = Mockito.mock(Game.class);
        Long id = 1L;

        when(gameRepository.findById(id)).thenReturn(Optional.of(game));
        when(game.allPlayersGuessed()).thenReturn(true);

        boolean allPlayersGuessed = gameService.updateAmtOfGuesses(id);
        assertTrue(allPlayersGuessed);
    }

    @Test
    void updateAmtOfGuessesNotAllPlayersHaveGuessed(){
        Game game = Mockito.mock(Game.class);
        Long id = 1L;

        when(gameRepository.findById(id)).thenReturn(Optional.of(game));
        when(game.allPlayersGuessed()).thenReturn(false);

        boolean allPlayersGuessed = gameService.updateAmtOfGuesses(id);
        assertFalse(allPlayersGuessed);
    }
}