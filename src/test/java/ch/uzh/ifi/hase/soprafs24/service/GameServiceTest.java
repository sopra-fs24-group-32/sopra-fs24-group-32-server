package ch.uzh.ifi.hase.soprafs24.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.game.lobby.Lobby;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GamePostDTO;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.Lob;

public class GameServiceTest {

    private GameService gameService;
    private UserService userService;
    private UserRepository userRepository;
    private GameRepository gameRepository;

    @BeforeEach
    public void setup() {
        userRepository = mock(UserRepository.class);
        userService = mock(UserService.class);
        gameRepository = mock(GameRepository.class);
        gameService = new GameService(userRepository, gameRepository, userService);
    }

    @Test
    public void joinLobbyAndLobbyIdIsNullOrEmpty(){
        Exception exceptionIdIsNull = assertThrows(ResponseStatusException.class, () -> {
            gameService.joinLobby(null, "userToken");
        });

        String expectedMessage = "Lobby with sent invitationCodes does not exist";
        String actualMessage = exceptionIdIsNull.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        Exception exceptionIdIsEmpty = assertThrows(ResponseStatusException.class, () -> {
            gameService.joinLobby("", "userToken");
        });

        String expectedMessageWhenIdIsEmpty = "Lobby with sent invitationCodes does not exist";
        String actualMessageWhenIdIsEmpty = exceptionIdIsNull.getMessage();

        assertTrue(actualMessageWhenIdIsEmpty.contains(expectedMessageWhenIdIsEmpty));

    }

    @Test void joinLobbyAndUserTokenIsNullOrEmpty(){
        Exception exceptionUserTokenIsNull = assertThrows(ResponseStatusException.class, () -> {
            gameService.joinLobby("1", null);
        });

        String expectedMessage = "User does not exists";
        String actualMessage = exceptionUserTokenIsNull.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        Exception exceptionUserTokenIsEmpty = assertThrows(ResponseStatusException.class, () -> {
            gameService.joinLobby("1", "");
        });

        String expectedMessageWhenUserTokenIsEmpty = "User does not exists";
        String actualMessageWhenUserTokenIsEmpty = exceptionUserTokenIsEmpty.getMessage();

        assertTrue(actualMessageWhenUserTokenIsEmpty.contains(expectedMessageWhenUserTokenIsEmpty));
    }

