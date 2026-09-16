package com.gmail.smanis.konstantinos.qttt;

import static org.junit.Assert.assertEquals;

import android.content.Intent;
import android.view.View;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SingleActivityLifecycleTest {
    @Test
    public void aiTurnCompletes() {
        try (ActivityScenario<SingleActivity> scenario =
                launch(Player.O, SingleActivity.Difficulty.Optimal)) {
            waitForTurn(scenario, 1);
        }
    }

    @Test
    public void resetWhileMinimaxIsRunningLeavesFreshBoard() {
        try (ActivityScenario<SingleActivity> scenario =
                launch(Player.X, SingleActivity.Difficulty.Optimal)) {
            scenario.onActivity(
                    activity -> {
                        seedBotTurn(activity);
                        activity.botPlay();
                        assertEquals(
                                View.VISIBLE,
                                activity.findViewById(R.id.progressBar).getVisibility());
                        activity.resetBoard();
                        assertEquals(
                                View.INVISIBLE,
                                activity.findViewById(R.id.progressBar).getVisibility());
                    });

            assertState(scenario, 0, Player.X);
        }
    }

    @Test
    public void undoWhileMinimaxIsRunningRemovesHumanMove() {
        try (ActivityScenario<SingleActivity> scenario =
                launch(Player.X, SingleActivity.Difficulty.Optimal)) {
            scenario.onActivity(
                    activity -> {
                        seedBotTurn(activity);
                        activity.botPlay();
                        assertEquals(
                                View.VISIBLE,
                                activity.findViewById(R.id.progressBar).getVisibility());
                        activity.undoMove();
                        assertEquals(
                                View.INVISIBLE,
                                activity.findViewById(R.id.progressBar).getVisibility());
                    });

            assertState(scenario, 4, Player.X);
        }
    }

    @Test
    public void destructionBeforeAiResultDoesNotCrash() {
        try (ActivityScenario<SingleActivity> scenario =
                launch(Player.X, SingleActivity.Difficulty.Optimal)) {
            scenario.onActivity(
                    activity -> {
                        seedBotTurn(activity);
                        activity.botPlay();
                        assertEquals(
                                View.VISIBLE,
                                activity.findViewById(R.id.progressBar).getVisibility());
                        activity.finish();
                    });
        }
    }

    @Test
    public void recreationPreservesStateAndTurnOwnership() {
        try (ActivityScenario<SingleActivity> scenario =
                launch(Player.X, SingleActivity.Difficulty.Random)) {
            scenario.onActivity(
                    activity -> {
                        State state = ((GameView) activity.findViewById(R.id.gameView)).state();
                        state.applyMove(new Move(0, 1, CellState.X1));
                        state.applyMove(new Move(2, 3, CellState.O2));
                        ((GameView) activity.findViewById(R.id.gameView)).refresh();
                    });

            scenario.recreate();

            assertState(scenario, 2, Player.X);
        }
    }

    private static ActivityScenario<SingleActivity> launch(
            Player humanPlayer, SingleActivity.Difficulty difficulty) {
        Intent intent =
                new Intent(ApplicationProvider.getApplicationContext(), SingleActivity.class);
        intent.putExtra(OptionsActivity.EXTRA_PLAYER, humanPlayer.id());
        intent.putExtra(OptionsActivity.EXTRA_DIFFICULTY, difficulty.id());
        return ActivityScenario.launch(intent);
    }

    private static void seedBotTurn(SingleActivity activity) {
        GameView gameView = activity.findViewById(R.id.gameView);
        State state = gameView.state();
        while (state.currentTurn() < 5) {
            state.applyMove(state.availableMoves(false).get(0));
        }
        gameView.refresh();
    }

    private static void waitForTurn(ActivityScenario<SingleActivity> scenario, int expectedTurn) {
        long deadline = System.currentTimeMillis() + 10_000;
        AssertionError lastError = null;
        while (System.currentTimeMillis() < deadline) {
            try {
                assertState(scenario, expectedTurn, Player.O);
                return;
            } catch (AssertionError error) {
                lastError = error;
                Thread.yield();
            }
        }
        throw lastError;
    }

    private static void assertState(
            ActivityScenario<SingleActivity> scenario, int expectedTurn, Player expectedPlayer) {
        scenario.onActivity(
                activity -> {
                    State state = ((GameView) activity.findViewById(R.id.gameView)).state();
                    assertEquals(expectedTurn, state.currentTurn());
                    assertEquals(expectedPlayer, state.currentPlayer());
                });
    }
}
