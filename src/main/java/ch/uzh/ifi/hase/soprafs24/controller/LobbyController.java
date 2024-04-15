package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.rest.dto.GameGetDTO;
import ch.uzh.ifi.hase.soprafs24.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/lobby")
public class LobbyController {

    private final GameService gameService;

    @Autowired
    public LobbyController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<GameGetDTO> getLobby(@PathVariable Long gameId) {
        GameGetDTO game = gameService.getGame(gameId);
        return ResponseEntity.ok(game);
    }
}
