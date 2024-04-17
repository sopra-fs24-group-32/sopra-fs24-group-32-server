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
import ch.uzh.ifi.hase.soprafs24.rest.dto.ChatGPTPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GameGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GameService {

    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final UserService userService;
    private final DallE dallE = new DallE();
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


    public GameGetDTO getGame(Long gameId) {
        return gameRepository.findById(gameId)
            .map(game -> DTOMapper.INSTANCE.convertEntityToGameGetDTO(game))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lobby not found"));
    }



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
        nextId++;

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


   public Game leaveLobby(Long lobbyId, String userToken) throws Exception {
    try {
        // Check for empty userToken
        if (userToken == null || userToken.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User token is null or empty");
        }
    
        // Check for empty lobbyId
        if (lobbyId == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lobby ID is null");
        }
    
        // Parse userToken to find the user
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> map = objectMapper.readValue(userToken, Map.class);
        String mappedToken = map.get("userToken");
    
        User user = userRepository.findByUserToken(mappedToken);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User does not exist");
        }
    
        // Find the game using lobbyId
        Game game = gameRepository.findById(lobbyId).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Lobby with ID " + lobbyId + " does not exist"));
    
        // Check if the user is in the specified lobby
        if (!game.getUsers().contains(user)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User is not in the specified lobby");
        }
    
        // Remove the user from the lobby
        game.removePlayer(user);
        gameRepository.save(game);

        return game;
    } catch (Exception e) {
        // Log the exception
        System.out.println("Error during leaveLobby: " + e.getMessage());
        throw e;  // Re-throw the exception to handle it according to your error handling strategy
    }
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

    ObjectMapper objectMapper = new ObjectMapper();
    Map<String, String> map = objectMapper.readValue(prompt, Map.class);
    String mappedPrompt = map.get("description");

    if (mappedPrompt == null || mappedPrompt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Text prompt provided by the player is null or empty");
        }
    String imgUrl = dallE.generatePicture(mappedPrompt);
    dallE.setInputPhrase(mappedPrompt);
    dallE.setImageUrl(imgUrl);
    
    return imgUrl;
    
    }

    public String getImageGeneratedByDallE() {
        String imgUrl = dallE.getImageUrl();
        dallE.setImageUrl("");
        return imgUrl;
    }

    public int evaluatePlayerGuessWithChatGPT(String playerGuessed) throws Exception{

        ChatGPT chatGPT = new ChatGPT();

        String originalText = dallE.getInputPhrase();

        if (playerGuessed == null || playerGuessed.isEmpty()) {
            return 0;
        } else {
            float chatGPTResult = chatGPT.rateInputs(originalText, playerGuessed);
            int pointsAwarded = chatGPT.convertSimilarityScoreToPoints(chatGPTResult);
            return pointsAwarded;
        }
    }

    public void playerLeaveGame(Long gameId, String userToken) throws Exception {

        if (userToken == null || userToken.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "UserToken is null or empty");
        }

        if (gameId == null || gameId == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game ID is null or empty");
        }
        
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lobby not found"));
         
        User user = userRepository.findByUserToken(userToken);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User with sent userToken does not exist");
        }

        game.removePlayer(user);
        gameRepository.save(game);
        // gameRepository.flush();
    }
}
        
