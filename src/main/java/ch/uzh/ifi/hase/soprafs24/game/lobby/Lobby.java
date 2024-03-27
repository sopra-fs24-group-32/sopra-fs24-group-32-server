package ch.uzh.ifi.hase.soprafs24.game.lobby;

import ch.uzh.ifi.hase.soprafs24.game.Game;
import ch.uzh.ifi.hase.soprafs24.game.player.Player;
import ch.uzh.ifi.hase.soprafs24.entity.User;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;


@Entity
@Table(name = "lobby")
public class Lobby {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int maxAmtPlayers;
    
    private User owner;
    @Column(nullable = false, name = "lobbyId")
    private String lobbyId;
    private String invitationCodes;

    @OneToMany
    private List<Player> players = new ArrayList<>();

    @Column(nullable = false)
    private int amtOfRounds;

    @Column(nullable = false)
    private float timeLimit;

    public Lobby(){}
    public Lobby(long id, float timeLimit, int amtOfRounds) {
        this.lobbyId = "roomId" + id;
        this.timeLimit = timeLimit;
        this.amtOfRounds = amtOfRounds;
    }

    public String getLobbyId() {
        return lobbyId;
    }

    public void setAmtOfRounds(int amtOfRounds) {
        this.amtOfRounds = amtOfRounds;
    }

    public void setTimeLimit(float timeLimit) {
        this.timeLimit = timeLimit;
    }

    public float getTimeLimit() {
        return timeLimit;
    }

    public int getAmtOfRounds() {
        return amtOfRounds;
    }

    public void startGame() {
        if (atLeastTwoPlayers() && timeLimit <= 5) {
            Game game = new Game();
            game.startGame();
        } else {
            throw new IllegalArgumentException("Not enough players to start the game or guessing time is too short.");
        }
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public List<Player> getAllPlayers() {
        return players;
    }

    public void removePlayer(Player player) {
        players.remove(player);
    }

    public boolean atLeastTwoPlayers() {
        return players.size() >= 2;
    }

    private String generateNewInvitationCode() {
        // Implementation not provided
        return null;
    }

    public void endSession() {
        // Additional cleanup logic can be added here
    }
}
