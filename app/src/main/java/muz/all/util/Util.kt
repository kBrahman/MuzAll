package muz.all.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.core.content.ContextCompat

const val ID_NATIVE = "ca-app-pub-8761730220693010/7300183909"

fun isNetworkConnected(ctx: Context): Boolean {
    val cm = ContextCompat.getSystemService(ctx, ConnectivityManager::class.java)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val activeNetwork = cm?.activeNetwork ?: return false
        val networkCapabilities = cm.getNetworkCapabilities(activeNetwork)
        return !(networkCapabilities == null
                || !networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET))
    }
    val activeNetworkInfo = cm?.activeNetworkInfo
    return activeNetworkInfo != null && activeNetworkInfo.isConnected
}