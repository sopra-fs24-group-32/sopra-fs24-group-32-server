package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Player;

import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import ch.uzh.ifi.hase.soprafs24.game.dallE.DallE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class GameService {

    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final UserService userService;
    private final DallE dallE = new DallE();
    private static final Map<Long, Lobby> lobbies = new HashMap<>();
    private long nextId=1;    

    @Autowired
    public GameService(@Qualifier("userRepository") UserRepository userRepository, @Qualifier("gameRepository") GameRepository gameRepository,
                        @Qualifier("userService") UserService userService
                        ){
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
        this.userService = userService;
    }

    public List<User> getAllUsers() {
        return this.userRepository.findAll();
    }

    public Map<Long, Lobby> getAllLobbies() {
        return lobbies;
    }

    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // HERE IS THE WRONG PLACE FOR FIND FUNCTIONS!!! THESE HAVE TO BE PLACED IN THE GAMEREPOSITORY.JAVA SEE EXAMPLES THERE
    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
   public Lobby findByLobbyId(String lobbyId) {
       for (Map.Entry<Long, Lobby> entry : lobbies.entrySet()) {
           if (entry.getValue().getLobbyId().equals(lobbyId)) {
               return entry.getValue();
           }
       }
       return null; // Return null if lobby is not found
   }
   
   public Lobby findByLobbyInvitationCodes(String invitationCodes) {
        List<Lobby> lobbies = gameRepository.findAll();

        for (Lobby lobby : lobbies) {
            if (lobby.getLobbyInvitationCode().equals(invitationCodes)) {
                return lobby;
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
        gameRepository.save(lobby);
        gameRepository.flush();

        return lobby;
    }
 

    public Lobby updateGameSettings(String lobbyId, GamePostDTO gamePostDTO) {
        Lobby lobby = gameRepository.findByLobbyId(lobbyId);

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

    public Lobby joinLobby(String invitationCodes, String userToken) throws Exception {

        if(invitationCodes == null || invitationCodes.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lobby with sent invitationCodes does not exist");
        }

        if(userToken == null || userToken.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User does not exists 111");
        }

        // ObjectMapper objectMapper = new ObjectMapper();
        // Map<String, String> map = objectMapper.readValue(userToken, Map.class);
        // // Extract the userToken from the Map
        // String mappedToken = map.get("userToken");

        User user = userRepository.findByUserToken(userToken);
        // System.out.println(mappedToken);
        // System.out.println(user);
        Lobby lobby = gameRepository.findByLobbyInvitationCode(invitationCodes);

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

        gameRepository.save(lobby);
        gameRepository.flush();

        return lobby;
    }

    public String generatePictureDallE(String prompt) throws Exception {
        
        if (prompt == null || prompt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Image prompt provided by the player is null or empty");
        }

        return dallE.generatePicture(prompt);
        
    }
    
}
        
