package ch.uzh.ifi.hase.soprafs24.game;

import ch.uzh.ifi.hase.soprafs24.game.player.Player;
import ch.uzh.ifi.hase.soprafs24.game.round.Round;
import ch.uzh.ifi.hase.soprafs24.game.score.Score;

import java.util.List;

public class Game {

    private List<Player> players;
    private Score scores;
    private int amtOfRounds;
    private float timeLimit;
    private Round round;
    private String username;

    public void setTimeLimit(float timeLimit) {
        this.timeLimit = timeLimit;
    }
    public void setAmtOfRounds(int amtOfRounds) {
        this.amtOfRounds = amtOfRounds;
    }
    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public float getTimeLimit() {
        return timeLimit;
    }

    public int getAmtOfRounds() {
        return amtOfRounds;
    }

    public int getNumofRouds() {
        return amtOfRounds;
    }

    public Game() {}
        public Game(List<Player> players, int amtOfRounds, float timeLimit){
        this.players = players;
        this.amtOfRounds = amtOfRounds;
        this.timeLimit = timeLimit;
        this.scores = new Score();
    }

    public void startGame() {

        for (int i = 0; i < amtOfRounds; i++) {
            round = new Round(players);
            round.startNewRound(timeLimit, players, scores);
        }
    }


    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

}
