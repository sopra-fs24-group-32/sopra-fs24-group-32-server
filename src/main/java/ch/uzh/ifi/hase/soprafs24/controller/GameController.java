package ch.uzh.ifi.hase.soprafs24.controller;
import ch.uzh.ifi.hase.soprafs24.service.GameService;
import ch.uzh.ifi.hase.soprafs24.game.Game;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.entity.User;

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
    public Game createGame(@RequestBody UserPostDTO userPostDTO) {
        User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);
        String userToken = userInput.getUserToken();
        Game game = gameService.createGame(userToken);
        if (game != null) {
            return game;
        } else {
            // Assuming a null game means the userToken was invalid or another issue occurred
            System.out.println("Error creating game: No user matches token");
            return null;
        }
    }

    public class UserTokenRequest {
        private String userToken;
    
        // Getter
        public String getUserToken() {
            return userToken;
        }
    
        // Setter
        public void setUserToken(String userToken) {
            this.userToken = userToken;
        }
    }
    
}
