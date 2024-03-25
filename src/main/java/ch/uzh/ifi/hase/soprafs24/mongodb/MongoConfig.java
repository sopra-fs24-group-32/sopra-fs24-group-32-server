package ch.uzh.ifi.hase.soprafs24.mongodb;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import io.github.cdimascio.dotenv.Dotenv;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
// import com.mongodb.ServerApi;
// import com.mongodb.ServerApiVersion;


// @Configuration
// @EnableMongoRepositories(basePackages = "ch.uzh.ifi.hase.soprafs24.repository.UserRepository")
// public class MongoConfig extends AbstractMongoClientConfiguration {

//     @Override
//     protected String getDatabaseName() {
//         return "fs24_group32";
//     }

//     @Override
//     protected void configureClientSettings(MongoClientSettings.Builder builder) {
        
//         // String connectionString = System.getenv("MONGO_DB_URL");
//         if (connectionString == null || connectionString.isEmpty()) {
//             throw new IllegalStateException("-----------------------------------------------------MONGO_DB_URL is not set.");
//         }
//         // MongoClient mongoClient = MongoClients.create(connectionString);
//         // MongoDatabase database = mongoClient.getDatabase("gpt_uesser");
//         // MongoCollection collection = database.getCollection("users");
//         builder.applyConnectionString(new ConnectionString(connectionString));
//     }

// }



// import com.mongodb.ConnectionString;
// import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
// import com.mongodb.ServerApi;
// import com.mongodb.ServerApiVersion;
// import com.mongodb.client.MongoClient;
// import com.mongodb.client.MongoClients;
// import com.mongodb.client.MongoDatabase;
import org.bson.Document;

// @Configuration
// // @EnableMongoRepositories(basePackages = "ch.uzh.ifi.hase.soprafs24.repository")
// public class MongoConfig {

//     public void initializeMongoDB() {

//         Dotenv dotenv = Dotenv.load();
//         String connectionString = dotenv.get("MONGO_DB_URL");

//         // ServerApi serverApi = ServerApi.builder()
//         //         .version(ServerApiVersion.V1)
//         //         .build();

//         MongoClientSettings settings = MongoClientSettings.builder()
//                 .applyConnectionString(new ConnectionString(connectionString))
//                 // .serverApi(serverApi)
//                 .build();

//         // Create a new client and connect to the server
//         try (MongoClient mongoClient = MongoClients.create(settings)) {
//             try {
//                 // Send a ping to confirm a successful connection
//                 MongoDatabase database = mongoClient.getDatabase("roger");
//                 database.runCommand(new Document("ping", 1));
//                 System.out.println("Pinged your deployment. You successfully connected to MongoDB!-----------------------------------");
//             } catch (MongoException e) {
//                 e.printStackTrace();
//             }
//         }
//     }
// }
