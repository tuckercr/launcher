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

import android.content.Context
import android.util.Log
import android.util.SparseIntArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.text.MessageFormat

/**
 * Provide views to RecyclerView with data from mDataSet.
 */
class MainFragmentRecyclerViewAdapter internal constructor(
    private val mContext: Context,
    private val mDataSet: List<AppIconPair>
) : RecyclerView.Adapter<MainFragmentRecyclerViewAdapter.ViewHolder>() {
    private val mColorMap = SparseIntArray()
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.app_icon_grid_item_pair, viewGroup, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val appIconPair = mDataSet[position]
        Log.d(
            TAG, MessageFormat.format(
                "onBindViewHolder() called with: position = [{0}], appIconPair = [{1}]",
                position, appIconPair
            )
        )
        val left = appIconPair.left
        val right = appIconPair.right

        // Left is never null
        viewHolder.leftLabelText.setText(left.labelRes)
        viewHolder.leftLabelText.setTextColor(getColor(left.getFgColorRes()))
        viewHolder.leftImage.contentDescription = mContext.getString(left.labelRes)
        viewHolder.leftImage.setImageResource(left.getIconRes())
        viewHolder.leftItemLayout.setBackgroundColor(getColor(left.getBgColorRes()))
        viewHolder.leftItemLayout.setOnClickListener(left.clickListener)
        viewHolder.leftItemLayout.setOnLongClickListener(left.longClickListener)

        // Right can be though
        if (right == null) {
            // hide views
            viewHolder.rightItemLayout.visibility = View.GONE
            viewHolder.rightLabelText.visibility = View.GONE
            viewHolder.rightImage.visibility = View.GONE
            viewHolder.rightItemLayout.setOnClickListener(null)
        } else {
            viewHolder.rightLabelText.setText(right.labelRes)
            viewHolder.rightLabelText.setTextColor(getColor(right.getFgColorRes()))
            viewHolder.rightImage.contentDescription = mContext.getString(right.labelRes)
            viewHolder.rightImage.setImageResource(right.getIconRes())
            viewHolder.rightItemLayout.setOnClickListener(right.clickListener)

            // Show the right views
            viewHolder.rightItemLayout.visibility = View.VISIBLE
            viewHolder.rightLabelText.visibility = View.VISIBLE
            viewHolder.rightImage.visibility = View.VISIBLE
            viewHolder.rightItemLayout.setBackgroundColor(getColor(right.getBgColorRes()))
        }
    }

    @ColorInt
    private fun getColor(@ColorRes colorRes: Int): Int {
        if (mColorMap.indexOfKey(colorRes) >= 0) {
            return mColorMap[colorRes]
        }
        val color = ContextCompat.getColor(mContext, colorRes)
        mColorMap.put(colorRes, color)
        return color
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val leftLabelText: TextView
        val leftImage: ImageView
        val rightLabelText: TextView
        val rightImage: ImageView
        val leftItemLayout: View
        val rightItemLayout: View

        init {
            v.setOnClickListener {
                Log.d(
                    TAG,
                    "Element $bindingAdapterPosition clicked."
                )
            }
            leftItemLayout = v.findViewById(R.id.leftItemLayout)
            rightItemLayout = v.findViewById(R.id.rightItemLayout)
            leftLabelText = v.findViewById(R.id.labelLeftTextView)
            leftImage = v.findViewById(R.id.bigIconLeftImage)
            rightLabelText = v.findViewById(R.id.labelRightLeftTextView)
            rightImage = v.findViewById(R.id.bigIconRightImage)
        }
    }

    companion object {
        private const val TAG = "MainFragmentRecyclerVie"
    }


}