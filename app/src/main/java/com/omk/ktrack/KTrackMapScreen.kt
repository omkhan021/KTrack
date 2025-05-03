package com.omk.ktrack.ui

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.tasks.await

@Composable
fun KTrackMapScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val fusedLocationProviderClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    var location by remember { mutableStateOf<LatLng?>(null) }
    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(Unit) {
        try {
            val lastLocation = fusedLocationProviderClient.lastLocation.await()
            lastLocation?.let {
                val latLng = LatLng(it.latitude, it.longitude)
                location = latLng
                cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 16f)
            }
        } catch (e: Exception) {
            Log.e("KTrackMap", "Failed to get location", e)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        ) {
            location?.let {
                Marker(state = MarkerState(position = it), title = "You are here")
            }
        }

        FloatingActionButton(
            onClick = {
                location?.let {
                    cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(it, 16f))
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 88.dp, end = 16.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            Icon(Icons.Default.MyLocation, contentDescription = "My Location")
        }
    }
}
