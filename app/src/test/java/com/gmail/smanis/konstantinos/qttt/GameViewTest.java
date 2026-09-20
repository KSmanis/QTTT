package com.gmail.smanis.konstantinos.qttt;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class GameViewTest {
    @Test
    public void glowSpeedDependsOnElapsedTime() {
        assertEquals(5f, GameView.glowRadiusAt(0), 0f);
        assertEquals(20f, GameView.glowRadiusAt(500), 0f);
        assertEquals(5f, GameView.glowRadiusAt(1000), 0f);
    }
}
