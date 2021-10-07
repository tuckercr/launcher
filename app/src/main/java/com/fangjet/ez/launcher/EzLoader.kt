package com.fangjet.ez.launcher

import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.util.Log
import androidx.loader.content.AsyncTaskLoader
import java.util.*

/**
 * An AsyncTaskLoader for getting the package list.  Although it only takes 36ms.
 *
 *
 * Will probably add more things here in future
 *
 *
 * Created by ctucker on 6/14/17.
 */
internal class EzLoader(context: Context?) : AsyncTaskLoader<EzLoaderResult>(
    context!!
) {
    override fun loadInBackground(): EzLoaderResult {
        val start = System.currentTimeMillis()
        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        val pkgAppsList = context
            .packageManager
            .queryIntentActivities(mainIntent, 0) as ArrayList<ResolveInfo>
        Log.d(TAG, "loadInBackground() took " + (System.currentTimeMillis() - start) + "ms")
        return EzLoaderResult(pkgAppsList)
    }

    companion object {
        private const val TAG = "EzLoader"
    }
}