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

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import java.util.*

/**
 * Provide views to RecyclerView with data from mDataSet.
 */
class AppListRecyclerViewAdapter

/**
 * Initialize the dataset of the Adapter.
 *
 * @param dataSet  List of all the apps
 */
internal constructor(
    private val mMostUsed: List<AndroidApplication>?,
    dataSet: ArrayList<AndroidApplication>
) : Adapter<ViewHolder>() {

    private var mDataSet: List<AndroidApplication>

    init {
        dataSet.sort()
        mDataSet = dataSet
    }

    override fun getItemViewType(position: Int): Int {
        if (mMostUsed == null || mMostUsed.isEmpty()) {
            return TYPE_ITEM
        }

        return if (position == 0 || position == mMostUsed.size + 1)
            TYPE_HEADER
        else
            TYPE_ITEM
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {

        return if (viewType == TYPE_ITEM) {
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.app_list_item, viewGroup, false)
            ItemViewHolder(view)
        } else {
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.app_list_header, viewGroup, false)
            HeaderViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        if (holder is ItemViewHolder) {

            val androidApp = getItem(position)

            holder.labelTextView.text = androidApp.label
            holder.bigIconImageView.contentDescription = androidApp.label
            holder.bigIconImageView.setImageDrawable(androidApp.getDrawableIcon(holder.mView.context))

            holder.mView.setOnClickListener {
                val intent = Intent()
                val androidApplication = getItem(holder.bindingAdapterPosition)
                val componentName = androidApplication.componentName
                Log.d(TAG, "onClick: " + androidApplication.componentName)
                intent.component = componentName
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(it.context, intent, null)
            }
        }

        if (holder is HeaderViewHolder) {
            if (position == 0) {
                holder.headerTextView.setText(R.string.most_recent)
            } else {
                holder.headerTextView.setText(R.string.all_applications)
            }
        }
    }

    private fun getItem(position: Int): AndroidApplication {

        if (mMostUsed == null || mMostUsed.isEmpty()) {
            return mDataSet[position]
        }

        if (position == 0) {
            throw RuntimeException("Row 0 is a header")
        }

        return if (position <= mMostUsed.size) {
            mMostUsed[position - 1]
        } else {
            mDataSet[position - mMostUsed.size]
        }

    }

    override fun getItemCount(): Int {
        return (mMostUsed?.size ?: 0) + mDataSet.size
    }

    internal class ItemViewHolder(internal val mView: View) : ViewHolder(mView) {
        val labelTextView: TextView = mView.findViewById(R.id.labelTextView)
        val bigIconImageView: ImageView = mView.findViewById(R.id.bigIconImage)

    }

    internal class HeaderViewHolder(v: View) : ViewHolder(v) {
        val headerTextView: TextView = v.findViewById(R.id.headerTextView)

    }

    companion object {

        private const val TAG = "AppListRecyclerViewAdap"

        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }


//    override fun getSectionName(position: Int): String {
//        return getItem(position).label.substring(0, 1).toUpperCase(Locale.ENGLISH)
//    }

}
