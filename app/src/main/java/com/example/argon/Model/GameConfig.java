package com.example.argon.Model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.example.argon.MainActivity;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @since 11-01-22
 * Represents a single Game Configuration with it's dedicated
 achievement level calculator */

public class GameConfig implements Iterable<GamePlay> {

    /** Names of all achievement levels in order of lowest minimum score to highest minimum score */
    public static String[] achievementFruits = {"Blasphemous Bananas", "Pompous Plums", "Terrible Tomatoes", "Mediocre Mangoes", "Mid Melons",
            "Swaggy Strawberries", "Powerful Persimmons", "Epic Eggplants", "Great Grapes", "Awesome Apples"};
    public static String[] achievementAnimals = {"Bad Bears", "Pompous Pigs", "Terrible Tigers", "Mediocre Men", "Mid Monkeys", "Swaggy Snake",
            "Powerful Pandas", "Epic Elephants", "Great Giraffes", "Awesome Anteaters"};
    public static String[] achievementPlanets = {"Poopy Pluto", "Nerdy Neptune", "Ugly Uranus", "Sad Saturn", "Junky Jupiter", "Magical Mars",
            "Epic Earth", "Vital Venus", "Marvelous Mercury", "Super Sun"};
    private int[] achievementTime;
    /** Number of Achievement levels decided by Argon */
    private static final int NUM_ACHIEVEMENTS = 10;
    /** List of all game plays of this particular configuration */
    private List<GamePlay> gamesPlayed = new ArrayList<>();
    /** Name of configuration */
    private String name;
    /** poorScore of configuration */
    private int poorScore;
    /** greatScore of configuration */
    private int greatScore;

    private String boardImg;


    // Constructor
    GameConfig(String name, int poorScore, int greatScore) {
        this.name = name;
        this.poorScore = poorScore;
        this.greatScore = greatScore;
        achievementTime = new int[10];
        this.boardImg = "";
    }


    // Main Methods
    /** Adds a game play to list of game plays (gamesPlayed)
     * @param numPlayers number of players that played the game
     * @param combScore combined score of all players */
    public void addGamePlay(int numPlayers, int combScore, double difficulty, List<Integer> playerScores) {
        // Also calculates the achievement level of all players
        String earned = calculateAchieveEarned(numPlayers, combScore, difficulty);
        gamesPlayed.add(new GamePlay(numPlayers, combScore, earned, difficulty, playerScores));
        changeAchieveTimesEarned(1, earned);
    }

    /** Alters the difficulty of the game play as well as recalculates the new achievement earned
        based on this new difficulty */
    public void editGameDifficultyAndAchieve(int index, double newDifficulty) {
        String oldEarned = gamesPlayed.get(index).getAchieveEarned();
        changeAchieveTimesEarned(-1, oldEarned);
        int numPlayers = gamesPlayed.get(index).getNumPlayers();
        int combScore = gamesPlayed.get(index).getCombScore();
        String earned = calculateAchieveEarned(numPlayers, combScore, newDifficulty);
        gamesPlayed.get(index).setDifficulty(newDifficulty);
        gamesPlayed.get(index).setAchieveEarned(earned);
        changeAchieveTimesEarned(1, earned);
    }

    /** Alters the player scores of the game play as well as recalculates the new achievement earned
     based on this new difficulty */
    public void editPlayerScoresAndAchieve(int index, List<Integer> playerScores) {
        String oldEarned = gamesPlayed.get(index).getAchieveEarned();
        changeAchieveTimesEarned(-1, oldEarned);
        gamesPlayed.get(index).editPlayerScoresAndCombScore(playerScores);
        int numPlayers = gamesPlayed.get(index).getNumPlayers();
        int combScore = gamesPlayed.get(index).getCombScore();
        double difficulty = gamesPlayed.get(index).getDifficulty();
        String earned = calculateAchieveEarned(numPlayers, combScore, difficulty);
        gamesPlayed.get(index).setAchieveEarned(earned);
        changeAchieveTimesEarned(1, earned);
    }

    // Helper Methods

    /** Generates an Array of all minimum scores of the Achievement levels
     (in order of least to greatest) i.e. (0, 20, 40, 60, 80, 100, 120, 140, 160, 180))
     with respect to the poorScore and greatScore of the Game Config */
    public int[] calculateAchievementMins(int numPlayers, double difficulty) {
        int[] levels = new int[10];
        levels[0] = 0; // first minimum score is the worst score possible (0)
        levels[1] = (int) ((poorScore * numPlayers) * difficulty); // second minimum score
        // the difference between all minimum scores except the first and second
        int incremental = (int) ((((greatScore - poorScore) / (NUM_ACHIEVEMENTS - 2)) * numPlayers) * difficulty);

        for (int i = 2; i < NUM_ACHIEVEMENTS - 1; i++) { // fills the rest of the minimum scores
            levels[i] = levels[i-1] + incremental;
        }
        // last and highest minimum score is the great score
        levels[NUM_ACHIEVEMENTS -1] = (int) ((greatScore * numPlayers) * difficulty);
        return levels;
    }


