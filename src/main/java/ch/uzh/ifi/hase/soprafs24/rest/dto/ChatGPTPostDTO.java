package ch.uzh.ifi.hase.soprafs24.rest.dto;

public class ChatGPTPostDTO {
    private String playerGuessed;
    private float timeGuessSubmitted;


    public String getPlayerGuessed() {
        return playerGuessed;
    }

    public void setPlayerGuessed(String playerGuessed) {
        this.playerGuessed = playerGuessed;
    }

    public float getTimeGuessSubmitted() {
        return timeGuessSubmitted;
    }

    public void setTimeGuessSubmitted(float timeGuessSubmitted) {
        this.timeGuessSubmitted = timeGuessSubmitted;
    }
}
