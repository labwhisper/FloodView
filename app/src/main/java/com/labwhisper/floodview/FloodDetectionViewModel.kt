package com.labwhisper.floodview

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.labwhisper.floodview.flooddata.FloodData
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class FloodDetectionViewModel : ViewModel() {
    private val _floodData = MutableStateFlow<FloodData>(FloodData.Loading)
    val floodData: StateFlow<FloodData> = _floodData

    // near Wroclaw
    val initialPosition = LatLng(51.1107, 17.0326)
    private var currentCoordinates: List<List<Double>> = emptyList()
    private var startDate = "2024-09-01T00:00:00Z"
    private var endDate = "2024-09-20T23:59:59Z"

    private var activeJob: Job? = null

    fun updateBounds(coordinates: List<List<Double>>) {
        currentCoordinates = coordinates
        getFloodDetection()
    }

    fun updateDateRange(start: String, end: String) {
        startDate = start
        endDate = end
        getFloodDetection()
    }

    private fun getFloodDetection() {
        activeJob?.cancel()
        activeJob = viewModelScope.launch {
            Log.d("FloodDetectionViewModel", "Will fetch data  coordinates: $currentCoordinates")
            if (currentCoordinates.isNotEmpty()) {
                _floodData.emit(FloodData.Loading)
                val floodDetectionRequest = FloodDetectionRequest(
                    coordinates = currentCoordinates,
                    timeRange = TimeRange(
                        from = startDate,
                        to = endDate
                    )
                )
                delay(200)
                try {
                    val response = floodDetectionService.getFloodDetection(floodDetectionRequest)
                    if (response.isSuccessful) {
                        response.body()?.let { result ->
                            Log.d("FloodDetectionViewModel", "Result: ${result.body}")
                            val floodBody = parseFloodResponseBody(result.body)
                            _floodData.emit(FloodData.Data(floodBody.image_data))
                        }
                    } else {
                        Log.e(
                            "FloodDetectionViewModel",
                            "Failed to get flood data: ${response.errorBody()?.string()}"
                        )
                        _floodData.emit(FloodData.Empty)
                    }
                } catch (e: CancellationException) {
                    Log.d("FloodDetectionViewModel", "Network call was cancelled.")
                    _floodData.emit(FloodData.Empty)
                    throw e
                } catch (e: Exception) {
                    Log.e("FloodDetectionViewModel", "Error Downloading image : ${e.message}")
                    _floodData.emit(FloodData.Empty)
                }
            } else {
                Log.w(
                    "FloodDetectionViewModel",
                    "Coordinates are empty. Cannot request flood data."
                )
            }
        }
    }


    fun parseFloodResponseBody(bodyString: String): FloodResponseBody {
        return try {
            Gson().fromJson(bodyString, FloodResponseBody::class.java)
        } catch (e: JsonSyntaxException) {
            Log.e("FloodDetectionViewModel", "Failed to parse FloodResponseBody: ${e.message}")
            FloodResponseBody(message = "Error parsing response", image_data = "")
        }
    }

}
