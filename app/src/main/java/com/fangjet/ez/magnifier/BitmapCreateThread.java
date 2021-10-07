package com.fangjet.ez.magnifier;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;

import androidx.annotation.Nullable;

/**
 * Render the bitmap out of the raw yuvData
 */
class BitmapCreateThread implements Runnable {

    private static final String TAG = "BitmapCreateThread";
    private static final int MAX_THREADS = 4;
    private static int sNumberOfThreads = 0;

    private int mPreviewWidth;
    private int mPreviewHeight;
    private int mTargetWidth;
    private int mTargetHeight;
    private Renderer mRenderer;
    private byte[] mYuvData;

    /**
     * Constructor
     *
     * @param yuvDataArray  The data
     * @param renderer      The Renderer to use
     * @param previewWidth  The preview width
     * @param previewHeight The preview height
     * @param targetWidth   The target width
     * @param targetHeight  The target height
     * @return the thread, or null if MAX_THREADS is reached
     */
    @Nullable
    static BitmapCreateThread getInstance(byte[] yuvDataArray, Renderer renderer,
                                          int previewWidth, int previewHeight, int targetWidth,
                                          int targetHeight) {

        if (sNumberOfThreads >= MAX_THREADS) {
            Log.d(TAG, "Thread not created, sNumberOfThreads >= MAX_THREADS");
            return null;
        }

        BitmapCreateThread instance = new BitmapCreateThread();
        sNumberOfThreads++;

        instance.setYuvData(yuvDataArray);
        instance.setPreviewWidth(previewWidth);
        instance.setPreviewHeight(previewHeight);
        instance.setTargetWidth(targetWidth);
        instance.setTargetHeight(targetHeight);
        instance.setRenderer(renderer);

        return instance;
    }

    private void setPreviewHeight(int previewHeight) {
        mPreviewHeight = previewHeight;
    }

    private void setPreviewWidth(int previewWidth) {
        mPreviewWidth = previewWidth;
    }

    private void setRenderer(Renderer renderer) {
        mRenderer = renderer;
    }

    private void setYuvData(byte[] yuvData) {
        mYuvData = yuvData;
    }

    /**
     * Create the bitmap
     *
     * @param data The data
     * @return the resulting bitmap.
     */
    private Bitmap createBitmap(byte[] data) {

        // Crate the bitmap from the data
        Bitmap editedBitmap = Bitmap.createBitmap(mPreviewWidth, mPreviewHeight, Bitmap.Config.ARGB_8888);
        int[] rgbData = decodeGreyscale(data, mPreviewWidth, mPreviewHeight);
        editedBitmap.setPixels(rgbData, 0, mPreviewWidth, 0, 0, mPreviewWidth, mPreviewHeight);

        // Rotate it
        // TODO this may be inefficient
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        Bitmap rotatedBitmap = Bitmap.createBitmap(editedBitmap, 0, 0, mPreviewWidth, mPreviewHeight,
                matrix, true);

        // Scale it and return
        return Bitmap.createScaledBitmap(rotatedBitmap, mTargetWidth, mTargetHeight, true);
    }

    /**
     * TODO: can this be done more efficiently?
     * <p>
     * https://stackoverflow.com/a/9330203/1496122
     * <p>
     * note this is based on a downvoted answer
     * <p>
     * https://stackoverflow.com/a/29963291/1496122
     */
    private int[] decodeGreyscale(byte[] nv21, int width, int height) {
        int pixelCount = width * height;
        int[] out = new int[pixelCount];
        int luminance;
        for (int i = 0; i < pixelCount; ++i) {
            luminance = nv21[i] & 0xFF;
            out[i] = 0xff000000 | luminance << 16 | luminance << 8 | luminance;
        }
        return out;
    }

    @Override
    public void run() {
        mRenderer.renderBitmap(createBitmap(mYuvData));
        sNumberOfThreads--;
    }

    private void setTargetWidth(int targetWidth) {
        mTargetWidth = targetWidth;
    }

    private void setTargetHeight(int targetHeight) {
        mTargetHeight = targetHeight;
    }
}
