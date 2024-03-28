package ch.uzh.ifi.hase.soprafs24.game.player;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name = "player")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;
    @Column(nullable = false)
    private int score = 0;

    public Player() {
    }
    public Player(int score) {
        this.score = score;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int points) {
        score += points;
    }

    // public void setId(Long id) {
    //     this.id = id;
    // }
    // public Long getId() {
    //     return id;
    // }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

}
