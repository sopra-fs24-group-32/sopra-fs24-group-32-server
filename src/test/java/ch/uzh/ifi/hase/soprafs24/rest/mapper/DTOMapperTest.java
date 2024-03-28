package ch.uzh.ifi.hase.soprafs24.rest.mapper;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GamePostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * DTOMapperTest
 * Tests if the mapping between the internal and the external/API representation
 * works.
 */
public class DTOMapperTest {
  @Test
  public void testCreateUser_fromUserPostDTO_toUser_success() {
    // create UserPostDTO
    UserPostDTO userPostDTO = new UserPostDTO();
    // userPostDTO.setName("name");
    userPostDTO.setUsername("username");

    // MAP -> Create user
    User user = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

    // check content
    // assertEquals(userPostDTO.getName(), user.getName());
    assertEquals(userPostDTO.getUsername(), user.getUsername());
  }

  @Test
  public void testGetUser_fromUser_toUserGetDTO_success() {
    // create User
    User user = new User();
    // user.setName("Firstname Lastname");
    user.setUsername("firstname@lastname");
    user.setStatus(UserStatus.OFFLINE);
    user.setUserToken("1");

    // MAP -> Create UserGetDTO
    UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);

    // check content
    assertEquals(user.getId(), userGetDTO.getId());
    // assertEquals(user.getName(), userGetDTO.getName());
    assertEquals(user.getUsername(), userGetDTO.getUsername());
    assertEquals(user.getStatus(), userGetDTO.getStatus());
  }

  @Test
    public void testGetAndSetTimeLimit() {
        // Arrange
        float expectedTimeLimit = 10.0f;
        GamePostDTO dto = new GamePostDTO();

        // Act
        dto.setTimeLimit(expectedTimeLimit);
        float actualTimeLimit = dto.getTimeLimit();

        // Assert
        assertEquals(expectedTimeLimit, actualTimeLimit);
    }

    @Test
    public void testGetAndSetAmtOfRounds() {
        // Arrange
        int expectedAmtOfRounds = 5;
        GamePostDTO dto = new GamePostDTO();

        // Act
        dto.setAmtOfRounds(expectedAmtOfRounds);
        int actualAmtOfRounds = dto.getAmtOfRounds();

        // Assert
        assertEquals(expectedAmtOfRounds, actualAmtOfRounds);
    }

    @Test
    public void testGetAndSetMaxAmtPlayers() {
        // Arrange
        int expectedMaxAmtPlayers = 4;
        GamePostDTO dto = new GamePostDTO();

        // Act
        dto.setMaxAmtPlayers(expectedMaxAmtPlayers);
        int actualMaxAmtPlayers = dto.getMaxAmtPlayers();

        // Assert
        assertEquals(expectedMaxAmtPlayers, actualMaxAmtPlayers);
    }
}
