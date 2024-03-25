package ch.uzh.ifi.hase.soprafs24.service;

import org.bson.Document;
import org.springframework.stereotype.Service;
import ch.uzh.ifi.hase.soprafs24.game.Game;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import static com.mongodb.client.model.Filters.eq;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import io.github.cdimascio.dotenv.Dotenv;

@Service
public class GameService {
    
    Dotenv dotenv = Dotenv.load();
    String connectionString = dotenv.get("MONGO_DB_URL");
    MongoClient mongoClient = MongoClients.create(connectionString);
    MongoDatabase databaseGame = mongoClient.getDatabase("game");
    MongoDatabase databaseUsers = mongoClient.getDatabase("usersdb");
    MongoCollection<Document> collectionLobby = databaseGame.getCollection("lobby");
    MongoCollection<Document> collectionUsers = databaseUsers.getCollection("users");

    private static final Map<Long, Game> games = new HashMap<>();
    private long nextId = 1;

    public String fetchUserToken(MongoCollection<Document> collectionUser, String userToken) {
        Document user = collectionUser.find(eq("userToken", userToken))
                .first();

        try {
            // user = collectionUsers.find(eq("userToken", userToken)).first();
            if (user != null) {
                return user.getString("userToken");
        }} catch (Exception e) {
            System.out.println("Invalid User Token Error:" + e);
        }
        return null;
    } // fetchUserToken

    public Game createGame(String userToken) {
        
        try {
            long id = nextId++;
            Game game = new Game(id);
            String userTokenVerified = fetchUserToken(collectionUsers, userToken);
            if (userTokenVerified != null) {
                System.out.println("User Token Verified: " + userTokenVerified);
                Document lobbyCreated = new Document("lobbyId", game.getLobbyId());
                collectionLobby.insertOne(lobbyCreated);
                games.put(id, game);
                return game;
            } else {
                return null;
            }
        } catch (Exception e) {
                // Handle exception
            throw new RuntimeException("Error creating game: No user matches token" + e);
            }
        } 
}
        
