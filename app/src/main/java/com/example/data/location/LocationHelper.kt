package com.example.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume

object LocationHelper {

    fun hasLocationPermission(context: Context): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineGranted || coarseGranted
    }

    fun formatGoogleMapsUrl(latitude: Double, longitude: Double): String {
        val lat = String.format(Locale.US, "%.6f", latitude)
        val lng = String.format(Locale.US, "%.6f", longitude)
        return "https://maps.google.com/?q=$lat,$lng"
    }

    @SuppressLint("MissingPermission")
    suspend fun fetchCurrentGoogleMapsUrl(context: Context): Result<String> {
        if (!hasLocationPermission(context)) {
            return Result.failure(IllegalStateException("يرجى منح صلاحية الوصول إلى الموقع أولاً."))
        }

        val fusedLocation = try {
            val client = LocationServices.getFusedLocationProviderClient(context)
            val tokenSource = CancellationTokenSource()
            val fresh = withTimeoutOrNull(8_000L) {
                client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, tokenSource.token)
                    .awaitNullable()
            }
            if (fresh == null) {
                tokenSource.cancel()
            }
            fresh ?: withTimeoutOrNull(3_000L) { client.lastLocation.awaitNullable() }
        } catch (_: Exception) {
            null
        }

        val resolved = fusedLocation ?: readBestLastKnownLocation(context)
        return if (resolved != null) {
            Result.success(formatGoogleMapsUrl(resolved.latitude, resolved.longitude))
        } else {
            Result.failure(
                IllegalStateException("تعذّر التقاط إشارة GPS حالياً. تأكد من تفعيل الموقع في الهاتف أو اكتب العنوان يدوياً.")
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun readBestLastKnownLocation(context: Context): Location? {
        return try {
            val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                ?: return null
            val providers = listOf(
                LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER,
                LocationManager.PASSIVE_PROVIDER
            )
            providers.mapNotNull { provider ->
                runCatching { manager.getLastKnownLocation(provider) }.getOrNull()
            }.maxByOrNull { it.time }
        } catch (_: Exception) {
            null
        }
    }

    fun openGoogleMapsUrl(context: Context, url: String) {
        val trimmed = url.trim()
        if (trimmed.isEmpty()) return
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(trimmed))
        try {
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, "لا يوجد تطبيق خرائط أو متصفح لفتح الرابط", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun <T> Task<T>.awaitNullable(): T? =
        suspendCancellableCoroutine { continuation ->
            addOnSuccessListener { result ->
                if (continuation.isActive) continuation.resume(result)
            }
            addOnFailureListener {
                if (continuation.isActive) continuation.resume(null)
            }
            addOnCanceledListener {
                if (continuation.isActive) continuation.resume(null)
            }
        }
}
