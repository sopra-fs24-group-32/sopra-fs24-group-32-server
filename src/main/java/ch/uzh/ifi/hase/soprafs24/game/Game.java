package ch.uzh.ifi.hase.soprafs24.game;

import ch.uzh.ifi.hase.soprafs24.game.player.Player;
import ch.uzh.ifi.hase.soprafs24.game.round.Round;
import ch.uzh.ifi.hase.soprafs24.game.score.Score;

import java.util.ArrayList;
import java.util.List;


public class Game {
    
    private List<Player> players;
    private Score scores;
    private int amtOfRounds;
    private float timeLimit;
    private Round round;
    

    // public Game(long id) {
    //     this.players = new ArrayList<>();
    //     this.lobbyId = "roomId" + id;
    // }

    public Game(List<Player> players, int numOfRounds, float timeLimit) {
        this.players = players;
        this.amtOfRounds = numOfRounds;
        this.timeLimit = timeLimit;
        this.scores = new Score();
    }

    public void startGame() {

        for (int i = 0; i < amtOfRounds; i++) {
            round = new Round(players);
            round.startNewRound(timeLimit, players, scores);
        }
    }

}
