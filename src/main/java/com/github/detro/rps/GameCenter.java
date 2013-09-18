package com.github.detro.rps;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * TODO Map error cases to different Exceptions: having all "RuntimeException" is very limiting in terms of error handling
 */
public class GameCenter {
    private static final Logger LOG = Logger.getLogger(GameCenter.class);

    private final Map<String, Match> matches = new HashMap<String, Match>();

    public GameCenter() {
        // nothing to do for now
    }

    public void addMatch(Match newMatch) {
        if (!containsMatch(newMatch.getId())) {
            matches.put(newMatch.getId(), newMatch);
        } else {
            String errMsg = String.format("Match '%s' already in GameCenter", newMatch.getId());
            LOG.error(errMsg);
            throw new RuntimeException(errMsg);
        }
    }

    public Map<String, Match> getMatches() {
        return matches;
    }

    public boolean containsMatch(String matchId) {
        return matches.containsKey(matchId);
    }

    public Match getMatch(String matchId) {
        if (!containsMatch(matchId)) {
            String errMsg = String.format("Match '%s' not found", matchId);
            LOG.error(errMsg);
            throw new RuntimeException(errMsg);
        }

        return matches.get(matchId);
    }
}
