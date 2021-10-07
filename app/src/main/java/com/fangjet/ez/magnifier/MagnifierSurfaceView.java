package com.fangjet.ez.magnifier;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Point;
import android.hardware.Camera;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatActivity;

import com.fangjet.ez.launcher.R;
import com.fangjet.ez.magnifier.filters.BWFilter;
import com.fangjet.ez.magnifier.filters.ColorFilter;
import com.fangjet.ez.magnifier.filters.NoFilter;
import com.fangjet.ez.magnifier.filters.WBFilter;
import com.fangjet.ez.utils.CameraUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The surface view for the camera preview
 */
public class MagnifierSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Renderer {

    public final static ColorFilter BLACK_WHITE_COLOR_FILTER = new BWFilter();
    public final static ColorFilter WHITE_BLACK_COLOR_FILTER = new WBFilter();
    public final static ColorFilter NO_FILTER = new NoFilter();

    private static final String TAG = "MagnifierSurfaceView";

    private static final int ZOOM_STEPS = 3;

    // TODO convert to enums
    private static final int STATE_CLOSED = 0;
    private static final int STATE_OPENED = 1;
    private static final int STATE_PREVIEW = 2;

    private static final int MAX_CAMERA_PREVIEW_RESOLUTION_WIDTH = 800;

    private final SurfaceHolder mHolder;
    private final int mWidth;
    private final int mHeight;
    private final Paint mColorFilterPaint;
    private Camera mCamera;
    private int mCameraCurrentZoomLevel;
    private boolean mCameraFlashMode;
    private int mCameraMaxZoomLevel;
    private int mCameraPreviewWidth;
    private int mCameraPreviewHeight;
    /**
     * the current state of the camera device.
     * i.e. open, closed or preview.
     */
    // TODO enum
    private int mState;

    @NonNull
    private List<ColorFilter> mColorFilters = new ArrayList<>();

    private int mCurrentColorFilterIndex;
    private byte[] mCameraPreviewBufferData;
    private final Camera.PreviewCallback mCameraPreviewCallbackHandler = new Camera.PreviewCallback() {

        @UiThread
        @Override
        public void onPreviewFrame(final byte[] data, Camera camera) {

            mCameraPreviewBufferData = data;
            if (!hasActiveFilterEnabled()) {
                invalidate();
                return;
            }

            runBitmapCreateThread();
        }
    };
    private Bitmap mCameraPreviewBitmapBuffer;
    private ImageButton mZoomButton;
    private ImageButton mFlashButton;
    private ImageButton mFilterButton;

