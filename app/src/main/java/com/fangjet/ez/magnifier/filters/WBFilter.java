package com.fangjet.ez.magnifier.filters;

import android.graphics.ColorMatrix;

import com.fangjet.ez.launcher.R;


/**
 * White on black filter
 */
public class WBFilter extends BWFilter {

    @Override
    public int getFilterIcon() {
        return R.drawable.ic_brightness_more;
    }

    @Override
    public void filter(ColorMatrix colorMatrix) {
        super.filter(colorMatrix);

        float[] inverted = getInvertMatrix();
        colorMatrix.postConcat(new ColorMatrix(inverted));
    }
}