package com.fangjet.ez.magnifier.filters;

import android.graphics.ColorMatrix;

import com.fangjet.ez.launcher.R;

/**
 * Default mode - no filtering
 */
public class NoFilter implements ColorFilter {

    @Override
    public int getFilterIcon() {
        return R.drawable.ic_brightness_med;
    }

    @Override
    public void filter(ColorMatrix colorMatrix) {
        // Do nothing
    }
}
