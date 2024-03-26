package ch.uzh.ifi.hase.soprafs24.game.score;

import java.util.HashMap;
import java.util.Map;

import ch.uzh.ifi.hase.soprafs24.game.player.Player;


public class Score {
    private Map<Player, Integer> scoreMap;

    public Score() {
        this.scoreMap = new HashMap<>();
    }

    public int getScore(Player player) {
        return scoreMap.get(player);
    }

    public void updateScore(Player player, int points) {
        int currentScore = scoreMap.get(player) + points;
        scoreMap.put(player, currentScore);
    }


    public Map<Player, Integer> getAllScores() {
        return new HashMap<>(scoreMap);
    }
}
