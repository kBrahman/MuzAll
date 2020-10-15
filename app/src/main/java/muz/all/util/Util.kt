package muz.all.util

import android.content.Context
import android.net.ConnectivityManager
import androidx.core.content.ContextCompat.getSystemService


const val TRACK = "track"

fun isNetworkConnected(ctx: Context): Boolean {
    val cm = getSystemService(ctx, ConnectivityManager::class.java)
    return cm?.activeNetworkInfo != null && cm.activeNetworkInfo!!.isConnected
}