package ch.uzh.ifi.hase.soprafs24.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.game.lobby.Lobby;
import ch.uzh.ifi.hase.soprafs24.game.player.Player;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GamePostDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class GameService {

    private final UserRepository userRepository;
    private final UserService userService;
    private static final Map<Long, Lobby> lobbies = new HashMap<>();
    private long nextId=1;    

    @Autowired
    public GameService(@Qualifier("userRepository") UserRepository userRepository,
                        @Qualifier("userService") UserService userService
                        ){
        this.userRepository = userRepository;
        this.userService = userService;
    }

    public List<User> getAllUsers() {
        return this.userRepository.findAll();
    }

    public Map<Long, Lobby> getAllLobbies() {
        return lobbies;
    }

    public Lobby findByLobbyId(String lobbyId) {
        for (Map.Entry<Long, Lobby> entry : lobbies.entrySet()) {
            if (entry.getValue().getLobbyId().equals(lobbyId)) {
                return entry.getValue();
            }
        }
        return null; // Return null if lobby is not found
    }

    //create a lobby
    public Lobby createLobby(String userToken) {
        
        User lobbyOwner = userService.findByToken(userToken);
        long id = nextId++;
        Lobby lobby = new Lobby(id, lobbyOwner.getUsername());
        Player host = new Player();
        host.setUsername(lobbyOwner.getUsername());
        lobby.addPlayer(host);
        lobbies.put(id, lobby);
        return lobby;
    }


    public Lobby updateGame(String lobbyId, GamePostDTO gamePostDTO) {
        Lobby reqLobby = findByLobbyId(lobbyId);
        if (reqLobby != null) {
            if (gamePostDTO.getTimeLimit() != 0){
                reqLobby.setTimeLimit(gamePostDTO.getTimeLimit());
            }
            if (gamePostDTO.getAmtOfRounds() != 0){
                reqLobby.setAmtOfRounds(gamePostDTO.getAmtOfRounds());
            }
            if (gamePostDTO.getMaxAmtPlayers() != 0){
                reqLobby.setMaxAmtPlayers(gamePostDTO.getMaxAmtPlayers());
            }
            lobbies.put(reqLobby.getId(), reqLobby);
            return reqLobby;
        } 
        return reqLobby;
    }

    public Lobby joinGame(String lobbyId, String userToken) {

        User verifiedUser = userService.findByToken(userToken);
        Lobby reqLobby = findByLobbyId(lobbyId);
        if (verifiedUser.getUserToken() !=null && reqLobby != null) {
            Player player = new Player();
            player.setUsername(verifiedUser.getUsername());
            reqLobby.addPlayer(player);
            lobbies.put(reqLobby.getId(), reqLobby);
            return reqLobby;
            }
        return reqLobby;       
    }
    
}
        
