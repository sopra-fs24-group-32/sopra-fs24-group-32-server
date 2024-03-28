package ch.uzh.ifi.hase.soprafs24.rest.dto;

import java.util.List;

import ch.uzh.ifi.hase.soprafs24.game.player.Player;

public class GamePostDTO {
    
    private float timeLimit;
    private int amtOfRounds;
    private List<Player> players;
    private String username;

    public String getUsername() {
        return username;
    }

    public void setUserToken(String username) {
        this.username = username;
    }

    public float getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(float timeLimit) {
        this.timeLimit = timeLimit;
    }

    public int getAmtOfRounds() {
        return amtOfRounds;
    }

    public void setAmtOfRounds(int amtOfRounds) {
        this.amtOfRounds = amtOfRounds;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }
}
