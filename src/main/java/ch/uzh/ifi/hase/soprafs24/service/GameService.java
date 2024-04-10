package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Player;

import org.aspectj.weaver.bcel.UnwovenClassFile.ChildClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.game.lobby.Lobby;
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
    public Lobby findByLobbyInvitationCodes(String invitationCodes) {
        for (Map.Entry<Long, Lobby> entry : lobbies.entrySet()) {
            if (entry.getValue().getInvitationCodes().equals(invitationCodes)) {
                return entry.getValue();
            }
        }
        return null; // Return null if lobby is not found
    }

    //create a lobby
    public Lobby createLobby(String userToken) throws Exception {
        
        User lobbyOwner = userRepository.findByUserToken(userToken);
        long id = nextId++;
        String lobbyOwnerName = lobbyOwner.getUsername();
        Lobby lobby = new Lobby(id, lobbyOwnerName);
        Player host = new Player();
        host.setUsername(lobbyOwner.getUsername());
        lobby.addPlayer(host);
        lobbies.put(id, lobby);
        return lobby;
    }


    public Lobby updateGame(String lobbyId, GamePostDTO gamePostDTO) {

        Lobby reqLobby = findByLobbyId(lobbyId);
        float timeLimit = gamePostDTO.getTimeLimit();
        if (reqLobby != null) {
            if (timeLimit >= 5.0 && timeLimit <= 100.0){
                reqLobby.setTimeLimit(timeLimit);
                
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Time limit is too low or too high");
            }

            if (gamePostDTO.getAmtOfRounds() > 0){
                reqLobby.setAmtOfRounds(gamePostDTO.getAmtOfRounds());
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Amount of rounds cannot be negative or zero");
            }

            if (gamePostDTO.getMaxAmtPlayers() >= 2){
                reqLobby.setMaxAmtPlayers(gamePostDTO.getMaxAmtPlayers());
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Maximum amount of players cannot be less than 2");
            }
            lobbies.put(reqLobby.getId(), reqLobby);
            return reqLobby;
        } 
        return reqLobby;
    }

    public Lobby updateGameSettings(String lobbyId, GamePostDTO gamePostDTO) {
        Lobby lobby = findByLobbyId(lobbyId);

        //Validate if lobby exists
        if (lobby == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lobby not found");
        }
        // Validate the amount of rounds
        int amtOfRounds = gamePostDTO.getAmtOfRounds();
        if (amtOfRounds < 1) {
            throw new IllegalArgumentException("There must be at least one round.");
        }

        // Validate the time limit
        float timeLimit = gamePostDTO.getTimeLimit();
        if (timeLimit < 5 || timeLimit > 100) {
            throw new IllegalArgumentException("Time limit must be between 5 seconds and 100 Seconds.");
        }

        //Validate max amount of players
        int maxAmtPlayers = gamePostDTO.getMaxAmtPlayers();
        if (maxAmtPlayers < 2) {
            throw new IllegalArgumentException("The maximum number of players cannot be less than 2.");
        }

        lobby.setMaxAmtPlayers(maxAmtPlayers);
        lobby.setAmtOfRounds(amtOfRounds);
        lobby.setTimeLimit(timeLimit);

        lobbies.put(lobby.getId(), lobby);
        return lobby;
    }

    public void joinLobby(String invitationCodes, String userToken) throws Exception {

        if(invitationCodes == null || invitationCodes.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lobby with sent invitationCodes does not exist");
        }

        if(userToken == null || userToken.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User does not exists 111");
        }

        User user = userRepository.findByUserToken(userToken);
        Lobby lobby = findByLobbyInvitationCodes(invitationCodes);

        /*
        Currently checked within the userService.findByUserToken() method -> same exception gets thrown
        if(user == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User with sent userToken does not exist");
        }
         */

        if(lobby == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lobby with id " + invitationCodes + " does not exist");
        }

        if(lobby.gameHasStarted()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game has already started");
        }

        // Player newPlayer = (Player) user;
        Player newPlayer = new Player();
        newPlayer.setUsername(user.getUsername());
        lobby.addPlayer(newPlayer);
        lobbies.put(lobby.getId(), lobby);
    }
    
}
        
