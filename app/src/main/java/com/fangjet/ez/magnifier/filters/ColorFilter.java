package com.fangjet.ez.magnifier.filters;

import android.graphics.ColorMatrix;

import androidx.annotation.DrawableRes;

public interface ColorFilter {

    @DrawableRes
    int getFilterIcon();

    void filter(ColorMatrix colorMatrix);
}
