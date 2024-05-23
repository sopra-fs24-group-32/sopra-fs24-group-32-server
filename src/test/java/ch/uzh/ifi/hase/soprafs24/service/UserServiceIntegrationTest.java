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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
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
    void updateUser_UserNotFound_ThrowsException() {
        // Arrange
        Long nonExistentId = 999L;
        User dummyUser = new User();
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> userService.updateUser(nonExistentId, dummyUser),
        "User not found");
    }

  @Test
    void updateUser_SuccessfullyUpdatesUser() throws Exception {
        // Arrange
        Long userId = 1L;
        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setUsername("oldUsername");
        existingUser.setEmail("old@example.com");

        User updatedUser = new User();
        updatedUser.setUsername("newUsername");
        updatedUser.setEmail("new@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser); // Mock saving user

        // Act
        User result = userService.updateUser(userId, updatedUser);

        // Assert
        assertNotNull(result);
        assertEquals("newUsername", result.getUsername());
        assertEquals("new@example.com", result.getEmail());
        verify(userRepository).save(any(User.class)); // Ensure save was called
    }

  @Test
  public void updateUser_ValidBirthDate() throws Exception {
        // Arrange
        Long userId = 1L;
        User existingUser = new User();
        LocalDate localDate = LocalDate.of(2000, 11, 4); // java.time.LocalDate uses 1-based month indexing
        Date validBirthDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    
        User updatedUser = new User();
        updatedUser.setBirthDay(validBirthDate);
    
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);
    
        // Act
        User result = userService.updateUser(userId, updatedUser);
    
        // Assert
        assertEquals(validBirthDate, result.getBirthDay());
        verify(userRepository).save(any(User.class));
    }

  @Test
  public void updateUser_EmptyOrNullFields_DoNotOverwrite() throws Exception {
        // Arrange
        Long userId = 1L;
        User existingUser = new User();
        existingUser.setUsername("existingUsername");
        existingUser.setEmail("existing@example.com");
    
        User updatedUser = new User();
        updatedUser.setUsername("");  // Attempt to overwrite with empty username
        updatedUser.setEmail(null);   // Attempt to overwrite with null email
    
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);
    
        // Act
        User result = userService.updateUser(userId, updatedUser);
    
        // Assert
        assertEquals("existingUsername", result.getUsername());
        assertEquals("existing@example.com", result.getEmail());
        verify(userRepository).save(any(User.class));
    }
  
  @Test
  public void updateUser_InvalidEmailFormat_ThrowsException() {
      // Arrange
      Long userId = 1L;
      User updatedUser = new User();
      updatedUser.setEmail("bademail");

      when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));

      // Act & Assert
      assertThrows(ResponseStatusException.class, () -> userService.updateUser(userId, updatedUser));
  }

  @Test
  public void updateUser_InvalidBirthDate_ThrowsException() {
    // Arrange
    Long userId = 1L;
    User updatedUser = new User();
    updatedUser.setBirthDay(Date.from(LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()));

    when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));

    // Act & Assert
    assertThrows(ResponseStatusException.class, () -> userService.updateUser(userId, updatedUser));
  }

  @Test
  public void updateUser_UsernameAlreadyExists_ThrowsException() {
    // Arrange
    Long userId = 1L;
    User existingUser = new User();
    existingUser.setId(2L); // Different user
    existingUser.setUsername("newUsername");

    User updateUser = new User();
    updateUser.setUsername("newUsername");

    when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
    when(userRepository.findByUsername("newUsername")).thenReturn(existingUser);

    // Act & Assert
    assertThrows(ResponseStatusException.class, () -> userService.updateUser(userId, updateUser));
  }

  @Test
  public  void updateUser_WithBirthDateLessThan5Years_ShouldThrowException() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setUsername("testUser");
        user.setBirthDay(Date.from(LocalDate.now().minusYears(3).atStartOfDay(ZoneId.systemDefault()).toInstant()));

        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(user));

        // Act & Assert
        Exception exception = assertThrows(ResponseStatusException.class, () -> userService.updateUser(user.getId(), user));
        assertTrue(exception.getMessage().contains("User must be at least 5 years old."));
  }

  @Test
  public  void updateUser_WithBirthDateGreaterThan120Years_ShouldThrowException() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setUsername("testUser");
        user.setBirthDay(Date.from(LocalDate.now().minusYears(125).atStartOfDay(ZoneId.systemDefault()).toInstant()));

        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(user));

        // Act & Assert
        Exception exception = assertThrows(ResponseStatusException.class, () -> userService.updateUser(user.getId(), user));
        assertTrue(exception.getMessage().contains("User must be younger than 120 years."));
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