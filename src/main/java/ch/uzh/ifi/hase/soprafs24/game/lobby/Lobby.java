package ch.uzh.ifi.hase.soprafs24.game.lobby;

import ch.uzh.ifi.hase.soprafs24.game.Game;
import ch.uzh.ifi.hase.soprafs24.game.player.Player;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import org.apache.commons.lang3.RandomStringUtils;

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
    private Game game;
    private User lobbyOwner;
    private String invitationCode;


    
    @Column(nullable = false, name = "lobbyId")
    private String lobbyId;
    @OneToMany
    private List<Player> players = new ArrayList<>();

    @Column(nullable = false)
    private int amtOfRounds;

    @Column(nullable = false)
    private float timeLimit;

    public Lobby(){}
    public Lobby(long id, User lobbyOwner) {
        this.lobbyId = "roomId" + id;
        this.lobbyOwner = lobbyOwner;
        this.invitationCode = generateNewInvitationCode();
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
        if (atLeastTwoPlayers() && timeLimit >= 5) {
            Game game = new Game(players, timeLimit, amtOfRounds);
            game.startGame();
        } else {
            throw new IllegalArgumentException("Not enough players to start the game or guessing time is too short.");
        }
    }

    public void addPlayer(Player player) {
        boolean playerExists = players.stream().anyMatch(p -> p.getUsername().equals(player.getUsername()));
        if (!playerExists) {
            players.add(player);
        } else {
            System.out.println("Player already exists in the lobby.");
        }

    }

    public List<Player> getAllPlayers() {
        return players;
    }

    public void removePlayer(Player player) {
        players.remove(player);
    }

    public List<Player> kickedPlayers() {
        // Implementation not provided: Sprint 2
        return null;
    }

    public boolean atLeastTwoPlayers() {
        return players.size() >= 2;
    }

    private String generateNewInvitationCode() {
        int length = 10;
        boolean useLetters = true;
        boolean useNumbers = false;
        return RandomStringUtils.random(length, useLetters, useNumbers);
    }

    public void endSession() {
        // Additional cleanup logic can be added here
    }

    public String getInvitationCodes() {
        return invitationCode;
    }


    public User getOwner() {
        return lobbyOwner;
    }

    public void setOwner(User lobbyOwner) {
        this.lobbyOwner = lobbyOwner;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public int getMaxAmtPlayers() {
        return maxAmtPlayers;
    }

    public void setMaxAmtPlayers(int maxAmtPlayers) {
        this.maxAmtPlayers = maxAmtPlayers;
    }
}
