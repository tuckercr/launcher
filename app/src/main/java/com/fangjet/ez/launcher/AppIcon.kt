package com.fangjet.ez.launcher

import android.util.Log
import android.view.View
import android.view.View.OnLongClickListener
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

/**
 * Represents a big icon + text on the home screen
 *
 *
 * Created by ctucker on 5/8/17.
 *
 * @param iconRes           Icon Resource Id
 * @param labelRes          Label Resource Id
 * @param bgColorRes        Background Color
 * @param fgColorRes        Foreground Color
 * @param clickListener     The listener
 * @param longClickListener The long click listener
 */
internal class AppIcon @JvmOverloads constructor(
    @field:DrawableRes @param:DrawableRes private val iconRes: Int,
    @field:StringRes @get:StringRes
    @param:StringRes val labelRes: Int,
    @field:ColorRes @param:ColorRes private val bgColorRes: Int,
    @field:ColorRes @param:ColorRes private val fgColorRes: Int,
    val clickListener: View.OnClickListener,
    val longClickListener: OnLongClickListener,
    @field:ColorRes private val fgColorResActive: Int =
        0,
    @field:ColorRes private val bgColorResActive: Int = 0,
    iconResActive: Int = 0
) {

    @DrawableRes
    private var iconResActive = 0
    private var isActive = false

    @DrawableRes
    fun getIconRes(): Int {
        return if (isActive) {
            iconResActive
        } else {
            iconRes
        }
    }

    @ColorRes
    fun getBgColorRes(): Int {
        return if (isActive) {
            bgColorResActive
        } else {
            bgColorRes
        }
    }

    @ColorRes
    fun getFgColorRes(): Int {
        return if (isActive) {
            fgColorResActive
        } else {
            fgColorRes
        }
    }

    fun isActive(): Boolean {
        Log.d(TAG, "isActive() returned: $isActive")
        return isActive
    }

    fun setActive(active: Boolean) {
        isActive = active
    }

    override fun toString(): String {
        return "AppIcon{" +
                "iconRes=" + iconRes +
                ", labelRes=" + labelRes +
                '}'
    }

    companion object {
        private const val TAG = "AppIcon"
    }

    init {
        if (iconResActive == 0) {
            this.iconResActive = iconRes
        } else {
            this.iconResActive = iconResActive
        }
    }
}