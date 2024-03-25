package ch.uzh.ifi.hase.soprafs24.game.player;

public class Player {
    private Long id;
    private String username;
    private int score = 0;

    public Player(String name, Long id, String username) {
        this.username = username;
        this.id = id;
    }

    public int getScore() {
        return score;
    }

    public void addScore(int points) {
        score += points;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getId() {
        return id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

}
