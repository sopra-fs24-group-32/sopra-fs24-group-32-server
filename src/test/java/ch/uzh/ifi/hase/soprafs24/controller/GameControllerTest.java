package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.game.lobby.Lobby;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.service.GameService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GameController.class)
public class GameControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private UserService userService;

  @MockBean
  private GameService gameService;

  @MockBean
  private UserRepository userRepository;

  @InjectMocks
  private UserController userController;

  @MockBean
  private GameRepository gameRepository;

  @InjectMocks
   private GameController gameController;

  @BeforeEach
  public void setUp(){
    MockitoAnnotations.openMocks(this);
  }

    @Test
    public void joinLobbyAndLobbyIdDoesNotExists() throws Exception{
      String userToken = "random";
      String invitationCodes = "-1";

      when(gameRepository.findByLobbyInvitationCode(invitationCodes)).thenReturn(null);
      doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND))
              .when(gameService)
              .joinLobby(invitationCodes, userToken);

      MockHttpServletRequestBuilder postRequest = post("/lobby/join/{invitationCodes}", invitationCodes)
              .contentType(MediaType.APPLICATION_JSON)
              .content(userToken);

      mockMvc.perform(postRequest)
              .andExpect(status().isNotFound());

    }


    // Test creating a lobby
    @Test
    public void createLobbyAndUserTokenIsNullOrEmpty() throws Exception {
        // Prepare an empty UserPostDTO
        User emptyUser = new User();
        emptyUser.setUserToken(""); // Set an empty userToken
        
        User nullUser = new User();
        nullUser.setUserToken(null); // Set a null userToken

        given(gameService.createLobby("")).willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "userToken is null or empty"));
        given(gameService.createLobby(null)).willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "userToken is null or empty"));
        
        // Test for the empty userToken
        mockMvc.perform(post("/lobby/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(emptyUser)))
                .andExpect(status().isNotFound());        
        // Test for the null userToken
        mockMvc.perform(post("/lobby/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(nullUser)))
                .andExpect(status().isNotFound());
    }


    @Test
    public void createLobbyWithValidUserToken() throws Exception {
        User user = new User(); // Assuming User has a setter for username
        String userToken = "valid_token";
        String username = "owner";
        long lobbyId = 1L;
        user.setUserToken(userToken);
        user.setUsername(username);

        Lobby lobby = new Lobby(lobbyId, username); // Assuming Lobby has an appropriate constructor

        given(userRepository.findByUserToken(userToken)).willReturn(user);
        given(gameService.createLobby(userToken)).willReturn(lobby);

        mockMvc.perform(post("/lobby/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(lobbyId));
    }


//     @Test
//     public void updateLobbyWithValidParameters() throws Exception {
//         // Ensure the lobbyId is a consistent, literal value.
//         final String lobbyId = "roomId1";
//         GamePostDTO validGamePostDTO = new GamePostDTO();
//         validGamePostDTO.setTimeLimit(20); // Assume valid time limit
//         validGamePostDTO.setAmtOfRounds(10); // Assume valid amount of rounds
//         validGamePostDTO.setMaxAmtPlayers(10); // Assume valid max amount of players

//         // Ensure the Lobby object matches what your service is expected to return.
//         Lobby updatedLobby = new Lobby(1L, "owner");
//         updatedLobby.setTimeLimit(20);
//         updatedLobby.setAmtOfRounds(10);
//         updatedLobby.setMaxAmtPlayers(10);

//         // Ensure the findByLobbyId and updateGame methods are correctly mocked.
//         // Assuming findByLobbyId is needed to first fetch the lobby before update
//         given(gameRepository.findByLobbyId(lobbyId)).willReturn(updatedLobby);
//         given(gameService.updateGame(lobbyId, validGamePostDTO)).willReturn(updatedLobby);


//         mockMvc.perform(put("/lobby/update/{lobbyId}", lobbyId)
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(asJsonString(validGamePostDTO)))
//                 .andExpect(status().isOk());
//     }


        // Utility method to convert an object to JSON string
        private String asJsonString(Object object) {
                try {
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.writeValueAsString(object);
                } catch (Exception e) {
                throw new RuntimeException(e);
                }
        }
    
}
