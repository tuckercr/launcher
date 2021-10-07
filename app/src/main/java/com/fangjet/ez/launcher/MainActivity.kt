package com.fangjet.ez.launcher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.navigation.NavigationView
import com.fangjet.ez.launcher.battery.BatteryInfo
import com.fangjet.ez.launcher.battery.BatteryInfoService
import com.fangjet.ez.magnifier.MagnifierActivity
import java.text.DateFormat
import java.util.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private val messageHandler: MessageHandler = MessageHandler(this)
    private val messenger = Messenger(messageHandler)
    private var mTimeTextView: TextView? = null
    private var mDateTextView: TextView? = null
    private lateinit var mBatteryTextView: TextView
    private var mTimeTickReceiver: BroadcastReceiver? = null
    private var mDateChangeReceiver: BroadcastReceiver? = null
    private lateinit var mBatteryImage: ImageView
    private var mServiceConnected = false
    private var serviceMessenger: Messenger? = null
    private var mHeaderView: View? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mHeaderView = findViewById(R.id.headerView)
        mTimeTextView = findViewById(R.id.timeTextView)
        mDateTextView = findViewById(R.id.dateTextView)
        mBatteryTextView = findViewById(R.id.batteryTextView)
        mBatteryImage = findViewById(R.id.batteryImageView)
        val batteryClickListener = View.OnClickListener { v: View? ->
            val intentBatteryUsage = Intent(Intent.ACTION_POWER_USAGE_SUMMARY)
            startActivity(intentBatteryUsage)
        }
        mBatteryImage.setOnClickListener(batteryClickListener)
        mBatteryTextView.setOnClickListener(batteryClickListener)
        val mServiceConnection = BatteryInfoService.RemoteConnection(messenger)
        val biServiceIntent = Intent(this, BatteryInfoService::class.java)
        startService(biServiceIntent)
        bindService(biServiceIntent, mServiceConnection, 0)
        mServiceConnected = true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_CAMERA_FOR_MAGNIFIER -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    Log.d(
                        TAG,
                        "onRequestPermissionsResult() called with: requestCode = [$requestCode]"
                    )
                    val intent = Intent(this, MagnifierActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }
            }
            PERMISSIONS_REQUEST_CAMERA_FOR_CAMERA -> {
                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    Log.d(
                        TAG,
                        "onRequestPermissionsResult() called with: requestCode = [$requestCode]"
                    )

                    // This captures an image then returns to the app.
                    val intent = Intent("android.media.action.IMAGE_CAPTURE")
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)

                    // TODO do something with it

                    // This is not working
//                    Intent cameraIntent = new Intent(Intent.ACTION_CAMERA_BUTTON, null);
//                    startActivity(cameraIntent);
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        populateClockUi()
        mTimeTickReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                populateClockUi()
            }
        }
        registerReceiver(mTimeTickReceiver, IntentFilter(Intent.ACTION_TIME_TICK))
        mDateChangeReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Log.d(TAG, "onReceive() called - Date Changed")
                // TODO refactor to listen for day changes (midnight)
            }
        }
        registerReceiver(mDateChangeReceiver, IntentFilter(Intent.ACTION_DATE_CHANGED))

        // hideSystemUI()
    }

//    override fun onWindowFocusChanged(hasFocus: Boolean) {
//        super.onWindowFocusChanged(hasFocus)
//
//        if (hasFocus) {
//            window.decorView.apply {
//                // Hide the navigation bar
//                systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
//                        View.SYSTEM_UI_FLAG_IMMERSIVE
//            }
//        }
//    }

