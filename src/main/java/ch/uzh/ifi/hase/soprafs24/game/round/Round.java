package ch.uzh.ifi.hase.soprafs24.game.round;

import java.time.Clock;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;

import javax.persistence.*;

import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.game.score.Score;

public class Round {

    private String inputPhrase;
    private Clock timer;
    private Map<Player, Map<String, Duration>> guessedInputs;
    private List<Player> players;
    private Player pictureGenerator;
    @OneToMany
    private List<Player> remainingGenerators;
    // private ChatGPT chatGPT;
    private Score scores;

    public Round(List<Player> players) {
        this.players = players;
        this.guessedInputs = new HashMap<>();
    }

    public void startNewRound(List<Player> players, float timeLimit, Score scores) {

        Collections.shuffle(players);
        // Implementation of the method
    }

    public Player nextGenerator() {
        // Implementation of the method
        return null;
    }

    public void startTimer() {
        // Implementation of the method
    }

    public boolean timeIsOver(float timeLimit, float timeGuessSubmitted) {
        // Implementation of the method
        if (getTimeLeft(timeLimit, timeGuessSubmitted) <= 0) {
            return true;
        }
        return false;
    }

    public float getTimeLeft(float timeLimit, float timeGuessSubmitted) {
        // Implementation of the method
        return timeLimit - timeGuessSubmitted;
    }

    public void scalePointsByDuration(Player player, float timeLimit, float timeGuessSubmitted) {
        // Implementation of the method
        // if the timeGuessSubmitted is over the timeLimit, the player gets 0 points
        // if the timeGuessSubmitted is >=75% of the timeLimit, the player gets full points (maxPoints = 6)
        // if the timeGuessSubmitted is 50% of the timeLimit, the player gets half of the points (3)
        // if the timeGuessSubmitted is 25% of the timeLimit, the player gets 1 point

        // IMPLEMENTATION NOT TOTALLY CORRECT (problem with (int) (maxPoints * percentage)
        // Rounding issue, should be fixed by using Math.round() or Math.floor() or Math.ceil() instead of casting to int

        int maxPoints = 6;

        if (timeGuessSubmitted >= timeLimit) {
            scores.updateScore(player, 0);
        } else {
            float timeLeft = getTimeLeft(timeLimit, timeGuessSubmitted);
            float percentage = timeLeft / timeLimit;
            int points = (int) (maxPoints * percentage);
            scores.updateScore(player, points);
        }
    }


    public void chatGPTSimilarityScore(Player player, float similarityScore) throws Exception {
        
        if (similarityScore < 0 || similarityScore > 1) {
            throw new IllegalArgumentException("Similarity score must be between 0 and 1.");
        }
        
        int pointsAwarded;
        if (similarityScore >= 0.75) {
            pointsAwarded = 6;
        } else if (similarityScore >= 0.5) { // Logical AND is replaced with proper syntax
            pointsAwarded = 4;
        } else if (similarityScore >= 0.25) {
            pointsAwarded = 2;
        } else {
            pointsAwarded = 0;
        }
        
        player.setPoint(pointsAwarded);
    }
}
