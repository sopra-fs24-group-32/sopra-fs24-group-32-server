package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.service.GameService;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.game.lobby.Lobby;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GamePostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;

import java.util.Map;

import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class GameController {

    private final GameService gameService;
    private UserService userService;


    GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/lobby/create")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public Lobby createGame(@RequestBody UserPostDTO userPostDTO) throws Exception {
        User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);
        String userToken = userInput.getUserToken();
        if(userToken == null || userToken.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "userToken is null or empty");
        }
        Lobby lobby = gameService.createLobby(userToken);

        if(lobby == null){
            throw new Exception("newly created lobby is null");
        }
        return lobby;
    }

    // @PutMapping("lobby/update/{lobbyId}")
    // @ResponseStatus(HttpStatus.NO_CONTENT)
    // @ResponseBody
    // public void updateLobby(GamePostDTO gamePostDTO){

    // }

    @PostMapping("/lobby/join/{lobbyId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ResponseBody
    public void joinLobby(@PathVariable String lobbyId, @RequestBody String userToken) throws Exception{
        if (userToken == null || userToken.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "userToken is null or empty");
        }

        if(lobbyId == null || lobbyId.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "lobbyId is null or empty");
        }

        gameService.joinLobby(lobbyId, userToken);
    }


    @GetMapping("/lobby/create")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Map<Long, Lobby> getAllLobby() {
        return gameService.getAllLobbies();
    }

    @GetMapping("/lobby/{lobbyId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Lobby getLobby(@PathVariable String lobbyId) throws Exception{
        Lobby lobby = gameService.findByLobbyId(lobbyId);

        if (lobby == null) {
            throw new Exception("Lobby not found");
        }

        return lobby;
    }

    @PutMapping("/lobby/update/{lobbyId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Lobby updateGame(@PathVariable String lobbyId, @RequestBody GamePostDTO gamePostDTO) throws Exception{
        Lobby lobby = gameService.updateGame(lobbyId, gamePostDTO);

        if (lobby == null) {
            throw new Exception("Lobby Id: " + lobbyId + " not found");
        }
        return lobby;
    }

    @PutMapping("/lobby/update/{lobbyId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ResponseBody
    public Lobby updateGameSettings(@PathVariable String lobbyId, @RequestBody GamePostDTO gamePostDTO, @RequestHeader("userToken") String userToken) throws Exception {

        Lobby lobby = gameService.findByLobbyId(lobbyId);
        if (lobby == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lobby not found");
        }
        //ensure user is the host
        User user = userService.findByToken(userToken);
        if (!user.getUsername().equals(lobby.getLobbyOwner())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the lobby host can update settings");
        }

        return gameService.updateGameSettings(lobbyId, gamePostDTO);
    }
}
