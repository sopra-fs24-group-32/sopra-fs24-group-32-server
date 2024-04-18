package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.game.Game;
import ch.uzh.ifi.hase.soprafs24.rest.dto.ChatGPTPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GameGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.service.GameService;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GamePostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
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
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;


@RestController
public class GameWebSocketController {

    Logger log = LoggerFactory.getLogger(GameWebSocketController.class);

    private final GameService gameService;
    private final UserService userService;

    private final UserRepository userRepository;
    private final GameRepository gameRepository;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    GameWebSocketController(GameService gameService, UserService userService, UserRepository userRepository, GameRepository gameRepository,
                            SimpMessagingTemplate simpMessagingTemplate) {
        this.gameService = gameService;
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
        this.userService = userService;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    // @MessageMapping("/lobby/create")  // client.send("/app/lobby/create", {}, JSON.stringify(userToken))
    // public ResponseEntity<GameGetDTO> createGame(String userToken) {
    //     try {
    //         log.info("Request to create new game with userToken: " + userToken);
    //         Game createdLobby = gameService.createLobby(userToken);
    //         GameGetDTO lobbyGetDTO = DTOMapper.INSTANCE.convertEntityToGameGetDTO(createdLobby);
    //         simpMessagingTemplate.convertAndSend("/topic/lobby/create", lobbyGetDTO);
    //         log.info("Game created successfully");
    //         return new ResponseEntity<>(lobbyGetDTO, HttpStatus.CREATED);
    //     } catch (ResponseStatusException e) {
    //         throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User does not exist");
    //     } catch (Exception e) {
    //         throw new RuntimeException("Could not create lobby:", e);
    //     }
    // }

    @PostMapping("/lobby/create")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public ResponseEntity<GameGetDTO> createGame(@RequestBody String userToken) {
    try {
        User user = userRepository.findByUserToken(userToken);
        Game createdLobby = gameService.createLobby(userToken);
        GameGetDTO lobbyGetDTO = DTOMapper.INSTANCE.convertEntityToGameGetDTO(createdLobby);
        return new ResponseEntity<>(lobbyGetDTO, HttpStatus.CREATED);
    } catch (ResponseStatusException e) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userToken is null or empty");
    } catch (Exception e) {
        throw new RuntimeException("Could not create lobby:", e);
        }
    }   

    // @PutMapping("lobby/update/{id}")
    // @ResponseStatus(HttpStatus.NO_CONTENT)
    // @ResponseBody
    // public void updateLobby(GamePostDTO gamePostDTO){

    // }

   @PostMapping("/lobby/join/{invitationCodes}")
   @ResponseStatus(HttpStatus.OK)
   @ResponseBody
   public Game joinLobby(@PathVariable String invitationCodes, @RequestBody String userToken) throws Exception{
       if (userToken == null || userToken.isEmpty()) {
           throw new ResponseStatusException(HttpStatus.NOT_FOUND, "userToken is null or empty");
       }

       if(invitationCodes == null || invitationCodes.isEmpty()){
           throw new ResponseStatusException(HttpStatus.NOT_FOUND, "invitationCodes is null or empty");
       }

       Game game = gameService.joinLobby(invitationCodes, userToken);

       return game;
   }

    @PostMapping("/lobby/leave/{lobbyId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Game leaveLobby(@PathVariable Long lobbyId, @RequestBody String userToken) throws Exception {
        if (userToken == null || userToken.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User token is null or empty");
        }

        if (lobbyId == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lobby ID is null");
        }

        return gameService.leaveLobby(lobbyId, userToken);
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
        return gameRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lobby not found"));
    }

    // @MessageMapping("/lobby/{id}")
    // public ResponseEntity<GameGetDTO> getLobby(@DestinationVariable Long id) {

    //     if (id == null || id == 0) {
    //         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game ID is null or empty");
    //     }

    //     try {
    //         log.info("Request to get lobby with id: " + id);
    //         Game lobby = gameService.findById(id);

    //         if (lobby == null) {
    //             throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lobby not found");
    //         }

    //         GameGetDTO lobbyGetDTO = DTOMapper.INSTANCE.convertEntityToGameGetDTO(lobby);
    //         simpMessagingTemplate.convertAndSend("/topic/lobby/" + id, lobby);
    //         log.info("Lobby found successfully");
    //         return new ResponseEntity<>(lobbyGetDTO, HttpStatus.OK);
    //     } catch (Exception e) {
    //         throw new RuntimeException("Could not get lobby:", e);
    //     }
    // }

   @PutMapping("/lobby/update/{id}")
   @ResponseStatus(HttpStatus.CREATED)
   @ResponseBody
   public Game updateGameSettings(@PathVariable Long id, @RequestBody GamePostDTO gamePostDTO, @RequestHeader("userToken") String userToken) throws ResponseStatusException, JsonMappingException, JsonProcessingException {

       if (userToken == null || userToken.isEmpty()) {
           throw new ResponseStatusException(HttpStatus.NOT_FOUND, "userToken is null or empty");
       }

       if (id == null || id == 0) {
           throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game ID is null or empty");
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
              throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
       }
    }

    @PostMapping("/lobby/start/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Game startGame(@PathVariable Long id, @RequestHeader("userToken") String userToken) throws ResponseStatusException, JsonMappingException, JsonProcessingException {

        if (userToken == null || userToken.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userToken is null or empty");
        }

        if (id == null || id == 0) {
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
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Image prompt provided by the player is null or empty");
        }

        if (gameId == null || gameId == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "gameId is null or empty");
        }

        Optional<Game> lobby = gameRepository.findById(gameId);

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

        if (gameId == null || gameId == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "gameId is null or empty");
        }

        Optional<Game> lobby = gameRepository.findById(gameId);
        if (!lobby.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found");
        }

        String pictureGenerated =  gameService.getImageGeneratedByDallE();

        if (pictureGenerated == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Failed to get image with DALL-E");
        }

        return new ResponseEntity<>(pictureGenerated, HttpStatus.OK);
    }

    @PutMapping("/game/chatgpt/{gameId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<UserGetDTO> evaluateGuessesByChatGPT(@PathVariable Long gameId, @RequestBody ChatGPTPostDTO chatGPTPostDTO, @RequestHeader("userToken") String userToken) throws Exception {

        if (gameId == null || gameId == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "gameId is null or empty");
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

        UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
        return new ResponseEntity<>(userGetDTO, HttpStatus.CREATED);
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
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "userToken is null or empty");
        }

        if (gameId == null || gameId == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "gameId is null or empty");
        }

        Optional<Game> lobby = gameRepository.findById(gameId);
        if (!lobby.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found");
        }
        // Remove the user from the game
        gameService.playerLeaveGame(gameId, mappedToken);
    }

}