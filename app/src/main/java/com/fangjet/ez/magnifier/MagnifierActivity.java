package com.fangjet.ez.magnifier;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.fangjet.ez.launcher.R;
import com.fangjet.ez.magnifier.filters.ColorFilter;

import java.util.ArrayList;
import java.util.List;


/**
 * The Magnifier activity
 */
public class MagnifierActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CAMERA = 100;
    private static final String TAG = "MagnifierActivity";

    private MagnifierSurfaceView mSurfaceView;

    private final View.OnClickListener colorModeClickHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mSurfaceView.toggleColorMode();
        }
    };
    private final View.OnClickListener flashLightClickHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mSurfaceView.toggleFlashlight();
        }
    };
    private final View.OnClickListener zoomClickHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mSurfaceView.nextZoomLevel();
        }
    };
    private final View.OnLongClickListener tapAndHoldListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            mSurfaceView.toggleAutoFocusMode();
            return true;
        }
    };

    public static boolean checkCameraPermission(final Activity activity, final int requestCode) {

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)) {

            new AlertDialog.Builder(activity)
                    .setMessage("Permission Required")
                    .setTitle("This app needs to access your camera in order to work")
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        // Now request the permission
                        ActivityCompat.requestPermissions(activity,
                                new String[]{Manifest.permission.CAMERA},
                                requestCode);
                    }).show();

        } else {
            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.CAMERA},
                    requestCode);

        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_magnifier);

        mSurfaceView = new MagnifierSurfaceView(this);

        List<ColorFilter> filterList = new ArrayList<>();
        filterList.add(MagnifierSurfaceView.NO_FILTER);
        filterList.add(MagnifierSurfaceView.BLACK_WHITE_COLOR_FILTER);
        filterList.add(MagnifierSurfaceView.WHITE_BLACK_COLOR_FILTER);

        mSurfaceView.setCameraColorFilters(filterList);
        FrameLayout previewLayout = findViewById(R.id.camera_preview);
        previewLayout.addView(mSurfaceView);

        ImageButton zoomButton = findViewById(R.id.button_zoom);
        zoomButton.setOnClickListener(zoomClickHandler);

        ImageButton flashButton = findViewById(R.id.button_flash);
        flashButton.setOnClickListener(flashLightClickHandler);
        flashButton.setImageResource(R.drawable.ic_circle);

        ImageButton colorButton = findViewById(R.id.button_color);
        colorButton.setOnClickListener(colorModeClickHandler);
        colorButton.setImageResource(R.drawable.ic_brightness_med);

        mSurfaceView.setZoomButton(zoomButton);
        mSurfaceView.setFlashButton(flashButton);
        mSurfaceView.setFilterButton(colorButton);

        // Add a listener to the surface view
        mSurfaceView.setOnClickListener(v -> mSurfaceView.autoFocusCamera());

        mSurfaceView.setOnLongClickListener(tapAndHoldListener);

        checkCameraPermission(this, PERMISSIONS_REQUEST_CAMERA);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CAMERA) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // enable camera
                Log.d(TAG, "onRequestPermissionsResult() called with: requestCode = [" + requestCode + "]");
                mSurfaceView.enableCamera();
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

}
