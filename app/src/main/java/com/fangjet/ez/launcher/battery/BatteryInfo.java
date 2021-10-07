package com.fangjet.ez.launcher.battery;

import static com.fangjet.ez.launcher.battery.BatteryInfo.Field.FIELD_PERCENT;
import static com.fangjet.ez.launcher.battery.BatteryInfo.Field.FIELD_PLUGGED;
import static com.fangjet.ez.launcher.battery.BatteryInfo.Field.FIELD_STATUS;
import static com.fangjet.ez.launcher.battery.BatteryInfo.Plugged.PLUGGED_AC;
import static com.fangjet.ez.launcher.battery.BatteryInfo.Plugged.PLUGGED_MAX;
import static com.fangjet.ez.launcher.battery.BatteryInfo.Plugged.PLUGGED_UNKNOWN;
import static com.fangjet.ez.launcher.battery.BatteryInfo.Plugged.PLUGGED_UNPLUGGED;
import static com.fangjet.ez.launcher.battery.BatteryInfo.Plugged.PLUGGED_USB;
import static com.fangjet.ez.launcher.battery.BatteryInfo.Plugged.PLUGGED_WIRELESS;
import static com.fangjet.ez.launcher.battery.BatteryInfo.Status.STATUS_CHARGING;
import static com.fangjet.ez.launcher.battery.BatteryInfo.Status.STATUS_DISCHARGING;
import static com.fangjet.ez.launcher.battery.BatteryInfo.Status.STATUS_FULLY_CHARGED;
import static com.fangjet.ez.launcher.battery.BatteryInfo.Status.STATUS_NOT_CHARGING;
import static com.fangjet.ez.launcher.battery.BatteryInfo.Status.STATUS_UNKNOWN;
import static com.fangjet.ez.launcher.battery.BatteryInfo.Status.STATUS_UNPLUGGED;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.IntDef;
import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class BatteryInfo {

    private static final String EXTRA_LEVEL = "level";
    private static final String EXTRA_SCALE = "scale";
    private static final String EXTRA_STATUS = "status";
    private static final String EXTRA_PLUGGED = "plugged";

    @Status
    private int status;
    @Plugged
    private int plugged;

    private int percent;

    void load(Intent intent) {
        int level = intent.getIntExtra(EXTRA_LEVEL, 50);
        int scale = intent.getIntExtra(EXTRA_SCALE, 100);

        status = intent.getIntExtra(EXTRA_STATUS, STATUS_UNKNOWN);
        plugged = intent.getIntExtra(EXTRA_PLUGGED, PLUGGED_UNKNOWN);
        percent = level * 100 / scale;

        if (percent > 100) {
            percent = 100;
        } else if (percent < 0) {
            percent = 0;
        }

        if (plugged == PLUGGED_UNPLUGGED) {
            status = STATUS_UNPLUGGED;
        } else if (status > STATUS_FULLY_CHARGED) {
            status = STATUS_UNKNOWN;
        }

        if (plugged > PLUGGED_MAX) {
            plugged = PLUGGED_UNKNOWN;
        }
    }

    Bundle toBundle() {
        Bundle bundle = new Bundle();
        bundle.putInt(FIELD_PERCENT, percent);
        bundle.putInt(FIELD_STATUS, status);
        bundle.putInt(FIELD_PLUGGED, plugged);
        return bundle;
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({STATUS_UNPLUGGED, STATUS_UNKNOWN, STATUS_CHARGING, STATUS_DISCHARGING,
            STATUS_NOT_CHARGING, STATUS_FULLY_CHARGED})
    public @interface Status {
        int STATUS_UNPLUGGED = 0;
        int STATUS_UNKNOWN = 1;
        int STATUS_CHARGING = 2;
        int STATUS_DISCHARGING = 3;
        int STATUS_NOT_CHARGING = 4;
        int STATUS_FULLY_CHARGED = 5;
    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({FIELD_PERCENT, FIELD_STATUS, FIELD_PLUGGED})
    public @interface Field {
        String FIELD_PERCENT = "percent";
        String FIELD_STATUS = "status";
        String FIELD_PLUGGED = "plugged";
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({PLUGGED_UNPLUGGED, PLUGGED_AC, PLUGGED_USB, PLUGGED_UNKNOWN,
            PLUGGED_WIRELESS, PLUGGED_MAX})
    public @interface Plugged {
        int PLUGGED_UNPLUGGED = 0;
        int PLUGGED_AC = 1;
        int PLUGGED_USB = 2;
        int PLUGGED_UNKNOWN = 3;
        int PLUGGED_WIRELESS = 4;
        int PLUGGED_MAX = 5;
    }
}
