package ch.uzh.ifi.hase.soprafs24.ws;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.game.Game;
import ch.uzh.ifi.hase.soprafs24.rest.dto.SimpleUserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.GameService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
// import io.grpc.xds.shaded.io.envoyproxy.envoy.config.rbac.v2.Permission.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;

@Controller
public class WSController {

    private final GameService gameService;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;
    private final GameRepository gameRepository;


    @Autowired
    public WSController(GameService gameService, UserService userService, SimpMessagingTemplate messagingTemplate, GameRepository gameRepository) {
        this.gameService = gameService;
        this.messagingTemplate = messagingTemplate;
        this.userService = userService;
        this.gameRepository = gameRepository;
    }

    private void assertGameIdNotNull(String gameId){
        if(gameId == null || gameId.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Path Variable 'gameId' is null or empty");
        }
    }

    @MessageMapping("/lobby/join/{gameId}")
    @SendTo("/game/join/{gameId}")
    public UserGetDTO joinGame(@DestinationVariable String gameId, @Payload String userToken) {

        this.assertGameIdNotNull(gameId);

        if(userToken == null || userToken.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "userToken is null or empty");
        }
        try {
            User user = userService.findByToken(userToken);
            return DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Join failed: " + e.getMessage(), e);
        }
    }

    @MessageMapping("/continueGame/{gameId}")
    @SendTo("/game/public/{gameId}")
    public SimpleUserGetDTO continueGame(@DestinationVariable String gameId, @Payload Long id){
        if(id == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "id is null");
        }

        this.assertGameIdNotNull(gameId);

        gameService.resetDallEsImageURL();

        String nextPictureGenerator = gameService.getNextPictureGenerator(id);
        User newUser = new User(nextPictureGenerator, null);
        return DTOMapper.INSTANCE.convertEntityToSimpleUserGetDTO(newUser);
    }

    @MessageMapping("/lobby/startgame/{gameId}")
    @SendTo("/game/public/{gameId}")
    public SimpleUserGetDTO startGame(@DestinationVariable String gameId, @Payload Long id){

        if (id == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "id is null");
        }

        this.assertGameIdNotNull(gameId);

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
