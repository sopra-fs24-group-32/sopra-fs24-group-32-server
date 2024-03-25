package ch.uzh.ifi.hase.soprafs24.rest.dto;

<<<<<<< HEAD
import java.util.Date;
=======
import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
>>>>>>> f322693062fbc635ab0b52e2f858a0f1d072cb65

public class UserPostDTO {

  private String username;
  private String password;
  private boolean isLoggedIn;
  private Date birthDay;
  private String email;
  private String picture;
  private String userToken;

<<<<<<< HEAD
    // Getter
  public String getUserToken() {
        return userToken;
    }

    // Setter
  public void setUserToken(String userToken) {
        this.userToken = userToken;
    }
=======
  private String email;

  private String status;

  private String id;

  private String token;

  private String isHost;

>>>>>>> f322693062fbc635ab0b52e2f858a0f1d072cb65

  public String getUsername() {
    return username;
  }


  public void setUsername(String username) {
    this.username = username;
  }

<<<<<<< HEAD
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

=======
  public String getToken(){
      return token;
  }

  public void setToken(String token){
      this.token = token;
  }

  public String getEmail(){
      return email;
  }

  public void setEmail(String email){
      this.email = email;
  }

  public String getIsHost(){
      return isHost;
  }

  public void setIsHost(String isHost){
      this.isHost = isHost;
  }

  public String getId(){
      return id;
  }

  public void setId(String id){
      this.id = id;
  }

  public String getStatus(){
      return status;
  }

  public void setStatus(String status){
      this.status = status;
  }



>>>>>>> f322693062fbc635ab0b52e2f858a0f1d072cb65
}
