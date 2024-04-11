package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.game.lobby.Lobby;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("gameRepository")
public interface GameRepository extends JpaRepository<Lobby, Long> {

  Lobby findByLobbyId(String lobbyId);
  Lobby findByLobbyInvitationCode(String invitationCodes);
  
}
