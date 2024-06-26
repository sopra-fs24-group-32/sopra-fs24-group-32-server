package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.TokenDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.apache.commons.codec.binary.Base64;


import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * User Controller
 * This class is responsible for handling all REST request that are related to
 * the user.
 * The controller will receive the request and delegate the execution to the
 * UserService and finally return the result.
 */
@RestController
public class UserController {

  private final UserService userService;
  private final UserRepository userRepository;

  UserController(UserService userService, UserRepository userRepository) {
    this.userService = userService;
    this.userRepository = userRepository;
  }

  @GetMapping("/users")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public List<UserGetDTO> getAllUsers() {
    // fetch all users in the internal representation
    List<User> users = userService.getAllUsers();
    List<UserGetDTO> userGetDTOs = new ArrayList<>();

    // convert each user to the API representation
    for (User user : users) {
      userGetDTOs.add(DTOMapper.INSTANCE.convertEntityToUserGetDTO(user));
    }
    return userGetDTOs;
  }

  @GetMapping("/users/{id}")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public UserGetDTO User(@PathVariable Long id) throws InterruptedException, ExecutionException {
    Optional<User> user = userRepository.findById(id);
    if (user.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
    }

    return DTOMapper.INSTANCE.convertEntityToUserGetDTO(user.get());
    // return user;
    // return DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
  }

  @PostMapping("/user/register")
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public ResponseEntity<?> registerUser(@RequestBody UserPostDTO userPostDTO) {
      
      try {
        User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);
        User createdUser = userService.registerUser(userInput);
        UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(createdUser);
        return new ResponseEntity<>(userGetDTO, HttpStatus.CREATED);
      } catch (ResponseStatusException e) {
        String errorMessage = "Registration failed: " + e.getReason();
        return ResponseEntity.status(e.getStatus()).body(errorMessage);
      }
  }


    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<?> loginUser(@RequestBody UserPostDTO userPostDTO) {
      User user = userRepository.findByUsername(userPostDTO.getUsername());

      if (user == null) { 
        ResponseStatusException error = new ResponseStatusException(HttpStatus.NOT_FOUND, "No user exists with username: " + userPostDTO.getUsername());
        String errorMessage = "Login failed: " + error.getReason();
        return ResponseEntity.status(error.getStatus()).body(errorMessage);
      }
  
      if (!Objects.equals(userRepository.findByUsername(userPostDTO.getUsername()).getPassword(), userPostDTO.getPassword())) {
        ResponseStatusException error = new ResponseStatusException(HttpStatus.NOT_FOUND, "Wrong password or username");
        String errorMessage = "Login failed: " + error.getReason();
        return ResponseEntity.status(error.getStatus()).body(errorMessage);
      }
  
      userService.loginUser(user);
      return new ResponseEntity<>(DTOMapper.INSTANCE.convertEntityToUserGetDTO(user), HttpStatus.OK);
    }

  @PostMapping("/users/logout/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void logoutUser(@PathVariable Long id)
    {
      if (id == null || id == 0) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User ID cannot be null");
      }

      userService.logoutUser(id);
    }

    @PostMapping("/logoutByToken")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void logoutUserToken(@RequestBody TokenDTO tokenDTO) {
        String userToken = tokenDTO.getUserToken(); // Extracting the token from the DTO
        System.out.println(userToken); // Logging the extracted token

        User user = userRepository.findByUserToken(userToken); // Finding the user by token
        if (user != null) {
            userService.logoutUser(user); // Logout the user if found
        } else {
            // Optionally handle the case where the user is not found
            System.out.println("No user found with the provided token.");
            // WE CAN'T THROW AN ERROR HERE OR ELSE THE APP CRASHES IF A LOUGOUT WITH A USERTOKEN THAT NO LONGER EXISTS IS ATTEMPTED
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No user found with the provided token.");
        }
    }

  @PutMapping("/users/update/{id}")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserPostDTO userPostDTO) throws Exception {
    try {
        if (id == null || id == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User ID cannot be null");
        }

        User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

        User updatedUser = userService.updateUser(id, userInput);

        UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(updatedUser);
        return new ResponseEntity<>(userGetDTO, HttpStatus.OK);
    }catch (ResponseStatusException e){
        String errorMessage = e.getReason();
        return ResponseEntity.status(e.getStatus()).body(errorMessage);
    }
  }


}
