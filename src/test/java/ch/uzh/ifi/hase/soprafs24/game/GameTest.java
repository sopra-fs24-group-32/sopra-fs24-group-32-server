package ch.uzh.ifi.hase.soprafs24.game;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs24.entity.User;

@ExtendWith(MockitoExtension.class)
public class GameTest {

    @InjectMocks
    private Game game;

    @Mock
    private User user;

    @BeforeEach
    void setUp() {
        game.setTimeLimit(60); // Set time limit for tests
    }

    @Test
    void testAddPlayer_Success() throws Exception {
        game.addPlayer(user);
        assertTrue(game.getUsers().contains(user));
    }

    @Test
    void testAddPlayer_GameStarted_Failure() {
        game.setGameStarted(true);
        assertThrows(ResponseStatusException.class, () -> game.addPlayer(user));
    }

    @Test
    void testAddPlayer_NullUser_Failure() {
        assertThrows(ResponseStatusException.class, () -> game.addPlayer(null));
    }

    @Test
    void testStartGame_Success() {
        game.getUsers().add(new User());  // Adding one user to meet the minimum requirement
        game.getUsers().add(new User());  // Two users needed to start the game
        game.startGame();
        assertTrue(game.isGameStarted());
    }

    @Test
    void testStartGame_NotEnoughPlayers_Failure() {
        game.getUsers().add(new User());  // Only one user
        assertThrows(ResponseStatusException.class, game::startGame);
    }

    @Test
    void testRemovePlayer_Success() {
        User newUser = new User();
        newUser.setUsername("testUser");
        game.getUsers().add(newUser);
        game.removePlayer(newUser);
        assertFalse(game.getUsers().contains(newUser));
    }

    @Test
    void testRemovePlayer_NotFound_Failure() {
        User newUser = new User();
        newUser.setUsername("testUser");
        assertThrows(ResponseStatusException.class, () -> game.removePlayer(newUser));
    }

    @Test
    void testAddPlayer_UserAlreadyInLobby_Failure() {
        User user = mock(User.class);
        game.getUsers().add(user);  // Add the user to the lobby first

        Exception exception = assertThrows(ResponseStatusException.class, () -> game.addPlayer(user));
        assertTrue(exception.getMessage().contains("User already in lobby"));
    }

    @Test
    void testAddPlayer_MaximumPlayersReached_Failure() {
        User user = mock(User.class);
        // Simulate the lobby being full
        for (int i = 0; i < game.getMaxAmtUsers(); i++) {
            game.getUsers().add(mock(User.class));
        }

        Exception exception = assertThrows(ResponseStatusException.class, () -> game.addPlayer(user));
        assertTrue(exception.getMessage().contains("Maximum amount of users in lobby already reached"));
    }

    @Test
    void testAddPlayer_GameAlreadyStarted_Failure() {
        User user = mock(User.class);
        game.setGameStarted(true);  // Set the game as started

        Exception exception = assertThrows(ResponseStatusException.class, () -> game.addPlayer(user));
        assertTrue(exception.getMessage().contains("Game has already started"));
    }

    @Test
    void testAddPlayer_PlayerHasBeenRemoved_Failure() {
        User user = mock(User.class);
        when(user.getUsername()).thenReturn("testUser");
        game.getListOfRemovedPlayers().add("testUser");  // Mark the user as removed by the host

        Exception exception = assertThrows(ResponseStatusException.class, () -> game.addPlayer(user));
        assertTrue(exception.getMessage().contains("You have been removed from the lobby by the host. Sorry, you cannot rejoin."));
    }

    @Test
    void testStartGame_GameAlreadyStarted_Failure() {
        User user1 = new User();
        User user2 = new User();

        game.getUsers().add(user1);
        game.getUsers().add(user2);

        game.startGame();  // Start the game first time
        assertTrue(game.isGameStarted());  // Verify game started

        Exception exception = assertThrows(ResponseStatusException.class, game::startGame);
        assertTrue(exception.getMessage().contains("Game has already started"));
    }

    @Test
    void testScalePointsByDuration_TimeLeftZero() throws Exception {
        when(user.getPointsAwardedFromChatGPT()).thenReturn(10);
        game.scalePointsByDuration(user, 60); // Time guess submitted is exactly the time limit

        verify(user).setTimeGuessSubmitted(60);
        verify(user, never()).setBonusPoints(anyInt());
        verify(user, never()).setTotalPoints(anyInt());
        assertEquals(0, user.getTotalPoints());
        assertEquals(0, user.getTotalPoints());
    }

    @Test
    void testScalePointsByDuration_PercentageOfTimeLeft_Above75() throws Exception {
        when(user.getPointsAwardedFromChatGPT()).thenReturn(6);
        game.scalePointsByDuration(user, 10); // Time guess submitted is 10 seconds, time left is 50 seconds

        verify(user).setTimeGuessSubmitted(10);
        verify(user).setBonusPoints(2); 
        verify(user).setTotalPoints(8);
    }

    @Test
    void testScalePointsByDuration_PercentageOfTimeLeft_Above50() throws Exception {
        when(user.getPointsAwardedFromChatGPT()).thenReturn(6);
        game.scalePointsByDuration(user, 30); // Time guess submitted is 30 seconds, time left is 30 seconds

        verify(user).setTimeGuessSubmitted(30);
        verify(user).setBonusPoints(1); 
        verify(user).setTotalPoints(7);
    }

    @Test
    void testScalePointsByDuration_PercentageOfTimeLeft_Below50() throws Exception {
        when(user.getPointsAwardedFromChatGPT()).thenReturn(6);
        game.scalePointsByDuration(user, 40); // Time guess submitted is 40 seconds, time left is 20 seconds

        verify(user).setTimeGuessSubmitted(40);
        verify(user).setTotalPoints(6); // No bonus points, total points = points from ChatGPT
    }


}
