package ch.uzh.ifi.hase.soprafs24.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.game.Game;
import ch.uzh.ifi.hase.soprafs24.game.chatGPT.ChatGPT;
import ch.uzh.ifi.hase.soprafs24.game.dallE.DallE;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GamePostDTO;

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


    // @Test
    // public void testUpdateGame() throws Exception {
        
    //     User lobbyOwen = new User();
    //     lobbyOwen.setUserToken("ownerToken");
    //     lobbyOwen.setUsername("owner");
            
    //     when(userRepository.findByUserToken("ownerToken")).thenReturn(lobbyOwen);

    //     Game lobby = gameService.createLobby("ownerToken");

    //     GamePostDTO gamePostDTO = new GamePostDTO();
    //     gamePostDTO.setTimeLimit(15.0f);
    //     gamePostDTO.setAmtOfRounds(7);
    //     gamePostDTO.setMaxAmtUsers(12);

    //     when(gameRepository.findById(lobby.getId())).thenReturn(lobby);

    //     Game updatedLobby = gameService.updateGameSettings(lobby.getId(), gamePostDTO);

    //     // Assert
    //     assertNotNull(updatedLobby);
    //     assertEquals(15.0f, updatedLobby.getTimeLimit());
    //     assertEquals(7, updatedLobby.getAmtOfRounds());
    //     assertEquals(12, updatedLobby.getMaxAmtUsers());
    // }

    // @Test
    // public void testUpdateGameIllegalArgument() throws Exception {

    //     User lobbyOwen = new User();
    //     lobbyOwen.setUserToken("ownerToken");
    //     lobbyOwen.setUsername("owner");

    //     when(userRepository.findByUserToken("ownerToken")).thenReturn(lobbyOwen);

    //     Game lobby = gameService.createLobby("ownerToken");

    //     GamePostDTO gamePostDTO = new GamePostDTO();
    //     gamePostDTO.setTimeLimit(15.0f);
    //     gamePostDTO.setAmtOfRounds(0);      //illegal number of rounds
    //     gamePostDTO.setMaxAmtUsers(12);

    //     when(gameRepository.findById(lobby.getId())).thenReturn(lobby);

    //     assertThrows(IllegalArgumentException.class, () -> {
    //         gameService.updateGameSettings(lobby.getId(), gamePostDTO);
    //     }, "There must be at least one round.");
    // }

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
    public void playerLeaveGame_ShouldThrowExceptionWhenUserIsLobbyOwner() throws Exception {
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

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            gameService.playerLeaveGame(gameId, "userToken");
        });

        String expectedMessage = "Cannot remove the lobby owner from the game";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }
    
    // @Test
    // public void generatePictureWithDallE_WithValidPrompt_ShouldReturnImageUrl() throws Exception {
    //     String inputPhrase =  "{\"description\":\"A picture of a cat\"}";
    //     String imageUrl = gameService.generatePictureDallE(inputPhrase);
    //     assertNotNull(imageUrl);
    // }

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

    @Test
    public void evaluatePlayerGuessWithChatGPT_WithValidInputs_ShouldReturnSimilarityScore() throws Exception {
        String originalText = "cat on the floor";
        String playerGuessed = "cat on the ground";
        float chatGPTResult = 0.85f;
        int expectedPoints = 6; 

        DallE dallE = mock(DallE.class);
        ChatGPT chatGPT = mock(ChatGPT.class);

        when(dallE.getInputPhrase()).thenReturn(originalText);
        when(chatGPT.rateInputs(originalText, playerGuessed)).thenReturn(chatGPTResult);
        when(chatGPT.convertSimilarityScoreToPoints(chatGPTResult)).thenReturn(expectedPoints);
        GameService gameService = new GameService(userRepository, gameRepository, userService, dallE, chatGPT);
        int pointsAwarded = gameService.evaluatePlayerGuessWithChatGPT(playerGuessed);
        assertEquals(expectedPoints, pointsAwarded);
    }

    @Test
    public void evaluatePlayerGuessWithChatGPT_WithIncorrectGuess_ShouldReturnZero() throws Exception {
        String originalText = "cat on the floor";
        String playerGuessed = "dog on the floor";
        float chatGPTResult = 0.45f;
        int expectedPoints = 0;

        DallE dallE = mock(DallE.class);
        ChatGPT chatGPT = mock(ChatGPT.class);

        when(dallE.getInputPhrase()).thenReturn(originalText);
        when(chatGPT.rateInputs(originalText, playerGuessed)).thenReturn(chatGPTResult);
        when(chatGPT.convertSimilarityScoreToPoints(chatGPTResult)).thenReturn(expectedPoints);

        GameService gameService = new GameService(userRepository, gameRepository, userService, dallE, chatGPT);
        int pointsAwarded = gameService.evaluatePlayerGuessWithChatGPT(playerGuessed);
        assertEquals(expectedPoints, pointsAwarded);
    }

    @Test
    public void evaluatePlayerGuessWithChatGPT_WithSimilarGuess_ShouldReturnFour() throws Exception {
        String originalText = "cat on the floor";
        String playerGuessed = "cat on the ground";
        float chatGPTResult = 0.70f;
        int expectedPoints = 4;

        DallE dallE = mock(DallE.class);
        ChatGPT chatGPT = mock(ChatGPT.class);

        when(dallE.getInputPhrase()).thenReturn(originalText);
        when(chatGPT.rateInputs(originalText, playerGuessed)).thenReturn(chatGPTResult);
        when(chatGPT.convertSimilarityScoreToPoints(chatGPTResult)).thenReturn(expectedPoints);

        GameService gameService = new GameService(userRepository, gameRepository, userService, dallE, chatGPT);
        int pointsAwarded = gameService.evaluatePlayerGuessWithChatGPT(playerGuessed);
        assertEquals(expectedPoints, pointsAwarded);
    }

    @Test
    public void evaluatePlayerGuessWithChatGPT_WithEmptyOrNullPlayerGuess_ShouldReturnZero() throws Exception {
        String originalText = "cat on the floor";
        String emptyPlayerGuessed = "";
        String nullPlayerGuessed = null;

        int expectedNullPoints = 0;
        int expectedEmptyPoints = 0;

        DallE dallE = mock(DallE.class);

        when(dallE.getInputPhrase()).thenReturn(originalText);

        GameService gameService = new GameService(userRepository, gameRepository, userService, dallE, chatGPT);

        int pointsAwardedEmpty = gameService.evaluatePlayerGuessWithChatGPT(emptyPlayerGuessed);
        int pointsAwardedNull = gameService.evaluatePlayerGuessWithChatGPT(nullPlayerGuessed);

        assertEquals(expectedEmptyPoints, pointsAwardedEmpty);
        assertEquals(expectedNullPoints, pointsAwardedNull);
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

        // game.s
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        String selectedPlayer = gameService.getNextPictureGenerator(gameId);

        assertNotNull(selectedPlayer);
        assertEquals(1, remaininPictureGenerators.size());
        verify(gameRepository).findById(gameId);
        verify(gameRepository).save(game);
        verify(gameRepository).flush();
    }

    @Test
    public void getNextPictureGenerator_WithGameIdAndAllPlayersSelected_ShouldThrowException() {
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

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        String selectedPlayer1 = gameService.getNextPictureGenerator(gameId);
        String selectedPlayer2 = gameService.getNextPictureGenerator(gameId);

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            gameService.getNextPictureGenerator(gameId);
        });

        String expectedMessage = "All the users have already created a picture once";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
        assertNotNull(selectedPlayer1);
        assertNotNull(selectedPlayer2);
    }

}
