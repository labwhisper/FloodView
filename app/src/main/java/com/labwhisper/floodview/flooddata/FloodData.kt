package com.labwhisper.floodview.flooddata

sealed interface FloodData {
    data object Loading: FloodData
    data object Empty: FloodData
    data class Data(val base64Image: String): FloodData
}