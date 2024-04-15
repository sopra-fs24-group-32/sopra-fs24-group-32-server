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

    public Round() {
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
}
