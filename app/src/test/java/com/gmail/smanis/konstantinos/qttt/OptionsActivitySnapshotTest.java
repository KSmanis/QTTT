package com.gmail.smanis.konstantinos.qttt;

import static org.junit.Assert.assertEquals;

import android.view.View;
import android.widget.TextView;
import app.cash.paparazzi.DeviceConfig;
import app.cash.paparazzi.Paparazzi;
import org.junit.Rule;
import org.junit.Test;

public class OptionsActivitySnapshotTest {
    @Rule public final Paparazzi paparazzi = new Paparazzi();

    @Test
    public void defaultOptions() {
        paparazzi.unsafeUpdateConfig(null, "Theme.AppCompat.Light.NoActionBar", null);
        View view = paparazzi.inflate(R.layout.activity_options);

        paparazzi.snapshot(view);
    }

    @Test
    public void greekOptions() {
        paparazzi.unsafeUpdateConfig(
                deviceWithLocale(DeviceConfig.PIXEL_2, "el"),
                "Theme.AppCompat.Light.NoActionBar",
                null);
        View view = paparazzi.inflate(R.layout.activity_options);
        TextView playerLabel = view.findViewById(R.id.textView_player);

        assertEquals("Ανθρώπινος Παίκτης", playerLabel.getText().toString());
        paparazzi.snapshot(view);
    }

    private static DeviceConfig deviceWithLocale(DeviceConfig device, String locale) {
        return new DeviceConfig(
                device.getScreenHeight(),
                device.getScreenWidth(),
                device.getXdpi(),
                device.getYdpi(),
                device.getOrientation(),
                device.getUiMode(),
                device.getNightMode(),
                device.getDensity(),
                device.getFontScale(),
                device.getLayoutDirection(),
                locale,
                device.getRatio(),
                device.getSize(),
                device.getKeyboard(),
                device.getTouchScreen(),
                device.getKeyboardState(),
                device.getSoftButtons(),
                device.getNavigation(),
                device.getScreenRound(),
                device.getReleased());
    }
}
