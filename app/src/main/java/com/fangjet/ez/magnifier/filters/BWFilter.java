package com.fangjet.ez.magnifier.filters;

import android.graphics.ColorMatrix;

import com.fangjet.ez.launcher.R;


/**
 * Black and White filter
 */
public class BWFilter extends BaseFilter {

    @Override
    public int getFilterIcon() {
        return R.drawable.ic_brightness_half;
    }

    @Override
    public void filter(ColorMatrix colorMatrix) {
        float[] contrast = getContrastMatrix();
        float[] greyscale = getGreyscaleMatrix();

        colorMatrix.postConcat(new ColorMatrix(greyscale));
        colorMatrix.postConcat(new ColorMatrix(contrast));
    }
}
