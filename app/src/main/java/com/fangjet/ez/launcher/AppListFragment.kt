package com.fangjet.ez.launcher

import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

/**
 * Contains the Date, Time, Battery Levels and the AppIcon Grid
 */
class AppListFragment : Fragment() {

    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: AppListRecyclerViewAdapter? = null
    private var mLayoutManager: LinearLayoutManager? = null

    private var mPackages: ArrayList<AndroidApplication>? = null
    private var mMostUsed: MutableList<AndroidApplication>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val packages: List<ResolveInfo>? = if (arguments != null) {
            Log.e(TAG, "Got packages from args")
            requireArguments().getParcelableArrayList(ARG_RESOLVE_INFO_LIST)
        } else {
            Log.v(TAG, "Getting application list now")
            getApplicationList()
        }

        when {
            packages == null -> {
                Log.e(TAG, "onCreate: null packages")
            }
            packages.isEmpty() -> {
                Log.e(TAG, "onCreate: no packages")
            }
            else -> {
                Log.v(TAG, "onCreate: got " + packages.size + " packages")
            }
        }

        initDataset(packages!!)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_app_list, container, false)
        rootView.tag = TAG

        mRecyclerView = rootView.findViewById(R.id.recyclerView)
        mLayoutManager = GridLayoutManager(activity, 2)

        // mAdapter = AppListRecyclerViewAdapter(container!!.context, mMostUsed, mPackages!!)
        mAdapter = AppListRecyclerViewAdapter(mMostUsed, mPackages!!)
        mRecyclerView!!.adapter = mAdapter

        var scrollPosition = 0

        // If a layout manager has already been set, get current scroll position.
        if (mRecyclerView!!.layoutManager != null) {
            scrollPosition = (mRecyclerView!!.layoutManager as LinearLayoutManager)
                .findFirstCompletelyVisibleItemPosition()
        }

        mLayoutManager = LinearLayoutManager(activity)
        mRecyclerView!!.layoutManager = mLayoutManager
        mRecyclerView!!.scrollToPosition(scrollPosition)
        return rootView
    }


    /**
     * Initialize the package list and recent apps lists
     *
     * @param packages The ResolveInfo packages
     */
    private fun initDataset(packages: List<ResolveInfo>) {

        mPackages = ArrayList()
        for (info in packages) {
            mPackages!!.add(AndroidApplication(requireContext(), info))
        }
        mPackages!!.sort()

        // FIXME HACK HACK
        mMostUsed = ArrayList()
        // mMostUsed!!.add(mPackages!![10])
        // mMostUsed!!.add(mPackages!![2])
    }

    private fun getApplicationList(): List<ResolveInfo> {

        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        return requireContext().packageManager.queryIntentActivities(mainIntent, 0)
    }

    companion object {

        private const val TAG = "AppListFragment"
        private const val ARG_RESOLVE_INFO_LIST = ""

        // FIXME use safeargs
        fun newInstance(info: ArrayList<ResolveInfo>?): AppListFragment {

            val args = Bundle()
            args.putParcelableArrayList(ARG_RESOLVE_INFO_LIST, info)
            val fragment = AppListFragment()
            fragment.arguments = args

            return fragment
        }
    }
}
