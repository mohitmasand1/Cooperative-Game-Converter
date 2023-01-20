package com.example.argon.Model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * @since 11-01-22
 * Represents a single Game Play of a particular
 Game Configuration */

public class GamePlay {

    /** Number of players in game play */
    private int numPlayers;
    /** Combined score of players */
    private int combScore;
    /** Name of achievement level which all players achieved */
    private String achieveEarned;
    /**
     0.75 --> easy
     1.00  --> normal
     1.25  --> hard
     */
    private double difficulty;

    /** List of all player scores */
    private List<Integer> playerScores = new ArrayList<>();

    /** String type of time and date of game creation */
    private final String now;

    /** Camera taken image for this game play */
    private String memory;


    // Constructor
    GamePlay(int numPlayers, int combScore, String achieveEarned, double difficulty, List<Integer> playerScores) {
        this.numPlayers = numPlayers;
        this.combScore = combScore;
        this.achieveEarned = achieveEarned;
        this.difficulty = difficulty;
        this.now = generateTimeString();
        setPlayerScores(playerScores);
        this.memory = "";
    }

    protected void editPlayerScoresAndCombScore(List<Integer> newPlayerScores) {
        this.combScore = newPlayerScores.stream().mapToInt(Integer::intValue).sum();
        this.numPlayers = newPlayerScores.size();
    }

    private String generateTimeString() {
        // Time of creation
        DateTimeFormatter format = DateTimeFormatter.ofPattern("MMM dd @ HH:mm a");
        LocalDateTime time = LocalDateTime.now();
        // String value of the time
        return time.format(format);
    }


    // Getters and Setters for both attributes

    public int getNumPlayers() {
        return numPlayers;
    }

    public void setNumPlayers(int numPlayers) {
        this.numPlayers = numPlayers;
    }

    public int getCombScore() {
        return combScore;
    }

    public void setCombScore(int combScore) {
        this.combScore = combScore;
    }

    public String getAchieveEarned() {
        return achieveEarned;
    }

    public void setAchieveEarned(String achieveEarned) {
        this.achieveEarned = achieveEarned;
    }

    public void setPlayerScores(List<Integer> playerScores){
        this.playerScores.clear();
        this.playerScores.addAll(playerScores);
    }

    public List<Integer> getPlayerScores(){
        return playerScores;
    }

    public String getTimeCreated() {
        return now;
    }

    public double getDifficulty() {
        return difficulty;
    }

    public String getDifficultyAsString() {
        if(difficulty == 0.75){
            return "Easy";
        } else if(difficulty == 1.25){
            return "Hard";
        } else{
            return "Normal";
        }
    }

    public void setDifficulty(double difficulty) {
        this.difficulty = difficulty;
    }

    /** Converts string format of image to Bitmap Image */
    public Bitmap getMemory() {
        byte[] decodedString = Base64.decode(memory, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }

    /** Converts Bitmap image to string format */
    public void setMemory(Bitmap memory) {
        final int COMPRESSION_QUALITY = 100;
        ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
        memory.compress(Bitmap.CompressFormat.PNG, COMPRESSION_QUALITY,
                byteArrayBitmapStream);
        byte[] b = byteArrayBitmapStream.toByteArray();
        this.memory = Base64.encodeToString(b, Base64.DEFAULT);
    }
}
