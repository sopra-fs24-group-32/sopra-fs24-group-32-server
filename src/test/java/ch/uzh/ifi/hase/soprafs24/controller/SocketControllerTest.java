package ch.uzh.ifi.hase.soprafs24.controller;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import ch.uzh.ifi.hase.soprafs24.ws.WSController;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.game.Game;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.SimpleUserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.service.GameService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import java.util.Optional;


@SpringBootTest
public class SocketControllerTest {

    @Mock
    private GameService gameService;

    @Mock
    private UserService userService;

    @Mock
    private GameRepository gameRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private WSController socketController;

    

    @Test
    public void joinGame_WithValidUserToken_ShouldReturnUserGetDTO() throws Exception {
        String userToken = "valid-token";
        String gameId = "1";
        User mockUser = new User();
        mockUser.setUsername("validUser");
        mockUser.setUserToken(userToken);
        
        given(userService.findByToken(userToken)).willReturn(mockUser);
        
        UserGetDTO returnedDTO = socketController.joinGame(gameId, userToken);
        
        verify(userService).findByToken(userToken);
        assertNotNull(returnedDTO);
        assertEquals(returnedDTO.getUsername(), mockUser.getUsername());
    }

    @Test
    public void continueGame_WithNullId_ShouldThrowResponseStatusException() {
        String gameID = "1";
        assertThrows(ResponseStatusException.class, () -> socketController.continueGame(gameID, null));
    }

    @Test
    public void continueGame_WithValidId_ShouldReturnSimpleUserGetDTO() {
        String gameIdDestinationVariable = "1";
        Long gameId = 1L;
        when(gameService.getNextPictureGenerator(gameId)).thenReturn("user");

        SimpleUserGetDTO dto = socketController.continueGame(gameIdDestinationVariable, gameId);

        assertNotNull(dto);
        verify(gameService).resetDallEsImageURL();
        verify(gameService).getNextPictureGenerator(gameId);
    }

    @Test
    public void startGame_WithValidId_GameNotFound_ShouldThrowResponseStatusException() {
        String gameIdDestinationVariable = "1";
        Long gameId = 1L;
        when(gameRepository.findById(gameId)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> socketController.startGame(gameIdDestinationVariable, gameId));
    }

    @Test
    public void startGame_WithValidId_ShouldReturnSimpleUserGetDTO() {
        String gameIdDestinationVariable = "1";
        Long gameId = 1L;
        Game mockGame = new Game();
        mockGame.setId(gameId);
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(mockGame));
        when(gameService.getNextPictureGenerator(gameId)).thenReturn("user");

        SimpleUserGetDTO dto = socketController.startGame(gameIdDestinationVariable, gameId);

        assertNotNull(dto);
        verify(gameService).startGameLobby(gameId);
    }

    @Test
    public void startGame_WithLobbyIdNull_ShouldThrowResponseStatusException() {
        String gameIdDestinationVariable = "1";
        Long gameId = null;

        assertThrows(ResponseStatusException.class, () -> socketController.startGame(gameIdDestinationVariable, gameId));
    }

    @Test
    public void startGame_WithNextPictureGeneratorNull_ShouldThrowResponseStatusException() {
        String gameIdDestinationVariable = "1";
        Long gameId = 1L;
        Game mockGame = new Game();
        mockGame.setId(gameId);
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(mockGame));
        when(gameService.getNextPictureGenerator(gameId)).thenReturn(null);

        gameService.startGameLobby(gameId);

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            socketController.startGame(gameIdDestinationVariable, gameId);
        });

        assertTrue(exception instanceof ResponseStatusException);
        assertEquals(HttpStatus.BAD_REQUEST, ((ResponseStatusException)exception).getStatus());

    }

    @Test
    public void joinGame_WithInvalidUserToken_ShouldThrowResponseStatusException() {
        String gameIdDestinationVariable = "1";
        String userToken = "invalid-token";

        given(userService.findByToken(userToken)).willThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found"));

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            socketController.joinGame(gameIdDestinationVariable, userToken);
        });

        assertTrue(exception instanceof ResponseStatusException);
        assertEquals(HttpStatus.BAD_REQUEST, ((ResponseStatusException)exception).getStatus());

    }


}
