package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.game.Game;
import ch.uzh.ifi.hase.soprafs24.game.lobby.Lobby;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("gameRepository")
public interface GameRepository extends JpaRepository<Game, Long> {

  Game findByLobbyId(String lobbyId);
  Game findById(String lobbyId);
  Game findByLobbyInvitationCode(String invitationCodes);
  
}
