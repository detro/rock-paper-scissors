package com.github.detro.rps.test;

import com.github.detro.rps.Match;
import com.github.detro.rps.PvCMatch;
import com.github.detro.rps.Weapons;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertNotNull;

public class PvCMatchTest {

    @DataProvider(name = "pvcMatchProvider")
    public Object[][] pvcMatchProvider() {
        return new Object[][] {
                { "player1ID", 1 },
                { "player1ID", 1 },
                { "player1ID", 2 },
                { "player1ID", 0 },
                { "player1ID", 1 },
                { "player1ID", 0 },
        };
    }

    @Test(dataProvider = "pvcMatchProvider")
    public void shouldPlayPvCMatch (String player1id, int player1weapon) {
        Match match = new PvCMatch(player1id + PvCMatch.COMPUTER_PLAYER_ID);
        assertNotNull(match.getId());
        assertEquals(match.getPlayersAmount(), 1);
        assertEquals(match.getStatus(), Match.WAITING_FOR_ANOTHER_PLAYER);

        match.addPlayer(player1id);
        assertEquals(match.getPlayersAmount(), 2);
        assertEquals(match.getStatus(), Match.WAITING_PLAYERS_WEAPONS);
        assertTrue(match.containsPlayer(player1id));

        match.setPlayerWeapon(player1id, player1weapon);
        assertEquals(match.getStatus(), Match.PLAYED);

        assertTrue(Arrays.asList(new String[] { player1id, PvCMatch.COMPUTER_PLAYER_ID, Match.NO_WINNER }).contains(match.getWinningPlayer()));
    }

    @Test(dataProvider = "pvcMatchProvider")
    public void shouldReturnTheCorrectPvCMatchResultAfterReset(String player1id, int player1weapon) {
        // play a first time
        Match match = new PvCMatch(player1id + PvCMatch.COMPUTER_PLAYER_ID);
        assertNotNull(match.getId());
        assertEquals(match.getPlayersAmount(), 1);
        assertEquals(match.getStatus(), Match.WAITING_FOR_ANOTHER_PLAYER);

        match.addPlayer(player1id);
        assertEquals(match.getPlayersAmount(), 2);
        assertEquals(match.getStatus(), Match.WAITING_PLAYERS_WEAPONS);
        assertTrue(match.containsPlayer(player1id));

        match.setPlayerWeapon(player1id, player1weapon);
        assertEquals(match.getStatus(), Match.PLAYED);
        assertTrue(Arrays.asList(new String[]{player1id, PvCMatch.COMPUTER_PLAYER_ID, Match.NO_WINNER }).contains(match.getWinningPlayer()));

        // reset and play a second time
        match.reset();
        assertEquals(match.getPlayersAmount(), 2);
        assertEquals(match.getStatus(), Match.WAITING_PLAYERS_WEAPONS);
        assertTrue(match.containsPlayer(player1id));

        match.setPlayerWeapon(player1id, player1weapon);
        assertEquals(match.getStatus(), Match.PLAYED);
        assertTrue(Arrays.asList(new String[] { player1id, PvCMatch.COMPUTER_PLAYER_ID, Match.NO_WINNER }).contains(match.getWinningPlayer()));
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void shouldNotAllowToAddSamePlayerTwiceToAPvCMatch() {
        Match match = new PvCMatch("seed");
        match.addPlayer("player");
        match.addPlayer("player");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void shouldNotAllowToAddMoreThan1PlayersToAPvCMatch() {
        Match match = new PvCMatch("seed");
        match.addPlayer("player1");
        match.addPlayer("player2");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void shouldNotAllowPlayerNotInThePvCMatchToSetTheirWeapon() {
        Match match = new PvCMatch("seed");
        match.addPlayer("player1");

        match.setPlayerWeapon("player3", Weapons.pickRandomWeapon());
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void shouldNotAllowToGetPvCMatchResultBeforeBothPlayersHaveSetTheirWeapon() {
        Match match = new PvCMatch("seed");
        match.addPlayer("player1");
        match.getWinningPlayer();
    }
}
