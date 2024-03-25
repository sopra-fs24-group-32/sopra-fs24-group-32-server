// package ch.uzh.ifi.hase.soprafs24.repository;

// import ch.uzh.ifi.hase.soprafs24.entity.User;
// import org.springframework.data.mongodb.repository.MongoRepository;
// import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.stereotype.Repository;

// // @EnableMongoRepositories(basePackages = "ch.uzh.ifi.hase.soprafs24.repository.UserRepository")
// // public interface UserRepository extends MongoRepository<User, Long> {
// //   User findByName(String name);

// //   User findByUsername(String username);
// // }

// @Repository("userRepository")
// public interface UserRepository extends JpaRepository<User, Long> {
//   User findByUsername(String username);
// }