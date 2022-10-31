package app.util

import android.content.Context
import android.util.Log
import com.facebook.ads.AdSettings
import com.facebook.ads.AudienceNetworkAds
import muz.all.BuildConfig.DEBUG


class AudienceNetworkInitializeHelper : AudienceNetworkAds.InitListener {
    override fun onInitialized(result: AudienceNetworkAds.InitResult) {
        Log.d(AudienceNetworkAds.TAG, result.message)
    }

    companion object {
        /**
         * It's recommended to call this method from Application.onCreate().
         * Otherwise you can call it from all Activity.onCreate()
         * methods for Activities that contain ads.
         *
         * @param context Application or Activity.
         */
        fun initialize(context: Context?) {
            if (!AudienceNetworkAds.isInitialized(context)) {
                if (DEBUG) {
                    AdSettings.turnOnSDKDebugger(context)
                }
                AudienceNetworkAds
                    .buildInitSettings(context)
                    .withInitListener(AudienceNetworkInitializeHelper())
                    .initialize()
            }
        }
    }
}