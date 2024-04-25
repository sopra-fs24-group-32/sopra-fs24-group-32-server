package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.game.Game;
import ch.uzh.ifi.hase.soprafs24.rest.dto.SimpleUserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GameGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.GameService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
// import io.grpc.xds.shaded.io.envoyproxy.envoy.config.rbac.v2.Permission.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;

import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class SocketController {

    private final GameService gameService;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;
    private final GameRepository gameRepository;


    @Autowired
    public SocketController(GameService gameService, UserService userService, SimpMessagingTemplate messagingTemplate, GameRepository gameRepository) {
        this.gameService = gameService;
        this.messagingTemplate = messagingTemplate;
        this.userService = userService;
        this.gameRepository = gameRepository;
    }

    @MessageMapping("/lobby/join")
    @SendTo("/game/join")
    public UserGetDTO joinGame(@Payload String userToken) {
        //log the userToken
        System.out.println("User joined: " + userToken);
        try {
            User user = userService.findByToken(userToken);
            //log the user
            System.out.println("User joined: " + user);
            // Broadcast to all subscribers that a new user has joined
            return DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Join failed: " + e.getMessage(), e);
        }
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

        Game game = gameRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lobby not found"));

        gameService.startGameLobby(id);

        String nextPictureGenerator = gameService.getNextPictureGenerator(id);
        if (nextPictureGenerator == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "All rounds have been played. Game is over.");
        }
        User newUser = new User(nextPictureGenerator, null);
        return DTOMapper.INSTANCE.convertEntityToSimpleUserGetDTO(newUser);
    }
}
