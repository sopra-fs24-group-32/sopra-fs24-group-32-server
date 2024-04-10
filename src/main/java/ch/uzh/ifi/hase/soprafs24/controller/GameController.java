package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.service.GameService;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.game.lobby.Lobby;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GamePostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;

import java.util.Map;

import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
    //    public ResponseEntity<LobbyGetDTO> createLobby(@RequestBody LobbyPostDTO lobbyPostDTO) {
//        try {
//            Lobby lobbyInput = DTOMapper.INSTANCE.convertLobbyPostDTOtoEntity(lobbyPostDTO);
//            Lobby createdLobby = lobbyService.registerLobby(lobbyInput);
//            LobbyGetDTO lobbyGetDTO = DTOMapper.INSTANCE.convertEntityToLobbyGetDTO(createdLobby);
//            return new ResponseEntity<>(lobbyGetDTO, HttpStatus.CREATED);
//        } catch (ResponseStatusException e) {
//            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
//        }
//    }

    // @PutMapping("lobby/update/{lobbyId}")
    // @ResponseStatus(HttpStatus.NO_CONTENT)
    // @ResponseBody
    // public void updateLobby(GamePostDTO gamePostDTO){

    // }

    @PostMapping("/lobby/join/{invitationCodes}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ResponseBody
    public Lobby joinLobby(@PathVariable String invitationCodes, @RequestBody String userToken) throws Exception{
        if (userToken == null || userToken.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "userToken is null or empty");
        }

        if(invitationCodes == null || invitationCodes.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "invitationCodes is null or empty");
        }

        Lobby lobby = gameService.joinLobby(invitationCodes, userToken);

        return lobby;
    }




    @GetMapping("/lobby/{lobbyId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Lobby getLobby(@PathVariable String lobbyId) throws ResponseStatusException{
        Lobby lobby = gameRepository.findByLobbyId(lobbyId);

        if (lobby == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lobby not found");
        }

        return lobby;
    }

    @PutMapping("/lobby/update/{lobbyId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ResponseBody
    public Lobby updateGameSettings(@PathVariable String lobbyId, @RequestBody GamePostDTO gamePostDTO, @RequestHeader("userToken") String userToken) throws ResponseStatusException {

        Lobby lobby = gameRepository.findByLobbyId(lobbyId);
        if (lobby == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lobby not found");
        }
        //ensure user is the host
        User user = userRepository.findByUserToken(userToken);
        if (!user.getUsername().equals(lobby.getLobbyOwner())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the lobby host can update settings");
        }

        return gameService.updateGameSettings(lobbyId, gamePostDTO);
    }
}
