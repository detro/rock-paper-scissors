package com.github.detro.rps.test;

import com.github.detro.rps.GameCenter;
import com.github.detro.rps.Match;
import com.github.detro.rps.PvPMatch;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class GameCenterTest {

    @Test
    public void shouldBeAbleToContainMatches() {
        GameCenter gCenter = new GameCenter();

        // Add 1 match and check it's there
        Match match = new PvPMatch("seed1");
        gCenter.addMatch(match);
        assertTrue(gCenter.containsMatch(match.getId()));
        assertEquals(gCenter.getMatch(match.getId()), match);
        assertEquals(gCenter.getMatches().size(), 1);

        // Add another match and check it's there as well
        match = new PvPMatch("seed2");
        gCenter.addMatch(match);
        assertTrue(gCenter.containsMatch(match.getId()));
        assertEquals(gCenter.getMatch(match.getId()), match);
        assertEquals(gCenter.getMatches().size(), 2);
    }

    @Test
    public void shouldBeAbleToFindAllTheMatchesAPlayerIsPlaying() {
        GameCenter gCenter = new GameCenter();

        // Add 1 match without any user
        Match match = new PvPMatch("seed1");
        gCenter.addMatch(match);

        // Add 1 match with a player "player1Id"
        match = new PvPMatch("seed2");
        match.addPlayer("player1Id");
        gCenter.addMatch(match);

        // Add 1 match with 2 players, one of which is also in another match
        match = new PvPMatch("seed3");
        match.addPlayer("player1Id");
        match.addPlayer("player2Id");
        gCenter.addMatch(match);

        // Add 1 match with 2 players that are in no other match
        match = new PvPMatch("seed4");
        match.addPlayer("player3Id");
        match.addPlayer("player4Id");
        gCenter.addMatch(match);

        assertEquals(gCenter.getMatches("player1Id").size(), 2);
        assertEquals(gCenter.getMatches("player2Id").size(), 1);
        assertEquals(gCenter.getMatches("player3Id").size(), 1);
        assertEquals(gCenter.getMatches("player4Id").size(), 1);
        assertEquals(gCenter.getMatches("anotherPlayer").size(), 0);
    }

    @Test
    public void shouldBeAbleToFindAllMatchesAPlayerCouldJoin() {
        GameCenter gCenter = new GameCenter();

        // Add 1 match without any user
        Match match = new PvPMatch("seed1");
        gCenter.addMatch(match);

        // Add 1 match with a player "player1Id"
        match = new PvPMatch("seed2");
        match.addPlayer("player1Id");
        gCenter.addMatch(match);

        // Add 1 match with 2 players, one of which is also in another match
        match = new PvPMatch("seed3");
        match.addPlayer("player1Id");
        match.addPlayer("player2Id");
        gCenter.addMatch(match);

        // Add 1 match with 2 players that are in no other match
        match = new PvPMatch("seed4");
        match.addPlayer("player3Id");
        match.addPlayer("player4Id");
        gCenter.addMatch(match);
        System.out.println(match.getStatus());

        assertEquals(gCenter.getAvailableMatches("player1Id").size(), 1);
        assertEquals(gCenter.getAvailableMatches("player2Id").size(), 2);
        assertEquals(gCenter.getAvailableMatches("player3Id").size(), 2);
        assertEquals(gCenter.getAvailableMatches("player4Id").size(), 2);
    }
}
