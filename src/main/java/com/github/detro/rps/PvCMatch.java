package com.github.detro.rps;

public class PvCMatch extends PvPMatch {
    public static final String COMPUTER_PLAYER_ID = "COMPUTER";

    public PvCMatch(String seed) {
        super(seed);

        // 'Computer' joins and picks a random weapon
        playersAndWeapons.put(COMPUTER_PLAYER_ID, Weapons.pickRandomWeapon());

        // The status is updated straightaway
        status = WAITING_FOR_ANOTHER_PLAYER;
    }

    @Override
    public String getKind() {
        return "pvc";
    }

    @Override
    public void reset() {
        super.reset();

        // 'Computer' picks another random weapon
        playersAndWeapons.put(COMPUTER_PLAYER_ID, Weapons.pickRandomWeapon());
    }
}
