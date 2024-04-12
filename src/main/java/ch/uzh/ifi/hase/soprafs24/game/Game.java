package ch.uzh.ifi.hase.soprafs24.game;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.game.round.Round;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.util.List;

import javax.persistence.*;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "game")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int maxAmtUsers = 50;

    @Column(nullable = false)
    private String lobbyOwner; // This serves as the username of the game host.

    @Column(nullable = false, unique = true)
    private String lobbyInvitationCode;

    @Column(nullable = false)
    private boolean gameStarted = false;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "game")
    @JsonManagedReference
    private List<User> users = new ArrayList<>();

    @Column(nullable = false)
    private int amtOfRounds;

    @Column(nullable = false)
    private float timeLimit;

    @Column(nullable = false)
    private String lobbyId;

    // Constructors
    public Game() {}

    public Game(long id, String lobbyOwner) {
        this.lobbyOwner = lobbyOwner;
        this.lobbyId = "roomId" + id;
        this.lobbyInvitationCode = generateNewInvitationCode();
    }

    // Getter and Setter methods
    // Including new or updated methods for the added or modified properties
    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getLobbyOwner() { return lobbyOwner; }

    public void setLobbyOwner(String lobbyOwner) { this.lobbyOwner = lobbyOwner; }

    public int getAmtOfRounds() { return amtOfRounds; }

    public void setAmtOfRounds(int amtOfRounds) { this.amtOfRounds = amtOfRounds; }

    public float getTimeLimit() { return timeLimit; }

    public void setTimeLimit(float timeLimit) { this.timeLimit = timeLimit; }

    public int getMaxAmtUsers() { return maxAmtUsers; }

    public void setMaxAmtUsers(int maxAmtUsers) { this.maxAmtUsers = maxAmtUsers; }

    public List<User> getUsers() { return users; }


    public void removePlayer(User user) {
        // Method logic...
    }

//    public List<Round> getRounds() { return rounds; }

//    public void addRound(Round round) {
//        rounds.add(round);
//        round.setGame(this);
//    }
//
//    public void removeRound(Round round) {
//        rounds.remove(round);
//        round.setGame(null);
//    }

    public boolean isGameStarted() { return gameStarted; }

    public void setGameStarted(boolean gameStarted) { this.gameStarted = gameStarted; }

    public String getLobbyInvitationCode() { return lobbyInvitationCode; }
    public void setLobbyInvitationCode(String lobbyInvitationCode) {
        this.lobbyInvitationCode = lobbyInvitationCode;
    }

    // Additional methods...

    public void addPlayer(User user) throws Exception {
        if(user == null){
            throw new Exception("User cannot be null");
        }
        if(users.contains(user)){
            throw new Exception("User already in lobby");
        }
        if(users.size() == maxAmtUsers){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Maximum amount of users in lobby already reached");
        }
        user.setScore(0);
        users.add(user);
        user.setGame(this);
    }

    public void setUsers(List<User> users) {
        this.users = users;
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

    public void startGame() {
        if(users.size() < 2){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not enough players in lobby");
        }
        if(gameStarted){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game has already started");
        }
        gameStarted = true;
    }

    public boolean gameHasStarted() {
        return gameStarted;
    }

    @Override
    public int hashCode(){
        return Objects.hash(id, maxAmtUsers, lobbyOwner, lobbyInvitationCode, gameStarted, id, users, amtOfRounds, timeLimit);
    }

    @Override
    public String toString(){
        return "Game:"+
                "id:"+id+
                "id:"+id;
    }
}

