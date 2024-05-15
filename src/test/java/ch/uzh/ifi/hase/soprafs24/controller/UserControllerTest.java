package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.game.Game;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;


import java.util.List;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.Optional;


import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyLong;
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

  @Test
  public void givenUsers_whenGetUsers_thenReturnJsonArray() throws Exception {
    // given
    User user = new User();
    user.setUsername("testUsername");
    user.setStatus(UserStatus.OFFLINE);

    List<User> allUsers = Collections.singletonList(user);

    given(userService.getAllUsers()).willReturn(allUsers);

    MockHttpServletRequestBuilder getRequest = get("/users").contentType(MediaType.APPLICATION_JSON);

    // then
    mockMvc.perform(getRequest).andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].username", is(user.getUsername())))
        .andExpect(jsonPath("$[0].status", is(user.getStatus().toString())));
  }

  @Test
  public void createUser_validInput_userCreated() throws Exception {
    // given
    User user = new User();
    user.setId(1L);
    user.setUsername("testUsername");
    user.setPassword("password");
    user.setUserToken("1");
    user.setStatus(UserStatus.ONLINE);

    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setUsername("testUsername");
     userPostDTO.setPassword("password");

    given(userService.registerUser(Mockito.any())).willReturn(user);

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder postRequest = post("/user/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(userPostDTO));

    // then
    mockMvc.perform(postRequest)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(user.getId().intValue())))
        .andExpect(jsonPath("$.username", is(user.getUsername())))
        .andExpect(jsonPath("$.status", is(user.getStatus().toString())));
  }

  @Test
  public void createUser_invalidInput_userNotCreated() throws Exception {
        // given
        User user = new User();
        user.setId(1L);
        user.setUsername("testUsername");
        user.setPassword("password");
        user.setUserToken("1");
        user.setStatus(UserStatus.ONLINE);

        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setUsername("testUsername");
        userPostDTO.setPassword("password");

        given(userService.registerUser(Mockito.any())).willThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already exists"));

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder postRequest = post("/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPostDTO));

        // then
        mockMvc.perform(postRequest)
                .andExpect(status().isBadRequest());
   }

    @Test
    public void loginUser_withValidCredentials_returnsOk() throws Exception {
        // Arrange
        UserPostDTO loginDTO = new UserPostDTO();
        loginDTO.setUsername("validUsername");
        loginDTO.setPassword("validPassword");

        User mockUser = new User();
        mockUser.setUsername("validUsername");
        mockUser.setPassword("validPassword");

        when(userRepository.findByUsername("validUsername")).thenReturn(mockUser);

        // Act & Assert
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(loginDTO)))
                .andExpect(status().isOk());
    }


   @Test
   public void loginUser_withInvalidCredentials_returnsNotFound() throws Exception {
       // Arrange
       UserPostDTO loginDTO = new UserPostDTO();
       loginDTO.setUsername("invalidUsername");
       loginDTO.setPassword("invalidPassword");

       User mockUser = new User();
       mockUser.setUsername("validUsername");
       mockUser.setPassword("validPassword");

       when(userRepository.findByUsername("invalidUsername")).thenReturn(new User());

       // Act & Assert
       mockMvc.perform(post("/login")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(asJsonString(loginDTO)))
               .andExpect(status().isNotFound());
   }


  @Test
  public void logoutUser_withValidUserId_returnsOk() throws Exception {
    final Long userId = 1L;

    mockMvc.perform(post("/users/logout/{id}", userId)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
}

@Test
public void logoutUser_withInvalidUserId_returnsNotFound() throws Exception {
    final Long userId = 1L;

    doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User does not exist"))
            .when(userService).logoutUser(userId);

    mockMvc.perform(post("/users/logout/{id}", userId)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
}

@Test
public void getAllUsers_ReturnsUserList() throws Exception {
    List<User> allUsers = Arrays.asList(new User("user1", "pass1"), new User("user2", "pass2"));
    List<UserGetDTO> allUserDTOs = allUsers.stream()
                                           .map(user -> DTOMapper.INSTANCE.convertEntityToUserGetDTO(user))
                                           .collect(Collectors.toList());

    when(userService.getAllUsers()).thenReturn(allUsers);

    mockMvc.perform(get("/users")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].username", is("user1")))
            .andExpect(jsonPath("$[1].username", is("user2")));
}

@Test
public void getUserById_UserExists_ReturnsUser() throws Exception {
    User user = new User("user1", "pass1");
    user.setId(1L);
    UserGetDTO userDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    mockMvc.perform(get("/users/{id}", 1L)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username", is("user1")));
}

@Test
public void getUserById_UserDoesNotExist_ReturnsNotFound() throws Exception {
    when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

    mockMvc.perform(get("/users/{id}", 1L))
            .andExpect(status().isNotFound());
}




  @Test
    public void joinLobbyAndIdIsNullOrEmpty() throws Exception {
      
       UserPostDTO userPostDTO = new UserPostDTO();
       userPostDTO.setUserToken("valid_token");
       userPostDTO.setUsername("owner");

      String requestBody = asJsonString(userPostDTO);

      MockHttpServletRequestBuilder postRequestEmpty = post("/lobby/join/{id}", "")
              .contentType(MediaType.APPLICATION_JSON)
              .content(requestBody);

      MockHttpServletRequestBuilder postRequestNull = post("/lobby/join/{id}", (String) null)
              .contentType(MediaType.APPLICATION_JSON)
              .content(requestBody);

      mockMvc.perform(postRequestEmpty)
              .andExpect(status().isNotFound());
      mockMvc.perform(postRequestNull)
              .andExpect(status().isNotFound());
    }


    // Test updating a lobby
        @Test
        public void updateLobbyAndIdIsNullOrEmpty() throws Exception {
            // Prepare an empty GamePostDTO
            Game lobby = new Game(1L, "owner");
            
            GamePostDTO GamePostDTO = new GamePostDTO();
            GamePostDTO.setAmtOfRounds(15); // Set a null amtOfRounds
            GamePostDTO.setTimeLimit((float) 15); // Set a null timeLimit
            GamePostDTO.setMaxAmtUsers(10); // Set a null maxAmtUsers
            
            given(gameService.updateGameSettings(null, GamePostDTO)).willReturn(null);
            given(gameService.updateGameSettings(0L, GamePostDTO)).willReturn(null);
            
            // Test for the empty id

                mockMvc.perform(put("/lobby/update/{id}", "")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(GamePostDTO)))
                        .andExpect(status().isNotFound());

        // Test for the null id
        mockMvc.perform(put("/lobby/update/{id}", (String) null)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(GamePostDTO)))
                .andExpect(status().isNotFound());

        }


        @Test
        public void updateLobbyInvalidTimeLimit() throws Exception {
            Long id = 1L;
    
            // time limit is too low
            GamePostDTO gamePostDTOTooLow = new GamePostDTO();
            gamePostDTOTooLow.setMaxAmtUsers(50);
            gamePostDTOTooLow.setTimeLimit(2F);
            gamePostDTOTooLow.setAmtOfRounds(15);
    
            // time limit is too high
            GamePostDTO gamePostDTOTooHigh = new GamePostDTO(); 
            gamePostDTOTooHigh.setMaxAmtUsers(50);
            gamePostDTOTooHigh.setTimeLimit(101F);
            gamePostDTOTooHigh.setAmtOfRounds(15);
    
            given(gameService.updateGameSettings(id, gamePostDTOTooLow))
                    .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Time limit is too low or too high"));
            
            given(gameService.updateGameSettings(id, gamePostDTOTooHigh))
                    .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Time limit is too low or too high"));
            
            mockMvc.perform(put("/lobby/update/{id}", id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(gamePostDTOTooLow)))
                    .andExpect(status().isNotFound());
    
            mockMvc.perform(put("/lobby/update/{id}", id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(gamePostDTOTooHigh)))
                    .andExpect(status().isNotFound());
            }
    
    
            @Test
            public void updateLobbyInvalidAmtOfRounds() throws Exception {
                Long id = 1L;
                GamePostDTO gamePostDTO = new GamePostDTO();
                gamePostDTO.setMaxAmtUsers(50);
                gamePostDTO.setTimeLimit(3F);
                gamePostDTO.setAmtOfRounds(0);
            
                given(gameService.updateGameSettings(id, gamePostDTO))
                        .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Amount of rounds cannot be negative or zero"));
                
                mockMvc.perform(put("/lobby/update/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(gamePostDTO)))
                        .andExpect(status().isNotFound());
            }
    
            @Test
            public void updateLobbyInvalidMaxAmtUsers() throws Exception {
                Long id = 1L;
                GamePostDTO gamePostDTO = new GamePostDTO(); // Invalid time limit
                gamePostDTO.setMaxAmtUsers(1);
                gamePostDTO.setTimeLimit(30F);
                gamePostDTO.setAmtOfRounds(15);
            
                given(gameService.updateGameSettings(id, gamePostDTO))
                        .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Maximum amount of users cannot be less than 2"));
                
                mockMvc.perform(put("/lobby/update/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(gamePostDTO)))
                        .andExpect(status().isNotFound());
            }


  @Test
        public void updateLobbyAndIdDoesNotExists() throws Exception {
            Game lobby = new Game(1L, "owner");
            Long id = lobby.getId();
            User owner = new User();
            owner.setUsername("owner");
            User host = new User();
            host.setUsername("owner");
            lobby.addPlayer(host);
            lobby.setAmtOfRounds(15);
            lobby.setTimeLimit(10);
            lobby.setMaxAmtUsers(50);

            GamePostDTO gamePostDTO = new GamePostDTO();
            gamePostDTO.setAmtOfRounds(15);
            gamePostDTO.setTimeLimit(10F);
            gamePostDTO.setMaxAmtUsers(50);            
            when(gameService.updateGameSettings(id, gamePostDTO)).thenReturn(lobby);
            
            mockMvc.perform(put("/lobby/update/{id}", 123L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(gamePostDTO)))
                    .andExpect(status().isNotFound());
                             
        }

   @Test
   public void logout_WithValidToken_ReturnsOkStatus() throws Exception {
       User user = new User();
       user.setUserToken("valid_token");

       when(userRepository.findByUserToken("valid_token")).thenReturn(user);

       mockMvc.perform(post("/logoutByToken")
               .contentType(MediaType.APPLICATION_JSON)
               .content(asJsonString(user)))
               .andExpect(status().isOk());
   }

   @Test 
   public void logout_WithInvalidToken_ReturnsNotFound() throws Exception {
       User user = new User();
       user.setUserToken("invalid_token");

       when(userService.findByToken("invalid_token")).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User does not exist"));

       mockMvc.perform(post("/logoutByToken")
               .contentType(MediaType.APPLICATION_JSON)
               .content(asJsonString(user)))
               .andExpect(status().isNotFound());        
   }



  @Test
  public void update_user_test() throws Exception {
      User user = new User();
      user.setId(1L);
      user.setPassword("new");
      user.setUsername("new");
      user.setStatus(UserStatus.ONLINE);

//       given(userService.updateUser(1L,user)).willReturn(user);
      given(userRepository.findById(1L)).willReturn(Optional.of(user));

      UserPostDTO userPostDTO = new UserPostDTO();
      userPostDTO.setUsername("updatedUsername");

      mockMvc.perform(put("/users/update/{id}", 1L)
              .contentType(MediaType.APPLICATION_JSON)
              .content(asJsonString(userPostDTO)))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.username", is(user.getUsername())))
              .andExpect(jsonPath("$.status", is(user.getStatus().toString())));
  }

  @Test
  public void update_user_test_invalid_id() throws Exception {
      User user = new User();
      user.setId(1L);
      user.setPassword("new");
      user.setUsername("new");
      user.setStatus(UserStatus.ONLINE);

      given(userRepository.findById(1L)).willReturn(Optional.of(user));

      UserPostDTO userPostDTO = new UserPostDTO();
      userPostDTO.setUsername("updatedUsername");

      mockMvc.perform(put("/users/update/{id}", 0L)
              .contentType(MediaType.APPLICATION_JSON)
              .content(asJsonString(userPostDTO)))
              .andExpect(status().isBadRequest());
  }

  @Test
  public void update_user_test_invalid_user() throws Exception {
      User user = new User();
      user.setId(1L);
      user.setPassword("new");
      user.setUsername("new");
      user.setStatus(UserStatus.ONLINE);

      given(userRepository.findById(1L)).willReturn(Optional.of(user));

      UserPostDTO userPostDTO = new UserPostDTO();
      userPostDTO.setUsername("updatedUsername");

      mockMvc.perform(put("/users/update/{id}", 2L)
              .contentType(MediaType.APPLICATION_JSON)
              .content(asJsonString(userPostDTO)))
              .andExpect(status().isNotFound());
  }

  @Test
   public void loginUser_WithNonExistentUsername_ShouldReturnNotFound() throws Exception {
        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setUsername("nonExistentUsername");
        userPostDTO.setPassword("password");

        when(userRepository.findByUsername("nonExistentUsername")).thenReturn(null); // Ensure username does not exist

        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPostDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void logoutUser_WithNullId_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/users/logout/{id}", 0L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }


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
  @Test
  public void loginUser_WithWrongPassword_ShouldReturnNotFound() throws Exception {
    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setUsername("validUsername");
    userPostDTO.setPassword("wrongPassword");

    User mockUser = new User();
    mockUser.setUsername("validUsername");
    mockUser.setPassword("validPassword");

    when(userRepository.findByUsername("validUsername")).thenReturn(mockUser);

    mockMvc.perform(post("/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(userPostDTO)))
        .andExpect(status().isNotFound());
  }

  @Test
  public void updateUser_WithNullUserId_ShouldReturnBadRequest() throws Exception {
    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setUsername("newUsername");

    mockMvc.perform(put("/users/update/{id}", 0L)
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(userPostDTO)))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void updateUser_WithInvalidUserId_ShouldReturnNotFound() throws Exception {
    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setUsername("newUsername");

    when(userRepository.findById(999L)).thenReturn(Optional.empty());

    mockMvc.perform(put("/users/update/{id}", 999L)
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(userPostDTO)))
        .andExpect(status().isNotFound());
  }

  @Test
  public void updateUser_WithValidData_ShouldUpdateUser() throws Exception {
    User user = new User();
    user.setId(1L);
    user.setUsername("oldUsername");

    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setUsername("newUsername");

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    mockMvc.perform(put("/users/update/{id}", 1L)
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(userPostDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username", is("newUsername")));
  }
  
}
