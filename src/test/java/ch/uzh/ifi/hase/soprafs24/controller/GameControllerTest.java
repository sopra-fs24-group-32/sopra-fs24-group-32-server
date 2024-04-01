package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.game.lobby.Lobby;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GamePostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.GameService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import java.beans.Transient;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class GameControllerTest {

    @Autowired
  private MockMvc mockMvc;

  private User testUser;
  private UserGetDTO testUserGetDTO;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private UserService userService;

  @MockBean
  private GameService gameService;

  @MockBean
  private UserRepository userRepository;

  @InjectMocks
  private UserController userController;

  @InjectMocks
   private GameController gameController;

  @BeforeEach
  public void setUp(){
    MockitoAnnotations.openMocks(this);
  }

  @BeforeEach
    public void setup() {
        // Setup test data
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUsername");
        testUser.setStatus(UserStatus.ONLINE);

        testUserGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(testUser);
    }


    //Test joining a lobby
    @Test
    public void joinLobbyAndLobbyIdIsNullOrEmpty() throws Exception {
      MockHttpServletRequestBuilder postRequestEmpty = post("/lobby/join/{lobbyId}", "")
              .contentType(MediaType.APPLICATION_JSON)
              .content("random_token");

      MockHttpServletRequestBuilder postRequestNull = post("/lobby/join/{lobbyId}", (String) null)
              .contentType(MediaType.APPLICATION_JSON)
              .content("random_token");

      mockMvc.perform(postRequestEmpty)
              .andExpect(status().isNotFound());
      mockMvc.perform(postRequestNull)
              .andExpect(status().isNotFound());
    }

    @Test
    public void joinLobbyAndLobbyIdDoesNotExists() throws Exception{
      String lobbyId = "1";
      String userToken = "random";

      doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND))
              .when(gameService)
              .joinLobby(lobbyId, userToken);

      MockHttpServletRequestBuilder postRequest = post("/join/lobby/{lobbyId}", lobbyId)
              .contentType(MediaType.APPLICATION_JSON)
              .content(userToken);

      mockMvc.perform(postRequest)
              .andExpect(status().isNotFound());

    }


    // Test creating a lobby
    @Test
    public void createLobbyAndUserTokenIsNullOrEmpty() throws Exception {
        // Prepare an empty UserPostDTO
        UserPostDTO emptyUserPostDTO = new UserPostDTO();
        emptyUserPostDTO.setUserToken(""); // Set an empty userToken
        
        UserPostDTO nullUserPostDTO = new UserPostDTO();
        nullUserPostDTO.setUserToken(null); // Set a null userToken
        
        given(gameService.createLobby("")).willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "userToken is null or empty"));
        given(gameService.createLobby(null)).willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "userToken is null or empty"));
        
        // Test for the empty userToken
        mockMvc.perform(post("/lobby/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(emptyUserPostDTO)))
                .andExpect(status().isNotFound());
        
        // Test for the null userToken
        mockMvc.perform(post("/lobby/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(nullUserPostDTO)))
                .andExpect(status().isNotFound());
    }


//     @Test
//     public void createLobbyAndUserTokenExists() throws Exception {
//         // Prepare a UserPostDTO with a valid userToken
//         UserPostDTO userPostDTO = new UserPostDTO();
//         userPostDTO.setUserToken("valid_token");
//         userPostDTO.setUsername("owner");

//         // Define the behavior of gameService.createLobby() to return a valid Lobby
//         given(gameService.createLobby("valid_token")).willReturn(new Lobby(1L, "owner"));

//         // Perform the POST request to /lobby/create with the valid UserPostDTO
//         mockMvc.perform(post("/lobby/create")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(asJsonString(userPostDTO)))
//                 .andExpect(status().isCreated()); // Assert that the response status is CREATED (HTTP status code 201)
//         }
    
    // Utility method to convert an object to JSON string
    private String asJsonString(Object object) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    // Test updating a lobby
        @Test
        public void updateLobbyAndLobbyIdIsNullOrEmpty() throws Exception {
            // Prepare an empty GamePostDTO
            GamePostDTO emptyGamePostDTO = new GamePostDTO();
            emptyGamePostDTO.setAmtOfRounds(0); // Set an empty amtOfRounds
            emptyGamePostDTO.setTimeLimit(0); // Set an empty timeLimit
            emptyGamePostDTO.setMaxAmtPlayers(0); // Set an empty maxAmtPlayers
            
            GamePostDTO nullGamePostDTO = new GamePostDTO();
            nullGamePostDTO.setAmtOfRounds(0); // Set a null amtOfRounds
            nullGamePostDTO.setTimeLimit(0); // Set a null timeLimit
            nullGamePostDTO.setMaxAmtPlayers(0); // Set a null maxAmtPlayers
            
            given(gameService.updateGame("", emptyGamePostDTO)).willReturn(null);
            given(gameService.updateGame(null, nullGamePostDTO)).willReturn(null);
            
            // Test for the empty lobbyId

                mockMvc.perform(put("/lobby/update/{lobbyId}", "")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(emptyGamePostDTO)))
                        .andExpect(status().isNotFound());

        // Test for the null lobbyId
        mockMvc.perform(post("/lobby/update/{lobbyId}", (String) null)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(nullGamePostDTO)))
                .andExpect(status().isNotFound());

        }

        @Test
        public void updateLobbyAndLobbyIdDoesNotExists() throws Exception {
            Lobby lobby = new Lobby(1L, "owner");
            String lobbyId = lobby.getLobbyId();
            Player host = new Player();
            host.setUsername("owner");
            lobby.addPlayer(host);
            lobby.setLobbyId(lobbyId);
            lobby.setAmtOfRounds(15);
            lobby.setTimeLimit(10);
            lobby.setMaxAmtPlayers(50);

            GamePostDTO gamePostDTO = new GamePostDTO();
            gamePostDTO.setAmtOfRounds(15);
            gamePostDTO.setTimeLimit(10);
            gamePostDTO.setMaxAmtPlayers(50);            
            when(gameService.updateGame(lobbyId, gamePostDTO)).thenReturn(lobby);
            
            mockMvc.perform(put("/lobby/update/{lobbyId}", "invalidLobbyId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(gamePostDTO)))
                    .andExpect(status().isNotFound());
                             
        }


//         @Test
//         public void updateLobbyAndLobbyIdExists() throws Exception {
//         Lobby lobby = new Lobby(1L, "owner");

//         String lobbyId = "1"; // Assuming the Lobby ID is a String. Adjust based on your actual Lobby class.
//         Player host = new Player();
//         host.setUsername("owner");
//         lobby.addPlayer(host);
//         lobby.setLobbyId(lobbyId);
//         lobby.setAmtOfRounds(15);
//         lobby.setTimeLimit(10);
//         lobby.setMaxAmtPlayers(50);

//         Lobby initLobby = new Lobby(1L, "owner");

//         GamePostDTO gamePostDTO = new GamePostDTO();
//         gamePostDTO.setAmtOfRounds(15);
//         gamePostDTO.setTimeLimit(10);
//         gamePostDTO.setMaxAmtPlayers(50);
        
//         when(gameService.updateGame(lobbyId, gamePostDTO)).thenReturn(lobby);

//         mockMvc.perform(put("/lobby/update/{lobbyId}", lobbyId)
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(asJsonString(gamePostDTO))) // Ensure you have a method to convert objects to JSON string
//                 .andExpect(status().isOk());
//     }


    @Test
    public void updateLobbyInvalidTimeLimit() throws Exception {
        String lobbyId = "1";

        // time limit is too low
        GamePostDTO gamePostDTOTooLow = new GamePostDTO();
        gamePostDTOTooLow.setMaxAmtPlayers(50);
        gamePostDTOTooLow.setTimeLimit(3);
        gamePostDTOTooLow.setAmtOfRounds(15);

        // time limit is too high
        GamePostDTO gamePostDTOTooHigh = new GamePostDTO(); 
        gamePostDTOTooHigh.setMaxAmtPlayers(50);
        gamePostDTOTooHigh.setTimeLimit(101);
        gamePostDTOTooHigh.setAmtOfRounds(15);

        given(gameService.updateGame(lobbyId, gamePostDTOTooLow))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Time limit is too low or too high"));
        
        given(gameService.updateGame(lobbyId, gamePostDTOTooHigh))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Time limit is too low or too high"));
        
        mockMvc.perform(put("/lobby/update/{lobbyId}", lobbyId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(gamePostDTOTooLow)))
                .andExpect(status().isNotFound());

        mockMvc.perform(put("/lobby/update/{lobbyId}", lobbyId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(gamePostDTOTooHigh)))
                .andExpect(status().isNotFound());
        }


        @Test
        public void updateLobbyInvalidAmtOfRounds() throws Exception {
            String lobbyId = "1";
            GamePostDTO gamePostDTO = new GamePostDTO();
            gamePostDTO.setMaxAmtPlayers(50);
            gamePostDTO.setTimeLimit(3);
            gamePostDTO.setAmtOfRounds(0);
        
            given(gameService.updateGame(lobbyId, gamePostDTO))
                    .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Amount of rounds cannot be negative or zero"));
            
            mockMvc.perform(put("/lobby/update/{lobbyId}", lobbyId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(gamePostDTO)))
                    .andExpect(status().isNotFound());
        }

        @Test
        public void updateLobbyInvalidMaxAmtPlayers() throws Exception {
            String lobbyId = "1";
            GamePostDTO gamePostDTO = new GamePostDTO(); // Invalid time limit
            gamePostDTO.setMaxAmtPlayers(1);
            gamePostDTO.setTimeLimit(30);
            gamePostDTO.setAmtOfRounds(15);
        
            given(gameService.updateGame(lobbyId, gamePostDTO))
                    .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Maximum amount of players cannot be less than 2"));
            
            mockMvc.perform(put("/lobby/update/{lobbyId}", lobbyId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(gamePostDTO)))
                    .andExpect(status().isNotFound());
        }

        // @Test
        // public void updateLobby_Success() throws Exception {
        //         String lobbyId = "1";
        //         GamePostDTO gamePostDTO = new GamePostDTO(); // Invalid time limit
        //         gamePostDTO.setMaxAmtPlayers(100);
        //         gamePostDTO.setTimeLimit(30);
        //         gamePostDTO.setAmtOfRounds(15);
        //         Lobby lobby = new Lobby();
        //         lobby.setLobbyId(lobbyId);
        //         lobby.setMaxAmtPlayers(100);
        //         lobby.setTimeLimit(30);
        //         lobby.setAmtOfRounds(15);
        //         lobby.setOwner("owner");
                
        //         given(gameService.updateGame(lobbyId, gamePostDTO)).willReturn(lobby);
                
        //         mockMvc.perform(put("/lobby/update/{lobbyId}", lobbyId)
        //                 .contentType(MediaType.APPLICATION_JSON)
        //                 .content(asJsonString(gamePostDTO)))
        //                 .andExpect(status().isOk());
        //     }
    
}
