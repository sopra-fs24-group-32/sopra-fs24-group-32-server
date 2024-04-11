package ch.uzh.ifi.hase.soprafs24.game.lobby;

import ch.uzh.ifi.hase.soprafs24.entity.User;
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
    private int maxAmtUsers =50;
    // private Game game;
    private String lobbyOwner;
    private String lobbyInvitationCode;
    @Column(nullable = false)
    private boolean gameStarted = false;

    
    @Column(nullable = false, name = "lobbyId")
    private String lobbyId;
    @OneToMany
    private List<User> users = new ArrayList<>();

    @Column(nullable = false)
    private int amtOfRounds;

    @Column(nullable = false)
    private float timeLimit;

    // EACH NEW FIELD YOU SPECIFY HERE MUST ALSO BE STECIFIED IN GAMEGETDTO.JAVA AND GAMEPOSTDTO.JAVA FILE !!!!!!!!!!!!!!!!

    public Lobby(){}
    public Lobby(long id, String lobbyOwner) {
        this.lobbyId = "roomId" + id;
        this.lobbyOwner = lobbyOwner;
        this.id = id;
        this.lobbyInvitationCode = generateNewInvitationCode();
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLobbyOwner() {
        return lobbyOwner;
    }

    public void setLobbyOwner(String lobbyOwner) {
        this.lobbyOwner = lobbyOwner;
    }

    public String getLobbyId() {
        return lobbyId;
    }

    public void setLobbyInvitationCode(String lobbyInvitationCode) {
        this.lobbyInvitationCode = lobbyInvitationCode;
    }

    public String getLobbyInvitationCode() {
        return lobbyInvitationCode;
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

//    public void startGame() {
//        if (atLeastTwoUsers() && timeLimit >= 5) {
//            Game game = new Game(users, timeLimit, amtOfRounds);
//            this.gameStarted = true;
//            game.startGame();
//        } else {
//            throw new IllegalArgumentException("Not enough users to start the game or guessing time is too short.");
//        }
//    }

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
        users.add(user);
    }

    public List<User> getAllUsers() {
        return users;
    }

    public void removePlayer(User user) {
        users.remove(user);
    }

    public List<User> kickedUsers() {
        // Implementation not provided: Sprint 2
        return null;
    }

    public boolean atLeastTwoUsers() {
        return users.size() >= 2;
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

    public int getMaxAmtUsers() {
        return maxAmtUsers;
    }

    public void setMaxAmtUsers(int maxAmtUsers) {
        this.maxAmtUsers = maxAmtUsers;
    }

    public boolean gameHasStarted() {
        return gameStarted;
    }

    public void setGameHasStarted(boolean gameHasStarted) {
        this.gameStarted = gameHasStarted;
    }
}
