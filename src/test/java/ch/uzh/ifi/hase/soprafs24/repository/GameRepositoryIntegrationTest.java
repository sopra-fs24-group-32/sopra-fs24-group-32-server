package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.game.Game;
import ch.uzh.ifi.hase.soprafs24.game.chatGPT.ChatGPT;
import ch.uzh.ifi.hase.soprafs24.game.dallE.DallE;
import ch.uzh.ifi.hase.soprafs24.service.GameService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import javax.persistence.EntityManager;
import java.util.UUID;

import static org.mockito.Mockito.mock;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DataJpaTest
public class GameRepositoryIntegrationTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameRepository gameRepository;

    @Mock
    private ChatGPT chatGPT;

    @Mock
    private DallE dallE;

    @Mock
    UserService userService;

    GameService gameService;

    @Autowired
    private TestEntityManager entityManager;


    Long setUp(User user1, User user2, User user3) throws Exception {
        long gameId = 1L;
        Game game = new Game(gameId, user1.getUsername());
        game.addPlayer(user1);
        game.addPlayer(user2);
        game.addPlayer(user3);
        entityManager.persist(game);
        long id = game.getId();
        entityManager.flush();
        return id;
    }


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
    public void setupState() {
        gameService = new GameService(userRepository, gameRepository, userService, dallE, chatGPT);
    }

    @AfterEach
    public void cleanUp(){
        entityManager.clear();
    }


    @Test
    void givenEntities_whenRemoveFromOwner_thenRemoveAssociation() throws Exception {
        User user1 = createUser("1");
        User user2 = createUser("2");
        User user3 = createUser("3");

        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.persist(user3);

        Long gameId = setUp(user1, user2, user3);

        Game game = (Game) entityManager.find(Game.class, gameId);

        User userFound = entityManager.find(User.class, user1.getId());

        userFound.deleteGame(game);
        entityManager.persist(userFound);

        assert userFound.getGame() == null;
        assert !game.getUsers().contains(userFound);
    }


    @Test
    public void removingPlayerFromLobby() throws Exception {
        User user1 = createUser("1");
        User user2 = createUser("2");
        User user3 = createUser("3");

        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.persist(user3);
        entityManager.flush();

        Long gameId = setUp(user1, user2, user3);

        Game game = (Game) entityManager.find(Game.class, gameId);

        User userFound = entityManager.find(User.class, user1.getId());

        game.removePlayer(userFound);

        entityManager.persist(game);

        assert !game.getUsers().contains(userFound);
        assert userFound.getGame() == null;
    }
}

