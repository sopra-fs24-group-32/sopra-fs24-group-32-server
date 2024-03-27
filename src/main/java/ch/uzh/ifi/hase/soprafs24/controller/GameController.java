package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.service.GameService;
import ch.uzh.ifi.hase.soprafs24.game.lobby.Lobby;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GamePostDTO;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class GameController {

    private final GameService gameService;
    

    GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/lobby/create")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public Lobby createGame(@RequestBody GamePostDTO gamePostDTO) {
        Lobby lobby = gameService.createGame(gamePostDTO);
        String lobbyId = lobby.getLobbyId();
        if (lobbyId != null) {
            System.out.println("Game lobby successfully created with Id: " + lobbyId);
            return lobby;
        } else {
            // Assuming a null game means the userToken was invalid or another issue occurred
            System.out.println("Error creating game: No user matches token");
            return null;
        }
    }


    @GetMapping("/lobby/create")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Map<Long, Lobby> getAllLobby() {
        return gameService.getAllLobbies();
    }
    
}
