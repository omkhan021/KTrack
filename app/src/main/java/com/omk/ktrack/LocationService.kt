package com.omk.ktrack

import android.app.*
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*

class LocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    companion object {
        private const val TAG = "KTrackLocation"
        private const val CHANNEL_ID = "ktrack_location_channel"
        private const val NOTIF_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate()")
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        startForegroundServiceWithNotification()
        startLocationUpdates()
    }

    private fun startForegroundServiceWithNotification() {
        Log.d(TAG, "Creating notification channel & starting foreground")

        val channelName = "KTrack Location Service"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notificationIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("KTrack: Tracking Location")
            .setContentText("Location service is running")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent) // UI launches ONLY when user taps
            .setOngoing(true)
            .build()

        startForeground(NOTIF_ID, notification)
    }

    private fun startLocationUpdates() {
        Log.d(TAG, "Starting location updates")

        val locationRequest = LocationRequest.create().apply {
            interval = 5000
            fastestInterval = 3000
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location: Location? = result.lastLocation
                if (location != null) {
                    Log.d(TAG, "Lat: ${location.latitude}, Lon: ${location.longitude}")
                    // TODO: send to server via Retrofit
                } else {
                    Log.d(TAG, "Location is null")
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            Log.e(TAG, "Missing location permissions", e)
        }
    }

    private fun stopLocationUpdates() {
        Log.d(TAG, "Stopping location updates")
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "LocationService started via onStartCommand()")
        return START_STICKY
    }

    override fun onDestroy() {
        Log.d(TAG, "Service onDestroy()")
        stopLocationUpdates()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
