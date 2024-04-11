package ch.uzh.ifi.hase.soprafs24.game.score;

import java.util.HashMap;
import java.util.Map;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.entity.User;


public class Score {
    private Map<User, Integer> scoreMap;

    public Score(Map<User, Integer> scoreMap) {
        this.scoreMap = scoreMap;
    }
    public Score() {
        this.scoreMap = new HashMap<>();
    }

    public int getScore(User user) {
        return scoreMap.get(user);
    }

    public void updateScore(User user, int points) {
        int currentScore = scoreMap.get(user) + points;
        scoreMap.put(user, currentScore);
    }


    public Map<User, Integer> getAllScores() {
        return new HashMap<>(scoreMap);
    }
}
