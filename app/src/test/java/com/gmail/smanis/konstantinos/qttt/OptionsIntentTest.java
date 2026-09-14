package com.gmail.smanis.konstantinos.qttt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

public class OptionsIntentTest {
    @Test
    public void playerIdsRemainStableAndInvalidValuesFail() {
        assertEquals(0, Player.X.id());
        assertEquals(1, Player.O.id());
        assertSame(Player.X, Player.fromId(0));
        assertSame(Player.O, Player.fromId(1));
        assertThrows(IllegalArgumentException.class, () -> Player.fromId(-1));
    }

    @Test
    public void difficultyIdsRemainStableAndInvalidValuesFail() {
        assertEquals(0, SingleActivity.Difficulty.Random.id());
        assertEquals(1, SingleActivity.Difficulty.Easy.id());
        assertEquals(2, SingleActivity.Difficulty.Medium.id());
        assertEquals(3, SingleActivity.Difficulty.Hard.id());
        assertEquals(4, SingleActivity.Difficulty.Optimal.id());
        assertSame(SingleActivity.Difficulty.Medium, SingleActivity.Difficulty.fromId(2));
        assertThrows(IllegalArgumentException.class, () -> SingleActivity.Difficulty.fromId(-1));
    }

    @Test
    public void radioButtonMappingsDoNotDependOnChildOrder() {
        assertSame(Player.X, OptionsActivity.playerForRadioButton(R.id.radioButton_x));
        assertSame(Player.O, OptionsActivity.playerForRadioButton(R.id.radioButton_o));
        assertSame(
                SingleActivity.Difficulty.Random,
                OptionsActivity.difficultyForRadioButton(R.id.radioButton_random));
        assertSame(
                SingleActivity.Difficulty.Easy,
                OptionsActivity.difficultyForRadioButton(R.id.radioButton_easy));
        assertSame(
                SingleActivity.Difficulty.Medium,
                OptionsActivity.difficultyForRadioButton(R.id.radioButton_medium));
        assertSame(
                SingleActivity.Difficulty.Hard,
                OptionsActivity.difficultyForRadioButton(R.id.radioButton_hard));
        assertSame(
                SingleActivity.Difficulty.Optimal,
                OptionsActivity.difficultyForRadioButton(R.id.radioButton_optimal));
        assertThrows(
                IllegalArgumentException.class, () -> OptionsActivity.playerForRadioButton(-1));
        assertThrows(
                IllegalArgumentException.class, () -> OptionsActivity.difficultyForRadioButton(-1));
    }
}
