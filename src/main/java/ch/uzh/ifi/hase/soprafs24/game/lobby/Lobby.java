package ch.uzh.ifi.hase.soprafs24.game.lobby;

import ch.uzh.ifi.hase.soprafs24.game.Game;
import ch.uzh.ifi.hase.soprafs24.game.player.Player;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import java.util.ArrayList;
import java.util.List;

public class Lobby {
    
    private int maxAmtPlayers;
    private List<Player> players;
    private Game game;
    private User owner;
    private String lobbyId;
    private String invitationCodes;
    private int amtOfRounds;
    private float timeLimit;


    public Lobby(long id) {
        this.lobbyId = "roomId" + id;
        this.players = new ArrayList<>();
    }

    public String getLobbyId() {
        return lobbyId;
    }

    public void startGame(List<Player> players, int amtOfRounds, float timeLimit) {
        if (atLeastTwoPlayers() && timeLimit <= 5) {
            this.game = new Game(players, amtOfRounds, timeLimit);
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
