package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

/**
 * Test class for the UserResource REST resource.
 *
 * @see UserService
 */
@WebAppConfiguration
@SpringBootTest
public class UserServiceIntegrationTest {

  @Autowired
  private UserService userService;

  @MockBean
  private UserRepository userRepository;

  @Test
  public void createUser_validInputs_success() {    

    User testUser = new User();
    testUser.setUsername("testUsername");
    testUser.setPassword("testPassword");
    testUser.setId(1L);

    // when
    User createdUser = userService.registerUser(testUser);

    // then
    assertEquals(testUser.getId(), createdUser.getId());
    assertEquals(testUser.getUsername(), createdUser.getUsername());
    assertNotNull(createdUser.getUserToken());
    assertEquals(UserStatus.ONLINE, createdUser.getStatus());
  }

  @Test
  public void createUser_duplicateUsername_throwsException() {
    User testUser = new User();
    testUser.setUsername("testUsername");
    testUser.setPassword("testPassword");

        // Simulate the first registration
    when(userRepository.save(any(User.class))).thenReturn(testUser);
    userService.registerUser(testUser);

        // Attempt to register again with the same username
    when(userRepository.findByUsername(testUser.getUsername())).thenReturn(new User());
    assertThrows(ResponseStatusException.class, () -> userService.registerUser(testUser));
  }

  @Test
  public void loginUser_validInputs_success() {
    User testUser = new User();
    testUser.setUsername("testUsername");
    testUser.setPassword("testPassword");
    testUser.setStatus(UserStatus.ONLINE);
    testUser.setIsLoggedIn(true);
    testUser.setId(1L);
    testUser.setUserToken("testToken");

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

    userService.loginUser(testUser);

    User loggedInUser = userRepository.findById(testUser.getId()).orElseThrow();

    assertEquals(UserStatus.ONLINE, loggedInUser.getStatus());
    assertTrue(loggedInUser.getIsLoggedIn());
}

  @Test
  public void loginUser_invalidInputs_throwsException() throws Exception{
    User testUser = new User();
    testUser.setUsername("testUsername");
    testUser.setPassword("testPassword");
    testUser.setId(1L);

    User createdUser = userService.registerUser(testUser);

    createdUser.setId(2L);

    assertThrows(ResponseStatusException.class, () -> userService.loginUser(createdUser));
  }

  @Test
  public void logoutUser_validInputs_success() {
    User testUser = new User();
    testUser.setUsername("testUsername");
    testUser.setPassword("testPassword");
    testUser.setId(1L);
    testUser.setStatus(UserStatus.ONLINE);

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

    userService.logoutUser(testUser);

    assertEquals(UserStatus.OFFLINE, testUser.getStatus());
  }

  @Test
  public void logoutUser_invalidInputs_throwsException() throws Exception{
    User testUser = new User();
    testUser.setUsername("testUsername");
    testUser.setPassword("testPassword");
    testUser.setId(1L);

    User createdUser = userService.registerUser(testUser);

    createdUser.setId(2L);

    assertThrows(ResponseStatusException.class, () -> userService.logoutUser(createdUser));
  }

  @Test
  public void logoutUser_invalidInputs_UserNotInRepo_throwsException() {
    User testUser = new User();
    testUser.setUsername("testUsername");
    testUser.setPassword("testPassword");
    testUser.setId(1L);

    assertThrows(ResponseStatusException.class, () -> userService.logoutUser(testUser));
  }

  @Test
  public void updatedUser_validInputs_success() throws Exception {
    User testUser = new User();
    testUser.setUsername("testUsername");
    testUser.setPassword("testPassword");
    testUser.setId(1L);

    User updatedUser = new User();
    updatedUser.setUsername("updatedUsername");
    updatedUser.setPassword("updatedPassword");
    updatedUser.setId(1L);

    when(userRepository.findById(1L)).thenReturn(Optional.of(updatedUser));

    User newUserUpdated = userService.updateUser(1L, updatedUser);

    assertEquals(updatedUser.getUsername(), newUserUpdated.getUsername());
  }

  @Test
  public void logoutUser_WithValidId_ShouldReturnUser() {
    User testUser = new User();
    testUser.setUsername("testUsername");
    testUser.setPassword("testPassword");
    testUser.setId(1L);
    testUser.setStatus(UserStatus.ONLINE);
    testUser.setIsLoggedIn(true);

    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

    userService.logoutUser(testUser);

    assertEquals(testUser.getStatus(), UserStatus.OFFLINE);
  }

  @Test
  public void logoutUser_WithInvalidId_ShouldThrowResponseStatusException() {
    User testUser = new User();
    testUser.setUsername("testUsername");
    testUser.setPassword("testPassword");
    testUser.setId(1L);

    when(userRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(ResponseStatusException.class, () -> userService.logoutUser(testUser));
  }
}