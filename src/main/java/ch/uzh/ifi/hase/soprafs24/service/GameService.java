package ch.uzh.ifi.hase.soprafs24.service;

import org.springframework.stereotype.Service;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.game.lobby.Lobby;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GameService {

    private static final Map<Long, Lobby> lobbies = new HashMap<>();
    private UserService userService;
    private long nextId = 1;    

    public boolean fetchUserToken(List<User> users, String userToken) {
        for (User user : users) {
            if (user.getUserToken().equals(userToken)) {
                return true;
            }
        }
        return false;
    }

    public String createGame(String userToken) {
        List<User> users = userService.getAllUsers();
        try {
            long id = nextId++;
            boolean userTokenVerified = fetchUserToken(users, userToken);
            if (userTokenVerified) {
                System.out.println("User Token Verified: " + userTokenVerified);
                Lobby lobbyCreated = new Lobby(id);
                lobbies.put(id, lobbyCreated);
                
                return lobbyCreated.getLobbyId();
            } else {
                System.out.println("Error creating game: No user matches token");
                return null;
            }
        } catch (Exception e) {
                // Handle exception
            throw new RuntimeException("Something went wrong creating game: " + e);
            }
        } 
}
        
