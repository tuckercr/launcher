/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fangjet.ez.launcher

import android.content.ActivityNotFoundException
import android.content.ContentUris
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ResolveInfo
import android.hardware.Camera
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fangjet.ez.magnifier.MagnifierActivity
import com.fangjet.ez.utils.CameraUtils
import java.util.*

/**
 * Contains the Date, Time, Battery Levels and the AppIcon Grid
 */
class MainFragment : Fragment(), LoaderManager.LoaderCallbacks<EzLoaderResult?> {

    private var mRecyclerView: RecyclerView? = null
    private var mDataset: ArrayList<AppIconPair>? = null
    private var mPackageInfo: ArrayList<ResolveInfo>? = null
    private var flashLightIcon: AppIcon? = null
    private var mCamera: Camera? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_main, container, false)
        rootView.tag = TAG
        mRecyclerView = rootView.findViewById(R.id.recyclerView)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initDataset()
        val adapter = MainFragmentRecyclerViewAdapter(view.context, mDataset!!)
        mRecyclerView!!.adapter = adapter
        var scrollPosition = 0

        // If a layout manager has already been set, get current scroll position.
        if (mRecyclerView!!.layoutManager != null) {
            scrollPosition = (mRecyclerView!!.layoutManager as LinearLayoutManager?)
                ?.findFirstCompletelyVisibleItemPosition() ?: 0
        }

