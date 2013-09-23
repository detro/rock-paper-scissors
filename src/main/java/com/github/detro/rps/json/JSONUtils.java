package com.github.detro.rps.json;

import com.github.detro.rps.Match;
import com.github.detro.rps.Weapons;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.List;
import java.util.Set;

/**
 * I need a set of JSON utilities to serialize data before REST response.
 * I could have used Gson directly from POJO objects but, in this case, serialized
 * data do not dipend exclusively on the Object I'm tryin to serialize.
 */
public class JSONUtils {
    public static JsonElement matchToJson(Match match, String playerId) {
        JsonObject result = new JsonObject();

        result.add("id", new JsonPrimitive(match.getId()));
        result.add("kind", new JsonPrimitive(match.getKind()));
        result.add("status", new JsonPrimitive(match.getStatus()));
        result.add("players", new JsonPrimitive(match.getPlayersAmount()));

        if (match.containsPlayer(playerId)) {
            JsonObject weapons = new JsonObject();

            // If already in match, player can only reset or leave
            result.add("canJoin", new JsonPrimitive(false));
            result.add("canReset", new JsonPrimitive(true));
            result.add("canLeave", new JsonPrimitive(true));

            // Show player's weapon
            weapons.add("you", new JsonPrimitive(match.getPlayerWeapon(playerId)));

            // If match has been played, it's time to report the result
            if (match.getStatus() == Match.PLAYED) {
                String winner = match.getWinningPlayer();

                if (winner == Match.NO_WINNER) {
                    result.add("result", new JsonPrimitive(Match.NO_WINNER));
                } else if (winner == playerId) {
                    result.add("result", new JsonPrimitive("won"));
                } else {
                    result.add("result", new JsonPrimitive("lost"));
                }

                // Report opponent weapon
                Set<String> players = match.getPlayers();
                players.remove(playerId);
                String opponentId = players.iterator().next();

                weapons.add("opponent", new JsonPrimitive(match.getPlayerWeapon(opponentId)));
            }

            // If both players are ready, it's time to choose the Weapons
            if (match.getStatus() == Match.WAITING_PLAYERS_WEAPONS) {
                result.add("canChooseWeapon", new JsonPrimitive(true));
            } else {
                result.add("canChooseWeapon", new JsonPrimitive(false));
            }

            // Report the chosen weapons
            result.add("weapons", weapons);
        } else {
            // Cannot reset or leave
            result.add("canReset", new JsonPrimitive(false));
            result.add("canLeave", new JsonPrimitive(false));

            // But, if not full, might join
            if (match.getPlayersAmount() < 2) {
                result.add("canJoin", new JsonPrimitive(true));
            } else {
                result.add("canJoin", new JsonPrimitive(false));
            }
        }

        return result;
    }

    public static final JsonElement matchesToJson(List<Match> matches, String playerId) {
        JsonArray result = new JsonArray();

        for (int i = 0, ilen = matches.size(); i < ilen; ++i) {
            result.add(matchToJson(matches.get(i), playerId));
        }

        return result;
    }

    public static final JsonElement weaponsToJson() {
        JsonArray result = new JsonArray();

        for (int i = 0, ilen = Weapons.weaponsAmount(); i < ilen; ++i) {
            result.add(new JsonPrimitive(Weapons.getName(i)));
        }

        return result;
    }
}
