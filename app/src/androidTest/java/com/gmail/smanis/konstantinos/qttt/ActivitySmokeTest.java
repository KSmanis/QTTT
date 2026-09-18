package com.gmail.smanis.konstantinos.qttt;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertEquals;

import android.content.pm.ActivityInfo;
import android.view.InputDevice;
import android.view.MotionEvent;
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
    public void multiplayerGameOverCanBeReset() {
        try (ActivityScenario<MultiActivity> scenario =
                ActivityScenario.launch(MultiActivity.class)) {
            scenario.onActivity(
                    activity -> {
                        GameViewModel game =
                                new ViewModelProvider(activity).get(GameViewModel.class);
                        Move[] moves = {
                            new Move(3, 7, CellState.X1),
                            new Move(2, 4, CellState.O2),
                            new Move(1, 7, CellState.X3),
                            new Move(1, 5, CellState.O4),
                            new Move(7, 8, CellState.X5),
                            new Move(4, 8, CellState.O6),
                            new Move(7, 8, CellState.X7),
                            new Move(7, CellState.X7),
                            new Move(0, 6, CellState.O8),
                            new Move(0, 6, CellState.X9),
                            new Move(0, CellState.X9)
                        };
                        for (Move move : moves) {
                            game.applyMove(move);
                        }
                    });

            onView(
                            withText(
                                    ApplicationProvider.getApplicationContext()
                                            .getString(R.string.result_winner, Player.O)))
                    .check(matches(isDisplayed()));
            onView(withText(R.string.action_reset)).perform(click());
            onView(withId(R.id.gameView)).check(hasCurrentTurn(0));
            onView(withId(R.id.action_undo)).check(doesNotExist());
        }
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