//        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
//        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView!!.scrollToPosition(scrollPosition)
        mRecyclerView!!.addItemDecoration(MyDividerItemDecoration(20))
        if (activity != null) {
            LoaderManager.getInstance(this).initLoader(LOADER_ID, null, this).forceLoad()
        }
    }

    /**
     * Generates the dataset for RecyclerView's adapter
     */
    private fun initDataset() {
        mDataset = ArrayList()
        val longClickListener = OnLongClickListener { view: View ->
            Log.e(TAG, "onLongClick($view)")
            AlertDialog.Builder(requireContext())
                .setItems(R.array.settings_menu) { dialogInterface: DialogInterface?, i: Int ->
                    when (i) {
                        0 -> {
                        }
                        1 -> {
                        }
                        2 -> {
                        }
                    }
                }
                .setCancelable(true)
                .show()
            true
        }
        val phone = AppIcon(
            R.drawable.ic_call, R.string.phone, R.color.phone,
            android.R.color.white, { v: View ->
                Log.d(TAG, "onClick() - call icon")
                val callIntent = Intent(Intent.ACTION_DIAL, null)
                callIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                v.context.startActivity(callIntent)
            }, longClickListener
        )
        val sms = AppIcon(
            R.drawable.ic_sms, R.string.sms, R.color.text,
            android.R.color.white, { v: View? ->
                val intent = Intent(Intent.ACTION_MAIN)
                intent.addCategory(Intent.CATEGORY_APP_MESSAGING)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }, longClickListener
        )
        val camera = AppIcon(
            R.drawable.ic_camera, R.string.camera, R.color.camera,
            android.R.color.white, { v: View? ->
                Log.d(TAG, "onClick() - call icon")
                try {
                    if (MagnifierActivity.checkCameraPermission(
                            activity,
                            MainActivity.PERMISSIONS_REQUEST_CAMERA_FOR_CAMERA
                        )
                    ) {

                        // This captures an image then returns to the app.
                        val intent = Intent("android.media.action.IMAGE_CAPTURE")
                        startActivity(intent)

                        // This is not working...
                        // Intent cameraIntent = new Intent(Intent.ACTION_CAMERA_BUTTON, null);
                        // v.getContext().startActivity(cameraIntent);
                    }
                } catch (e: ActivityNotFoundException) {
                    // TODO handle this... is there a different intent?
                    Toast.makeText(activity, "Camera App Not Found", Toast.LENGTH_SHORT).show()
                }
            }, longClickListener
        )

// System Settings:
//
//        mDataset.add(new AppIcon(R.drawable.ic_settings, R.string.settings, new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // review
//                v.getContext().startActivity(intent);
//            }
//        }));
        val gallery = AppIcon(
            R.drawable.ic_photo_library, R.string.gallery, R.color.gallery,
            android.R.color.white, { v: View? ->
                val intent = Intent()
                intent.action = Intent.ACTION_VIEW
                intent.type = "image/*"
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }, longClickListener
        )
        val calendar = AppIcon(
            R.drawable.ic_date_range, R.string.calendar, R.color.calendar,
            android.R.color.white, { v: View? ->
                val builder = CalendarContract.CONTENT_URI.buildUpon()
                builder.appendPath("time")
                ContentUris.appendId(builder, System.currentTimeMillis())
                val intent = Intent(Intent.ACTION_VIEW)
                    .setData(builder.build())
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }, longClickListener
        )
        val clock = AppIcon(
            R.drawable.ic_alarm, R.string.clock, R.color.clock,
            android.R.color.white, { v: View? -> }, longClickListener
        )
        val contacts = AppIcon(
            R.drawable.ic_people, R.string.contacts, R.color.contacts,
            android.R.color.white, { v: View? -> }, longClickListener
        )
        val calculator = AppIcon(
            R.drawable.ic_grid, R.string.calculator, R.color.calculator,
            android.R.color.white, { v: View? -> }, longClickListener
        )
        val internet = AppIcon(
            R.drawable.ic_web, R.string.internet, R.color.internet,
            android.R.color.white, { v: View? ->
                // Can we open without specifying a URL?
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"))
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }, longClickListener
        )
        val email = AppIcon(
            R.drawable.ic_email, R.string.email, R.color.email,
            android.R.color.white, { v: View? ->
                val intent =
                    Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_EMAIL)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }, longClickListener
        )
        val maps = AppIcon(
            R.drawable.ic_directions, R.string.maps, R.color.map,
            android.R.color.white, { v: View? ->
                // FIXME not properly implemented
                val gmmIntentUri = Uri.parse("google.navigation:q=Miami+Zoo,+Miami+Florida")
                val intent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                intent.setPackage("com.google.android.apps.maps")
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }, longClickListener
        )
        val sos = AppIcon(
            R.drawable.ic_antenna,
            R.string.sos,
            R.color.sos,
            android.R.color.white,
            { v: View? -> Toast.makeText(context, "Coming Soon!", Toast.LENGTH_SHORT).show() },
            longClickListener
        )
        val magnifier = AppIcon(
            R.drawable.ic_zoom_in, R.string.magnifier, R.color.magnifier,
            android.R.color.white, { v: View? ->
                if (MagnifierActivity.checkCameraPermission(
                        activity, MainActivity.PERMISSIONS_REQUEST_CAMERA_FOR_MAGNIFIER
                    )
                ) {
                    val intent = Intent(activity, MagnifierActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }
            }, longClickListener
        )
        flashLightIcon = AppIcon(
            R.drawable.ic_highlight,
            R.string.flash_light,
            R.color.flashlight,
            android.R.color.white,
            { v: View? -> toggleFlashlight(flashLightIcon) },
            longClickListener,
            android.R.color.white,
            R.color.flashlight,
            R.drawable.ic_highlight
        )
        val apps = AppIcon(
            R.drawable.ic_apps, R.string.apps, R.color.black,
            android.R.color.white, { v: View? ->
                findNavController().navigate(R.id.action_mainFragment_to_appListFragment)
            }, longClickListener
        )
        mDataset!!.add(AppIconPair(phone, sms))
        mDataset!!.add(AppIconPair(camera, gallery))
        mDataset!!.add(AppIconPair(calendar, clock))
        mDataset!!.add(AppIconPair(contacts, calculator))
        mDataset!!.add(AppIconPair(internet, email))
        mDataset!!.add(AppIconPair(maps, sos))
        mDataset!!.add(AppIconPair(flashLightIcon!!, magnifier))
        mDataset!!.add(AppIconPair(apps))
    }

    private fun toggleFlashlight(flashLightIcon: AppIcon?) {

        // TODO is not working?

        // TODO check if feature available:
        // if (!getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
        if (!MagnifierActivity.checkCameraPermission(
                activity,
                MainActivity.PERMISSIONS_REQUEST_CAMERA_FOR_FLASHLIGHT
            )
        ) {
            Log.e(TAG, "toggleFlashlight() - insufficient permissions")
            return
        }
        if (mCamera == null) {
            mCamera = CameraUtils.cameraInstance
        }
        if (mCamera == null) {
            Toast.makeText(context, "Flashlight not found", Toast.LENGTH_SHORT).show()
            return
        }
        CameraUtils.toggleFlashlight(mCamera!!, !flashLightIcon!!.isActive())
        mCamera!!.startPreview()
        flashLightIcon.setActive(!flashLightIcon.isActive())

        // FIXME just refresh the one item
        // mRecyclerView.getAdapter().notifyItemChanged(mRecyclerView.getAdapter(), );
        if (mRecyclerView!!.adapter != null) {
            mRecyclerView!!.adapter!!.notifyDataSetChanged()
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<EzLoaderResult?> {
        return EzLoader(context)
    }

    override fun onLoadFinished(loader: Loader<EzLoaderResult?>, data: EzLoaderResult?) {
        if (data != null) {
            mPackageInfo = data.pkgAppsList
            Log.d(TAG, "onLoadFinished() called, data = [" + mPackageInfo!!.size + "]")
        }
    }

    override fun onLoaderReset(loader: Loader<EzLoaderResult?>) {
        // Not required (?)
    }

    companion object {
        private const val TAG = "MainFragment"
        private const val LOADER_ID = 2000
    }
}