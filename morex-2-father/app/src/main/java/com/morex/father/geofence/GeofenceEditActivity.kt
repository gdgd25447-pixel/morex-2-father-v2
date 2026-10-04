package com.morex.father.geofence

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.morex.father.R
import com.morex.father.databinding.ActivityGeofenceEditBinding
import com.morex.father.network.ApiClient
import com.morex.father.network.models.Geofence
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * إضافة/تعديل/حذف سياج جغرافي.
 * extras: child_id (إلزامي للإضافة)، geofence_json (عند التعديل، يأتي من GeofenceListActivity).
 * حدود السيرفر: radius_m بين 50 و10000، type = safe | danger.
 */
class GeofenceEditActivity : AppCompatActivity() {

    private lateinit var b: ActivityGeofenceEditBinding
    private var childId: String = ""
    private var existing: Geofence? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityGeofenceEditBinding.inflate(layoutInflater)
        setContentView(b.root)

        childId = intent.getStringExtra("child_id") ?: ""
        existing = intent.getStringExtra("geofence_json")?.let {
            try { Gson().fromJson(it, Geofence::class.java) } catch (_: Exception) { null }
        }

        b.ivBack.setOnClickListener { finish() }
        b.btnSave.setOnClickListener { save() }
        b.btnUseChildLocation.setOnClickListener { fillFromChildLocation() }
        b.btnDelete.setOnClickListener { confirmDelete() }

        existing?.let { g ->
            b.etName.setText(g.name)
            b.etLat.setText(g.latitude.toString())
            b.etLng.setText(g.longitude.toString())
            b.etRadius.setText(g.radius.toString())
            b.rgType.check(if (g.type == "danger") R.id.rbDanger else R.id.rbSafe)
            b.switchEnter.isChecked = g.notifyOnEnter
            b.switchExit.isChecked = g.notifyOnExit
            b.btnDelete.visibility = View.VISIBLE
            if (childId.isEmpty()) childId = g.childId.orEmpty()
        }
    }

    private fun busy(on: Boolean) {
        b.progressBar.visibility = if (on) View.VISIBLE else View.GONE
        b.btnSave.isEnabled = !on
        b.btnDelete.isEnabled = !on
    }

    private fun fillFromChildLocation() {
        busy(true)
        lifecycleScope.launch {
            try {
                val resp = withContext(Dispatchers.IO) { ApiClient.get().getLiveLocations() }
                busy(false)
                val dev = resp.body()?.devices?.firstOrNull { it.childId == childId }
                val lat = dev?.latitude
                val lng = dev?.longitude
                if (lat == null || lng == null) {
                    Toast.makeText(this@GeofenceEditActivity, R.string.map_location_unavailable, Toast.LENGTH_SHORT).show()
                } else {
                    b.etLat.setText(lat.toString())
                    b.etLng.setText(lng.toString())
                }
            } catch (_: Exception) {
                busy(false)
                Toast.makeText(this@GeofenceEditActivity, R.string.common_network_error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun save() {
        val name = b.etName.text?.toString()?.trim().orEmpty()
        val lat = b.etLat.text?.toString()?.toDoubleOrNull()
        val lng = b.etLng.text?.toString()?.toDoubleOrNull()
        val radius = b.etRadius.text?.toString()?.toIntOrNull()

        if (name.isEmpty()) { b.etName.error = getString(R.string.geofence_err_name); return }
        if (lat == null || lat !in -90.0..90.0 || lng == null || lng !in -180.0..180.0) {
            Toast.makeText(this, R.string.geofence_err_coords, Toast.LENGTH_SHORT).show(); return
        }
        if (radius == null || radius !in 50..10000) {
            b.etRadius.error = getString(R.string.geofence_err_radius); return
        }
        if (childId.isEmpty()) return

        val geofence = Geofence(
            childId = childId,
            name = name,
            type = if (b.rgType.checkedRadioButtonId == R.id.rbDanger) "danger" else "safe",
            latitude = lat,
            longitude = lng,
            radius = radius,
            notifyOnEnter = b.switchEnter.isChecked,
            notifyOnExit = b.switchExit.isChecked
        )

        busy(true)
        lifecycleScope.launch {
            try {
                val id = existing?.id
                val resp = withContext(Dispatchers.IO) {
                    if (id.isNullOrEmpty()) ApiClient.get().addGeofence(geofence)
                    else ApiClient.get().updateGeofence(id, geofence)
                }
                busy(false)
                if (resp.isSuccessful) {
                    Toast.makeText(this@GeofenceEditActivity, R.string.common_saved, Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@GeofenceEditActivity, R.string.common_loading_failed, Toast.LENGTH_SHORT).show()
                }
            } catch (_: Exception) {
                busy(false)
                Toast.makeText(this@GeofenceEditActivity, R.string.common_network_error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun confirmDelete() {
        val id = existing?.id ?: return
        AlertDialog.Builder(this)
            .setMessage(R.string.geofence_delete_confirm)
            .setPositiveButton(R.string.common_delete) { _, _ -> delete(id) }
            .setNegativeButton(R.string.common_cancel, null)
            .show()
    }

    private fun delete(id: String) {
        busy(true)
        lifecycleScope.launch {
            try {
                val resp = withContext(Dispatchers.IO) { ApiClient.get().deleteGeofence(id) }
                busy(false)
                if (resp.isSuccessful) finish()
                else Toast.makeText(this@GeofenceEditActivity, R.string.common_loading_failed, Toast.LENGTH_SHORT).show()
            } catch (_: Exception) {
                busy(false)
                Toast.makeText(this@GeofenceEditActivity, R.string.common_network_error, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
