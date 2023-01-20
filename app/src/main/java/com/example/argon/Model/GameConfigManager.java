package com.example.argon.Model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @since 11-01-22
 * Represents a list of Game Configurations with
 operations such as add, delete, and edit game configs */

public class GameConfigManager implements Iterable<GameConfig> {

    /** List containing all added Game Configurations */
    private List<GameConfig> configs = new ArrayList<>();
    // Instance for Singleton support
    private static GameConfigManager instance;
    /**
     -1 --> Fruit Theme
     0 --> Animal Theme
     1 --> Planet Theme
     */
    private int themeSelected;
    private int themeColor;


    // Singleton Support
    private GameConfigManager() {
        themeSelected = -1;
    } // Private constructor so multiple instantiations are not possible
    public static GameConfigManager getInstance() {
        if (instance == null) {
            instance = new GameConfigManager();
        }
        return instance;
    }

    // set the current instance of the game to another game manager
    // used with shared preferences to keep instances of games which can be saved
    public static void setInstance(GameConfigManager gameConfigManager){
        instance = gameConfigManager;
    }

    // Main Methods

    /** Adds a new Game Config to the List
     * @param name name of new Game Config
     * @param poorScore poorScore of new Game Config
     * @param greatScore greatScore of new Game Config */
    public void addGameConfig(String name, int poorScore, int greatScore) {
        configs.add(new GameConfig(name, poorScore, greatScore));
    }

    /** Deletes a Game Config from the list
     * @param index index of Game Config to delete */
    public void deleteGameConfig(int index) {
        configs.remove(index);
    }

    /** Edits the name of a Game Config
     * @param index index of Game Config to delete
     * @param newName name to replace current name */
    public void editGameConfigName(int index, String newName) {
        configs.get(index).setName(newName);
    }

    /** Edits the poorScore of a Game Config and
     * updates the achievement earned of all previous games
     * because the levels are changed if poorScore is changed
     * @param index index of Game Config to delete
     * @param newPoor score to replace current poorScore */
    public void editGameConfigPoorScore(int index, int newPoor) {
        configs.get(index).setPoorScore(newPoor);
        configs.get(index).updateAllGamePlaysAchievementEarned();
    }

    /** Edits the greatScore of a Game Config
     * updates the achievement earned of all previous games
     * because the levels are changed if greatScore is changed
     * @param index index of Game Config to delete
     * @param newGreat score to replace current greatScore */
    public void editGameConfigGreatScore(int index, int newGreat) {
        configs.get(index).setGreatScore(newGreat);
        configs.get(index).updateAllGamePlaysAchievementEarned();
    }


    // Getters

    public List<GameConfig> getAllConfigs() {
        return configs;
    }

    public GameConfig getGameConfig(int index) {
        return configs.get(index);
    }


    // Iterator so "for(GameConfig g: GameConfigManager)" is possible
    @Override
    public Iterator<GameConfig> iterator() {
        return configs.iterator();
    }

    public int getSize(){
        return configs.size();
    }

    public int getThemeSelected() {
        return themeSelected;
    }

    public void setThemeSelected(int themeSelected) {
        this.themeSelected = themeSelected;
    }

    public int getThemeColor() {
        return themeColor;
    }

    public void setThemeColor(int themeColor) {
        this.themeColor = themeColor;
    }
}
