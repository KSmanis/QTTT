package com.gmail.smanis.konstantinos.qttt;

import android.view.View;
import app.cash.paparazzi.Paparazzi;
import org.junit.Rule;
import org.junit.Test;

public class MainActivitySnapshotTest {
    @Rule public final Paparazzi paparazzi = new Paparazzi();

    @Test
    public void mainScreen() {
        paparazzi.unsafeUpdateConfig(null, "Theme.AppCompat.Light.NoActionBar", null);
        View view = paparazzi.inflate(R.layout.activity_main);

        paparazzi.snapshot(view);
    }
}
