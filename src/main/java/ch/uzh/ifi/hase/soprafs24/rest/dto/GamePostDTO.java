package ch.uzh.ifi.hase.soprafs24.rest.dto;

import java.util.List;

import ch.uzh.ifi.hase.soprafs24.game.player.Player;

public class GamePostDTO {
    
    private float timeLimit;
    private int amtOfRounds;
    private int maxAmtPlayers;

    private String username;

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

    public int getMaxAmtPlayers() {
        return maxAmtPlayers;
    }

    public void setMaxAmtPlayers(int maxAmtPlayers) {
        this.maxAmtPlayers = maxAmtPlayers;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
