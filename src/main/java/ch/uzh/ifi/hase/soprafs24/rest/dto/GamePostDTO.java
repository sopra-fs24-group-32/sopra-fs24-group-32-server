package ch.uzh.ifi.hase.soprafs24.rest.dto;


import ch.uzh.ifi.hase.soprafs24.entity.User;

import java.util.List;
import java.util.Map;

public class GamePostDTO {



    // Field Declarations
    private Long id;
    private String lobbyOwner;
    private Integer amtOfRounds;
    private Float timeLimit;
    private Map<String, Integer> scores; // Assuming scores is a map for demonstration
    private String username;
    private List<User> users;
    private Integer maxAmtUsers;

    // Getter and Setter Methods

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLobbyOwner() {
        return lobbyOwner;
    }

    public void setLobbyOwner(String lobbyOwner) {
        this.lobbyOwner = lobbyOwner;
    }

    public Integer getAmtOfRounds() {
        return amtOfRounds;
    }

    public void setAmtOfRounds(Integer amtOfRounds) {
        this.amtOfRounds = amtOfRounds;
    }

    public Float getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(Float timeLimit) {
        this.timeLimit = timeLimit;
    }

    public Map<String, Integer> getScores() {
        return scores;
    }

    public void setScores(Map<String, Integer> scores) {
        this.scores = scores;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public Integer getMaxAmtUsers() {
        return maxAmtUsers;
    }

    public void setMaxAmtUsers(Integer maxAmtUsers) {
        this.maxAmtUsers = maxAmtUsers;
    }




}
