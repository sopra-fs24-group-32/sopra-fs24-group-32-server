package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.game.Game;
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

import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.Id;

@RestController
public class GameController {

    private final GameService gameService;
    private UserService userService;

    private final UserRepository userRepository;
    private final GameRepository gameRepository;

    GameController(GameService gameService, UserRepository userRepository, GameRepository gameRepository) {
        this.gameService = gameService;
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
    }

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
        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
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


    @GetMapping("/lobbies")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<Game> getAllLobbies() {
        List<Game> games = gameRepository.findAll();
        return games;
    }

    @GetMapping("/lobby/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Game getLobby(@PathVariable Long id) throws ResponseStatusException{
        return gameRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lobby not found"));
    }

//    @PutMapping("/lobby/update/{id}")
//    @ResponseStatus(HttpStatus.NO_CONTENT)
//    @ResponseBody
//    public Game updateGameSettings(@PathVariable String id, @RequestBody GamePostDTO gamePostDTO, @RequestHeader("userToken") String userToken) throws ResponseStatusException {
//
//        Game lobby = gameRepository.findById(id);
//        if (lobby == null) {
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lobby not found");
//        }
//        //ensure user is the host
//        User user = userRepository.findByUserToken(userToken);
//        if (!user.getUsername().equals(lobby.getLobbyOwner())) {
//            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the lobby host can update settings");
//        }
//
//        return gameService.updateGameSettings(id, gamePostDTO);
//    }
}
