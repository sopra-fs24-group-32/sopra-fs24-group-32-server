package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;

import java.util.Optional;
import java.util.UUID;
import java.util.List;


public class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private UserService userService;

  private User testUser;

  @BeforeEach
  public void setup() {
    MockitoAnnotations.openMocks(this);

    // given
    testUser = new User();
    testUser.setId(1L);
    // testUser.setName("testName");
    testUser.setUsername("testUsername");
    testUser.setPassword("testPassword");

    // when -> any object is being save in the userRepository -> return the dummy
    // testUser
    Mockito.when(userRepository.save(Mockito.any())).thenReturn(testUser);
  }

  @Test
  public void createUser_validInputs_success() {
    // when -> any object is being save in the userRepository -> return the dummy
    // testUser
    User createdUser = userService.registerUser(testUser);

    // then
    Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());

    assertEquals(testUser.getId(), createdUser.getId());
    // assertEquals(testUser.getName(), createdUser.getName());
    assertEquals(testUser.getUsername(), createdUser.getUsername());
    assertNotNull(createdUser.getUserToken());
    assertEquals(UserStatus.ONLINE, createdUser.getStatus());
  }

  @Test
  public void createUser_duplicateName_throwsException() {
    // given -> a first user has already been created
    userService.registerUser(testUser);

    // when -> setup additional mocks for UserRepository
    // Mockito.when(userRepository.findByName(Mockito.any())).thenReturn(testUser);
    Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(testUser);

    // then -> attempt to create second user with same user -> check that an error
    // is thrown
    assertThrows(ResponseStatusException.class, () -> userService.registerUser(testUser));
  }

  @Test
  public void createUser_duplicateInputs_throwsException() {
    // given -> a first user has already been created
    userService.registerUser(testUser);

    // when -> setup additional mocks for UserRepository
    // Mockito.when(userRepository.findByName(Mockito.any())).thenReturn(testUser);
    Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(testUser);

    // then -> attempt to create second user with same user -> check that an error
    // is thrown
    assertThrows(ResponseStatusException.class, () -> userService.registerUser(testUser));
  }

  @Test
    public void loginUser_existingUser_userStatusUpdated() {
        // given
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));

        // when
        userService.loginUser(testUser);

        // then
        verify(userRepository, times(1)).save(testUser);
        assertEquals(UserStatus.ONLINE, testUser.getStatus());
    }

    @Test
    public void logoutUser_existingUser_userStatusUpdated() {
        // given
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));

        // when
        userService.logoutUser(testUser);

        // then
        verify(userRepository, times(1)).save(testUser);
        assertEquals(UserStatus.OFFLINE, testUser.getStatus());
    }

    @Test
    public void findByToken_existingToken_userReturned() {
        // given
        String token = UUID.randomUUID().toString();
        testUser.setUserToken(token);
        when(userRepository.findAll()).thenReturn(List.of(testUser));

        // when
        User foundUser = userService.findByToken(token);

        // then
        assertNotNull(foundUser);
        assertEquals(testUser.getId(), foundUser.getId());
    }


    // I add some updateUserTests as soon as I merge the branch with func that also updates mail + picture
    //@Test
    //public void updateUser_validInput_userUpdated() {
    //    // given
    //    when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
    //    User updatedUser = new User();
    //    updatedUser.setUsername("newUsername");
    //
    //    // when
    //    try {
    //        User result = userService.updateUser(1, updatedUser);
    //
    //        // then
    //        verify(userRepository, times(1)).save(testUser);
    //        assertEquals(updatedUser.getUsername(), result.getUsername());
    //    } catch (Exception e) {
    //        // Handle any exceptions thrown during the test
    //        fail("Exception thrown: " + e.getMessage());
    //    }
    //}
    

    @Test
    public void getUserById_existingId_userReturned() {
        // given
        when(userRepository.findAll()).thenReturn(List.of(testUser));

        // when
        User result = userService.getUserById(1);

        // then
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
    }

//   @Test
//     public void logout_user_test() throws Exception {
//       userService.createUser(testUser);

//       User u = userService.logoutUser(Math.toIntExact(new Long(testUser.getId())));
//       assertEquals(UserStatus.OFFLINE,u.getStatus());
//   }

  

}
