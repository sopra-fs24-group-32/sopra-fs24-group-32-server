package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.game.lobby.Lobby;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GamePostDTO;
import org.springframework.web.server.ResponseStatusException;

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
    public Lobby createLobby(String userToken) throws Exception {
        
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
            String[] parts = lobbyId.split("(?<=\\D)(?=\\d)");
            long index = Long.parseLong(parts[1]);
            lobbies.put(index, reqLobby);
            return reqLobby;
        } 
        return reqLobby;
    }

    public void joinLobby(String lobbyId, String userToken) throws Exception {

        User user = userService.findByToken(userToken);
        Lobby lobby = findByLobbyId(lobbyId);

        if(user == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User with sent token does not exists");
        }

        if(lobby == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lobby with id" + lobbyId + "does not exist");
        }

        if(lobby.isGameHasStarted()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game has already started");
        }

        Player newPlayer = (Player) user;
        lobby.addPlayer(newPlayer);
    }
    
}
        
