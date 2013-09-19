package com.github.detro.rps.http.test;

import com.github.detro.rps.Match;
import com.github.detro.rps.Weapons;
import com.github.detro.rps.http.Router;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.openqa.selenium.net.PortProber;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.testng.Assert.*;

// TODO Rewrite those tests once a proper JSON library is introduced
// TODO Definitely needs more tests
public class RouterTest {
    private static final int PORT = PortProber.findFreePort();
    private static final Router ROUTER = new Router();
    private static final String BASEURL = "http://localhost:" + PORT;

    @BeforeClass
    public static void startRouter() {
        ROUTER.listen(PORT);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void shouldReturnListOfAllAvailableWeapons() {
        HttpClient client = new HttpClient();
        GetMethod getWeapons = new GetMethod(BASEURL + "/weapons");
        try {
            // execute and check status code
            int statusCode = client.executeMethod(getWeapons);
            assertEquals(statusCode, 200);

            // check response body
            String body = new String(getWeapons.getResponseBody());
            assertTrue(body.startsWith("["));
            assertTrue(body.endsWith("]"));
            for (int i = 0, ilen = Weapons.weaponsAmount(); i < ilen; ++i) {
                assertTrue(body.contains(Weapons.getName(i)));
            }
        } catch (IOException ioe) {
            fail();
        } finally {
            getWeapons.releaseConnection();
        }
    }

    @Test
    public void shouldReturnPlayerId() {
        HttpClient client = new HttpClient();
        GetMethod getMyId = new GetMethod(BASEURL + "/myid");
        try {
            // execute and check status code
            int statusCode = client.executeMethod(getMyId);
            assertEquals(statusCode, 200);

            // check response body
            String body = new String(getMyId.getResponseBody());
            assertTrue(body.startsWith("{"));
            assertTrue(body.endsWith("}"));
            assertTrue(body.contains("\"id\""));
        } catch (IOException ioe) {
            fail();
        } finally {
            getMyId.releaseConnection();
        }
    }

    @Test
    public void shouldAllowToPlayAMatch() {
        HttpClient client1 = new HttpClient();
        HttpClient client2 = new HttpClient();
        PostMethod createMatch = null;
        PutMethod joinMatch = null;
        PutMethod setWeapon = null;
        GetMethod getMatchInfo = null;

        // Create a Match
        try {
            // First Player creates the match
            createMatch = new PostMethod(BASEURL + "/match/pvp");
            assertEquals(client1.executeMethod(createMatch), 200);

            // check response body
            String body = new String(createMatch.getResponseBody());
            assertTrue(body.startsWith("{"));
            assertTrue(body.endsWith("}"));
            assertTrue(body.contains("\"matchId\""));

            // Extract Match ID from response body
            Pattern pattern = Pattern.compile("\"matchId\" : \"(\\p{XDigit}+)\"");
            Matcher matcher = pattern.matcher(body);
            assertTrue(matcher.find());
            String matchId = matcher.group(1);

            // First Player joins the match
            joinMatch = new PutMethod(BASEURL + "/match/" + matchId + "/join");
            assertEquals(client1.executeMethod(joinMatch), 200);

            // Second Player joins the match
            assertEquals(client2.executeMethod(joinMatch), 200);

            // First and Second Player set the same weapon
            setWeapon = new PutMethod(BASEURL + "/match/" + matchId + "/weapon/1");
            assertEquals(client1.executeMethod(setWeapon), 200);
            assertEquals(client2.executeMethod(setWeapon), 200);

            // Check Match is ready to be played
            getMatchInfo = new GetMethod(BASEURL + "/match/" + matchId);
            assertEquals(client1.executeMethod(getMatchInfo), 200);
            body = new String(getMatchInfo.getResponseBody());

            // Extract the Match status
            pattern = Pattern.compile("\"status\" : ([0-9])");
            matcher = pattern.matcher(body);
            assertTrue(matcher.find());
            assertEquals(Integer.parseInt(matcher.group(1)), Match.PLAYED);

            // Extract the Match result
            pattern = Pattern.compile("\"result\" : \"([a-z]+)\"");
            matcher = pattern.matcher(body);
            assertTrue(matcher.find());
            assertEquals(matcher.group(1), "draw");
        } catch (IOException ioe) {
            fail();
        } finally {
            if (createMatch != null) createMatch.releaseConnection();
            if (joinMatch != null) joinMatch.releaseConnection();
            if (setWeapon != null) setWeapon.releaseConnection();
            if (getMatchInfo != null) getMatchInfo.releaseConnection();
        }
    }
}
