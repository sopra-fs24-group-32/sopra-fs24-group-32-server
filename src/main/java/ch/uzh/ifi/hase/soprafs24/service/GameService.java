package ch.uzh.ifi.hase.soprafs24.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.game.lobby.Lobby;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GamePostDTO;



import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class GameService {

    private final Logger log = LoggerFactory.getLogger(GameService.class);
    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private static final Map<Long, Lobby> lobbies = new HashMap<>();
    private long nextId=1;    

    @Autowired
    public GameService(@Qualifier("userRepository") UserRepository userRepository,
                        @Qualifier("gameRepository") GameRepository gameRepository
                        ){
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
    }

    public List<User> getAllUsers() {
        return this.userRepository.findAll();
    }

    public Map<Long, Lobby> getAllLobbies() {
        return lobbies;
    }

    public Lobby findByLobbyId(long lobbyId) {
        Lobby findLobby = new Lobby();
        for (int i=0; i<lobbies.size(); i++){
          if (lobbies.get(i).getLobbyId().equals("roomId"+lobbyId)){
            findLobby = lobbies.get(i);
          }
        }
        return findLobby;
      }

    public boolean fetchUserToken(List<User> users, String userToken) {
        for (User user : users) {
            if (user.getUserToken().equals(userToken)) {
                return true;
            }
        }
        return false;
    }

    public Lobby createGame(GamePostDTO gamePostDTO) {
        String userToken = gamePostDTO.getUserToken();
        List<User> users = getAllUsers();
        try {
            long id = nextId++;
            boolean userTokenVerified = fetchUserToken(users, userToken);
            if (userTokenVerified) {
                float timeLimit = gamePostDTO.getTimeLimit();
                int amtOfRounds = gamePostDTO.getAmtOfRounds();
                Lobby lobbyCreated = new Lobby(id, timeLimit, amtOfRounds);
                lobbies.put(id, lobbyCreated);
                log.debug("Created Game Lobby Information: {}", lobbyCreated);
                gameRepository.save(lobbyCreated);
                gameRepository.flush();
                return lobbyCreated;
            } else {
                return null;
            }
        } catch (Exception e) {
                // Handle exception
            throw new RuntimeException("Something went wrong creating game: " + e);
            }
        } 
}
        
