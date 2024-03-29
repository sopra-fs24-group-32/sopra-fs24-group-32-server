package ch.uzh.ifi.hase.soprafs24.entity;

public class Player extends User{
    protected int points = 0;

    public void receivePoint(int points) throws Exception {
        if(points < 0){
            throw new Exception("points cannot be negative");
        }
        points += points;
    }

}
