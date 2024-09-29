package com.labwhisper.floodview

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.GroundOverlay
import com.google.maps.android.compose.GroundOverlayPosition
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.labwhisper.floodview.flooddata.FloodData

@Composable
fun FloodMapScreen(
    viewModel: FloodDetectionViewModel,
    floodData: FloodData,
    modifier: Modifier = Modifier,
) {
    var mapLoaded by remember { mutableStateOf(false) }
    var initialDataLoaded by remember { mutableStateOf(false) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(viewModel.initialPosition, 12f)
    }

    var lastBounds by remember { mutableStateOf<LatLngBounds?>(null) }

    Column {
        Box(modifier = Modifier.weight(1f)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(mapType = MapType.SATELLITE),
                uiSettings = MapUiSettings(compassEnabled = true, zoomControlsEnabled = true),
                onMapLoaded = {
                    mapLoaded = true
                    Log.d("FloodMapScreen", "Map loaded, triggering initial data load.")
                }
            ) {
                (floodData as? FloodData.Data)?.let {
                    val bitmap = decodeBase64ToBitmap(it.base64Image)
                    bitmap?.let {
                        val southwest = lastBounds?.southwest ?: LatLng(51.0603, 16.9206)
                        val northeast = lastBounds?.northeast ?: LatLng(51.1603, 17.1206)
                        val overlayBounds = LatLngBounds(southwest, northeast)

                        GroundOverlay(
                            position = GroundOverlayPosition.create(overlayBounds),
                            image = BitmapDescriptorFactory.fromBitmap(it)
                        )
                    }
                }
            }

            if (floodData is FloodData.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (floodData is FloodData.Empty) {
                Text(
                    text = "No flood data available.",
                    modifier = Modifier.align(Alignment.Center),
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    LaunchedEffect(mapLoaded) {
        if (mapLoaded && !initialDataLoaded) {
            cameraPositionState.projection?.visibleRegion?.latLngBounds?.let { bounds ->
                val initialBounds = convertBoundsToCoordinates(bounds)
                lastBounds = bounds
                viewModel.updateBounds(initialBounds)
                initialDataLoaded = true
                Log.d(
                    "FloodMapScreen",
                    "Initial flood data load completed with bounds: $initialBounds"
                )
            }
        }
    }

    LaunchedEffect(cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving) {
            cameraPositionState.projection?.visibleRegion?.latLngBounds?.let { bounds ->
                if (bounds != lastBounds) {
                    lastBounds = bounds
                    val updatedBounds = convertBoundsToCoordinates(bounds)
                    viewModel.updateBounds(updatedBounds)
                    Log.d("FloodMapScreen", "Map bounds updated: $updatedBounds")
                }
            }
        }
    }
}


fun decodeBase64ToBitmap(base64Str: String): Bitmap? {
    val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
    return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
}

fun convertBoundsToCoordinates(bounds: LatLngBounds): List<List<Double>> {
    return listOf(
        listOf(bounds.southwest.longitude, bounds.southwest.latitude),
        listOf(bounds.northeast.longitude, bounds.southwest.latitude),
        listOf(bounds.northeast.longitude, bounds.northeast.latitude),
        listOf(bounds.southwest.longitude, bounds.northeast.latitude),
        listOf(bounds.southwest.longitude, bounds.southwest.latitude)
    )
}