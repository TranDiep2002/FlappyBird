package com.example.flappybird;

import java.io.Serializable;

public class User implements Serializable {
    public String Id;
    public double LongLocation;
    public double LatLocation;
    public int Score;

    public String UserName;

    public User(String id, double longLocation, double latLocation, int score, String userName) {
        Id = id;
        LongLocation = longLocation;
        LatLocation = latLocation;
        Score = score;
        UserName = userName;
    }

    public User() {
    }
}
