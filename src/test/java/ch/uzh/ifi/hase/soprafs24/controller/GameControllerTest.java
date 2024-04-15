// package ch.uzh.ifi.hase.soprafs24.controller;

// import ch.uzh.ifi.hase.soprafs24.entity.User;
// import ch.uzh.ifi.hase.soprafs24.game.Game;
// import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
// import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
// import ch.uzh.ifi.hase.soprafs24.rest.dto.GamePostDTO;
// import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
// import ch.uzh.ifi.hase.soprafs24.service.GameService;
// import ch.uzh.ifi.hase.soprafs24.service.UserService;
// import java.util.Optional;

// import com.fasterxml.jackson.databind.ObjectMapper;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.InjectMocks;
// import org.mockito.MockitoAnnotations;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.MediaType;
// import org.springframework.test.web.servlet.MockMvc;
// import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
// import org.springframework.web.server.ResponseStatusException;

// import static org.mockito.BDDMockito.given;
// import static org.mockito.Mockito.doReturn;
// import static org.mockito.Mockito.doThrow;
// import static org.mockito.Mockito.mock;
// import static org.mockito.Mockito.when;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// @WebMvcTest(GameWebSocketController.class)
// public class GameControllerTest {

//   @Autowired
//   private MockMvc mockMvc;

//   @Autowired
//   private ObjectMapper objectMapper;

//   @MockBean
//   private UserService userService;

//   @MockBean
//   private GameService gameService;

//   @MockBean
//   private GameRepository gameRepository;

//   @MockBean
//   private UserRepository userRepository;

//   @InjectMocks
//   private UserController userController;

//   @InjectMocks
//    private GameWebSocketController gameController;

//   @BeforeEach
//   public void setUp(){
// //     MockitoAnnotations.openMocks(this);
// //     userRepository = mock(UserRepository.class);
// //         userService = mock(UserService.class);
// //         gameRepository = mock(GameRepository.class);
//   }

//     @Test
//     public void joinLobbyAndIdDoesNotExists() throws Exception{
//       String id = "1";
//       String userToken = "random";

//       doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND))
//               .when(gameService)
//               .joinLobby(id, userToken);

//       MockHttpServletRequestBuilder postRequest = post("/join/lobby/{id}", id)
//               .contentType(MediaType.APPLICATION_JSON)
//               .content(userToken);

//       mockMvc.perform(postRequest)
//               .andExpect(status().isNotFound());

//     }


// //     // Test creating a lobby
//     @Test
//     public void createLobbyAndUserTokenIsNullOrEmpty() throws Exception {
//         // Prepare an empty UserPostDTO
//         User emptyUser = new User();
//         emptyUser.setUsername("owner");
//         emptyUser.setUserToken(""); // Set an empty userToken
        
//         User nullUser = new User();
//         nullUser.setUsername("ownerNull");
//         nullUser.setUserToken(null); // Set a null userToken

//         String emptyToken = "{\"userToken\":\"\"}";
//         String nullToken = "{\"userToken\":null}";

//         when(userRepository.findByUserToken("")).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User does not exists"));
//         when(userRepository.findByUserToken(null)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User does not exists"));
        
//         given(gameService.createLobby(emptyToken)).willThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "userToken is null or empty"));
//         given(gameService.createLobby(nullToken)).willThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "userToken is null or empty"));
        
//         // Test for the empty userToken
//         mockMvc.perform(post("/lobby/create")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(emptyToken))
//                 .andExpect(status().isBadRequest());
        
//         // Test for the null userToken
//         mockMvc.perform(post("/lobby/create")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(nullToken))
//                 .andExpect(status().isBadRequest());
//     }


//     @Test
//     public void createLobbyWithValidUserToken() throws Exception {
//         String userToken = "valid_token";
//         String username = "owner";
//         long id = 1L;

//         User user = new User(); // Assuming User has a setter for username
//         user.setUsername(username);
//         user.setUserToken(userToken);

//         Game lobby = new Game(id, username); // Assuming Lobby has an appropriate constructor

//         given(userRepository.findByUserToken(userToken)).willReturn(user);
//         given(gameService.createLobby(userToken)).willReturn(lobby);

//         mockMvc.perform(post("/lobby/create")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(userToken))
//                 .andExpect(status().isCreated());
//         }


        // @Test
        // public void updateLobbyWithValidParameters() throws Exception {
        //     // Ensure the id is a consistent, literal value.
        //     Long id = 1L;
        //     GamePostDTO validGamePostDTO = new GamePostDTO();
        //     validGamePostDTO.setTimeLimit(20F); // Assume valid time limit
        //     validGamePostDTO.setAmtOfRounds(10); // Assume valid amount of rounds
        //     validGamePostDTO.setMaxAmtUsers(10); // Assume valid max amount of users
    
        //     Game lobby = new Game(id, "owner");
    
        //     // Ensure the Lobby object matches what your service is expected to return.
        //     Game updatedLobby = new Game(1L, "owner");
        //     updatedLobby.setTimeLimit(20F);
        //     updatedLobby.setAmtOfRounds(10);
        //     updatedLobby.setMaxAmtUsers(10);

        // //     String validArgument = "{\"timeLimit\":20.0,\"amtOfRounds\":10,\"maxAmtUsers\":10}";
    
        //     Optional<Game> optionalLobby = Optional.of(lobby);
    
        //     // Mocking findById to return the Optional<Game> with the lobby
        //     doReturn(optionalLobby).when(gameRepository).findById(id);
    
        //     // Mocking updateGameSettings method of gameService to return the updatedLobby
        //     doReturn(updatedLobby).when(gameService).updateGameSettings(id, validGamePostDTO);

        //     System.out.println("updated time limit----------------------------------: " + updatedLobby.getTimeLimit());
    
        //     // Perform the PUT request
        //     mockMvc.perform(put("/lobby/update/{id}", id)
        //             .contentType(MediaType.APPLICATION_JSON)
        //             .content(asJsonString(validGamePostDTO)))
        //             .andExpect(status().isOk());
        // }
    


        // Utility method to convert an object to JSON string
        // private String asJsonString(Object object) {
        //         try {
        //         ObjectMapper objectMapper = new ObjectMapper();
        //         return objectMapper.writeValueAsString(object);
        //         } catch (Exception e) {
        //         throw new RuntimeException(e);
        //         }
        // }
    
// }