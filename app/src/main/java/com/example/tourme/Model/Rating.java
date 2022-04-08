package com.example.tourme.Model;

public class Rating {

    double rating;
    String ratingDescription;

    public Rating(){

    }

    public Rating(double rating, String ratingDescription){
        this.rating = rating;
        this.ratingDescription = ratingDescription;
    }

    public double getRating() { return rating; }

    public void setRating(double rating) { this.rating = rating; }

    public String getRatingDescription() { return ratingDescription; }

    public void setRatingDescription(String ratingDescription) { this.ratingDescription = ratingDescription; }
}
