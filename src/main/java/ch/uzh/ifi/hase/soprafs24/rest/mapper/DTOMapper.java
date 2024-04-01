package ch.uzh.ifi.hase.soprafs24.rest.mapper;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.game.Game;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GamePostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

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

  @Mapping(source = "timeLimit", target = "timeLimit")
  @Mapping(source = "amtOfRounds", target = "amtOfRounds")
  @Mapping(source = "maxAmtPlayers", target = "maxAmtPlayers")
  Game convertGamePostDTOtoEntity(GamePostDTO gamePostDTO);

  @Mapping(source = "username", target = "username")
  @Mapping(source = "password", target = "password")
  @Mapping(source = "isLoggedIn", target = "isLoggedIn")
  @Mapping(source = "birthDay", target = "birthDay")
  @Mapping(source = "userToken", target = "userToken")
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
}
