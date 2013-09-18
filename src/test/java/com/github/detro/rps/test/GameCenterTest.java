package com.github.detro.rps.test;

import com.github.detro.rps.GameCenter;
import com.github.detro.rps.Match;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class GameCenterTest {

    @Test
    public void shouldBeAbleToContainMatches() {
        GameCenter gCenter = new GameCenter();

        // Add 1 match and check it's there
        Match match = new Match("seed");
        gCenter.addMatch(match);
        assertTrue(gCenter.containsMatch(match.getId()));
        assertEquals(gCenter.getMatch(match.getId()), match);
        assertEquals(gCenter.getMatches().size(), 1);

        // Add another match and check it's there as well
        match = new Match("seed");
        gCenter.addMatch(match);
        assertTrue(gCenter.containsMatch(match.getId()));
        assertEquals(gCenter.getMatch(match.getId()), match);
        assertEquals(gCenter.getMatches().size(), 2);
    }
}
