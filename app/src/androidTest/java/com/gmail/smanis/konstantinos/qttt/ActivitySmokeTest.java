package com.gmail.smanis.konstantinos.qttt;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.WindowInsetsController;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.ViewAssertion;
import androidx.test.espresso.action.CoordinatesProvider;
import androidx.test.espresso.action.GeneralClickAction;
import androidx.test.espresso.action.Press;
import androidx.test.espresso.action.Tap;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.lifecycle.ViewModelProvider;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ActivitySmokeTest {
    @Rule
    public final ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void usesDarkSystemBarIconsOnEdgeToEdgeDevices() {
        if (Build.VERSION.SDK_INT < 35) {
            return;
        }
        activityRule
                .getScenario()
                .onActivity(
                        activity -> {
                            int appearance =
                                    activity.getWindow()
                                            .getInsetsController()
                                            .getSystemBarsAppearance();
                            int lightSystemBars =
                                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                                            | WindowInsetsController
                                                    .APPEARANCE_LIGHT_NAVIGATION_BARS;
                            assertEquals(lightSystemBars, appearance & lightSystemBars);
                        });
    }

    @Test
    public void startsSinglePlayerGame() {
        clickPlayButton(R.id.card_view_single);
        onView(withId(R.id.radioButton_o)).perform(click());
        onView(withId(R.id.radioButton_random)).perform(click());
        onView(withId(R.id.button_start)).check(matches(isDisplayed())).perform(click());
        onView(withText(R.string.activity_single_title)).check(matches(isDisplayed()));
        onView(withId(R.id.gameView)).check(matches(isDisplayed())).check(hasCurrentTurn(1));
        onView(withId(R.id.gameView))
                .perform(clickCell(0, 0), clickCell(0, 1))
                .check(hasCurrentTurn(3));

        pressBack();
        pressBack();
    }

    @Test
    public void startsMultiplayerGameAndAcceptsMove() {
        clickPlayButton(R.id.card_view_multi);
        onView(withText(R.string.activity_multi_title)).check(matches(isDisplayed()));
        onView(withId(R.id.action_undo)).check(doesNotExist());
        onView(withId(R.id.gameView))
                .perform(clickCell(0, 0), clickCell(0, 1))
                .check(hasCurrentTurn(1));
        onView(withId(R.id.action_undo)).check(matches(isDisplayed()));
        onView(withId(R.id.action_undo)).perform(click());
        onView(withId(R.id.gameView)).check(hasCurrentTurn(0));
        onView(withId(R.id.action_undo)).check(doesNotExist());

        onView(withId(R.id.gameView)).perform(clickCell(0, 0), clickCell(0, 1));
        openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext());
        onView(withText(R.string.action_reset)).perform(click());
        onView(withId(R.id.gameView)).check(hasCurrentTurn(0));
        onView(withId(R.id.action_undo)).check(doesNotExist());

        pressBack();
    }

    @Test
    public void tutorialUsesTheBoardForSuperposition() {
        onView(withId(R.id.button_tutorial)).perform(click());
        onView(withText(R.string.tutorial_superposition_title)).check(matches(isDisplayed()));
        onView(withId(R.id.gameView)).check(hasCurrentTurn(0));
        onView(withId(R.id.tutorial_next)).check(matches(org.hamcrest.Matchers.not(isEnabled())));
        onView(withId(R.id.gameView))
                .perform(clickCell(0, 0), clickCell(0, 1))
                .check(hasCurrentTurn(1));
        onView(withId(R.id.tutorial_next)).check(matches(isEnabled()));

        onView(withId(R.id.tutorial_next)).perform(scrollTo(), click());
        onView(withText(R.string.tutorial_entanglement_title)).check(matches(isDisplayed()));
        onView(withId(R.id.tutorial_next)).check(matches(org.hamcrest.Matchers.not(isEnabled())));
        onView(withId(R.id.gameView))
                .perform(clickCell(0, 0), clickCell(0, 1), clickCell(1, 1))
                .check(hasCurrentTurn(2));
        onView(withId(R.id.tutorial_next)).check(matches(isEnabled()));

        onView(withId(R.id.tutorial_next)).perform(scrollTo(), click());
        onView(withText(R.string.tutorial_cycle_title)).check(matches(isDisplayed()));
        onView(withId(R.id.tutorial_next)).check(matches(org.hamcrest.Matchers.not(isEnabled())));
        onView(withId(R.id.gameView))
                .perform(clickCell(0, 1), clickCell(1, 1))
                .check(hasCurrentTurn(3));
        onView(withId(R.id.tutorial_next)).check(matches(isEnabled()));

        onView(withId(R.id.tutorial_next)).perform(scrollTo(), click());
        onView(withId(R.id.gameView)).perform(clickCell(0, 1));
        onView(withId(R.id.tutorial_next)).perform(scrollTo(), click());
        onView(withId(R.id.gameView))
                .check(
                        (view, error) -> {
                            if (error != null) {
                                throw error;
                            }
                            assertFalse(((GameView) view).isPaused());
                        });
        onView(withId(R.id.tutorial_next)).check(matches(isEnabled()));

        int[] resultTitles = {
            R.string.tutorial_complete_win_title,
            R.string.tutorial_loss_title,
            R.string.tutorial_draw_title,
            R.string.tutorial_narrow_first_title,
            R.string.tutorial_double_win_title
        };
        for (int resultTitle : resultTitles) {
            onView(withId(R.id.tutorial_next)).perform(scrollTo(), click());
            onView(withText(resultTitle)).perform(scrollTo()).check(matches(isDisplayed()));
        }

        pressBack();
    }

    @Test
    public void multiplayerStateSurvivesRotation() {
        try (ActivityScenario<MultiActivity> scenario =
                ActivityScenario.launch(MultiActivity.class)) {
            onView(withId(R.id.gameView))
                    .perform(clickCell(0, 0), clickCell(0, 1))
                    .check(hasCurrentTurn(1));

            scenario.onActivity(
                    activity ->
                            activity.setRequestedOrientation(
                                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE));
            onView(withId(R.id.gameView)).check(hasCurrentTurn(1));

            scenario.onActivity(
                    activity ->
                            activity.setRequestedOrientation(
                                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT));
            onView(withId(R.id.gameView)).check(hasCurrentTurn(1));
        }
    }

    @Test
    public void tutorialBoardFitsInLandscape() {
        try (ActivityScenario<TutorialActivity> scenario =
                ActivityScenario.launch(TutorialActivity.class)) {
            scenario.onActivity(
                    activity ->
                            activity.setRequestedOrientation(
                                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE));
            onView(withId(R.id.gameView)).check(matches(isCompletelyDisplayed()));
        }
    }

    @Test
    public void tutorialPendingMoveSurvivesRecreationFromResultPage() {
        try (ActivityScenario<TutorialActivity> scenario =
                ActivityScenario.launch(TutorialActivity.class)) {
            onView(withId(R.id.gameView)).perform(clickCell(0, 0), clickCell(0, 1));
            onView(withId(R.id.tutorial_next)).perform(scrollTo(), click());
            onView(withId(R.id.gameView))
                    .perform(clickCell(0, 0), clickCell(0, 1), clickCell(1, 1));
            onView(withId(R.id.tutorial_next)).perform(scrollTo(), click());
            onView(withId(R.id.gameView)).perform(clickCell(0, 1), clickCell(1, 1));
            onView(withId(R.id.tutorial_next)).perform(scrollTo(), click());
            onView(withId(R.id.gameView)).perform(clickCell(0, 1));
            onView(withId(R.id.tutorial_next)).perform(scrollTo(), click());

            onView(withId(R.id.gameView)).perform(clickCell(2, 2));
            onView(withId(R.id.tutorial_next)).perform(scrollTo(), click());
            scenario.recreate();
            onView(withId(R.id.tutorial_back)).perform(click());
            onView(withId(R.id.gameView)).perform(clickCell(2, 1)).check(hasCurrentTurn(4));
        }
    }

    @Test
    public void multiplayerGameOverCanBeReset() {
        try (ActivityScenario<MultiActivity> scenario =
                ActivityScenario.launch(MultiActivity.class)) {
            scenario.onActivity(
                    activity -> {
                        GameViewModel game =
                                new ViewModelProvider(activity).get(GameViewModel.class);
                        Move[] moves = {
                            new Move(0, 2, CellState.X1),
                            new Move(4, 7, CellState.O2),
                            new Move(2, 4, CellState.X3),
                            new Move(5, 7, CellState.O4),
                            new Move(7, 8, CellState.X5),
                            new Move(1, 3, CellState.O6),
                            new Move(4, 8, CellState.X7),
                            new Move(8, CellState.X7),
                            new Move(1, 6, CellState.O8),
                            new Move(1, 6, CellState.X9),
                            new Move(1, CellState.X9)
                        };
                        for (Move move : moves) {
                            game.applyMove(move);
                        }
                    });

            onView(
                            withText(
                                    ApplicationProvider.getApplicationContext()
                                            .getString(R.string.result_narrow_winner, Player.O)))
                    .check(matches(isDisplayed()));
            onView(withText(R.string.action_reset)).perform(click());
            onView(withId(R.id.gameView)).check(hasCurrentTurn(0));
            onView(withId(R.id.action_undo)).check(doesNotExist());

            scenario.onActivity(
                    activity -> {
                        GameViewModel game =
                                new ViewModelProvider(activity).get(GameViewModel.class);
                        Move[] moves = {
                            new Move(4, 5, CellState.X1),
                            new Move(2, 4, CellState.O2),
                            new Move(0, 4, CellState.X3),
                            new Move(1, 4, CellState.O4),
                            new Move(0, 4, CellState.X5),
                            new Move(0, CellState.X5),
                            new Move(3, 7, CellState.O6),
                            new Move(6, 8, CellState.X7),
                            new Move(3, 6, CellState.O8),
                            new Move(3, 7, CellState.X9),
                            new Move(3, CellState.X9)
                        };
                        for (Move move : moves) {
                            game.applyMove(move);
                        }
                    });

            onView(
                            withText(
                                    ApplicationProvider.getApplicationContext()
                                            .getString(R.string.result_double_winner)))
                    .check(matches(isDisplayed()));
        }
    }

    @Test
    public void formatsEveryGameResult() {
        Context context = ApplicationProvider.getApplicationContext();

        assertEquals(
                context.getString(R.string.result_draw),
                GameResultFormatter.format(context, GameResult.DRAW));
        assertEquals(
                context.getString(R.string.result_double_winner),
                GameResultFormatter.format(context, GameResult.DOUBLE_COMPLETE_WIN));
        assertEquals(
                context.getString(R.string.result_narrow_winner, Player.X),
                GameResultFormatter.format(context, GameResult.NARROW_WIN_FIRST));
        assertEquals(
                context.getString(R.string.result_narrow_winner, Player.O),
                GameResultFormatter.format(context, GameResult.NARROW_WIN_SECOND));
        assertEquals(
                context.getString(R.string.result_winner, Player.X),
                GameResultFormatter.format(context, GameResult.COMPLETE_WIN));
        assertEquals(
                context.getString(R.string.result_winner, Player.O),
                GameResultFormatter.format(context, GameResult.LOSS));
        assertThrows(
                IllegalArgumentException.class,
                () -> GameResultFormatter.format(context, GameResult.INVALID));
    }

    private static void clickPlayButton(int cardId) {
        onView(allOf(withText(R.string.button_play), isDescendantOfA(withId(cardId))))
                .perform(click());
    }

    private static ViewAssertion hasCurrentTurn(int expected) {
        return (view, error) -> {
            if (error != null) {
                throw error;
            }
            assertEquals(expected, ((GameView) view).currentTurn());
        };
    }

    private static GeneralClickAction clickCell(int row, int column) {
        CoordinatesProvider coordinates =
                view -> {
                    int[] location = new int[2];
                    view.getLocationOnScreen(location);
                    return new float[] {
                        location[0] + view.getWidth() * (column + 0.5f) / 3,
                        location[1] + view.getHeight() * (row + 0.5f) / 3
                    };
                };
        return new GeneralClickAction(
                Tap.SINGLE,
                coordinates,
                Press.FINGER,
                InputDevice.SOURCE_UNKNOWN,
                MotionEvent.BUTTON_PRIMARY);
    }
}
