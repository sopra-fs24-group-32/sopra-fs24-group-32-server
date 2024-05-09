package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.game.Game;
import ch.uzh.ifi.hase.soprafs24.rest.dto.ChatGPTPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GameGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.service.GameService;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GamePostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import ch.uzh.ifi.hase.soprafs24.service.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

// import simp messaging template


@RestController
public class GameController {

    Logger log = LoggerFactory.getLogger(GameController.class);

    private final GameService gameService;
    private final UserService userService;

    private final UserRepository userRepository;
    private final GameRepository gameRepository;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    private ObjectMapper mapper;


    GameController(GameService gameService, UserService userService, UserRepository userRepository, GameRepository gameRepository) {
        this.gameService = gameService;
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
        this.userService = userService;
        this.mapper = new ObjectMapper();
    }

    private void assertGameIdNotNull(Long gameId){
        if(gameId == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Path variable gameId is Null");
        }
    }

    private void assertUserTokenNotNull(String userToken){
        if(userToken == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userToken is null");
        }
    }

    @PostMapping("/lobby/create")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public ResponseEntity<GameGetDTO> createGame(@RequestBody GamePostDTO gamePostDTO, @RequestHeader("userToken") String userToken) {
    try {
        User user = userRepository.findByUserToken(userToken);
        Game createdLobby = gameService.createLobby(userToken, gamePostDTO);
        GameGetDTO lobbyGetDTO = DTOMapper.INSTANCE.convertEntityToGameGetDTO(createdLobby);
        return new ResponseEntity<>(lobbyGetDTO, HttpStatus.CREATED);
    } catch (ResponseStatusException e) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userToken is null or empty");
    } catch (Exception e) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to create the lobby. Reason: " + e.getMessage());
        }
    }   

   @PostMapping("/lobby/join/{invitationCodes}")
   @ResponseStatus(HttpStatus.OK)
   @ResponseBody
   public Game joinLobby(@PathVariable String invitationCodes, @RequestBody String userToken) throws Exception{
       if (userToken == null || userToken.isEmpty()) {
           throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userToken is null or empty");
       }

       if(invitationCodes == null || invitationCodes.isEmpty()){
           throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invitationCodes is null or empty");
       }

       Game game = gameService.joinLobby(invitationCodes, userToken);

        return game;
   }
   
   @PostMapping("/lobby/leave/{lobbyId}")
   @ResponseStatus(HttpStatus.OK)
   @ResponseBody
   public Game leaveLobby(@PathVariable Long lobbyId, @RequestBody String userTokenJson) throws Exception {
       System.out.println("user Token:" + userTokenJson);
       
       if (userTokenJson == null || userTokenJson.isEmpty()) {
           System.out.println("User token is null or empty");
           throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User token is null or empty");
       }
   
       if (lobbyId == null || lobbyId == 0) {
           throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lobby ID is null");
       }
   
       // Parse the userToken from JSON
       ObjectMapper objectMapper = new ObjectMapper();
       JsonNode jsonNode = objectMapper.readTree(userTokenJson);
       String userToken = jsonNode.get("userToken").asText();
   
       System.out.println("User token:" + userToken);
       User user = userService.findByToken(userToken);
       
       // Convert the user who left to a DTO to be sent to the subscribed clients
       UserGetDTO userLeft = DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
       simpMessagingTemplate.convertAndSend("/game/leave/" + lobbyId, userLeft);
   
       System.out.println("Request to leave lobby with id: " + lobbyId + " by user: " + user.getUsername() + " with token: " + userToken);
       Game game = gameService.leaveLobby(lobbyId, userToken);
   
       return game;
   }
   


    @GetMapping("/lobbies")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<Game> getAllLobbies() {;
        return gameService.getAllGames();
    }

    @GetMapping("/lobby/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Game getLobby(@PathVariable Long id) throws ResponseStatusException{
        if (id == null || id == 0 || id.toString().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game ID is null or empty");
        }
        return gameRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lobby not found"));
    }

   @PutMapping("/lobby/update/{id}")
   @ResponseStatus(HttpStatus.OK)
   @ResponseBody
   public Game updateGameSettings(@PathVariable Long id, @RequestBody GamePostDTO gamePostDTO, @RequestHeader("userToken") String userToken) throws ResponseStatusException, JsonMappingException, JsonProcessingException {

       if (userToken == null || userToken.isEmpty()) {
           throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userToken is null or empty");
       }

       if (id == null || id == 0 || id.toString().isEmpty()) {
           throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game ID is null or empty");
       }

       User user = userRepository.findByUserToken(userToken);
       if (user == null) {
           throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User does not exist");
       }

       Game lobby = gameRepository.findById(id)
               .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lobby not found"));

       if (!user.getUsername().equals(lobby.getLobbyOwner())) {
           throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the lobby host can update settings");
       }

       
       try {
            Game updatedGame = gameService.updateGameSettings(id, gamePostDTO);
            return updatedGame;
       } catch (Exception e) {
              throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to update lobby. Reason: " + e.getMessage());
       }
    }

    @PostMapping("/lobby/start/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Game startGame(@PathVariable Long id, @RequestHeader("userToken") String userToken) throws ResponseStatusException, JsonMappingException, JsonProcessingException {

        if (userToken == null || userToken.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userToken is null or empty");
        }

        if (id == null || id == 0 || id.toString().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game ID is null or empty");
        }

        //ensure user is the host
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> map = objectMapper.readValue(userToken, Map.class);
        // Extract the userToken from the Map
        String mappedToken = map.get("userToken");

        User user = userRepository.findByUserToken(mappedToken);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User does not exist");
        }

        Game lobby = gameRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lobby not found"));

        if (!user.getUsername().equals(lobby.getLobbyOwner())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the lobby host can start the game");
        }

        return gameService.startGameLobby(id);
    }

    @PostMapping("/game/image/{gameId}")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public ResponseEntity<String> generatePictureDallE(@PathVariable Long gameId, @RequestBody String text_prompt) throws Exception {

        if (text_prompt == null || text_prompt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image prompt provided by the player is null or empty");
        }

        if (gameId == null || gameId == 0 || gameId.toString().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "gameId is null or empty");
        }

        Optional<Game> lobby = gameRepository.findById(gameId);
        if (!lobby.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found");
        }

        String pictureGenerated =  gameService.generatePictureDallE(text_prompt);

        if (pictureGenerated == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Failed to generate image with DALL-E");
        }

        return new ResponseEntity<>(pictureGenerated, HttpStatus.CREATED);
    }

    @GetMapping("/game/image/{gameId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<String> getPictureGeneratedByDallE(@PathVariable Long gameId) throws Exception {

        if (gameId == null || gameId == 0 || gameId.toString().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "gameId is null or empty");
        }

        Optional<Game> lobby = gameRepository.findById(gameId);
        if (!lobby.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found");
        }

        String pictureGenerated =  gameService.getImageGeneratedByDallE();

        if (pictureGenerated == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to get image with DALL-E");
        }
        return new ResponseEntity<>(pictureGenerated, HttpStatus.OK);
    }

    @PutMapping("/game/chatgpt/{gameId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<UserGetDTO> evaluateGuessesByChatGPT(@PathVariable Long gameId, @RequestBody ChatGPTPostDTO chatGPTPostDTO, @RequestHeader("userToken") String userToken) throws Exception {

        if (gameId == null || gameId == 0 || gameId.toString().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "gameId is null or empty");
        }

        Optional<Game> lobby = gameRepository.findById(gameId);
        if (!lobby.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found");
        }

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> map = objectMapper.readValue(userToken, Map.class);
        // Extract the userToken from the Map
        String mappedToken = map.get("userToken");

        User user = userRepository.findByUserToken(mappedToken);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User does not exist");
        }

        // Point awarded for correct guess (with ChatGPT evaluation)
        String playerGuessed = chatGPTPostDTO.getPlayerGuessed();
        float timeGuessSubmitted = chatGPTPostDTO.getTimeGuessSubmitted();
        int score =  gameService.evaluatePlayerGuessWithChatGPT(playerGuessed);

        // Scale the score based on the time taken to submit the guess
        int scaledScore = lobby.get().scalePointsByDuration(score, timeGuessSubmitted);

        // Update the user's score
        // user.setScore(user.getScore() + scaledScore);
        user.updatedScore(scaledScore);
        userRepository.save(user);
        userRepository.flush();

        System.out.print("Player name: " + user.getUsername() + " Time taken to submit guess: " + timeGuessSubmitted + " Guess: " + playerGuessed + " ChatGPT Score: " + score + " Scaled score: " + scaledScore);

        UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
        return new ResponseEntity<>(userGetDTO, HttpStatus.CREATED);
    }

    @GetMapping("/game/lastDescription/{gameId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<String> getLastImageDescription(@PathVariable Long gameId) throws Exception {
        if (gameId == null || gameId == 0 || gameId.toString().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game ID is null or empty");
        }

        Optional<Game> gameOptional = gameRepository.findById(gameId);
        if (!gameOptional.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found");
        }

        Game game = gameOptional.get();
        String lastDescription = gameService.getLastImageDescription();

        if (lastDescription == null || lastDescription.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(lastDescription, HttpStatus.OK);
    }

    @PostMapping("/game/leave/{gameId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void playerLeaveTheGame(@PathVariable Long gameId, @RequestBody String userToken) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> map = objectMapper.readValue(userToken, Map.class);
        // Extract the userToken from the Map
        String mappedToken = map.get("userToken");
        if (mappedToken == null || mappedToken.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userToken is null or empty");
        }

        if (gameId == null || gameId == 0 || gameId.toString().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "gameId is null or empty");
        }

        Optional<Game> lobby = gameRepository.findById(gameId);
        if (!lobby.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found");
        }
        // Remove the user from the game
        gameService.playerLeaveGame(gameId, mappedToken);
    }

    @PostMapping("/lobby/leaveCurrentLobby")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void playerLeaveHisCurrentLobby(@RequestBody String jsonUserToken) throws Exception {

        this.assertUserTokenNotNull(jsonUserToken);

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> map = objectMapper.readValue(jsonUserToken, Map.class);
        // Extract the userToken from the Map
        String userToken = map.get("userToken");

        if (userToken == null || userToken.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userToken is null or empty");
        }

        gameService.playerLeaveCurrentLobby(userToken);
    }

    @PostMapping("/finishedGame/leave/{gameId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void playerLeavesLobbyAfterGame(@PathVariable Long gameId, @RequestBody String userToken) throws Exception {
        this.assertGameIdNotNull(gameId);
        this.assertUserTokenNotNull(userToken);

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> map = objectMapper.readValue(userToken, Map.class);
        // Extract the userToken from the Map
        String mappedToken = map.get("userToken");
        if (mappedToken == null || mappedToken.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userToken is null or empty");
        }

        if (gameId == null || gameId == 0 || gameId.toString().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "gameId is null or empty");
        }
        gameService.gameIsFinishedLeaveLobby(gameId, mappedToken);
    }

    @PostMapping("/deleteLobby/{gameId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void deleteLobbyLobbyAfterGame(@PathVariable Long gameId, @RequestBody String userToken) throws Exception {
        this.assertGameIdNotNull(gameId);
        this.assertUserTokenNotNull(userToken);

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> map = objectMapper.readValue(userToken, Map.class);
        // Extract the userToken from the Map
        String mappedToken = map.get("userToken");
        if (mappedToken == null || mappedToken.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userToken is null or empty");
        }

        if (gameId == null || gameId == 0 || gameId.toString().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "gameId is null or empty");
        }
        gameService.deleteLobby(gameId, mappedToken);
    }

    @PostMapping("/resetImageURL")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void resetImageURL() throws Exception {
        gameService.resetDallEsImageURL();
    }

    @PostMapping("/hostRemovePlayer/{gameId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void hostRemovePlayer(@PathVariable Long gameId, @RequestHeader("userToken") String hostToken, @RequestBody String playerToken) throws Exception {
    
        // Reading playerToken JSON to extract the user token
        Map<String, String> map = this.mapper.readValue(playerToken, Map.class);
        String mappedToken = map.get("userToken");
        if (mappedToken == null || mappedToken.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userToken is null or empty");
        }
    
        // Reading hostToken JSON to extract the host user token
        ObjectMapper hostObjectMapper = new ObjectMapper();
        Map<String, String> hostMap = hostObjectMapper.readValue(hostToken, Map.class);
        String hostMappedToken = hostMap.get("userToken");
        if (hostMappedToken == null || hostMappedToken.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "hostToken is null or empty");
        }
    
        if (gameId == null || gameId == 0 || gameId.toString().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game ID is null or zero");
        }
    
        // Find user by token and convert to DTO
        User user = userService.findByToken(mappedToken);
        UserGetDTO userKicked = DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
        
        // Updating the topic to include the gameId
        simpMessagingTemplate.convertAndSend("/game/kick/" + gameId, userKicked);
    
        // Performing the kick operation
        gameService.hostRemovePlayerFromLobby(gameId, hostMappedToken, mappedToken);
    }
    

}