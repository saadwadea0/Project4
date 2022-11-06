package com.udacity.project4.locationreminders

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityReminderDescriptionBinding
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

/**
 * Activity that displays the reminder details after the user clicks on the notification
 */
class ReminderDescriptionActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_ReminderDataItem = "EXTRA_ReminderDataItem"

        // receive the reminder object after the user clicks on the notification
        fun newIntent(context: Context, reminderDataItem: ReminderDataItem): Intent {
            val intent = Intent(context, ReminderDescriptionActivity::class.java)
            intent.putExtra(EXTRA_ReminderDataItem, reminderDataItem)
            return intent
        }
    }

    private lateinit var binding: ActivityReminderDescriptionBinding
    private lateinit var geofencingClient: GeofencingClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_reminder_description
        )
        geofencingClient = LocationServices.getGeofencingClient(this)
        val reminderItem = intent.getSerializableExtra(EXTRA_ReminderDataItem) as ReminderDataItem

        removeGeofence(reminderItem.id)
        binding.reminderDataItem = reminderItem
    }

    private fun removeGeofence(geofenceId: String) {
        geofencingClient.removeGeofences(listOf(geofenceId))?.run {
            addOnSuccessListener { //in case of success removing
                Log.d("GeofenceUtil", getString(R.string.geofences_removed))
                Toast.makeText(
                    applicationContext,
                    getString(R.string.geofences_removed),
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
            addOnFailureListener { ////in case of failure
                Toast.makeText(
                    applicationContext,
                    getString(R.string.geofences_not_removed),
                    Toast.LENGTH_SHORT
                )
                    .show()
                Log.d("GeofenceUtil", getString(R.string.geofences_not_removed))
            }
        }

    }
}
