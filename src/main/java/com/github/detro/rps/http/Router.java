package com.github.detro.rps.http;

import com.github.detro.rps.GameCenter;
import com.github.detro.rps.Match;
import com.github.detro.rps.PvCMatch;
import com.github.detro.rps.PvPMatch;

import com.github.detro.rps.json.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spark.Request;
import spark.Response;

import java.util.List;

import static spark.Spark.*;

public class Router {
    private static final Logger LOG = LoggerFactory.getLogger(Router.class);

    public static final String WEBSITE_RESOURCE_PATH    = "/public";
    public static final String API_PATH                 = "/api";

    private final GameCenter gameCenter = new GameCenter();

    public Router() {
        // Nothing to do here, for now
    }

    public void listen(int port) {
        setPort(port);
        staticFileLocation(WEBSITE_RESOURCE_PATH);

        // Returns list of available Weapons
        get(new JSONRoute(API_PATH + "/weapons") {
            @Override
            public void process(Request req, Response res, StringBuilder resBody) {
                // List all the Weapons
                resBody.append(JSONUtils.weaponsToJson().toString());
                res.status(200);
            }
        });

        // Return list of Matches
        get(new JSONRoute(API_PATH + "/matches") {
            @Override
            public void process(Request req, Response res, StringBuilder resBody) {
                String playerId = req.session().id();
                String type = req.queryParams("type");
                List<Match> matches = null;

                // Fetch a list of matches, based on the requested type
                if (type == null || type.equals("all")) {
                    matches = gameCenter.getMatches();
                } else if (type.equals("mine")) {
                    matches = gameCenter.getMatches(playerId);
                } else if (type.equals("available")) {
                    matches = gameCenter.getAvailableMatches(playerId);
                } else {
                    LOG.error(String.format("Invalid parameter: %s=%s", "type", type));
                    res.status(400);
                    return;
                }

                resBody.append(JSONUtils.matchesToJson(matches, playerId).toString());
                res.status(200);
            }
        });

        // Creates a new Match
        post(new JSONRoute(API_PATH + "/match") {
            @Override
            public void process(Request req, Response res, StringBuilder resBody) {
                String playerId = req.session().id();
                String kind = req.queryParams("kind");
                Match newMatch;

                if (kind == null || kind.equals("pvp")) {
                    newMatch = new PvPMatch(playerId);
                } else if (kind.equals("pvc")) {
                    newMatch = new PvCMatch(playerId);
                } else {
                    LOG.error(String.format("Invalid parameter: %s=%s", "kind", kind));
                    res.status(400);
                    return;
                }

                // Register the new Match
                gameCenter.addMatch(newMatch);

                resBody.append(JSONUtils.matchToJson(newMatch, playerId));
                res.status(200);
            }
        });

        // Get Match by Id
        get(new JSONRoute(API_PATH + "/match/:matchId") {
            @Override
            public void process(Request req, Response res, StringBuilder resBody) {
                // Fetch match info
                Match match = null;
                try {
                    match = gameCenter.getMatch(req.params(":matchId"));
                } catch(RuntimeException re) {
                    // Match not found
                    LOG.error(re.getMessage());
                    res.status(404);
                    return;
                }

                resBody.append(JSONUtils.matchToJson(match, req.session().id()));
                res.status(200);
            }
        });

        // Execute action on a Match
        put(new JSONRoute(API_PATH + "/match/:matchId") {
            @Override
            public void process(Request req, Response res, StringBuilder resBody) {
                String playerId = req.session().id();
                String matchId = req.params(":matchId");
                String action = req.queryParams("action");

                // Fetch match info
                Match match;
                try {
                    match = gameCenter.getMatch(matchId);
                } catch(RuntimeException re) {
                    // Match not found
                    LOG.error(re.getMessage());
                    res.status(404);
                    return;
                }

                if (action == null) {
                    LOG.error(String.format("Invalid parameter: %s=%s", "action", action));
                    res.status(400);
                    return;
                } else if (action.equals("join")) {
                    // Add Player to Match
                    try {
                        match.addPlayer(playerId);
                    } catch(RuntimeException re) {
                        // Player couldn't be added to Match
                        LOG.error(re.getMessage());
                        res.status(403);
                        return;
                    }
                } else if (action.equals("leave")) {
                    // Remove Player from Match
                    try {
                        match.removePlayer(playerId);
                    } catch(RuntimeException re) {
                        // Player couldn't be removed from Match
                        LOG.error(re.getMessage());
                        res.status(403);
                        return;
                    }
                } else if (action.equals("weapon")) {
                    // Check if Player is part of the Match first
                    if (!match.containsPlayer(playerId)) {
                        LOG.error(String.format("Player '%s' is not part of Match '%s'", playerId, matchId));
                        res.status(403);
                        return;
                    }

                    // Parse the Weapon ID
                    int weaponId;
                    try {
                        weaponId = Integer.parseInt(req.queryParams("weaponId"));
                    } catch(NumberFormatException nfe) {
                        LOG.error(String.format("Weapon '%s' is invalid", req.queryParams("weaponId")));
                        res.status(400);
                        return;
                    }

                    // Set Player Weapon for the given Match
                    try {
                        match.setPlayerWeapon(playerId, weaponId);
                    } catch(RuntimeException re) {
                        // Player couldn't set the Weapon
                        LOG.error(re.getMessage());
                        res.status(403);
                        return;
                    }
                } else if (action.equals("restart")) {
                    // Check if Player is part of the Match first
                    if (!match.containsPlayer(playerId)) {
                        LOG.error(String.format("Player '%s' is not part of Match '%s'", playerId, matchId));
                        res.status(403);
                        return;
                    }

                    // Reset this match
                    match.reset();
                } else {
                    LOG.error(String.format("Invalid parameter: %s=%s", "action", action));
                    res.status(400);
                    return;
                }

                resBody.append(JSONUtils.matchToJson(match, playerId).toString());
                res.status(200);
            }
        });
    }
}
