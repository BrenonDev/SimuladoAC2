package com.example.simuladoac2;

public class Movie {
    private String id;
    private String name;
    private String type;
    private String genre;
    private int releaseYear;
    private double rating;
    private boolean watched;

    public Movie() {
    }

    public Movie(
            String name,
            String type,
            String genre,
            int releaseYear,
            double rating,
            boolean watched
    ) {
        this.name = name;
        this.type = type;
        this.genre = genre;
        this.releaseYear = releaseYear;
        this.rating = rating;
        this.watched = watched;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(int releaseYear) {
        this.releaseYear = releaseYear;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public boolean isWatched() {
        return watched;
    }

    public void setWatched(boolean watched) {
        this.watched = watched;
    }
}
