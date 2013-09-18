package com.github.detro.rps;

/**
 * Contains the list of weapons and the "A vs B" logic.
 *
 * Useful to convert weapon indexes to weapon names, as well as
 * figuring out who wins.
 */
public class Weapons {
    private static final String[] NAMES = new String[] {
        "paper",    //< 0
        "scissors", //< 1
        "rock"      //< 2
    };

    private static final int[][] WINNERS = new int[][] {
            { -1,  1,  0},  //< 0 vs: 0, 1, 2
            {  1, -1,  2},  //< 1 vs: 0, 1, 2
            {  0,  2, -1}   //< 2 vs: 0, 1, 2
    };

    private static void validateWeaponIdx(int weaponIdx) {
        if (weaponIdx < 0 || weaponIdx > NAMES.length-1) {
            throw new RuntimeException("Invalid Weapon Index: " + weaponIdx);
        }
    }

    public static String getName(int weaponIdx) {
        validateWeaponIdx(weaponIdx);
        return NAMES[weaponIdx];
    }

    public static String[] getNames() {
        return NAMES;
    }

    /**
     * A vs B
     * @param weaponAIdx
     * @param weaponBIdx
     * @return Returns -1 if it's a draw, otherwise the Index of the Winning Weapon
     */
    public static int vs(int weaponAIdx, int weaponBIdx) {
        validateWeaponIdx(weaponAIdx);
        validateWeaponIdx(weaponBIdx);
        return WINNERS[weaponAIdx][weaponBIdx];
    }
}
