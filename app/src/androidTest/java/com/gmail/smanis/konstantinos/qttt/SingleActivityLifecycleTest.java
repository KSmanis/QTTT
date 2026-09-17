package com.gmail.smanis.konstantinos.qttt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Intent;
import android.view.View;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModelProvider;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SingleActivityLifecycleTest {
    @Test
    public void aiTurnSurvivesRecreation() {
        try (ActivityScenario<SingleActivity> scenario = launch(Player.O, Difficulty.Optimal)) {
            scenario.recreate();
            waitForTurn(scenario, 1);
        }
    }

    @Test
    public void resetWhileMinimaxIsRunningLeavesFreshBoard() {
        try (ActivityScenario<SingleActivity> scenario = launch(Player.X, Difficulty.Optimal)) {
            scenario.onActivity(
                    activity -> {
                        seedBotTurn(activity);
                        game(activity).playBot(Player.X, Difficulty.Optimal);
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
        try (ActivityScenario<SingleActivity> scenario = launch(Player.X, Difficulty.Optimal)) {
            scenario.onActivity(
                    activity -> {
                        seedBotTurn(activity);
                        game(activity).playBot(Player.X, Difficulty.Optimal);
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
        try (ActivityScenario<SingleActivity> scenario = launch(Player.X, Difficulty.Optimal)) {
            scenario.onActivity(
                    activity -> {
                        seedBotTurn(activity);
                        game(activity).playBot(Player.X, Difficulty.Optimal);
                        assertEquals(
                                View.VISIBLE,
                                activity.findViewById(R.id.progressBar).getVisibility());
                        activity.finish();
                    });
        }
    }

    @Test
    public void recreationPreservesStateAndTurnOwnership() {
        try (ActivityScenario<SingleActivity> scenario = launch(Player.X, Difficulty.Random)) {
            scenario.onActivity(
                    activity -> {
                        GameViewModel game = game(activity);
                        game.applyMove(new Move(0, 1, CellState.X1));
                        game.applyMove(new Move(2, 3, CellState.O2));
                    });

            scenario.recreate();

            assertState(scenario, 2, Player.X);
        }
    }

    @Test
    public void recreationPreservesIncompleteInput() {
        try (ActivityScenario<SingleActivity> scenario = launch(Player.X, Difficulty.Random)) {
            scenario.onActivity(activity -> game(activity).applyInput(0));

            scenario.recreate();

            scenario.onActivity(
                    activity -> {
                        GameViewModel game = game(activity);
                        assertEquals(0, game.snapshot().currentTurn());
                        game.applyInput(1);
                        assertEquals(1, game.snapshot().currentTurn());
                    });
        }
    }

    @Test
    public void savedStateRestoresCompletedAndIncompleteMoves() {
        InstrumentationRegistry.getInstrumentation()
                .runOnMainSync(
                        () -> {
                            SavedStateHandle savedState = new SavedStateHandle();
                            GameViewModel original =
                                    new GameViewModel(
                                            ApplicationProvider.getApplicationContext(),
                                            savedState);
                            original.applyInput(0);
                            original.applyInput(1);
                            original.applyInput(2);

                            GameViewModel restored =
                                    new GameViewModel(
                                            ApplicationProvider.getApplicationContext(),
                                            savedState);
                            assertEquals(1, restored.snapshot().currentTurn());
                            restored.applyInput(3);
                            assertEquals(2, restored.snapshot().currentTurn());

                            original.onCleared();
                            restored.onCleared();
                        });
    }

    @Test
    public void singlePlayerUndoRestoresEachHumanTurn() {
        InstrumentationRegistry.getInstrumentation()
                .runOnMainSync(
                        () -> {
                            GameViewModel xGame = gameWithMoves(2);
                            xGame.undoSinglePlayer(Player.X);
                            assertEquals(0, xGame.snapshot().currentTurn());
                            assertEquals(Player.X, xGame.snapshot().currentPlayer());

                            GameViewModel oGame = gameWithMoves(3);
                            oGame.undoSinglePlayer(Player.O);
                            assertEquals(1, oGame.snapshot().currentTurn());
                            assertEquals(Player.O, oGame.snapshot().currentPlayer());

                            assertTrue(oGame.applyInput(4));
                            oGame.undoSinglePlayer(Player.O);
                            assertEquals(1, oGame.snapshot().currentTurn());
                            assertFalse(oGame.snapshot().hasIncompleteInput());

                            xGame.onCleared();
                            oGame.onCleared();
                        });
    }

    private static ActivityScenario<SingleActivity> launch(
            Player humanPlayer, Difficulty difficulty) {
        Intent intent =
                new Intent(ApplicationProvider.getApplicationContext(), SingleActivity.class);
        intent.putExtra(OptionsActivity.EXTRA_PLAYER, humanPlayer.id());
        intent.putExtra(OptionsActivity.EXTRA_DIFFICULTY, difficulty.id());
        return ActivityScenario.launch(intent);
    }

    private static GameViewModel gameWithMoves(int count) {
        GameViewModel game =
                new GameViewModel(
                        ApplicationProvider.getApplicationContext(), new SavedStateHandle());
        for (int i = 0; i < count; ++i) {
            game.applyMove(game.snapshot().availableMoves(false).get(0));
        }
        return game;
    }

    private static void seedBotTurn(SingleActivity activity) {
        GameViewModel game = game(activity);
        while (game.snapshot().currentTurn() < 5) {
            State state = game.snapshot();
            game.applyMove(state.availableMoves(false).get(0));
        }
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
                    GameViewModel game = game(activity);
                    State state = game.snapshot();
                    assertEquals(expectedTurn, state.currentTurn());
                    assertEquals(expectedPlayer, state.currentPlayer());
                });
    }

    private static GameViewModel game(SingleActivity activity) {
        return new ViewModelProvider(activity).get(GameViewModel.class);
    }
}
