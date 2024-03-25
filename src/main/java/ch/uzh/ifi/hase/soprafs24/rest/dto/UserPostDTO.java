package ch.uzh.ifi.hase.soprafs24.rest.dto;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;

public class UserPostDTO {

  private String username;

  private String email;

  private String status;

  private String id;

  private String token;

  private String isHost;


  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

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



}
