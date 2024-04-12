package ch.uzh.ifi.hase.soprafs24.rest.dto;

import java.util.Date;

public class UserPostDTO {

  private String username;
  private String password;
  private boolean isLoggedIn;
  private Date birthDay;
  private String email;
  private String picture;
  private String userToken;
  private Integer score=0;

    // Getter
  public String getUserToken() {
        return userToken;
    }

    // Setter
  public void setUserToken(String userToken) {
        this.userToken = userToken;
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

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public boolean getIsLoggedIn() {
    return isLoggedIn;
  }

  public void setIsLoggedIn(boolean isLoggedIn) {
    this.isLoggedIn = isLoggedIn;
  }

  public Date getBirthDay() {
    return birthDay;
  }

  public void setBirthDay(Date birthDay) {
    this.birthDay = birthDay;
  }

  public Integer getScore() {
    return score;
  }

  public void setScore(Integer score) {
    this.score = score;
  }

}
