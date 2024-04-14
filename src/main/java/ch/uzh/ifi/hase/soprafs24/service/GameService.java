package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.User;

import ch.uzh.ifi.hase.soprafs24.game.Game;
import ch.uzh.ifi.hase.soprafs24.game.chatGPT.ChatGPT;
import ch.uzh.ifi.hase.soprafs24.game.dallE.DallE;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GamePostDTO;

import java.util.ArrayList;
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
    private final List<Game> games = new ArrayList<>(); 

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

    public List<Game> getAllGames() {
        return games;
    }

    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // HERE IS THE WRONG PLACE FOR FIND FUNCTIONS!!! THESE HAVE TO BE PLACED IN THE GAMEREPOSITORY.JAVA SEE EXAMPLES THERE
    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//    public Game findById(Long id) throws ResponseStatusException{
//             for (Game game : games) {
//                 if (game.getId().equals(id)) {
//                     return game;
//                 }
//             }
//         return null;
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

        if (lobbyOwner == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User does not exist");
        }

        List<User> users = new ArrayList<>();
        users.add(lobbyOwner);
        Game newGame = new Game(nextId, lobbyOwner.getUsername()); 
        newGame.addPlayer(lobbyOwner);
        newGame.setLobbyOwner(lobbyOwner.getUsername());
        newGame.setId(nextId);

        gameRepository.save(newGame);
        gameRepository.flush();

        games.add(newGame);

        return newGame;
    }


   public Game updateGameSettings(Long id, GamePostDTO gamePostDTO) {
       Game lobby = gameRepository.findById(id)
       .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lobby not found"));

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

       //Validate max amount of users
       int maxAmtUsers = gamePostDTO.getMaxAmtUsers();
       if (maxAmtUsers < 2) {
           throw new IllegalArgumentException("The maximum number of users cannot be less than 2.");
       }

       lobby.setMaxAmtUsers(maxAmtUsers);
       lobby.setAmtOfRounds(amtOfRounds);
       lobby.setTimeLimit(timeLimit);

       gameRepository.save(lobby);
       gameRepository.flush();

    //    lobbies.put(lobby.getId(), lobby);
       return lobby;
   }

   public Game joinLobby(String invitationCodes, String userToken) throws Exception {

       if(invitationCodes == null || invitationCodes.isEmpty()){
           throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lobby invitation code is null or empty");
       }

       if(userToken == null || userToken.isEmpty()){
           throw new ResponseStatusException(HttpStatus.NOT_FOUND, "UserToken is null or empty");
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

   // This is just an initial implementation of the startGameLobby method
   public Game startGameLobby(Long id) {
    if (id == null || id == 0) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game ID is null or empty");
    }
       
    Game lobby = gameRepository.findById(id)
       .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lobby not found"));

    lobby.startGame();
    lobby.setGameStarted(true);

    gameRepository.save(lobby);
    gameRepository.flush();

    return lobby;
   }

   public User getNextPictureGenerator(Long id){
       if (id == null || id == 0) {
           throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game ID is null or empty");
       }

       Game game = gameRepository.findById(id)
               .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lobby not found"));

       User nextPictureGenerator = game.selectPictureGenerator();

       gameRepository.save(game);
       gameRepository.flush();

       return nextPictureGenerator;
   }

   public String generatePictureDallE(String prompt) throws Exception {

    if (prompt == null || prompt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Text prompt provided by the player is null or empty");
        }

        return dallE.generatePicture(prompt);
    
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
        
