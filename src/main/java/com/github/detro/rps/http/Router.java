package com.github.detro.rps.http;

import com.github.detro.rps.GameCenter;
import com.github.detro.rps.Match;
import com.github.detro.rps.Weapons;
import org.apache.log4j.Logger;
import spark.Request;
import spark.Response;

import java.util.List;

import static spark.Spark.*;

public class Router {
    private static final Logger LOG = Logger.getLogger(Router.class);

    private final GameCenter gameCenter = new GameCenter();

    public Router() {
        // Nothing to do here, for now
    }

    public void listen(int port) {
        setPort(port);

        // Returns Player ID
        get(new JSONRoute("/myid") {
            @Override
            public void process(Request req, Response res, StringBuilder resBody) {
                // Return Player's ID
                resBody.append(String.format("{ \"id\" : \"%s\" }", req.session().id()));

                res.status(200);
            }
        });

        // Returns list of Matches the Player is in
        get(new JSONRoute("/mymatches") {
            @Override
            public void process(Request req, Response res, StringBuilder resBody) {
                String playerId = req.session().id();

                // List all Matches of a specific Player
                List<Match> myMatches = gameCenter.getMatches(playerId);
                resBody.append("[");
                for (int i = 0, ilen = myMatches.size(); i < ilen; ++i) {
                    resBody.append(matchToJSON(myMatches.get(i), playerId));
                    if (i + 1 != ilen) {
                        resBody.append(" , ");
                    }
                }
                resBody.append("]");

                res.status(200);
            }
        });

        // Returns list of available Weapons
        get(new JSONRoute("/weapons") {
            @Override
            public void process(Request req, Response res, StringBuilder resBody) {
                // List all the Weapons
                resBody.append("[");
                for (int i = 0, ilen = Weapons.weaponsAmount(); i < ilen; ++i) {
                    resBody.append("\"" + Weapons.getName(i) + "\"");
                    if (i + 1 != ilen) {
                        resBody.append(" , ");
                    }
                }
                resBody.append("]");

                res.status(200);
            }
        });

        // Return list of all Matches
        get(new JSONRoute("/matches") {
            @Override
            public void process(Request req, Response res, StringBuilder resBody) {
                // List all the Matches
                List<Match> myMatches = gameCenter.getMatches();
                resBody.append("[");
                for (int i = 0, ilen = myMatches.size(); i < ilen; ++i) {
                    resBody.append(matchToJSON(myMatches.get(i), req.session().id()));
                    if (i + 1 != ilen) {
                        resBody.append(" , ");
                    }
                }
                resBody.append("]");

                res.status(200);
            }
        });

        // Creates a new Player vs Player Match
        post(new JSONRoute("/match/pvp") {
            @Override
            public void process(Request req, Response res, StringBuilder resBody) {
                String playerId = req.session().id();

                // Create and register the new Match
                Match newMatch = new Match(playerId);
                gameCenter.addMatch(newMatch);
                resBody.append(matchToJSON(newMatch, playerId));

                res.status(200);
            }
        });

        // Get Match info by Id
        get(new JSONRoute("/match/:matchId") {
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

                resBody.append(matchToJSON(match, req.session().id()));
                res.status(200);
            }
        });

        // Join a Match
        put(new JSONRoute("/match/:matchId/join") {
            @Override
            public void process(Request req, Response res, StringBuilder resBody) {
                String playerId = req.session().id();

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

                // Add Player to Match
                try {
                    match.addPlayer(playerId);
                } catch(RuntimeException re) {
                    // Player couldn't be added to Match
                    LOG.error(re.getMessage());
                    res.status(403);
                    return;
                }

                resBody.append(matchToJSON(match, playerId));
                res.status(200);
            }
        });

        // Set Weapon in Match
        put(new JSONRoute("/match/:matchId/weapon/:weaponId") {
            @Override
            public void process(Request req, Response res, StringBuilder resBody) {
                String playerId = req.session().id();
                String matchId = req.params(":matchId");
                int weaponId;
                try {
                    weaponId = Integer.parseInt(req.params(":weaponId"));
                } catch(NumberFormatException nfe) {
                    LOG.error(String.format("Weapon '%s' is invalid", req.params(":weaponId")));
                    res.status(400);
                    return;
                }

                // Fetch match info
                Match match = null;
                try {
                    match = gameCenter.getMatch(matchId);
                } catch(RuntimeException re) {
                    // Match not found
                    LOG.error(re.getMessage());
                    res.status(404);
                    return;
                }

                // Check if Player is part of the Match first
                if (!match.containsPlayer(playerId)) {
                    LOG.error(String.format("Player '%s' is not part of Match '%s'", playerId, matchId));
                    res.status(403);
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

                resBody.append(matchToJSON(match, req.session().id()));
                res.status(200);
            }
        });

        // Reset Match
        put(new JSONRoute("/match/:matchId/reset") {
            @Override
            public void process(Request req, Response res, StringBuilder resBody) {
                String playerId = req.session().id();
                String matchId = req.params(":matchId");

                // Fetch match info
                Match match = null;
                try {
                    match = gameCenter.getMatch(matchId);
                } catch(RuntimeException re) {
                    // Match not found
                    LOG.error(re.getMessage());
                    res.status(404);
                    return;
                }

                // Check if Player is part of the Match first
                if (!match.containsPlayer(playerId)) {
                    LOG.error(String.format("Player '%s' is not part of Match '%s'", playerId, matchId));
                    res.status(403);
                    return;
                }

                // Reset this match
                match.reset();

                resBody.append(matchToJSON(match, req.session().id()));
                res.status(200);
            }
        });
    }

    private String matchToJSON(Match match, String currentPlayerId) {
        // TODO Introduce Gson (or something similar) to generate those JSONs: this is ugly!
        StringBuilder builder = new StringBuilder();

        builder.append(String.format("{ \"matchId\" : \"%s\" , \"status\" : %d , \"players\" : %d ",
                match.getId(),
                match.getStatus(),
                match.playersAmount()));
        if (match.getStatus() == Match.PLAYED && match.containsPlayer(currentPlayerId)) {
            String winner = match.getWinner();
            if (winner == currentPlayerId) {
                builder.append(" , \"result\" : \"won\"");
            } else if (winner == "draw") {
                builder.append(" , \"result\" : \"draw\"");
            } else {
                builder.append(" , \"result\" : \"lost\"");
            }
        }
        builder.append(" }");

        return builder.toString();
    }
}
