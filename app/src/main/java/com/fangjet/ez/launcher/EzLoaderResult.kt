package com.fangjet.ez.launcher

import android.content.pm.ResolveInfo
import java.util.*

/**
 * Package apps loader
 *
 *
 * TODO: how can we cache/sync this list? what's best practice for a launcher.
 *
 *
 * Created by ctucker on 6/14/17.
 */
class EzLoaderResult(val pkgAppsList: ArrayList<ResolveInfo>)