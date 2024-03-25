package ch.uzh.ifi.hase.soprafs24.game;

import ch.uzh.ifi.hase.soprafs24.game.player.Player;
import java.util.ArrayList;
import java.util.List;


public class Game {
    
    private List<Player> players;
    private String lobbyId;

    public Game(long id) {
        this.players = new ArrayList<>();
        this.lobbyId = "roomId" + id;
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void removePlayer(Player player) {
        players.remove(player);
    }
    public String getLobbyId() {
        return lobbyId;
    }

}
