package dev.mus.sound.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.core.content.ContextCompat


const val TRACK = "track"

fun milliSecondsToTime(ms: Int?): String {
    if (ms == null) return ""
    val secs = ms / 1000
    val h = secs / 3600
    val m = secs % 3600 / 60
    val s = secs % 60
    return if (h > 0) "$h:" else "" + (if (m < 10) "0$m:" else "$m:") + if (s < 10) "0$s" else s
}

fun isNetworkConnected(ctx: Context): Boolean {
    val cm = ContextCompat.getSystemService(ctx, ConnectivityManager::class.java)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val activeNetwork = cm?.activeNetwork ?: return false
        val networkCapabilities = cm.getNetworkCapabilities(activeNetwork)
        return !(networkCapabilities == null
                || !networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))
    }
    val activeNetworkInfo = cm?.activeNetworkInfo
    return activeNetworkInfo != null && activeNetworkInfo.isConnected
}