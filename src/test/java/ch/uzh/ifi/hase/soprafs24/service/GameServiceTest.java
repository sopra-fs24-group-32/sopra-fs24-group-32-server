package ch.uzh.ifi.hase.soprafs24.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.game.lobby.Lobby;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GamePostDTO;

public class GameServiceTest {

    private GameService gameService;
    private UserService userService;
    private UserRepository userRepository;

    @BeforeEach
    public void setup() {
        userRepository = mock(UserRepository.class);
        userService = mock(UserService.class);
        gameService = new GameService(userRepository, userService);
    }


    @Test
    public void testFindByLobbyIdWhenExists() {
        Lobby expectedLobby = new Lobby(1L, "TestOwner");
        gameService.getAllLobbies().put(1L, expectedLobby);
        assertEquals(expectedLobby, gameService.findByLobbyId("roomId1"));
    }

    @Test
    public void testFindByLobbyIdWhenNotExists() {
        assertNull(gameService.findByLobbyId("nonExistingId"));
    }


    @Test
    public void testCreateLobby() throws Exception {

        User lobbyOwner = new User();
        lobbyOwner.setUserToken("userToken");
        lobbyOwner.setUsername("lobbyOwner");

        when(userService.findByToken("userToken")).thenReturn(lobbyOwner);

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
            
        when(userService.findByToken("ownerToken")).thenReturn(lobbyOwen);

        Lobby lobby = gameService.createLobby("ownerToken");
        lobby.setTimeLimit(10.0f);
        lobby.setAmtOfRounds(5);
        lobby.setMaxAmtPlayers(10);

        GamePostDTO gamePostDTO = new GamePostDTO();
        gamePostDTO.setTimeLimit(15.0f);
        gamePostDTO.setAmtOfRounds(7);
        gamePostDTO.setMaxAmtPlayers(12);

        Lobby updatedLobby = gameService.updateGame(lobby.getLobbyId(), gamePostDTO);

        // Assert
        assertNotNull(updatedLobby);
        assertEquals(15.0f, updatedLobby.getTimeLimit());
        assertEquals(7, updatedLobby.getAmtOfRounds());
        assertEquals(12, updatedLobby.getMaxAmtPlayers());
    }


    @Test
    public void testJoinGame() throws Exception {

        User lobbyOwen = new User();
        lobbyOwen.setUserToken("ownerToken");
        lobbyOwen.setUsername("owner");

        User user = new User();
        user.setUserToken("userToken");
        user.setUsername("username");

        when(userService.findByToken("ownerToken")).thenReturn(lobbyOwen);
        when(userService.findByToken("userToken")).thenReturn(user);

        Lobby lobby = gameService.createLobby("ownerToken");
     
        gameService.joinLobby(lobby.getLobbyId(), "userToken");

        // Assert
        assertNotNull(lobby);
        assertEquals(2, lobby.getAllPlayers().size());
        assertTrue(lobby.getAllPlayers().stream().anyMatch(player -> player.getUsername().equals("username")));
    }
    
}
