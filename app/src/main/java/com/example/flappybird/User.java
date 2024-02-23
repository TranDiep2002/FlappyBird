package com.example.flappybird;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class User implements Serializable {
    public String Id;
    public double LongLocation;
    public double LatLocation;
    public int Score;

    public User(String id, double longLocation, double latLocation, int score, int maxScore, String userName) {
        Id = id;
        LongLocation = longLocation;
        LatLocation = latLocation;
        Score = score;
        MaxScore = maxScore;
        UserName = userName;
    }

    public int MaxScore;

    public String UserName;


    public User() {
    }

    public double distance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
    public List<User> getNearestUsers(double myLat, double myLong, List<User> allUsers) {
        List<User> nearestUsers = new ArrayList<>(allUsers);

        // Sort users based on their distance from the given location
        Collections.sort(nearestUsers, Comparator.comparingDouble(user ->
                distance(myLat, myLong, user.LatLocation, user.LongLocation)));

        // Return the nearest 20 users
        return nearestUsers.subList(0, Math.min(20, nearestUsers.size()));
    }
}
