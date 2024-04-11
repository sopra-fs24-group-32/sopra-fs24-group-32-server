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

import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.game.score.Score;

public class Round {

    private String inputPhrase;
    private Clock timer;
    private Map<Player, Map<String, Duration>> guessedInputs;
    private List<Player> players;
    private Iterator<Player> iterator;
    private Player pictureGenerator;
    @OneToMany
    private List<Player> remainingGenerators;
    // private ChatGPT chatGPT;
    private Score scores;

    public Round(List<Player> players) {
        this.players = new ArrayList<>(players);
        this.guessedInputs = new HashMap<>();
    }

    public void startNewRound(List<Player> players, float timeLimit, Score scores) {
        this.players = new ArrayList<>(players);
        Collections.shuffle(this.players);
        this.iterator = this.players.iterator();
        this.scores = scores;

        Player nextPlayer = nextGenerator();
        this.pictureGenerator = nextPlayer;
        // this.remainingGenerators = new ArrayList<>(players);
        // this.remainingGenerators.remove(nextPlayer);



    }

    public Player nextGenerator() {
        if (!iterator.hasNext()) {
            iterator = players.iterator();
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
        return timeLimit - timeGuessSubmitted;
    }


    public int chatGPTSimilarityScore( float similarityScore) throws Exception {
        
        if (similarityScore < 0 || similarityScore > 1) {
            throw new IllegalArgumentException("Similarity score must be between 0 and 1.");
        }
        
        int pointsAwarded;
        if (similarityScore >= 0.75) {
            pointsAwarded = 6;
        } else if (similarityScore >= 0.5) {
            pointsAwarded = 4;
        } else if (similarityScore >= 0.25) {
            pointsAwarded = 2;
        } else {
            pointsAwarded = 0;
        }
        return pointsAwarded;
    }

    public void scalePointsByDuration(Player player, int similarityScore, float timeLimit, float timeGuessSubmitted) throws Exception {
        

        int pointsAwarded = chatGPTSimilarityScore(similarityScore);

        float timeLeft = getTimeLeft(timeLimit, timeGuessSubmitted);
        float bonusPoints = 0;

        if (timeLeft <= 0) {
            player.receivePoints(0);
        } else {
            float percentageOfTimeLeft = timeLeft / timeLimit;
            if (percentageOfTimeLeft >= 0.75) {
                bonusPoints = 0.25f;
            } else if (percentageOfTimeLeft >= 0.5) {
                bonusPoints = 0.10f;
            }

            int finalPointsAwarded = (int) (pointsAwarded + (pointsAwarded * bonusPoints));
            player.receivePoints(finalPointsAwarded);
        }
    }
}