    /**
     * @param context activity
     */
    public MagnifierSurfaceView(@NonNull Context context) {
        super(context);

        mCameraCurrentZoomLevel = 0;
        mCameraMaxZoomLevel = 0;
        mCurrentColorFilterIndex = 0;

        mCameraFlashMode = false;
        mColorFilterPaint = new Paint();

        mState = STATE_CLOSED;

        Display mDisplay = ((AppCompatActivity) context).getWindowManager().getDefaultDisplay();

        Point sizePoint = new Point();

        mDisplay.getSize(sizePoint);
        // getting a preciser value of the screen size to be more accurate.
        mDisplay.getRealSize(sizePoint);

        mWidth = sizePoint.x;
        mHeight = sizePoint.y;

        // We are using our own onDraw method
        setWillNotDraw(false);

        setDrawingCacheEnabled(true);

        mCamera = null;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        enableCamera();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

        try {
            if (mCamera == null) {
                Log.d(TAG, "surfaceDestroyed() called but camera is null");
            } else {
                Log.d(TAG, "surfaceDestroyed() called");

                mCamera.stopPreview();
                mCamera.setPreviewCallback(null);
                mCamera.release();
                mCamera = null;

                mState = STATE_CLOSED;
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    /**
     * returns the maximum possible camera preview size which is the same or less than you've
     * specified with the {MAX_CAMERA_PREVIEW_RESOLUTION_WIDTH} const.
     *
     * @param parameters the camera parameters to receive all supported preview sizes.
     * @return Camera.Size or null if the parameters could not be accessed or some other issues occurred.
     */
    private Camera.Size getBestPreviewSize(Camera.Parameters parameters) {
        Camera.Size result = null;

        List<Camera.Size> size = parameters.getSupportedPreviewSizes();
        //noinspection ComparatorCombinators
        Collections.sort(size, (lhs, rhs) -> Integer.compare(lhs.width, rhs.width));

        if (size.size() <= 0) return null;

        for (int i = (size.size() - 1); i >= 0; i--) {
            final int currentWidth = size.get(i).width;
            if (currentWidth <= MAX_CAMERA_PREVIEW_RESOLUTION_WIDTH) {
                result = size.get(i);
                break;
            }
        }

        // just use the last one, if there are only a few supported sizes.
        if (result == null) {
            return size.get(size.size() - 1);
        }

        Log.d(TAG, "getBestPreviewSize() returned: " + result.width + "*" + result.height);
        return result;
    }

    /**
     * Open and enable the camera.
     */
    @SuppressLint("WrongThread")
    @UiThread
    public void enableCamera() {

        if (mState == STATE_PREVIEW) return;

        if (mCamera == null) {
            // FIXME lint thread warning here.
            mCamera = CameraUtils.getCameraInstance();
            mState = STATE_OPENED;
        }
        if (mCamera == null) {
            // Error handling...
            return;
        }
        mCamera.setDisplayOrientation(90);

        Camera.Parameters parameters = mCamera.getParameters();
        if (parameters.isZoomSupported()) {
            mCameraMaxZoomLevel = parameters.getMaxZoom();
        } else {
            mZoomButton.setVisibility(View.INVISIBLE);
        }

        if (!getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            mFlashButton.setVisibility(View.INVISIBLE);
        }

        int cameraPreviewFormat = parameters.getPreviewFormat();
        if (cameraPreviewFormat != ImageFormat.NV21) {
            parameters.setPreviewFormat(ImageFormat.NV21);
        }

        // no sizes found? something went wrong
        Camera.Size size = getBestPreviewSize(parameters);
        if (size == null) {
            return;
        }

        mCameraPreviewWidth = size.width;
        mCameraPreviewHeight = size.height;

        parameters.setPreviewSize(mCameraPreviewWidth, mCameraPreviewHeight);

        // Give the camera a hint that we're recording video.  This can have a big
        // impact on frame rate.
        parameters.setRecordingHint(true);

        mCamera.setParameters(parameters);

        // pre-define some variables for image processing.
        mCameraPreviewBufferData = new byte[mCameraPreviewWidth * mCameraPreviewHeight * 3 / 2];

        // The Surface has been created, now tell the
        // camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(mHolder);
        } catch (IOException e) {
            Log.e(TAG, "Caught:" + e.getMessage(), e);
            return;
        }

        mCamera.setPreviewCallback(mCameraPreviewCallbackHandler);
        mCamera.startPreview();
        mState = STATE_PREVIEW;

        // start with the first zoom level.
        // init zoom level member attr.
        if (mCameraCurrentZoomLevel == 0) {
            mCameraCurrentZoomLevel = mCameraMaxZoomLevel;
            nextZoomLevel();
        } else {
            setCameraZoomLevel(mCameraCurrentZoomLevel);
        }

        if (mCurrentColorFilterIndex > 0) {
            mCurrentColorFilterIndex--;
            toggleColorMode();
        }

        Log.d(TAG, "enableCamera() - finished");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        enableCamera();
    }

    /**
     * Auto focus once
     */
    public void autoFocusCamera() {
        if (mState != STATE_PREVIEW) {
            return;
        }
        mCamera.cancelAutoFocus();
        mCamera.autoFocus(null);
    }

    /**
     * Toggle the auto focus mode
     */
    public void toggleAutoFocusMode() {
        if (mState != STATE_PREVIEW) {
            return;
        }

        Camera.Parameters cameraParameters = mCamera.getParameters();

        List<String> focusModes = cameraParameters.getSupportedFocusModes();
        if (!focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            return;
        }
        if (!focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            return;
        }

        String currentMode = cameraParameters.getFocusMode();
        if (currentMode.equals(Camera.Parameters.FOCUS_MODE_AUTO)) {
            Toast.makeText(MagnifierSurfaceView.this.getContext(), R.string.text_autofocus_enabled, Toast.LENGTH_SHORT).show();
            cameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        } else {
            Toast.makeText(MagnifierSurfaceView.this.getContext(), R.string.text_autofocus_disabled, Toast.LENGTH_SHORT).show();
            cameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }

        mCamera.setParameters(cameraParameters);

    }

    /**
     * Toggles flashlight
     */
    public void toggleFlashlight() {
        if (mState != STATE_PREVIEW) {
            return;
        }

        mCameraFlashMode = !mCameraFlashMode;
        if (mCameraFlashMode) {
            turnFlashlightOn();
            mFlashButton.setImageResource(R.drawable.ic_lens);
        } else {
            turnFlashlightOff();
            mFlashButton.setImageResource(R.drawable.ic_circle);
        }
    }

    private void turnFlashlightOff() {
        if (mState != STATE_PREVIEW || !CameraUtils.supportsFlashlight(getContext(), mCamera)) {
            return;
        }
        CameraUtils.toggleFlashlight(mCamera, false);
    }

    private void turnFlashlightOn() {
        if (mState != STATE_PREVIEW || !CameraUtils.supportsFlashlight(getContext(), mCamera)) {
            return;
        }
        CameraUtils.toggleFlashlight(mCamera, true);
    }

    /**
     * TODO rewrite this
     */
    public void nextZoomLevel() {
        final int steps = (mCameraMaxZoomLevel / (ZOOM_STEPS - 1));
        final int modulo = (mCameraMaxZoomLevel % (ZOOM_STEPS - 1));

        int nextLevel = mCameraCurrentZoomLevel + steps;

        if (mCameraCurrentZoomLevel == mCameraMaxZoomLevel) {
            nextLevel = modulo;
        }

        if (mState == STATE_PREVIEW) {
            setCameraZoomLevel(nextLevel);
        }
    }

    /**
     * Set the available color filters
     *
     * @param colorFilters A list of color filters
     */
    public void setCameraColorFilters(@NonNull List<ColorFilter> colorFilters) {
        mColorFilters = colorFilters;
    }

    /**
     * Toggle the color mode
     */
    public void toggleColorMode() {
        if (mState == STATE_CLOSED) {
            return;
        }

        mCurrentColorFilterIndex++;
        if (mCurrentColorFilterIndex >= mColorFilters.size()) {
            mCurrentColorFilterIndex = 0;
        }

        ColorFilter currentFilter = mColorFilters.get(mCurrentColorFilterIndex);

        ColorMatrix colorMatrix = new ColorMatrix();
        currentFilter.filter(colorMatrix);

        ColorMatrixColorFilter colorFilter = new ColorMatrixColorFilter(colorMatrix);
        mColorFilterPaint.setColorFilter(colorFilter);
        mFilterButton.setImageResource(currentFilter.getFilterIcon());

        if (mState == STATE_OPENED) {
            invalidate();
        }
    }

    /**
     * Runs a bitmap create thread with the current `mCameraPreviewBufferData`.
     * If finished, the thread calls `renderBitmap` with the final bitmap as the result.
     */
    private void runBitmapCreateThread() {
        final BitmapCreateThread bitmapCreateThread = BitmapCreateThread.getInstance(
                mCameraPreviewBufferData,
                MagnifierSurfaceView.this,
                mCameraPreviewWidth,
                mCameraPreviewHeight,
                mWidth,
                mHeight
        );

        if (bitmapCreateThread == null) {
            // Cannot create another thread
            return;
        }
        new Thread(bitmapCreateThread).start();
    }

    /**
     * sets the bitmap.
     *
     * @param bitmap The bitmap
     */
    @WorkerThread
    public void renderBitmap(Bitmap bitmap) {

        mCameraPreviewBitmapBuffer = bitmap;

        ((Activity) getContext()).runOnUiThread(this::invalidate);
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (mState == STATE_CLOSED) {
            return;
        }
        if (mCameraPreviewBitmapBuffer == null || mCameraPreviewBitmapBuffer.isRecycled()) {
            return;
        }

        if (!((mState == STATE_PREVIEW && hasActiveFilterEnabled()) || mState == STATE_OPENED)) {
            return;
        }

        /*
         * Description:
         * If the state is opened the preview is probably paused
         */
        canvas.drawBitmap(mCameraPreviewBitmapBuffer, 0, 0, mColorFilterPaint);
    }

    /**
     * determines if a filter is active. A filter is active if it is not "NO_FILTER".
     * Used to save performance while have normal (without color effects) camera preview enabled.
     *
     * @return true if the current color mode is not NO_FILTER
     */
    private boolean hasActiveFilterEnabled() {
        return (mColorFilters.get(mCurrentColorFilterIndex) != NO_FILTER);
    }

    /**
     * Set the zoom level
     *
     * @param zoomLevel The desired zoom level
     */
    private void setCameraZoomLevel(int zoomLevel) {

        Camera.Parameters parameters = mCamera.getParameters();
        if (!parameters.isZoomSupported()) {
            Log.e(TAG, "setCameraZoomLevel() - not supported");
            return;
        }
        Log.d(TAG, "setCameraZoomLevel() called with: zoomLevel = [" + zoomLevel + "]");

        if (zoomLevel > mCameraMaxZoomLevel) {
            zoomLevel = mCameraMaxZoomLevel;
        }
        mCameraCurrentZoomLevel = zoomLevel;

        parameters.setZoom(mCameraCurrentZoomLevel);
        mCamera.setParameters(parameters);

        if (zoomLevel == mCameraMaxZoomLevel) {
            mZoomButton.setImageResource(R.drawable.ic_zoom_out);
        } else {
            mZoomButton.setImageResource(R.drawable.ic_zoom);
        }

    }

    public void setZoomButton(ImageButton zoomButton) {
        this.mZoomButton = zoomButton;
    }

    public void setFlashButton(ImageButton flashButton) {
        this.mFlashButton = flashButton;
    }

    public void setFilterButton(ImageButton filterButton) {
        mFilterButton = filterButton;
    }
}
