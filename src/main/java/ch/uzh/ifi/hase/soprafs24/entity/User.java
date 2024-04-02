package ch.uzh.ifi.hase.soprafs24.entity;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;

/**
 * Internal User Representation
 * This class composes the internal representation of the user and defines how
 * the user is stored in the database.
 * Every variable will be mapped into a database field with the @Column
 * annotation
 * - nullable = false -> this cannot be left empty
 * - unique = true -> this value must be unqiue across the database -> composes
 * the primary key
 */
@Entity
@Table(name = "USER")
public class User implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue
  private Long id;

  @Column(nullable = false)
  private boolean isLoggedIn;

  @Column(nullable = false, unique = true)
  private String username;

  @Column(nullable = false, unique = true)
  private String userToken;

  @Column(nullable = false)
  private UserStatus status;

  @Column
  @CreationTimestamp
  private LocalDateTime createDate;

  @DateTimeFormat(pattern = "dd-MM-yyyy")
  @Temporal(TemporalType.DATE)
  @Column(name="date")
  private Date birthDay;

  @Column(nullable = false)
  private String password;

  private String email;

   private String picture;

  public User() {}
    public User(String username, String password){
        this.username = username;
        this.password = password;
    }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }



  public void setUsername(String username) {
    this.username = username;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPicture() {
    return picture;
  }

  public void setPicture(String picture) {
    this.picture = picture;
  }

  public String getUserToken() {
    return userToken;
  }

  public void setUserToken(String userToken) {
    this.userToken = userToken;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public UserStatus getStatus() {
    return status;
  }

  public void setStatus(UserStatus status) {
    this.status = status;
  }

  public boolean getIsLoggedIn() {
    return isLoggedIn;
  }

  public void setIsLoggedIn(boolean isLoggedIn) {
    this.isLoggedIn = isLoggedIn;
  }

  public LocalDateTime getCreateDate() {
    return createDate;
  }

  public void setCreateDate(LocalDateTime createDate) {
    this.createDate = createDate;
  }

  public Date getBirthDay() {
    return birthDay;
  }

  public void setBirthDay(Date birthDay) {
    this.birthDay = birthDay;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o){
      return true;
    }
    if (!(o instanceof User)){
      return false;
    }
    User user = (User) o;
    return Objects.equals(username, user.username) && Objects.equals(password, user.password);
  }

  @Override
  public int hashCode(){
    return Objects.hash(id, username, password, status, isLoggedIn, userToken);
  }

  @Override
  public String toString(){
    return "User:"+
            "Username:"+username+
            "status:"+status;
  }
}