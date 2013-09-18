package com.github.detro.rps.test;

import com.github.detro.rps.Weapons;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

public class WeaponsTest {

    @DataProvider(name = "weaponMovesProvider")
    public Object[][] weaponMovesProvider() {
        return new Object[][] {
                {  1,  2,  2 }, // scissors vs rock => rock
                {  2,  2, -1 }, // rock vs rock => draw,
                {  0,  2,  0 }  // paper vs rock => paper
        };
    }

    @DataProvider(name = "weaponIdxAndNameProvider")
    public Object[][] weaponIdxAndNameProvider() {
        return new Object[][] {
                {  0, "paper"       },
                {  1, "scissors"    },
                {  2, "rock"        }
        };
    }

    @Test(dataProvider = "weaponMovesProvider")
    public void shouldReturnTheRightWinner(int weaponAIdx, int weaponBIdx, int expectedWinner) {
        assertEquals(Weapons.vs(weaponAIdx, weaponBIdx), expectedWinner);
    }

    @Test
    public void shouldEmitExceptionIfInvalidWeaponIdxAreUsed() {
        try {
            Weapons.vs(-5, 7);
            fail();
        } catch(RuntimeException re) { }

        try {
            Weapons.getName(11);
            fail();
        } catch(RuntimeException re) { }
    }

    @Test(dataProvider = "weaponIdxAndNameProvider")
    public void shouldReturnTheRightWeaponName(int weaponIdx, String weaponName) {
        assertEquals(Weapons.getName(weaponIdx), weaponName);
    }
}
