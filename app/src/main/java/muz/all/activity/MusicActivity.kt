package muz.all.activity

import android.os.Bundle
import android.os.Environment.DIRECTORY_MUSIC
import android.os.Environment.getExternalStoragePublicDirectory
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View.VISIBLE
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import kotlinx.android.synthetic.main.activity_music.*
import muz.all.R
import muz.all.adapter.MusicAdapter

class MusicActivity : AppCompatActivity() {
    companion object {
        private val TAG = MusicActivity::class.java.simpleName
    }

    var ad: InterstitialAd? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music)
        val files = getExternalStoragePublicDirectory(DIRECTORY_MUSIC).listFiles()
        Log.i(TAG, files.size.toString())
        rvMusic.setHasFixedSize(true)
        rvMusic.adapter = MusicAdapter(files)
        adViewMusic.adListener = object : AdListener() {
            override fun onAdLoaded() {
                adViewMusic.visibility = VISIBLE
            }
        }
        adViewMusic.loadAd(AdRequest.Builder().build())

        ad = InterstitialAd(this)
        ad?.adUnitId = getString(R.string.int_id)
        ad?.loadAd(AdRequest.Builder().build())
    }

    override fun onBackPressed() {
        super.onBackPressed()
        ad?.show()
    }
}
