package com.fangjet.ez.magnifier.filters;

/**
 * The base filter from which the others are derived.
 */
abstract class BaseFilter implements ColorFilter {

    private static final float CONTRAST_LEVEL = 0.60f;

    float[] getContrastMatrix() {
        float scale = CONTRAST_LEVEL + 1.f;
        float translate = (-.5f * scale + .5f) * 255.f;
        return new float[]{
                scale, 0, 0, 0, translate,
                0, scale, 0, 0, translate,
                0, 0, scale, 0, translate,
                0, 0, 0, 1, 0
        };
    }

    float[] getInvertMatrix() {
        return new float[]{
                -1, 0, 0, 0, 255,
                0, -1, 0, 0, 255,
                0, 0, -1, 0, 255,
                0, 0, 0, 1, 0
        };
    }

    float[] getGreyscaleMatrix() {
        return new float[]{
                0.5f, 0.5f, 0.5f, 0, 0,
                0.5f, 0.5f, 0.5f, 0, 0,
                0.5f, 0.5f, 0.5f, 0, 0,
                0, 0, 0, 1, 0
        };
    }
}
