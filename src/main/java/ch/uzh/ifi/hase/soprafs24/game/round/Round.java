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

    
    public void scalePointsByDuration(User user, float similarityScore, float timeLimit, float timeGuessSubmitted) throws Exception {

        int pointsAwarded = chatGPTSimilarityScore(similarityScore);

        float timeLeft = getTimeLeft(timeLimit, timeGuessSubmitted);
        float bonusPoints = 0;

        if (timeLeft <= 0) {
            user.updatedScore(0);
        } else {
            float percentageOfTimeLeft = timeLeft / timeLimit;
            if (percentageOfTimeLeft >= 0.75) {
                bonusPoints = 0.25f;
            } else if (percentageOfTimeLeft >= 0.5) {
                bonusPoints = 0.10f;
            }

            int finalPointsAwarded = (int) (pointsAwarded + (pointsAwarded * bonusPoints));
            user.updatedScore(finalPointsAwarded);
        }
    }


    public int chatGPTSimilarityScore(float similarityScore) throws Exception {
        
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

        return pointsAwarded;        
    }
}
