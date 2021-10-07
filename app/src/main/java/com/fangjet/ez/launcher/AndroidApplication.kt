package com.fangjet.ez.launcher

import android.content.ComponentName
import android.content.Context
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable

/**
 * Represents an Android App - contains the icon, name and intent
 *
 *
 * Created by ctucker on 5/27/17.
 */
internal class AndroidApplication(context: Context, private val mInfo: ResolveInfo) :
    Comparable<AndroidApplication> {
    val label: String = mInfo.loadLabel(context.packageManager).toString()
    private var drawableIcon: Drawable? = null

    /**
     * The component name is loaded on demand
     *
     * @return Component Name
     */
    var componentName: ComponentName? = null
        get() {
            if (field == null) {
                field = ComponentName(
                    mInfo.activityInfo.applicationInfo.packageName,
                    mInfo.activityInfo.name
                )
            }
            return field
        }
        private set

    /**
     * The drawable is loaded on-demand
     */
    fun getDrawableIcon(context: Context): Drawable? {
        if (drawableIcon == null) {
            drawableIcon = mInfo.loadIcon(context.packageManager)
            //            try {
//                drawableIcon = context.getPackageManager().getApplicationIcon(mInfo.activityInfo.applicationInfo.packageName);
//            } catch (PackageManager.NameNotFoundException e) {
//                e.printStackTrace();
//            }
        }
        return drawableIcon
    }

    /**
     * Compare by labels
     */
    override fun compareTo(other: AndroidApplication): Int {
        return label.compareTo(other.label)
    }

}