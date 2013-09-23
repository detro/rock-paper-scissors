package com.github.detro.rps;

import java.util.Set;

public interface Match {
    public static final int NO_PLAYERS_YET              = 0x0001;
    public static final int WAITING_FOR_ANOTHER_PLAYER  = 0x0003;
    public static final int WAITING_PLAYERS_WEAPONS     = 0x0004;
    public static final int PLAYED                      = 0x0008;

    public static final String NO_WINNER                = "draw";

    String getId();

    int getStatus();

    String getKind();

    int getPlayersAmount();

    boolean containsPlayer(String playerId);

    void addPlayer(String playerId);

    void removePlayer(String playerId);

    Set<String> getPlayers();

    int getPlayerWeapon(String playerId);

    void setPlayerWeapon(String playerId, int weaponIdx);

    String getWinningPlayer();

    void reset();
}
