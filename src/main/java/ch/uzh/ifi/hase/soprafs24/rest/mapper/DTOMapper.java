package ch.uzh.ifi.hase.soprafs24.rest.mapper;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.game.Game;
import ch.uzh.ifi.hase.soprafs24.game.chatGPT.ChatGPT;
import ch.uzh.ifi.hase.soprafs24.rest.dto.*;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;

/**
 * DTOMapper
 * This class is responsible for generating classes that will automatically
 * transform/map the internal representation
 * of an entity (e.g., the User) to the external/API representation (e.g.,
 * UserGetDTO for getting, UserPostDTO for creating)
 * and vice versa.
 * Additional mappers can be defined for new entities.
 * Always created one mapper for getting information (GET) and one mapper for
 * creating information (POST).
 */
// @Mapper
@Mapper(componentModel = "spring")
public interface DTOMapper {

  DTOMapper INSTANCE = Mappers.getMapper(DTOMapper.class);

    @Autowired
    UserRepository userRepository = null;
    

    // Custom mapping method (no implementation needed, MapStruct generates the code)
//    @Named("stringsToUsers")
//    default List<User> stringsToUsers(List<String> userIds) {
//        if (userIds == null) {
//            // Return an empty list or handle accordingly
//            return Collections.emptyList();
//        }
//        return userIds.stream()
//                .map(id -> userRepository.findById(Long.valueOf(id))
//                        .orElseThrow(() -> new RuntimeException("User not found for ID: " + id)))
//                .collect(Collectors.toList());
//    }
//
//    @Named("usersToStrings")
//    default List<String> usersToStrings(List<User> users) {
//        if (users == null) {
//            return Collections.emptyList(); // Return an empty list if users is null
//        }
//        return users.stream()
//                .map(user -> String.valueOf(user.getId()))
//                .collect(Collectors.toList());
//    }

  @Mapping(source = "timeLimit", target = "timeLimit")
  @Mapping(source = "amtOfRounds", target = "amtOfRounds")
  @Mapping(source = "maxAmtUsers", target = "maxAmtUsers")
  @Mapping(source = "users", target = "users") // Use custom mapping for users
  Game convertGamePostDTOtoEntity(GamePostDTO gamePostDTO);

  @Mapping(source = "id", target = "id")
  @Mapping(source = "users", target = "users") // Use the new custom mapping for users
  @Mapping(source = "timeLimit", target = "timeLimit")
  @Mapping(source = "amtOfRounds", target = "amtOfRounds")
  @Mapping(source = "maxAmtUsers", target = "maxAmtUsers")
  GameGetDTO convertEntityToGameGetDTO(Game createdLobby);

  @Mapping(source = "username", target = "username")
  @Mapping(source = "password", target = "password")
  @Mapping(source = "isLoggedIn", target = "isLoggedIn")
  @Mapping(source = "birthDay", target = "birthDay")
  @Mapping(source = "userToken", target = "userToken")
  @Mapping(source = "email", target = "email")
  @Mapping(source = "picture", target = "picture")
  User convertUserPostDTOtoEntity(UserPostDTO userPostDTO);

  @Mapping(source = "id", target = "id")
  @Mapping(source = "username", target = "username")
  @Mapping(source = "status", target = "status")
  @Mapping(source = "isLoggedIn", target = "isLoggedIn")
  @Mapping(source = "createDate", target = "createDate")
  @Mapping(source = "birthDay", target = "birthDay")
  @Mapping(source = "email", target = "email")
  @Mapping(source = "picture", target = "picture")
  UserGetDTO convertEntityToUserGetDTO(User user);

  @Mapping(source = "username", target = "username")
  SimpleUserGetDTO convertEntityToSimpleUserGetDTO(User user);

  @Mapping(source = "playerGuessed", target = "playerGuessed")
  // @Mapping(source = "timeGuessSubmitted", target = "timeGuessSubmitted")
  ChatGPTGetDTO convertEntityToChatGPTGetDTO(ChatGPT chatGPT);

  @Mapping(source = "playerGuessed", target = "playerGuessed")
  // @Mapping(source = "timeGuessSubmitted", target = "timeGuessSubmitted")
  ChatGPT convertChatGPTPostDTOtoEntity(ChatGPTPostDTO chatGPTPostDTO);

}