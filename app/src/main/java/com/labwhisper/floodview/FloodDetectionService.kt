package com.labwhisper.floodview

import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

data class FloodDetectionRequest(
    val coordinates: List<List<Double>>,
    val timeRange: TimeRange
)

data class TimeRange(
    val from: String,
    val to: String
)

data class FloodDetectionResponse(
    val statusCode: Int,
    val body: String
)

data class FloodResponseBody(
    val message: String,
    val image_data: String
)

interface FloodDetectionService {
    @POST("flood")
    suspend fun getFloodDetection(@Body request: FloodDetectionRequest): Response<FloodDetectionResponse>
}

val floodDetectionService: FloodDetectionService by lazy {

    val apiKey = BuildConfig.FLOOD_API_KEY

    val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request: Request = chain.request()
                .newBuilder()
                .addHeader("x-api-key", apiKey)
                .build()
            chain.proceed(request)
        }
        .build()

    Retrofit.Builder()
        .baseUrl("https://y45ki4ato7.execute-api.eu-north-1.amazonaws.com/Staging/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(FloodDetectionService::class.java)
}
