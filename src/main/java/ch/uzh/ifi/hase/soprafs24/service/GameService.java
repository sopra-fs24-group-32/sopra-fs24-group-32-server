package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.User;

import ch.uzh.ifi.hase.soprafs24.game.Game;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.weaver.bcel.UnwovenClassFile.ChildClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GamePostDTO;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class GameService {

    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final UserService userService;
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


    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // HERE IS THE WRONG PLACE FOR FIND FUNCTIONS!!! THESE HAVE TO BE PLACED IN THE GAMEREPOSITORY.JAVA SEE EXAMPLES THERE
    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//    public Lobby findById(String id) {
//        for (Map.Entry<Long, Lobby> entry : lobbies.entrySet()) {
//            if (entry.getValue().getId().equals(id)) {
//                return entry.getValue();
//            }
//        }
//        return null; // Return null if lobby is not found
//    }
//    public Lobby findByLobbyInvitationCodes(String invitationCodes) {
//        for (Map.Entry<Long, Lobby> entry : lobbies.entrySet()) {
//            if (entry.getValue().getInvitationCodes().equals(invitationCodes)) {
//                return entry.getValue();
//            }
//        }
//        return null; // Return null if lobby is not found
//    }

    //create a lobby
    public Game createLobby(Game newGame, String userToken) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> map = objectMapper.readValue(userToken, Map.class);
        // Extract the userToken from the Map
        String mappedToken = map.get("userToken");
        
        User lobbyOwner = userRepository.findByUserToken(mappedToken);

        List<User> users = new ArrayList<>();
        users.add(lobbyOwner);
        newGame.setUsers(users);
        newGame.setLobbyOwner(lobbyOwner.getId().toString());

        String LETTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        SecureRandom RANDOM = new SecureRandom();
        int length = 10;

        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(LETTERS.charAt(RANDOM.nextInt(LETTERS.length())));
        }
        ;
        newGame.setLobbyInvitationCode(sb.toString());

        gameRepository.save(newGame);
        gameRepository.flush();

        return newGame;
    }


//    public Game updateGame(String id, GamePostDTO gamePostDTO) {
//
//        Game reqLobby = gameRepository.findById(id);
//        float timeLimit = gamePostDTO.getTimeLimit();
//        if (reqLobby != null) {
//            if (timeLimit >= 5.0 && timeLimit <= 100.0){
//                reqLobby.setTimeLimit(timeLimit);
//
//            } else {
//                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Time limit is too low or too high");
//            }
//
//            if (gamePostDTO.getAmtOfRounds() > 0){
//                reqLobby.setAmtOfRounds(gamePostDTO.getAmtOfRounds());
//            } else {
//                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Amount of rounds cannot be negative or zero");
//            }
//
//            if (gamePostDTO.getMaxAmtUsers() >= 2){
//                reqLobby.setMaxAmtUsers(gamePostDTO.getMaxAmtUsers());
//            } else {
//                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Maximum amount of users cannot be less than 2");
//            }
//            lobbies.put(reqLobby.getId(), reqLobby);
//            return reqLobby;
//        }
//        return reqLobby;
//    }

//    public Game updateGameSettings(String id, GamePostDTO gamePostDTO) {
//        Game lobby = gameRepository.findById(id);
//
//        //Validate if lobby exists
//        if (lobby == null) {
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lobby not found");
//        }
//        // Validate the amount of rounds
//        int amtOfRounds = gamePostDTO.getAmtOfRounds();
//        if (amtOfRounds < 1) {
//            throw new IllegalArgumentException("There must be at least one round.");
//        }
//
//        // Validate the time limit
//        float timeLimit = gamePostDTO.getTimeLimit();
//        if (timeLimit < 5 || timeLimit > 100) {
//            throw new IllegalArgumentException("Time limit must be between 5 seconds and 100 Seconds.");
//        }
//
//        //Validate max amount of users
//        int maxAmtUsers = gamePostDTO.getMaxAmtUsers();
//        if (maxAmtUsers < 2) {
//            throw new IllegalArgumentException("The maximum number of users cannot be less than 2.");
//        }
//
//        lobby.setMaxAmtUsers(maxAmtUsers);
//        lobby.setAmtOfRounds(amtOfRounds);
//        lobby.setTimeLimit(timeLimit);
//
//        lobbies.put(lobby.getId(), lobby);
//        return lobby;
//    }

//    public Lobby joinLobby(String invitationCodes, String userToken) throws Exception {
//
//        if(invitationCodes == null || invitationCodes.isEmpty()){
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lobby with sent invitationCodes does not exist");
//        }
//
//        if(userToken == null || userToken.isEmpty()){
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User does not exists 111");
//        }
//
//        ObjectMapper objectMapper = new ObjectMapper();
//        Map<String, String> map = objectMapper.readValue(userToken, Map.class);
//        // Extract the userToken from the Map
//        String mappedToken = map.get("userToken");
//
//        User user = userRepository.findByUserToken(mappedToken);
//        System.out.println(mappedToken);
//        System.out.println(user);
//        Lobby lobby = gameRepository.findByLobbyInvitationCode(invitationCodes);
//
//        /*
//        Currently checked within the userService.findByUserToken() method -> same exception gets thrown
//        if(user == null){
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User with sent userToken does not exist");
//        }
//         */
//
//        if(lobby == null){
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lobby with id " + invitationCodes + " does not exist");
//        }
//
//        if(lobby.gameHasStarted()){
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game has already started");
//        }
//
//        // User newPlayer = (User) user;
//        User newPlayer = new User();
//        newPlayer.setUsername(user.getUsername());
//        lobby.addPlayer(newPlayer);
//        lobbies.put(lobby.getId(), lobby);
//
//        gameRepository.save(lobby);
//        gameRepository.flush();
//
//        return lobby;
//    }
    
}
        
