package com.example.tourme.Model;

public class Comment {

    private double rating;
    private String ratingDescription;
    private String userid;

    public Comment(){

    }

    public Comment(double rating, String ratingDescription, String userid) {
        this.rating = rating;
        this.ratingDescription = ratingDescription;
        this.userid = userid;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getRatingDescription() {
        return ratingDescription;
    }

    public void setRatingDescription(String ratingDescription) { this.ratingDescription = ratingDescription; }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }
}
