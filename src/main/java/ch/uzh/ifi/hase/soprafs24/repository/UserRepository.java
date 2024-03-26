package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.User;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

// // @EnableMongoRepositories(basePackages = "ch.uzh.ifi.hase.soprafs24.repository.UserRepository")
// // public interface UserRepository extends MongoRepository<User, Long> {
// //   User findByName(String name);

// //   User findByUsername(String username);
// // }
@Configuration
@Repository("userRepository")
public interface UserRepository extends MongoRepository<User, Long> {
}