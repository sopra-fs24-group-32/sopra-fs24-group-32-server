package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

/**
 * User Service
 * This class is the "worker" and responsible for all functionality related to
 * the user
 * (e.g., it creates, modifies, deletes, finds). The result will be passed back
 * to the caller.
 */
@Service
@Transactional
public class UserService {

  private final Logger log = LoggerFactory.getLogger(UserService.class);

  private final UserRepository userRepository;

  @Autowired
  public UserService(@Qualifier("userRepository") UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public List<User> getAllUsers() {
    return this.userRepository.findAll();
  }

  public User createUser(User newUser) {
    
    // newUser.setStatus(UserStatus.OFFLINE);
    checkIfUserExists(newUser);
    // saves the given entity but data is only persisted in the database once
    // flush() is called
    
    newUser.setStatus(UserStatus.ONLINE);
    newUser.setIsLoggedIn(true);
    newUser.setUserToken(UUID.randomUUID().toString());
    userRepository.save(newUser);
    userRepository.flush();
    
    // print user token to console
    System.out.println("User token---------------------------------: " + newUser.getUserToken());

    log.debug("Created Information for User: {}", newUser);
    return newUser;
  }

  public User findByUsername(String username) {
    List<User> users = userRepository.findAll();
    User findUser = new User();
    for (int i=0; i<users.size(); i++){
      if (users.get(i).getUsername().equals(username)){
        findUser = users.get(i);
      }
    }
    return findUser;
  }

  public User findByToken(String userToken){
      List<User> users = userRepository.findAll();
      for (int i=0; i<users.size(); i++){
          if (users.get(i).getUserToken().equals(userToken)){
              return users.get(i);
          }
      }
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User does not exists");
  }

  public User updateUser(int id, User user) throws Exception {
    User reqUser = this.getUserById(id);
    if (!user.getUsername().isBlank()){
      checkIfUserExists(user);
      reqUser.setUsername(user.getUsername());
    }
    if (user.getBirthDay() != null){
      reqUser.setBirthDay(user.getBirthDay());
    }
    reqUser.setPassword(this.getUserById(id).getPassword());
    reqUser.setUserToken(UUID.randomUUID().toString());
    reqUser.setStatus(UserStatus.ONLINE);
    reqUser.setIsLoggedIn(true);

    userRepository.save(reqUser);
    userRepository.flush();
    return reqUser;
  }

  public User getUserById(int id) {
    List<User> users = userRepository.findAll();
    User u = new User();
    for (int i=0; i<users.size(); i++){
      if (users.get(i).getId() == id){
        u = users.get(i);
      }
    }
    return u;
  }

  public User logoutUser(int id){
    User u = this.getUserById(id);
    u.setStatus(UserStatus.OFFLINE);
    u.setIsLoggedIn(false);
    return u;
  }

  /**
   * This is a helper method that will check the uniqueness criteria of the
   * username and the name
   * defined in the User entity. The method will do nothing if the input is unique
   * and throw an error otherwise.
   *
   * @param userToBeCreated
   * @throws org.springframework.web.server.ResponseStatusException
   * @see User
   */
  private void checkIfUserExists(User userToBeCreated) {
    User userByUsername = userRepository.findByUsername(userToBeCreated.getUsername());

    String baseErrorMessage = "The %s provided %s not unique. Therefore, the user could not be created!";
    if (userByUsername != null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format(baseErrorMessage, "username", "is"));
    }
  }
}
