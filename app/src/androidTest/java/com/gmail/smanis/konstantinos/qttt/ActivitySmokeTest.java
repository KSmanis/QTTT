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
import android.view.View;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.ViewAssertion;
import androidx.test.espresso.action.CoordinatesProvider;
import androidx.test.espresso.action.GeneralClickAction;
import androidx.test.espresso.action.Press;
import androidx.test.espresso.action.Tap;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
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

    private static void clickPlayButton(int cardId) {
        onView(allOf(withText(R.string.button_play), isDescendantOfA(withId(cardId))))
                .perform(click());
    }

    private static ViewAssertion hasCurrentTurn(int expected) {
        return (view, error) -> {
            if (error != null) {
                throw error;
            }
            assertEquals(expected, ((GameView) view).state().currentTurn());
        };
    }

    private static GeneralClickAction clickCell(int row, int column) {
        CoordinatesProvider coordinates =
                new CoordinatesProvider() {
                    @Override
                    public float[] calculateCoordinates(View view) {
                        int[] location = new int[2];
                        view.getLocationOnScreen(location);
                        return new float[] {
                            location[0] + view.getWidth() * (column + 0.5f) / 3,
                            location[1] + view.getHeight() * (row + 0.5f) / 3
                        };
                    }
                };
        return new GeneralClickAction(
                Tap.SINGLE,
                coordinates,
                Press.FINGER,
                InputDevice.SOURCE_UNKNOWN,
                MotionEvent.BUTTON_PRIMARY);
    }
}
