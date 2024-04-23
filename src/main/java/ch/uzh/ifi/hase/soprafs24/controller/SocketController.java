package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.game.Game;
import ch.uzh.ifi.hase.soprafs24.rest.dto.SimpleUserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GameGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.GameService;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Controller
public class SocketController {

    private GameService gameService;

    SocketController(GameService gameService) {
        this.gameService = gameService;
    }

    @MessageMapping("/lobby/join") //-> user needs to send to game/lobby/join
    @SendTo("/game/join") //user needs to listen to /game/public for a response
    public String joinGame(@Payload String message){

        System.out.println("Message received");

        return message;
    }

    @MessageMapping("/lobby/leave")
    @SendTo("/game/public")
    public Message leaveGame(@Payload Message message) {
        System.out.println("Leave message received");
        return message;
    }
    
    @MessageMapping("/continueGame")
    @SendTo("/game/public")
    public SimpleUserGetDTO continueGame(@Payload Long id){
        if(id == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "id is null");
        }

        String nextPictureGenerator = gameService.getNextPictureGenerator(id);
        User newUser = new User(nextPictureGenerator, null);
        return DTOMapper.INSTANCE.convertEntityToSimpleUserGetDTO(newUser);
    }
    


    @MessageMapping("/lobby/startgame")
    @SendTo("/game/public")
    public SimpleUserGetDTO startGame(@Payload Long id){

        if (id == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "id is null");
        }

        gameService.startGameLobby(id);

        String nextPictureGenerator = gameService.getNextPictureGenerator(id);
        User newUser = new User(nextPictureGenerator, null);
        return DTOMapper.INSTANCE.convertEntityToSimpleUserGetDTO(newUser);
    }
}
