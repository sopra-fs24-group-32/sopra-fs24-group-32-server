package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.game.Game;
import ch.uzh.ifi.hase.soprafs24.game.chatGPT.ChatGPT;
import ch.uzh.ifi.hase.soprafs24.game.dallE.DallE;
import ch.uzh.ifi.hase.soprafs24.service.GameService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DataJpaTest
public class GameRepositoryIntegrationTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Mock
    private ChatGPT chatGPT;

    @Mock
    private DallE dallE;

    @Mock
    UserService userService;

    GameService gameService;

    private User createUser(String number) {
        String username = "username" + number;
        String password = "password" + number;

        User newUser = new User(username, password);
        newUser.setStatus(UserStatus.ONLINE);
        newUser.setIsLoggedIn(true);
        newUser.setUserToken(UUID.randomUUID().toString());

        return newUser;
    }

    @BeforeEach
    public void setup() {
        gameService = new GameService(userRepository, gameRepository, userService, dallE, chatGPT);
    }

    @Test
    public void removingPlayerFromLobby() throws Exception {
        // Create users
        User user1 = createUser("1");
        User user2 = createUser("2");
        User user3 = createUser("3");

        // Persist users
        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.persist(user3);
        entityManager.flush();

        // Create a game
        String lobbyOwner = user1.getUsername();
        long gameId = 1L;
        Game game = new Game(gameId, lobbyOwner);
        game.addPlayer(user2);
        game.addPlayer(user3);

        // Persist the game
        entityManager.persist(game);
        entityManager.flush();

        User foundUserTest = userRepository.findByUsername(user3.getUsername());
        assertNotNull(foundUserTest);

        // Call the method under test
        gameService.leaveLobby(gameId, user3.getUserToken());

        // Retrieve the updated game from the database
        Game updatedGame = gameRepository.findById(gameId).orElse(null);
        assertNotNull(updatedGame);

        // Verify that the player is removed from the game
        assertFalse(updatedGame.getUsers().contains(user3));

        // Verify that the user still exists in the database
        User foundUser = userRepository.findByUsername(user3.getUsername());
        assertNotNull(foundUser);
    }
}

