package ch.uzh.ifi.hase.soprafs24.game;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.util.*;

import javax.persistence.*;
import javax.transaction.Transactional;

import java.security.SecureRandom;
import java.util.List;

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

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "game")
    @JsonManagedReference
    private List<User> users = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "game_remaining_picture_generators", joinColumns = @JoinColumn(name = "game_id"))
    @Column(name = "remaining_picture_generator")
    private Set<String> remaininPictureGenerators = new HashSet<>();

    @Column(nullable = false)
    private int amtOfRounds;

    @Column(nullable = false)
    private float timeLimit;

    @Column(nullable = false)
    private String lobbyId;

    @Column
    private int playersInLobby = 0;

    private static final SecureRandom RANDOMForPlayer = new SecureRandom();
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "game_picture_generator_queue", joinColumns = @JoinColumn(name = "game_id"))
    @Column(name = "picture_generator")
    private List<String> pictureGeneratorQueue = new ArrayList<>();
    @Column
    private int currentRound = 0;
    private int countNumPlayed = 0;
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "list_of_removed_players", joinColumns = @JoinColumn(name = "game_id"))
    @Column(name = "remove_player")
    private List<String> listOfRemovedPlayers = new ArrayList<>();

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
        if (user != null && users.contains(user)) {
            /*
            if (user.getUsername().equals(lobbyOwner)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot remove the lobby owner from the game");
            }

             */
            users.remove(user);
            user.deleteGame(this);
            if (remaininPictureGenerators.contains(user.getUsername())) {
                remaininPictureGenerators.remove(user.getUsername());
            }
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found in the game");
        }
    }

    public void ownerRemovePlayer(User user) {
        removePlayer(user);
        listOfRemovedPlayers.add(user.getUsername());
    }

    public Set<String> getRemaininPictureGenerators() { 
        return remaininPictureGenerators; }


    public void setRemaininPictureGenerators(Set<String> remaininPictureGenerators) { 
        this.remaininPictureGenerators = remaininPictureGenerators; }

    public boolean isGameStarted() { return gameStarted; }

    public void setGameStarted(boolean gameStarted) { this.gameStarted = gameStarted; }

    public String getLobbyInvitationCode() { return lobbyInvitationCode; }
    public void setLobbyInvitationCode(String lobbyInvitationCode) {
        this.lobbyInvitationCode = lobbyInvitationCode;
    }

    public void addPlayer(User user) throws Exception {
        if(user == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User cannot be null");
        }
        if(users.contains(user)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User already in lobby");
        }
        if(users.size() == maxAmtUsers){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Maximum amount of users in lobby already reached");
        }
        if (gameStarted) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game has already started");
        }
        if (listOfRemovedPlayers.contains(user.getUsername())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "You have been removed from the lobby by the host. Sorry, you cannot rejoin.");
        }

        user.setScore(0);
        users.add(user);
        this.playersInLobby += 1;
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

        for(User user: users){
            this.remaininPictureGenerators.add(user.getUsername());
        }

    }

    @Transactional
    public String selectPictureGenerator() {
        removeDuplicateUsers();
        if (!gameStarted) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game has not started yet");
        }
        if (currentRound >= amtOfRounds) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "All rounds have been played");
        }

        if (pictureGeneratorQueue.isEmpty()) {
            if (remaininPictureGenerators.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No remaining picture generators.");
            }
            // Refill the queue from remaining generators
            pictureGeneratorQueue.addAll(remaininPictureGenerators);
        }

        if (users.size() < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enough players in the game. There should be at least 2 players.");
        }

        // Select and remove a generator from the queue
        int index = RANDOMForPlayer.nextInt(pictureGeneratorQueue.size());
        String selectedGenerator = pictureGeneratorQueue.remove(index);

        // Increment played count and check for round completion
        countNumPlayed++;
        if (countNumPlayed % users.size() == 0) {
            currentRound++;
            System.out.println("-----------------Current round: " + currentRound + " / " + amtOfRounds + "Total number of played so far: " + countNumPlayed );
            if (currentRound < amtOfRounds) {
                pictureGeneratorQueue.addAll(remaininPictureGenerators);
            }
        }

        return selectedGenerator;
    }

    public int getPlayersInLobby(){
        return this.playersInLobby;
    }

    public void setCurrentRound(int currentRound) {
        this.currentRound = currentRound;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public void setListOfRemovedPlayers(List<String> listOfRemovedPlayers) {
        this.listOfRemovedPlayers = listOfRemovedPlayers;
    }

    public List<String> getListOfRemovedPlayers() {
        return listOfRemovedPlayers;
    }

    public void removeDuplicateUsers() {
        for (int i = 0; i < users.size(); i++) {
            for (int j = i + 1; j < users.size(); j++) {
                if (users.get(i).getUsername().equals(users.get(j).getUsername())) {
                    users.remove(j);
                    j--;
                }
            }
        }
    }
    public boolean gameHasStarted() {
        return gameStarted;
    }

    public int scalePointsByDuration(int pointsFromChatGPT, float timeGuessSubmitted) throws Exception {


        float timeLeft = timeLimit - timeGuessSubmitted;
        float bonusPoints = 0;
        
        if (timeLeft <= 0) {
            return 0;
        } else {
            float percentageOfTimeLeft = timeLeft / timeLimit;
            if (percentageOfTimeLeft >= 0.75) {
                bonusPoints = 0.25f;
            } else if (percentageOfTimeLeft >= 0.5) {
                bonusPoints = 0.10f;
            }

            int finalPointsAwarded = (int) Math.round(pointsFromChatGPT + (pointsFromChatGPT * bonusPoints));
            return finalPointsAwarded;
        }
    }

    @Override
    public boolean equals(Object o){
        if(this == o){
            return true;
        }
        if(o == null || getClass() != o.getClass()){
            return false;
        }
        Game game = (Game) o;
        return Objects.equals(id, game.id) &&
                Objects.equals(lobbyOwner, game.lobbyOwner);
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

