package com.fangjet.ez.utils

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Camera
import android.util.Log
import androidx.annotation.WorkerThread

/**
 * Camera Utilities
 *
 *
 * Created by ctucker on 6/16/17.
 */
object CameraUtils {
    private const val TAG = "CameraUtils"

    /**
     * open and return a camera instance.
     *
     * @param cameraId The camera ID
     * @return A Camera, or null if it went wrong
     */
    @WorkerThread
    private fun getCameraInstance(cameraId: Int): Camera? {
        var cameraId = cameraId
        val numOfCameras = Camera.getNumberOfCameras()
        if (cameraId >= numOfCameras) {
            Log.w(TAG, "getCameraInstance() called with: cameraId = [$cameraId] - Not Found")
            return null
        }
        val camera: Camera? = try {
            Camera.open(cameraId)
        } catch (e: RuntimeException) {
            Log.e(TAG, "Caught: " + e.message, e)
            getCameraInstance(++cameraId)
        }

        // TODO Tell the user we couldn't get the camera
        if (camera == null) {
            Log.e(TAG, "getCameraInstance() returned null")
        }
        return camera
    }

    /**
     * return camera with id 0 (default: back camera)
     *
     * @return Camera instance
     */
    @JvmStatic
    @get:WorkerThread
    val cameraInstance: Camera?
        get() = getCameraInstance(0)

    /**
     * Check if any of the current devices has a flash.
     *
     * @return true if flash is supported
     */
    @JvmStatic
    fun supportsFlashlight(context: Context, camera: Camera): Boolean {
        val hasFlash = context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
        if (!hasFlash) {
            return false
        }
        val parameters = camera.parameters
        val supportedFlashModes = parameters.supportedFlashModes
        return !(supportedFlashModes == null || !supportedFlashModes.contains(Camera.Parameters.FLASH_MODE_TORCH))
    }

    /**
     * Toggle the flashlight on or off
     *
     * @param camera  The camera
     * @param enabled The state (true=on)
     */
    @JvmStatic
    fun toggleFlashlight(camera: Camera, enabled: Boolean) {

        val parameters = camera.parameters
        if (enabled) {
            parameters.flashMode = Camera.Parameters.FLASH_MODE_TORCH
        } else {
            parameters.flashMode = Camera.Parameters.FLASH_MODE_OFF
        }
        // TODO this crashes the emulator, migrate to camera2
        camera.parameters = parameters
    }
}