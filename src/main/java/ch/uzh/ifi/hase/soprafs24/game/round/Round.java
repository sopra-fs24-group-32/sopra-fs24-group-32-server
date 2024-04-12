package ch.uzh.ifi.hase.soprafs24.game.round;

import java.time.Clock;
import java.time.Duration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;

import javax.persistence.*;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.game.score.Score;

public class Round {

    private String inputPhrase;
    private Clock timer;
    private Map<User, Map<String, Duration>> guessedInputs;
    private List<User> users;
    private Iterator<User> iterator;
    private User pictureGenerator;
    @OneToMany
    private List<User> remainingGenerators;
    // private ChatGPT chatGPT;
    private Score scores;

    public Round(List<User> users) {
        this.users = new ArrayList<>(users);
        this.guessedInputs = new HashMap<>();
    }

    public void startNewRound(List<User> users, float timeLimit, Score scores) {
        this.users = new ArrayList<>(users);
        Collections.shuffle(this.users);
        this.iterator = this.users.iterator();
        this.scores = scores;

        User nextPlayer = nextGenerator();
        this.pictureGenerator = nextPlayer;
        // this.remainingGenerators = new ArrayList<>(users);
        // this.remainingGenerators.remove(nextPlayer);



    }

    public User nextGenerator() {
        if (!iterator.hasNext()) {
            iterator = users.iterator();
        }
        return iterator.next();
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

    public void scalePointsByDuration(User user, float timeLimit, float timeGuessSubmitted) {
        // Implementation of the method
        // if the timeGuessSubmitted is over the timeLimit, the user gets 0 points
        // if the timeGuessSubmitted is >=75% of the timeLimit, the user gets full points (maxPoints = 6)
        // if the timeGuessSubmitted is 50% of the timeLimit, the user gets half of the points (3)
        // if the timeGuessSubmitted is 25% of the timeLimit, the user gets 1 point

        // IMPLEMENTATION NOT TOTALLY CORRECT (problem with (int) (maxPoints * percentage)
        // Rounding issue, should be fixed by using Math.round() or Math.floor() or Math.ceil() instead of casting to int

        int maxPoints = 6;

        if (timeGuessSubmitted >= timeLimit) {
            scores.updateScore(user, 0);
        } else {
            float timeLeft = getTimeLeft(timeLimit, timeGuessSubmitted);
            float percentage = timeLeft / timeLimit;
            int points = (int) (maxPoints * percentage);
            scores.updateScore(user, points);
        }
    }


    public void chatGPTSimilarityScore(User user, float similarityScore) throws Exception {
        
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
        
//        user.receivePoints(pointsAwarded);
    }
}
