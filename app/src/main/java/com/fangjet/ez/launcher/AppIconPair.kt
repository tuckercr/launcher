package com.fangjet.ez.launcher

/**
 * Represents one or two App Icons (ie a row)
 *
 *
 * Created by ctucker on 5/29/17.
 */
internal class AppIconPair {
    val left: AppIcon
    var right: AppIcon? = null
        private set

    constructor(left: AppIcon, right: AppIcon?) {
        this.left = left
        this.right = right
    }

    constructor(left: AppIcon) {
        this.left = left
    }

    override fun toString(): String {
        return "AppIconPair{" +
                "left=" + left +
                ", right=" + right +
                '}'
    }
}