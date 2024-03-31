package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.game.lobby.Lobby;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UserControllerTest
 * This is a WebMvcTest which allows to test the UserController i.e. GET/POST
 * request without actually sending them over the network.
 * This tests if the UserController works.
 */
@WebMvcTest(UserController.class)
public class UserControllerTest {

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

  // @Test
  // public void givenUsers_whenGetUsers_thenReturnJsonArray() throws Exception {
  //   // given
  //   User user = new User();
  //   user.setUsername("testUsername");
  //   user.setStatus(UserStatus.OFFLINE);

  //   List<User> allUsers = Collections.singletonList(user);

  //   // this mocks the UserService -> we define above what the userService should
  //   // return when getUsers() is called
  //   given(userService.getUsers()).willReturn(allUsers);

  //   // when
  //   MockHttpServletRequestBuilder getRequest = get("/users").contentType(MediaType.APPLICATION_JSON);

  //   // then
  //   mockMvc.perform(getRequest).andExpect(status().isOk())
  //       .andExpect(jsonPath("$", hasSize(1)))
  //       .andExpect(jsonPath("$[0].username", is(user.getUsername())))
  //       .andExpect(jsonPath("$[0].status", is(user.getStatus().toString())));
  // }

  @Test
  public void createUser_validInput_userCreated() throws Exception {
    // given
    User user = new User();
    user.setId(1L);
    user.setUsername("testUsername");
    user.setUserToken("1");
    user.setStatus(UserStatus.ONLINE);

    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setUsername("testUsername");

    given(userService.createUser(Mockito.any())).willReturn(user);

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder postRequest = post("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(userPostDTO));

    // then
    mockMvc.perform(postRequest)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(user.getId().intValue())))
        .andExpect(jsonPath("$.username", is(user.getUsername())))
        .andExpect(jsonPath("$.status", is(user.getStatus().toString())));
  }


  // @Test
  // public void getAllUsers_ReturnsUserList() throws Exception {
  //     List<User> allUsers = Arrays.asList(testUser);
  //     given(userService.getUsers()).willReturn(allUsers);

  //     mockMvc.perform(get("/users")
  //             .contentType(MediaType.APPLICATION_JSON))
  //             .andExpect(status().isOk())
  //             .andExpect(jsonPath("$[0].username", is(testUser.getUsername())))
  //             .andExpect(jsonPath("$[0].status", is(testUser.getStatus().toString())));
  //   }

    // @Test
    // public void login_user() throws Exception {
    //   // given
    //   User user = new User();
    //   user.setId(1L);
    //   user.setPassword("password");
    //   user.setUsername("testUsername");
    //   user.setToken("1");
    //   user.setStatus(UserStatus.ONLINE);
  
    //   UserPostDTO userPostDTO = new UserPostDTO();
    //   userPostDTO.setUsername("testUsername");
    //   userPostDTO.setPassword("password");
  
    //   // given(userService.createUser(Mockito.any())).willReturn(user);
    //   given(userService.getUsers()).willReturn(Arrays.asList(user));
  
    //   // when/then -> do the request + validate the result
    //   MockHttpServletRequestBuilder postRequest = post("/users/login")
    //           .contentType(MediaType.APPLICATION_JSON)
    //           .content(asJsonString(userPostDTO));
  
    //   // then
    //   mockMvc.perform(postRequest)
    //           .andExpect(status().isOk())
    //           .andExpect(jsonPath("$.username").value("testUsername"));
    // }


  // @Test
  // public void update_user_test() throws Exception {
  //     User user = new User();
  //     user.setPassword("new");
  //     user.setUsername("new");
  //     user.setStatus(UserStatus.OFFLINE);

  //     given(userService.updateUser(1,user)).willReturn(user);

  //     mockMvc.perform(MockMvcRequestBuilders
  //             .put("/users/{id}", 1)
  //             .content(asJsonString((user)))
  //             .contentType(MediaType.APPLICATION_JSON)
  //             .accept(MediaType.APPLICATION_JSON))
  //             .andExpect(status().isOk())
  //             .andExpect(MockMvcResultMatchers.jsonPath("$.username").value("new"));
  // }

  // @Test
  //   public void updateUser_ReturnsUpdatedUser() throws Exception {
  //       int userId = 1; // Example user ID
  //       User inputUser = new User(); // Assume User is your model class. Initialize it properly.
  //       inputUser.setUsername("Roger"); // Example property, adjust according to your User class.

  //       User updatedUser = new User();
  //       Date specificDate = new Date();
  //       updatedUser.setUsername("Roger Updated");
  //       updatedUser.setBirthDay(specificDate);

  //       Mockito.when(userService.updateUser(Mockito.eq(userId), Mockito.any(User.class))).thenReturn(updatedUser); // (4)

  //       mockMvc.perform(MockMvcRequestBuilders.put("/users/{id}", userId)
  //               .contentType(MediaType.APPLICATION_JSON)
  //               .content(objectMapper.writeValueAsString(inputUser))) // (5)
  //               .andExpect(MockMvcResultMatchers.status().isOk())
  //               .andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(updatedUser))); // (6)

  //       Mockito.verify(userService).updateUser(Mockito.eq(userId), Mockito.any(User.class)); // (7)
  //   }

  // @Test
  //   public void logoutUser_ReturnsOkStatus() throws Exception {
  //       int userId = 1; // Example user ID

  //       mockMvc.perform(MockMvcRequestBuilders.post("/users/logout/{id}", userId)
  //               .contentType(MediaType.APPLICATION_JSON))
  //               .andExpect(MockMvcResultMatchers.status().isOk());

  //       // Verify that userService.logoutUser was called with the correct parameter
  //       Mockito.verify(userService).logoutUser(userId); // (3)
  //   }

  /**
   * Helper Method to convert userPostDTO into a JSON string such that the input
   * can be processed
   * Input will look like this: {"name": "Test User", "username": "testUsername"}
   * 
   * @param object
   * @return string
   */
  private String asJsonString(final Object object) {
    try {
      return new ObjectMapper().writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          String.format("The request body could not be created.%s", e.toString()));
    }
  }
}
