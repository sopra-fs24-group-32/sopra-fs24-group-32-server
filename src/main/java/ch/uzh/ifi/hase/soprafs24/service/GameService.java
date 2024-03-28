package ch.uzh.ifi.hase.soprafs24.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.game.lobby.Lobby;
import ch.uzh.ifi.hase.soprafs24.game.player.Player;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GamePostDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class GameService {

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

    public Lobby findByLobbyId(String lobbyId) {
        // String[] parts = lobbyId.split("(?<=\\D)(?=\\d)");
        for (Map.Entry<Long, Lobby> entry : lobbies.entrySet()) {
            if (entry.getValue().getLobbyId().equals(lobbyId)) {
                return entry.getValue();
            }
        }
        return null; // Return null if lobby is not found
    }

    public Lobby createGame(GamePostDTO gamePostDTO) {
        User findUser = findByUsername(gamePostDTO.getUsername());
        String userToken = findUser.getUserToken();
        try {
            if (userToken != null) {
                long id = nextId++;
                float timeLimit = gamePostDTO.getTimeLimit();
                int amtOfRounds = gamePostDTO.getAmtOfRounds();
                Player host = new Player();
                host.setUsername(findUser.getUsername());
    
                Lobby lobbyCreated = new Lobby(id, timeLimit, amtOfRounds);
                lobbyCreated.addPlayer(host);
                lobbies.put(id, lobbyCreated);
                // gameRepository.save(lobbyCreated);
                // gameRepository.flush();
                return lobbyCreated;
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new RuntimeException("Something went wrong creating game: " + e);
            }
        } 


    public Lobby updateGame(String lobbyId, GamePostDTO gamePostDTO) {
        Lobby reqLobby = findByLobbyId(lobbyId);
        if (gamePostDTO.getTimeLimit() != 0){
            reqLobby.setTimeLimit(gamePostDTO.getTimeLimit());
        }
        if (gamePostDTO.getAmtOfRounds() != 0){
            reqLobby.setAmtOfRounds(gamePostDTO.getAmtOfRounds());
        }
        String[] parts = lobbyId.split("(?<=\\D)(?=\\d)");
        long index = Long.parseLong(parts[1]);
        lobbies.put(index, reqLobby);
        // gameRepository.save(reqLobby);
        // gameRepository.flush();
        return reqLobby;
    }

    public Lobby joinGame(String lobbyId, User user) {

        User findUser = findByUsername(user.getUsername());

        String userToken = findUser.getUserToken();

        try {
            Lobby reqLobby = findByLobbyId(lobbyId);
            if (userToken !=null && reqLobby != null) {
                Player player = new Player();
                player.setUsername(findUser.getUsername());
                reqLobby.addPlayer(player);
                String[] parts = lobbyId.split("(?<=\\D)(?=\\d)");
                long index = Long.parseLong(parts[1]);
                lobbies.put(index, reqLobby);
                // gameRepository.save(reqLobby);
                // gameRepository.flush();
                return reqLobby;
            } else {
                System.out.println("User token not verified or lobbyId:" + lobbyId + "not found");
                return null;
            }
        } catch (Exception e) {
            // Handle exception
            throw new RuntimeException("Something went wrong joining game: " + e);
        }     
    }


    public User findByUsername(String username) {
        List<User> users = userRepository.findAll();
        User findUser = new User();
        for (int i=0; i<users.size(); i++){
          if (users.get(i).getUsername().equals(username)){
            findUser = users.get(i);
          }
        }
        return findUser;
      }
    
}
        
