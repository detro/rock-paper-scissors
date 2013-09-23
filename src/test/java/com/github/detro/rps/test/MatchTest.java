package com.github.detro.rps.test;

import com.github.detro.rps.Match;
import com.github.detro.rps.PvPMatch;
import com.github.detro.rps.Weapons;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertNotNull;

public class MatchTest {

    @DataProvider(name = "matchAndResultProvider")
    public Object[][] matchAndResultProvider() {
        return new Object[][] {
                { "player1ID", 1, "player2ID", 0, "player1ID" },
                { "player1ID", 1, "player2ID", 2, "player2ID" },
                { "player1ID", 2, "player2ID", 2, "draw" },
                { "player1ID", 0, "player2ID", 2, "player1ID" },
                { "player1ID", 1, "player2ID", 1, "draw" },
                { "player1ID", 0, "player2ID", 0, "draw" },
        };
    }

    @Test(dataProvider = "matchAndResultProvider")
    public void shouldReturnTheCorrectMatchResult(
            String player1id, int player1weapon,
            String player2id, int player2weapon,
            String winner) {

        Match match = new PvPMatch(player1id + player2id);
        assertNotNull(match.getId());
        assertEquals(match.getPlayersAmount(), 0);
        assertEquals(match.getStatus(), Match.NO_PLAYERS_YET);

        match.addPlayer(player1id);
        assertEquals(match.getPlayersAmount(), 1);
        assertEquals(match.getStatus(), Match.WAITING_FOR_ANOTHER_PLAYER);
        assertTrue(match.containsPlayer(player1id));

        match.addPlayer(player2id);
        assertEquals(match.getPlayersAmount(), 2);
        assertEquals(match.getStatus(), Match.WAITING_PLAYERS_WEAPONS);

        match.setPlayerWeapon(player2id, player2weapon);
        assertEquals(match.getStatus(), Match.WAITING_PLAYERS_WEAPONS);
        match.setPlayerWeapon(player1id, player1weapon);
        assertEquals(match.getStatus(), Match.PLAYED);

        assertEquals(match.getWinningPlayer(), winner);
    }

    @Test(dataProvider = "matchAndResultProvider")
    public void shouldReturnTheCorrectMatchResultAfterReset(
            String player1id, int player1weapon,
            String player2id, int player2weapon,
            String winner) {

        Match match = new PvPMatch(player1id + player2id);

        // first match
        match.addPlayer(player1id);
        match.addPlayer(player2id);
        assertEquals(match.getStatus(), Match.WAITING_PLAYERS_WEAPONS);
        match.setPlayerWeapon(player2id, player2weapon);
        match.setPlayerWeapon(player1id, player1weapon);
        assertEquals(match.getStatus(), Match.PLAYED);
        assertEquals(match.getWinningPlayer(), winner);

        // reset match
        match.reset();
        assertEquals(match.getStatus(), Match.WAITING_PLAYERS_WEAPONS);
        match.setPlayerWeapon(player1id, player1weapon);
        match.setPlayerWeapon(player2id, player2weapon);
        assertEquals(match.getStatus(), Match.PLAYED);
        assertEquals(match.getWinningPlayer(), winner);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void shouldNotAllowToAddSamePlayerTwice() {
        Match match = new PvPMatch("seed");
        match.addPlayer("player");
        match.addPlayer("player");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void shouldNotAllowToAddMoreThan2Players() {
        Match match = new PvPMatch("seed");
        match.addPlayer("player1");
        match.addPlayer("player2");
        match.addPlayer("player3");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void shouldNotAllowPlayerNotInTheMatchToSetTheirWeapon() {
        Match match = new PvPMatch("seed");
        match.addPlayer("player1");
        match.addPlayer("player2");

        match.setPlayerWeapon("player3", Weapons.pickRandomWeapon());
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void shouldNotAllowToSetPlayerWeaponBefore2PlayersHaveBeenAdded() {
        Match match = new PvPMatch("seed");
        match.addPlayer("player1");
        match.setPlayerWeapon("player1", Weapons.pickRandomWeapon());
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void shouldNotAllowToGetMatchResultBeforeBothPlayersHaveSetTheirWeapon() {
        Match match = new PvPMatch("seed");
        match.addPlayer("player1");
        match.addPlayer("player2");
        match.setPlayerWeapon("player1", Weapons.pickRandomWeapon());
        match.getWinningPlayer();
    }
}
