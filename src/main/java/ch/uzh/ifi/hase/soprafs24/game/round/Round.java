package ch.uzh.ifi.hase.soprafs24.game.round;

import java.time.Clock;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import ch.uzh.ifi.hase.soprafs24.game.player.Player;
import ch.uzh.ifi.hase.soprafs24.game.score.Score;

public class Round {
    private String inputPhrase;
    private Clock timer;
    private Map<Player, Map<String, Duration>> guessedInputs;
    private List<Player> players;
    private Player pictureGenerator;
    private List<Player> remainingGenerators;
    // private ChatGPT chatGPT;
    private Score scores;

    public Round(List<Player> players) {
        this.players = players;
        this.guessedInputs = new HashMap<>();
    }

    public void startNewRound(float timeLimit, List<Player> players, Score scores) {
        // Implementation of the method
    }

    public Player nextGenerator() {
        // Implementation of the method
        return null;
    }
}
