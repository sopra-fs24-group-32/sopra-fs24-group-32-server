package ch.uzh.ifi.hase.soprafs24.rest.dto;


public class GamePostDTO {
    
    private float timeLimit;
    private int amtOfRounds;
    private int maxAmtPlayers=50;


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
}
