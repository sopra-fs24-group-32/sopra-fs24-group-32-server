package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.game.Game;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.service.GameService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class SocketController {

    private GameService gameService;

    SocketController(GameService gameService) {
        this.gameService = gameService;
    }

    @MessageMapping("/lobby/join") //-> user needs to send to game/lobby/join
    @SendTo("/game/public") //user needs to listen to /game/public for a response
    public Message joinGame(@Payload Message message){

        System.out.println("Message received");

        return message;
    }


    /*
    @MessageMapping("game/role")
    @SendTo("game/{userId}")
    public Role getRole(@Payload String userId, @DestinationVariable String userId){

    }

     */
}
