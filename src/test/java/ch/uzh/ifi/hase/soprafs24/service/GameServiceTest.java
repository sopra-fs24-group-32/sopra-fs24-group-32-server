package ch.uzh.ifi.hase.soprafs24.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.game.Game;
import ch.uzh.ifi.hase.soprafs24.game.dallE.DallE;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GamePostDTO;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.Lob;

public class GameServiceTest {

    private GameService gameService;
    private UserService userService;
    private UserRepository userRepository;
    private GameRepository gameRepository;
    private DallE mockDallE;

    @BeforeEach
    public void setup() {
        userRepository = mock(UserRepository.class);
        userService = mock(UserService.class);
        gameRepository = mock(GameRepository.class);
        mockDallE = mock(DallE.class);
        gameService = new GameService(userRepository, gameRepository, userService);
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

        String userToken = "{\"userToken\":\"userToken\"}";
        String userToken2 = "{\"userToken\":\"userToken2\"}";


        when(userRepository.findByUserToken("userToken")).thenReturn(lobbyOwner);
        when(userRepository.findByUserToken("userToken2")).thenReturn(newUser);

        Game lobby = gameService.createLobby(userToken);
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

        String userToken = "{\"userToken\":\"userToken\"}";

        when(userRepository.findByUserToken("userToken")).thenReturn(lobbyOwner);

        Game lobby = gameService.createLobby(userToken);

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

        String ownerToken = "{\"userToken\":\"ownerToken\"}";
        String userToken = "{\"userToken\":\"userToken\"}";

        when(userRepository.findByUserToken("ownerToken")).thenReturn(lobbyOwen);
        when(userRepository.findByUserToken("userToken")).thenReturn(user);

        Game lobby = gameService.createLobby(ownerToken);

        when(gameRepository.findByLobbyInvitationCode(lobby.getLobbyInvitationCode())).thenReturn(lobby);

        gameService.joinLobby(lobby.getLobbyInvitationCode(), userToken);

        // Assert
        assertNotNull(lobby);
        assertEquals(2, lobby.getUsers().size());
        assertTrue(lobby.getUsers().stream().anyMatch(u -> u.getUsername().equals("username")));
    }

    // @Test
    // public void generatePictureDallEShouldReturnValidUrl() throws Exception {

    //     String validPrompt = "{\"description\":\"a sunset over a mountain range\"}";
    //     String expectedUrl = "http://example.com/image.jpg";
    //     when(mockDallE.generatePicture("a sunset over a mountain range")).thenReturn(expectedUrl);

    //     String result = gameService.generatePictureDallE(validPrompt);

    //     assertEquals(expectedUrl, result);
    //     verify(mockDallE).generatePicture("a sunset over a mountain range");
    // }


    @Test
    public void playerLeaveGame_ShouldRemoveUserSuccessfully() throws Exception {
        
        Long gameId = 1L;
        User user = new User();
        user.setUsername("username");
        String lobbyOwner = "owner";
        Game game = new Game(gameId, lobbyOwner);
        String userToken = "valid-token";
        game.addPlayer(user);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(userRepository.findByUserToken(userToken)).thenReturn(user);

        gameService.playerLeaveGame(gameId, userToken);

        verify(gameRepository).save(game);
        assertEquals(0, game.getUsers().size());
    }

    @Test
    public void playerLeaveGame_ShouldThrowExceptionWhenUserTokenIsEmptyOrNull() throws Exception {
        Long gameId = 1L;
        String emptyUserToken = "";
        String nullUserToken = null;

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            gameService.playerLeaveGame(gameId, emptyUserToken);
        });

        Exception exception2 = assertThrows(ResponseStatusException.class, () -> {
            gameService.playerLeaveGame(gameId, nullUserToken);
        });

        String expectedMessage = "UserToken is null or empty";
        String actualMessageEmptyToken = exception.getMessage();
        String actualMessageNullToken = exception2.getMessage();

        assertTrue(actualMessageEmptyToken.contains(expectedMessage));
        assertTrue(actualMessageNullToken.contains(expectedMessage));
    }

    @Test
    public void playerLeaveGame_ShouldThrowExceptionWhenGameDoesNotExist() throws Exception {
        Long gameId = 1L;
        String userToken = "valid-token";

        when(gameRepository.findById(gameId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            gameService.playerLeaveGame(gameId, userToken);
        });

        String expectedMessage = "Lobby not found";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

    }

    @Test
    public void playerLeaveGame_ShouldThrowExceptionWhenUserIsNotInGame() throws Exception {
        Long gameId = 1L;
        String userToken = "valid-token";
        User user = new User();
        user.setUsername("username");

        Game game = new Game(gameId, "owner");

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(userRepository.findByUserToken(userToken)).thenReturn(user);

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            gameService.playerLeaveGame(gameId, userToken);
        });

        String expectedMessage = "User not found in the game";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

    }

    @Test
    public void playerLeaveGame_ShouldThrowExceptionWhenGameIdIsNullOrZero() throws Exception {
        Long gameIdNull = null;
        Long gameIdZero = 0L;

        String userToken = "valid-token";

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            gameService.playerLeaveGame(gameIdNull, userToken);
        });

        Exception exception2 = assertThrows(ResponseStatusException.class, () -> {
            gameService.playerLeaveGame(gameIdZero, userToken);
        });

        String expectedMessage = "Game ID is null or zero";
        String actualMessageNull = exception.getMessage();
        String actualMessageZero = exception2.getMessage();

        assertTrue(actualMessageNull.contains(expectedMessage));
        assertTrue(actualMessageZero.contains(expectedMessage));
    
    }

    
}
