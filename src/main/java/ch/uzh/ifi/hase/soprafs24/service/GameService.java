package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.User;

import ch.uzh.ifi.hase.soprafs24.game.Game;
import ch.uzh.ifi.hase.soprafs24.game.chatGPT.ChatGPT;
import ch.uzh.ifi.hase.soprafs24.game.dallE.DallE;
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
    private final DallE dallE = new DallE();
    private final ChatGPT chatGPT = new ChatGPT();
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
    public Game createLobby(String userToken) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> map = objectMapper.readValue(userToken, Map.class);
        // Extract the userToken from the Map
        String mappedToken = map.get("userToken");
        
        User lobbyOwner = userRepository.findByUserToken(mappedToken);

        List<User> users = new ArrayList<>();
        users.add(lobbyOwner);
        Game newGame = new Game(nextId, lobbyOwner.getUsername()); 
        newGame.addPlayer(lobbyOwner);
        newGame.setLobbyOwner(lobbyOwner.getUsername());

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

   public Game joinLobby(String invitationCodes, String userToken) throws Exception {

       if(invitationCodes == null || invitationCodes.isEmpty()){
           throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lobby with sent invitationCodes does not exist");
       }

       if(userToken == null || userToken.isEmpty()){
           throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User does not exists 111");
       }

       ObjectMapper objectMapper = new ObjectMapper();
       Map<String, String> map = objectMapper.readValue(userToken, Map.class);
       // Extract the userToken from the Map
       String mappedToken = map.get("userToken");

       User user = userRepository.findByUserToken(mappedToken);
       Game game = gameRepository.findByLobbyInvitationCode(invitationCodes);

       /*
       Currently checked within the userService.findByUserToken() method -> same exception gets thrown
       if(user == null){
           throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User with sent userToken does not exist");
       }
        */

       if(game == null){
           throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lobby with id " + invitationCodes + " does not exist");
       }

       if(game.isGameStarted()){
           throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game has already started");
       }

       game.addPlayer(user);

       gameRepository.save(game);
       gameRepository.flush();

       return game;
   }
    
    public Map<User, String> playerChatGTPScore(String originalPrompt, Map<User, String> playerInputGuessed) throws Exception {

        if (originalPrompt == null || originalPrompt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Text prompt provided by the player is null or empty");
        }
        
        for (Map.Entry<User, String> entry : playerInputGuessed.entrySet()) {
            String guessedInput = entry.getValue();
            if (guessedInput == null || guessedInput.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Text response provided by the player is null or empty");
            }
        }
        return chatGPT.rateInputs(originalPrompt, playerInputGuessed);
    }
}
        