    /** Finds which achievement level the given combination of score
     lies in by using the above function and then finding where combScore
     lands in the array */
    private String calculateAchieveEarned(int numPlayers, int combScore, double difficulty) {
        String levelAchieved = null;
        int[] levels = calculateAchievementMins(numPlayers, difficulty); // get array of achievement levels

        for (int i = 1; i < levels.length; i++) { // checks which level fits the score
            if (combScore >= levels[i - 1] && combScore < levels[i]) {
                levelAchieved = getAchievementEarnedBasedOnTheme(GameConfigManager.getInstance().getThemeSelected(), i);
            }
        }
        // if score is bigger than the largest minimum, then it fits into the last level
        if (combScore >= levels[NUM_ACHIEVEMENTS - 1]) {
            levelAchieved = getAchievementEarnedBasedOnTheme(GameConfigManager.getInstance().getThemeSelected(), NUM_ACHIEVEMENTS);
        }
        return levelAchieved;
    }

    /** Updates all the Achievements Earned of the previous game plays
     given that the user has edited a gameConfig's poorScore or greatScore */

    public void updateAllGamePlaysAchievementEarned() {
        for (GamePlay g: gamesPlayed) { // uses above function to find the new achievement level
            String oldEarned = g.getAchieveEarned();
            changeAchieveTimesEarned(-1, oldEarned);
            String newEarned = calculateAchieveEarned(g.getNumPlayers(), g.getCombScore(), g.getDifficulty());
            g.setAchieveEarned(newEarned);
            changeAchieveTimesEarned(1, newEarned);
        }
    }

    private String getAchievementEarnedBasedOnTheme(int theme, int position) {
        if (theme == MainActivity.FRUIT_THEME) {
            return achievementFruits[position - 1];
        } else if (theme == MainActivity.ANIMAL_THEME) {
            return achievementAnimals[position - 1];
        } else if (theme == MainActivity.PLANET_THEME) {
            return achievementPlanets[position - 1];
        }
        return achievementFruits[position - 1];
    }

    public void updateAchieveEarnedBasedOnTheme() {
        for (GamePlay g: gamesPlayed) {
            g.setAchieveEarned(calculateAchieveEarned(g.getNumPlayers(), g.getCombScore(), g.getDifficulty()));
        }
    }

    private void changeAchieveTimesEarned(int changeBy, String earned) {
        for (int i = 0; i < achievementTime.length; i++) {
            if (achievementAnimals[i].equals(earned) || achievementFruits[i].equals(earned) || achievementPlanets[i].equals(earned)) {
                achievementTime[i]+=changeBy;
            }
        }
    }


    // Getters and Setters for all 4 attributes

    // ** USE THIS TO GET ALL PREVIOUS GAMES PLAYED in Oldest to Newest order
    public List<GamePlay> getAllGamesPlayed() {
        return gamesPlayed;
    }

    public GamePlay getGamePlay(int index) {
        return gamesPlayed.get(index);
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPoorScore() {
        return poorScore;
    }

    public void setPoorScore(int poorScore) {
        this.poorScore = poorScore;
    }

    public int getGreatScore() {
        return greatScore;
    }

    public void setGreatScore(int greatScore) {
        this.greatScore = greatScore;
    }


    // Iterator so "for(GamePlay g: GameConfig)" is possible
    @Override
    public Iterator<GamePlay> iterator() {
        return gamesPlayed.iterator();
    }


    public String[] getAchievementFruits() {
        return achievementFruits;
    }


    public String[] getAchievementAnimals() {
        return achievementAnimals;
    }


    public String[] getAchievementPlanets() {
        return achievementPlanets;
    }

    public int getAchievementTime(int index) {
        return achievementTime[index];
    }

    /** Converts string format of image to Bitmap Image */
    public Bitmap getBoardImg() {
        byte[] decodedString = Base64.decode(boardImg, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }

    /** Converts Bitmap image to string format */
    public void setBoardImg(Bitmap memory) {
        final int COMPRESSION_QUALITY = 100;
        ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
        memory.compress(Bitmap.CompressFormat.PNG, COMPRESSION_QUALITY,
                byteArrayBitmapStream);
        byte[] b = byteArrayBitmapStream.toByteArray();
        this.boardImg = Base64.encodeToString(b, Base64.DEFAULT);
    }
}
