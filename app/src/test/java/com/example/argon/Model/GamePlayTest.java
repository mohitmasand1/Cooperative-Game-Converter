package com.example.argon.Model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

class GamePlayTest {
     /* Default attributes for testGamePlay:
    numPlayers: 3
    combScore: 100
    achieveEarned: Pompous Pigs
    Difficulty: 1.00
    playerScores: [50,50,50]
    */

    GamePlay createTestGamePlay(){
        List<Integer> playerScores = Arrays.asList(50,50,50);
        return new GamePlay(3, 100, "Pompous Pigs",
                1.00, playerScores);
    }

    String generateTimeString() {
        // Time of creation
        DateTimeFormatter format = DateTimeFormatter.ofPattern("MMM dd @ HH:mm a");
        LocalDateTime time = LocalDateTime.now();
        // String value of the time
        return time.format(format);
    }


    @Test
    void testEditPlayerScoresAndCombScore(){
        GamePlay testGamePlay = createTestGamePlay();
        List<Integer> expected = Arrays.asList(50, 50, 30);
        testGamePlay.editPlayerScoresAndCombScore(expected);
        assertEquals(130, testGamePlay.getCombScore());
    }


    @Test
    void testGetTimeCreated(){
        GamePlay testGamePlay = createTestGamePlay();
        assertEquals(generateTimeString(), testGamePlay.getTimeCreated());
    }

    @Test
    void testGetNumPlayers(){
        GamePlay testGamePlay = createTestGamePlay();
        assertEquals(3, testGamePlay.getNumPlayers());
    }

    @Test
    void testSetNumPlayers(){
        GamePlay testGamePlay = createTestGamePlay();
        testGamePlay.setNumPlayers(2);
        assertEquals(2, testGamePlay.getNumPlayers());
    }

    @Test
    void testGetCombScore(){
        GamePlay testGamePlay = createTestGamePlay();
        assertEquals(100, testGamePlay.getCombScore());
    }

    @Test
    void testSetCombScore(){
        GamePlay testGamePlay = createTestGamePlay();
        testGamePlay.setCombScore(200);
        assertEquals(200, testGamePlay.getCombScore());
    }

    @Test
    void testGetAchievedEarned(){
        GamePlay testGamePlay = createTestGamePlay();
        assertEquals("Pompous Pigs", testGamePlay.getAchieveEarned());
    }

    @Test
    void testSetAchievedEarned(){
        GamePlay testGamePlay = createTestGamePlay();
        testGamePlay.setAchieveEarned("Awesome Apples");
        assertEquals("Awesome Apples", testGamePlay.getAchieveEarned());
    }

    @Test
    void testSetPlayersScores(){
        GamePlay testGamePlay = createTestGamePlay();
        List<Integer> newPlayerScores = Arrays.asList(50, 50, 30);
        testGamePlay.setPlayerScores(newPlayerScores);
        assertEquals(newPlayerScores, testGamePlay.getPlayerScores());
    }

    @Test
    void testGetPlayerScores(){
        GamePlay testGamePlay = createTestGamePlay();
        List<Integer> expected = Arrays.asList(50,50,50);
        assertEquals(expected, testGamePlay.getPlayerScores());
    }

    @Test
    void testGetDifficulty(){
        GamePlay testGamePlay = createTestGamePlay();
        assertEquals(1.00, testGamePlay.getDifficulty());
    }

    @Test
    void testGetDifficultyAsString(){
        GamePlay testGamePlay = createTestGamePlay();
        assertEquals("Normal", testGamePlay.getDifficultyAsString());

        testGamePlay.setDifficulty(1.25);
        assertEquals("Hard", testGamePlay.getDifficultyAsString());

        testGamePlay.setDifficulty(0.75);
        assertEquals("Easy", testGamePlay.getDifficultyAsString());

        testGamePlay.setDifficulty(2.75);
        assertEquals("Normal", testGamePlay.getDifficultyAsString());
    }

    @Test
    void TestSetDifficulty(){
        GamePlay testGamePlay = createTestGamePlay();
        testGamePlay.setDifficulty(1.25);
        assertEquals(1.25, testGamePlay.getDifficulty());
    }
}