    @Test
    public void joinGameAndLobbyDoesNotExist() throws Exception {
        User lobbyOwner = new User();
        lobbyOwner.setUserToken("userToken");
        lobbyOwner.setUsername("lobbyOwner");

        when(userRepository.findByUserToken("userToken")).thenReturn(lobbyOwner);
        // Lobby lobby = gameService.createLobby("userToken");
        when(gameRepository.findByLobbyInvitationCode("-1")).thenReturn(null);

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
           gameService.joinLobby("-1", "userToken");
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


        when(userRepository.findByUserToken("userToken")).thenReturn(lobbyOwner);
        when(userRepository.findByUserToken("userToken2")).thenReturn(newUser);

        Lobby lobby = gameService.createLobby("userToken");
        Player player1 = new Player();
        lobby.addPlayer(player1);;
        lobby.setTimeLimit(15f);
        lobby.startGame();
        boolean I = lobby.gameHasStarted();
        String lobbyInvitationCode = lobby.getLobbyInvitationCode();

        when(gameRepository.findByLobbyInvitationCode(lobbyInvitationCode)).thenReturn(lobby);

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            gameService.joinLobby(lobbyInvitationCode, "userToken2");
        });

        String expectedMessage = "Game has already started";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testFindByLobbyIdWhenExists() {
        Lobby expectedLobby = new Lobby(1L, "TestOwner");
        gameService.getAllLobbies().put(1L, expectedLobby);
        Lobby foundLobby = gameService.findByLobbyId("roomId1");
        assertEquals(expectedLobby, foundLobby);
    }

    @Test
    public void testFindByLobbyIdWhenNotExists() {
        assertNull(gameRepository.findByLobbyId("nonExistingId"));
    }


    @Test
    public void testCreateLobby() throws Exception {

        User lobbyOwner = new User();
        lobbyOwner.setUserToken("userToken");
        lobbyOwner.setUsername("lobbyOwner");

        when(userRepository.findByUserToken("userToken")).thenReturn(lobbyOwner);

        Lobby lobby = gameService.createLobby("userToken");

        // Assert
        assertNotNull(lobby);
        assertEquals(1, lobby.getAllPlayers().size());
        assertTrue(lobby.getAllPlayers().stream().anyMatch(player -> player.getUsername().equals("lobbyOwner")));
    }


    @Test
    public void testUpdateGame() throws Exception {
        
        User lobbyOwen = new User();
        lobbyOwen.setUserToken("ownerToken");
        lobbyOwen.setUsername("owner");
            
        when(userRepository.findByUserToken("ownerToken")).thenReturn(lobbyOwen);

        Lobby lobby = gameService.createLobby("ownerToken");
        lobby.setTimeLimit(10.0f);
        lobby.setAmtOfRounds(5);
        lobby.setMaxAmtPlayers(10);

        GamePostDTO gamePostDTO = new GamePostDTO();
        gamePostDTO.setTimeLimit(15.0f);
        gamePostDTO.setAmtOfRounds(7);
        gamePostDTO.setMaxAmtPlayers(12);

        when(gameRepository.findByLobbyId(lobby.getLobbyId())).thenReturn(lobby);

        Lobby updatedLobby = gameService.updateGameSettings(lobby.getLobbyId(), gamePostDTO);

        // Assert
        assertNotNull(updatedLobby);
        assertEquals(15.0f, updatedLobby.getTimeLimit());
        assertEquals(7, updatedLobby.getAmtOfRounds());
        assertEquals(12, updatedLobby.getMaxAmtPlayers());
    }

    @Test
    public void testUpdateGameIllegalArgument() throws Exception {

        User lobbyOwen = new User();
        lobbyOwen.setUserToken("ownerToken");
        lobbyOwen.setUsername("owner");

        when(userRepository.findByUserToken("ownerToken")).thenReturn(lobbyOwen);

        Lobby lobby = gameService.createLobby("ownerToken");
        lobby.setTimeLimit(10.0f);
        lobby.setAmtOfRounds(5);
        lobby.setMaxAmtPlayers(10);

        GamePostDTO gamePostDTO = new GamePostDTO();
        gamePostDTO.setTimeLimit(15.0f);
        gamePostDTO.setAmtOfRounds(0);      //illegal number of rounds
        gamePostDTO.setMaxAmtPlayers(12);

        when(gameRepository.findByLobbyId(lobby.getLobbyId())).thenReturn(lobby);

        assertThrows(IllegalArgumentException.class, () -> {
            gameService.updateGameSettings(lobby.getLobbyId(), gamePostDTO);
        }, "There must be at least one round.");
    }

    @Test
    public void testJoinGame() throws Exception {

        User lobbyOwen = new User();
        lobbyOwen.setUserToken("ownerToken");
        lobbyOwen.setUsername("owner");

        User user = new User();
        user.setUserToken("userToken");
        user.setUsername("username");

        when(userRepository.findByUserToken("ownerToken")).thenReturn(lobbyOwen);
        when(userRepository.findByUserToken("userToken")).thenReturn(user);

        Lobby lobby = gameService.createLobby("ownerToken");

        when(gameRepository.findByLobbyInvitationCode(lobby.getLobbyInvitationCode())).thenReturn(lobby);

        gameService.joinLobby(lobby.getLobbyInvitationCode(), "userToken");

        // Assert
        assertNotNull(lobby);
        assertEquals(2, lobby.getAllPlayers().size());
        assertTrue(lobby.getAllPlayers().stream().anyMatch(player -> player.getUsername().equals("username")));
    }
    
}
