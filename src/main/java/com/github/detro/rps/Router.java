package com.github.detro.rps;

import org.apache.log4j.Logger;

import spark.Request;
import spark.Response;
import spark.Route;

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
        get(new Route("/myid") {
            @Override
            public Object handle(Request req, Response res) {
                res.header("content-type", "application/json");
                req.session(true);
                LOG.debug(String.format("HTTP GET on '/myid' - Session '%s' (new: %b)", req.session().id(), req.session().isNew()));

                StringBuilder bodyBuilder = new StringBuilder();

                bodyBuilder.append(String.format("{ \"id\" : \"%s\" }", req.session().id()));

                res.status(200);
                return bodyBuilder.toString();
            }
        });

        // Returns list of Matches the Player is in
        get(new Route("/mymatches") {
            @Override
            public Object handle(Request req, Response res) {
                res.header("content-type", "application/json");
                req.session(true);
                LOG.debug(String.format("HTTP GET on '/mymatches' - Session '%s' (new: %b)", req.session().id(), req.session().isNew()));

                StringBuilder bodyBuilder = new StringBuilder();

                List<Match> myMatches = gameCenter.getMatches(req.session().id());
                bodyBuilder.append("[");
                for (int i = 0, ilen = myMatches.size(); i < ilen; ++i) {
                    bodyBuilder.append(matchToJSON(myMatches.get(i), req.session().id()));
                    if (i + 1 != ilen) {
                        bodyBuilder.append(" , ");
                    }
                }
                bodyBuilder.append("]");

                res.status(200);
                return bodyBuilder.toString();
            }
        });

        // Returns list of available Weapons
        get(new Route("/weapons") {
            @Override
            public Object handle(Request req, Response res) {
                res.header("content-type", "application/json");
                req.session(true);
                LOG.debug(String.format("HTTP GET on '/weapons' - Session '%s' (new: %b)", req.session().id(), req.session().isNew()));

                StringBuilder bodyBuilder = new StringBuilder();

                bodyBuilder.append("[");
                for (int i = 0, ilen = Weapons.weaponsAmount(); i < ilen; ++i) {
                    bodyBuilder.append("\"" + Weapons.getName(i) + "\"");
                    if (i + 1 != ilen) {
                        bodyBuilder.append(" , ");
                    }
                }
                bodyBuilder.append("]");

                res.status(200);
                return bodyBuilder.toString();
            }
        });

        // Return list of all Matches
        get(new Route("/matches") {
            @Override
            public Object handle(Request req, Response res) {
                res.header("content-type", "application/json");
                req.session(true);
                LOG.debug(String.format("HTTP GET on '/matches' - Session '%s' (new: %b)", req.session().id(), req.session().isNew()));

                StringBuilder bodyBuilder = new StringBuilder();

                List<Match> myMatches = gameCenter.getMatches();
                bodyBuilder.append("[");
                for (int i = 0, ilen = myMatches.size(); i < ilen; ++i) {
                    bodyBuilder.append(matchToJSON(myMatches.get(i), req.session().id()));
                    if (i + 1 != ilen) {
                        bodyBuilder.append(" , ");
                    }
                }
                bodyBuilder.append("]");

                res.status(200);
                return bodyBuilder.toString();
            }
        });

        // Creates a new Player vs Player Match
        post(new Route("/match/pvp") {
            @Override
            public Object handle(Request req, Response res) {
                res.header("content-type", "application/json");
                req.session(true);
                LOG.debug(String.format("HTTP POST on '/match/pvp' - Session '%s' (new: %b)", req.session().id(), req.session().isNew()));

                Match newMatch = new Match(req.session().id());
                gameCenter.addMatch(newMatch);

                res.status(200);
                return matchToJSON(newMatch, req.session().id());
            }
        });

        // Get Match info by Id
        get(new Route("/match/:matchId") {
            @Override
            public Object handle(Request req, Response res) {
                res.header("content-type", "application/json");
                req.session(true);
                LOG.debug(String.format("HTTP GET on '/match/:matchId' - Session '%s' (new: %b)", req.session().id(), req.session().isNew()));

                // Fetch match info
                Match match = null;
                try {
                    match = gameCenter.getMatch(req.params(":matchId"));
                } catch(RuntimeException re) {
                    // Match not found
                    LOG.error(re.getMessage());
                    res.status(404);
                    return "";
                }

                res.status(200);
                return matchToJSON(match, req.session().id());
            }
        });

        // Join a Match
        put(new Route("/match/:matchId/join") {
            @Override
            public Object handle(Request req, Response res) {
                res.header("content-type", "application/json");
                req.session(true);
                LOG.debug(String.format("HTTP PUT on '/match/:matchId/join' - Session '%s' (new: %b)", req.session().id(), req.session().isNew()));

                // Fetch match info
                Match match = null;
                try {
                    match = gameCenter.getMatch(req.params(":matchId"));
                } catch(RuntimeException re) {
                    // Match not found
                    LOG.error(re.getMessage());
                    res.status(404);
                    return "";
                }

                // Add Player to Match
                try {
                    match.addPlayer(req.session().id());
                } catch(RuntimeException re) {
                    // Player couldn't be added to Match
                    LOG.error(re.getMessage());
                    res.status(403);
                    return "";
                }

                res.status(200);
                return matchToJSON(match, req.session().id());
            }
        });

        // Set Weapon in Match
        put(new Route("/match/:matchId/weapon/:weaponId") {
            @Override
            public Object handle(Request req, Response res) {
                res.header("content-type", "application/json");
                req.session(true);
                LOG.debug(String.format("HTTP PUT on '/match/:matchId/weapon/:weaponId' - Session '%s' (new: %b)", req.session().id(), req.session().isNew()));

                String playerId = req.session().id();
                String matchId = req.params(":matchId");
                int weaponId;
                try {
                    weaponId = Integer.parseInt(req.params(":weaponId"));
                } catch(NumberFormatException nfe) {
                    LOG.error(String.format("Weapon '%s' is invalid", req.params(":weaponId")));
                    res.status(400);
                    return "";
                }

                // Fetch match info
                Match match = null;
                try {
                    match = gameCenter.getMatch(matchId);
                } catch(RuntimeException re) {
                    // Match not found
                    LOG.error(re.getMessage());
                    res.status(404);
                    return "";
                }

                // Check if Player is part of the Match first
                if (!match.containsPlayer(playerId)) {
                    LOG.error(String.format("Player '%s' is not part of Match '%s'", playerId, matchId));
                    res.status(403);
                    return "";
                }

                // Set Player Weapon for the given Match
                try {
                    match.setPlayerWeapon(playerId, weaponId);
                } catch(RuntimeException re) {
                    // Player couldn't set the Weapon
                    LOG.error(re.getMessage());
                    res.status(403);
                    return "";
                }

                res.status(200);
                return matchToJSON(match, req.session().id());
            }
        });

        // Reset Match
        put(new Route("/match/:matchId/reset") {
            @Override
            public Object handle(Request req, Response res) {
                res.header("content-type", "application/json");
                req.session(true);
                LOG.debug(String.format("HTTP PUT on '/match/:matchId/reset' - Session '%s' (new: %b)", req.session().id(), req.session().isNew()));

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
                    return "";
                }

                // Check if Player is part of the Match first
                if (!match.containsPlayer(playerId)) {
                    LOG.error(String.format("Player '%s' is not part of Match '%s'", playerId, matchId));
                    res.status(403);
                    return "";
                }

                // Reset this match
                match.reset();

                res.status(200);
                return matchToJSON(match, req.session().id());
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
