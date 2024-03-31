package ch.uzh.ifi.hase.soprafs24.game.lobby;

import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.game.Game;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;


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

    @Column(nullable = false)
    private int maxAmtPlayers =50;
    // private Game game;
    private String lobbyOwner;
    private String invitationCode;
    private boolean gameStarted = false;

    
    @Column(nullable = false, name = "lobbyId")
    private String lobbyId;
    @OneToMany
    private List<Player> players = new ArrayList<>();

    @Column(nullable = false)
    private int amtOfRounds;

    @Column(nullable = false)
    private float timeLimit;

    public Lobby(){}
    public Lobby(long id, String lobbyOwner) {
        this.lobbyId = "roomId" + id;
        this.lobbyOwner = lobbyOwner;
        this.id = id;
        this.invitationCode = generateNewInvitationCode();
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLobbyId() {
        return lobbyId;
    }

    public void setLobbyId(String lobbyId) {
        this.lobbyId = lobbyId;
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

    public void addPlayer(Player player) throws Exception {
        if(player == null){
            throw new Exception("Player cannot be null");
        }
        if(players.contains(player)){
            throw new Exception("Player already in lobby");
        }
        if(players.size() == maxAmtPlayers){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Maximum amount of players in lobby already reached");
        }
        players.add(player);
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
        
        String LETTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        SecureRandom RANDOM = new SecureRandom();
        int length = 10;

        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(LETTERS.charAt(RANDOM.nextInt(LETTERS.length())));
        }
        return sb.toString();
    }

    public void endSession() {
        // Additional cleanup logic can be added here
    }

    public String getInvitationCodes() {
        return invitationCode;
    }


    public String getOwner() {
        return lobbyOwner;
    }

    public void setOwner(String lobbyOwner) {
        this.lobbyOwner = lobbyOwner;
    }

    // public Game getGame() {
    //     return game;
    // }

    // public void setGame(Game game) {
    //     this.game = game;
    // }

    public int getMaxAmtPlayers() {
        return maxAmtPlayers;
    }

    public void setMaxAmtPlayers(int maxAmtPlayers) {
        this.maxAmtPlayers = maxAmtPlayers;
    }

    public boolean gameHasStarted() {
        return gameStarted;
    }

    public void setGameHasStarted(boolean gameHasStarted) {
        this.gameStarted = gameHasStarted;
    }
}
