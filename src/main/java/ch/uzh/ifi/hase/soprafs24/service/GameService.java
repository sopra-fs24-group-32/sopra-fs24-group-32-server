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
import ch.uzh.ifi.hase.soprafs24.rest.dto.GameGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;

import javax.transaction.Transactional;
import java.util.*;

@Service
public class GameService {

    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final UserService userService;
    private final DallE dallE;
    private final ChatGPT chatGPT;
    private long nextId=1;   
    private final List<Game> games = new ArrayList<>();

    @Autowired
    public GameService(@Qualifier("userRepository") UserRepository userRepository, @Qualifier("gameRepository") GameRepository gameRepository,
                        @Qualifier("userService") UserService userService, DallE dallE, ChatGPT chatGPT
                        ){
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
        this.userService = userService;
        this.dallE = dallE;
        this.chatGPT = chatGPT;
    }

    public List<User> getAllUsers() {
        return this.userRepository.findAll();
    }

    public List<Game> getAllGames() {
        return gameRepository.findAll();
    }


    public GameGetDTO getGame(Long gameId) {
        if (gameId == null || gameId == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game ID is null");
        }
        return gameRepository.findById(gameId)
            .map(game -> DTOMapper.INSTANCE.convertEntityToGameGetDTO(game))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lobby not found"));
    }



    //create a lobby
    public Game createLobby(String userToken, GamePostDTO gamePostDTO) throws Exception {
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

        User lobbyOwner = userRepository.findByUserToken(userToken);

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

        newGame.setMaxAmtUsers(maxAmtUsers);
        newGame.setAmtOfRounds(amtOfRounds);
        newGame.setTimeLimit(timeLimit);

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
           throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lobby with invitation code: " + invitationCodes + " does not exist");
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
        if (userToken == null || userToken.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User token is null or empty");
        }
    
        if (lobbyId == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lobby ID is null");
        }
    
        User user = userRepository.findByUserToken(userToken);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User does not exist");
        }
    
        Game game = gameRepository.findById(lobbyId).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Lobby with ID " + lobbyId + " does not exist"));
    
        if (!game.getUsers().contains(user)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User is not in the specified lobby");
        }
        game.removePlayer(user);
        gameRepository.save(game);
        User foundUser = userRepository.findByUsername(user.getUsername());
        user.setGame(null);
        userRepository.save(user);
        return game;
    } catch (Exception e) {
        System.out.println("Error during leaveLobby: " + e.getMessage());
        throw e;
    }
    }




   // This is just an initial implementation of the startGameLobby method
   public Game startGameLobby(Long id) {
    if (id == null || id == 0) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game ID is null");
    }
       
    Game lobby = gameRepository.findById(id)
       .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lobby not found"));

    lobby.startGame();
    lobby.setGameStarted(true);

    gameRepository.save(lobby);
    gameRepository.flush();

    return lobby;
   }
   @Transactional
   public String getNextPictureGenerator(Long id){
       if (id == null || id == 0) {
           throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game ID is null");
       }

       Game game = gameRepository.findById(id)
               .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lobby not found"));

       String nextPictureGenerator = game.selectPictureGenerator();

       if (nextPictureGenerator == null) {
           throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "All rounds have been played. Game is over.");
       }

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

    System.out.println("Image URL generated by DALL-E-----: " + imgUrl);
    dallE.setInputPhrase(mappedPrompt);
    dallE.setImageUrl(imgUrl);
    
    return imgUrl;
    
    }

    public String getImageGeneratedByDallE() {
        return dallE.getImageUrl();
    }

    //gets triggered after each round so that players don't fetch the old picture
    public void resetDallEsImageURL(){
        dallE.setImageUrl("");
    }

    public int evaluatePlayerGuessWithChatGPT(String playerGuessed) throws Exception{

        String originalText = dallE.getInputPhrase();

        if (originalText == null || originalText.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image description is null or empty");
        }

        if (playerGuessed == null || playerGuessed.isEmpty()) {
            return 0;
        } else {
            float chatGPTResult = chatGPT.rateInputs(originalText, playerGuessed);
            int pointsAwarded = chatGPT.convertSimilarityScoreToPoints(chatGPTResult);
            System.out.println("ChatGPT similarity score: " + chatGPTResult);
            return pointsAwarded;
        }
    }

    public String getLastImageDescription() {
        return dallE.getInputPhrase();
    }

    public void playerLeaveGame(Long gameId, String userToken) throws Exception {

        if (userToken == null || userToken.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "UserToken is null or empty");
        }

        if (gameId == null || gameId == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game ID is null or zero");
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

    public void playerLeaveCurrentLobby(String userToken) throws Exception {
        User user = userRepository.findByUserToken(userToken);

        System.out.println("UserToken:");
        System.out.println(userToken);

        if(user == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User with userToken has not been found");
        }

        Game currentLobby = user.getGame();

        this.leaveLobby(currentLobby.getId(), userToken);
    }

    public void gameIsFinishedLeaveLobby(Long lobbyId, String userToken) throws Exception {
        try {
            if (userToken == null || userToken.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User token is null or empty");
            }

            if (lobbyId == null || lobbyId == 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lobby ID is null");
            }

            User user = userRepository.findByUserToken(userToken);
            if (user == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User does not exist");
            }

            Game game = gameRepository.findById(lobbyId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lobby not found"));

            if (!game.getUsers().contains(user)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User is not in the specified lobby");
            }
            game.removePlayer(user);
            gameRepository.save(game);
            user.setGame(null);
            userRepository.save(user);
        } catch (Exception e) {
            System.out.println("Error during leaveLobby: " + e.getMessage());
            throw e;
        }
    }

    public void deleteLobby(Long gameId, String userToken){
        if (userToken == null || userToken.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "UserToken is null or empty");
        }

        if (gameId == null || gameId == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game ID is null or zero");
        }

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lobby not found"));

        User user = userRepository.findByUserToken(userToken);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User with sent userToken does not exist");
        }

        if(!Objects.equals(game.getLobbyOwner(), user.getUsername())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only host is allowed to delete lobby");
        }

        List<User> users = game.getUsers();
        List<User> copyUsers = new ArrayList<>(users);

        for(User player: copyUsers) {
            player.deleteGame(game);
            userRepository.save(player);
        }
        userRepository.flush();

        game.getUsers().clear();

        gameRepository.delete(game);
        gameRepository.flush();

    }

    public void hostRemovePlayerFromLobby(Long gameId, String hostToken, String userToken){
        if (userToken == null || userToken.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "UserToken is null or empty");
        }

        if (hostToken == null || hostToken.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Host Token is null or empty");
        }

        if (gameId == null || gameId == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game ID is null or zero");
        }

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lobby not found"));

        User user = userRepository.findByUserToken(userToken);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User with sent userToken does not exist");
        }

        User host = userRepository.findByUserToken(hostToken);
        if (host == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Host with sent hostToken does not exist");
        }

        if(!Objects.equals(game.getLobbyOwner(), host.getUsername())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only host is allowed to remove player from lobby");
        }

        game.ownerRemovePlayer(user);
        gameRepository.save(game);
        gameRepository.flush();
    }
}
        
