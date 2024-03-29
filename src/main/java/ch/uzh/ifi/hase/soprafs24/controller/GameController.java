package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.service.GameService;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.game.lobby.Lobby;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GamePostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class GameController {

    private final GameService gameService;
    

    GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/lobby/create")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public Lobby createGame(@RequestBody String userToken) throws Exception {
        if(userToken == null || userToken.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "userToken is null or empty");
        }
        Lobby lobby = gameService.createLobby(userToken);

        if(lobby == null){
            throw new Exception("newly created lobby is null");
        }
        return lobby;
    }

    @PutMapping("lobby/update/{lobbyId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ResponseBody
    public void updateLobby(GamePostDTO gamePostDTO){

    }

    @PostMapping("/lobby/join/{lobbyId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Lobby joinGame(@PathVariable String lobbyId, @RequestBody UserPostDTO userPostDTO) {
        User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);
        Lobby lobby = gameService.joinGame(lobbyId, userInput);
        if (lobby != null) {
            System.out.println("Successfully joined game lobby with Id: " + lobby.getLobbyId());
            return lobby;
        } else {
            System.out.println("Error joining game: No user matches token");
            return null;
        }
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
    public Lobby getLobby(@PathVariable String lobbyId) {
        Lobby lobby = gameService.findByLobbyId(lobbyId);
        if (lobby != null) {
            System.out.println("Game lobby successfully found with Id: " + lobby.getLobbyId());
            return lobby;
        } else {
            System.out.println("Error finding game: No lobby matches Id");
            return null;
        }
    }


    @PutMapping("/lobby/update/{lobbyId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Lobby updateGame(@PathVariable String lobbyId, @RequestBody GamePostDTO gamePostDTO) {
        Lobby lobby = gameService.updateGame(lobbyId, gamePostDTO);
        if (lobby != null) {
            System.out.println("Game lobby successfully updated with Id: " + lobby.getLobbyId());
            return lobby;
        } else {
            // Assuming a null game means the userToken was invalid or another issue occurred
            System.out.println("Error updating game: No user matches token");
            return null;
        }
    }
}