//    private fun hideSystemUI() {
//        WindowCompat.setDecorFitsSystemWindows(window, false)
//        WindowInsetsControllerCompat(window, mainContainer).let { controller ->
//            controller.hide(WindowInsetsCompat.Type.navigationBars())
//            // controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
//        }
//    }
//
//    private fun showSystemUI() {
//        WindowCompat.setDecorFitsSystemWindows(window, true)
//        WindowInsetsControllerCompat(window, mainContainer).show(WindowInsetsCompat.Type.navigationBars())
//    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(mTimeTickReceiver)
        unregisterReceiver(mDateChangeReceiver)
    }

    private fun populateClockUi() {
        // FIXME review formats
        mDateTextView!!.text =
            DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date())
        mTimeTextView!!.text =
            DateFormat.getTimeInstance(DateFormat.SHORT).format(Date())
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        // This is not presently used/visible
        item.isChecked = !item.isChecked
        val itemId = item.itemId
        when (itemId) {
            R.id.drawer_home -> {
                Toast.makeText(this, R.string.home, Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.drawer_favourite -> {
                Toast.makeText(this, R.string.favorite, Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.drawer_share -> {
                // Toast.makeText(this, R.string.favorite, Toast.LENGTH_SHORT).show();
                // changeFragment(new ShareFragment());
                return true
            }
            R.id.drawer_settings -> {
                Toast.makeText(this, R.string.settings, Toast.LENGTH_SHORT).show()
                return true
            }
            else -> {
                Log.e(TAG, "Unknown Nav Drawer Id")
            }
        }
        return false
    }

    private fun sendServiceMessage(what: Int) {
        if (serviceMessenger == null) return
        val outgoing = Message.obtain()
        outgoing.what = what
        outgoing.replyTo = messenger
        try {
            serviceMessenger!!.send(outgoing)
        } catch (e: RemoteException) {
            Log.e(TAG, "Caught: " + e.message, e)
        }
    }

    private fun batteryInfoUpdated(bundle: Bundle) {
        val percent = bundle.getInt(BatteryInfo.Field.FIELD_PERCENT)
        val status = bundle.getInt(BatteryInfo.Field.FIELD_STATUS)
        val plugged = bundle.getInt(BatteryInfo.Field.FIELD_PLUGGED)
        val percentText = getString(R.string.battery_percent, percent.toString())
        Log.e(TAG, "batteryInfoUpdated() called: $percentText, ($status, $plugged)")
        mBatteryTextView.text = percentText
        when (plugged) {
            BatteryInfo.Plugged.PLUGGED_UNPLUGGED -> when {
                percent < 10 -> {
                    mBatteryImage.setImageResource(R.drawable.ic_battery_alert)
                }
                percent < 20 -> {
                    mBatteryImage.setImageResource(R.drawable.ic_battery_20)
                }
                percent < 40 -> {
                    mBatteryImage.setImageResource(R.drawable.ic_battery_30)
                }
                percent < 60 -> {
                    mBatteryImage.setImageResource(R.drawable.ic_battery_50)
                }
                percent < 70 -> {
                    mBatteryImage.setImageResource(R.drawable.ic_battery_60)
                }
                percent < 80 -> {
                    mBatteryImage.setImageResource(R.drawable.ic_battery_80)
                }
                percent < 99 -> {
                    mBatteryImage.setImageResource(R.drawable.ic_battery_90)
                }
                else -> {
                    mBatteryImage.setImageResource(R.drawable.ic_battery_full)
                }
            }
            else -> when {
                percent < 20 -> {
                    mBatteryImage.setImageResource(R.drawable.ic_battery_charging_20)
                }
                percent < 40 -> {
                    mBatteryImage.setImageResource(R.drawable.ic_battery_charging_30)
                }
                percent < 60 -> {
                    mBatteryImage.setImageResource(R.drawable.ic_battery_charging_50)
                }
                percent < 70 -> {
                    mBatteryImage.setImageResource(R.drawable.ic_battery_charging_60)
                }
                percent < 80 -> {
                    mBatteryImage.setImageResource(R.drawable.ic_battery_charging_80)
                }
                percent < 99 -> {
                    mBatteryImage.setImageResource(R.drawable.ic_battery_charging_90)
                }
                else -> {
                    mBatteryImage.setImageResource(R.drawable.ic_battery_charging_full)
                }
            }
        }

        // FIXME what about other charging states?
    }

    companion object {
        const val PERMISSIONS_REQUEST_CAMERA_FOR_MAGNIFIER = 100
        const val PERMISSIONS_REQUEST_CAMERA_FOR_CAMERA = 101
        const val PERMISSIONS_REQUEST_CAMERA_FOR_FLASHLIGHT = 102
        private const val TAG = "MainActivity"

        private class MessageHandler(private val mainActivity: MainActivity) : Handler() {
            override fun handleMessage(incoming: Message) {
                if (!mainActivity.mServiceConnected) {
                    Log.e(TAG, "mServiceConnected is false; ignoring message: $incoming")
                    return
                }
                when (incoming.what) {
                    BatteryInfoService.RemoteConnection.CLIENT_SERVICE_CONNECTED -> {
                        mainActivity.serviceMessenger = incoming.replyTo
                        mainActivity.sendServiceMessage(BatteryInfoService.RemoteConnection.SERVICE_REGISTER_CLIENT)
                    }
                    BatteryInfoService.RemoteConnection.CLIENT_BATTERY_INFO_UPDATED -> mainActivity.batteryInfoUpdated(
                        incoming.data
                    )
                    else -> super.handleMessage(incoming)
                }
            }
        }
    }
}