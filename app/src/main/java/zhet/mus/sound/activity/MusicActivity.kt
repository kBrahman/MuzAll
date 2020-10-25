package zhet.mus.sound.activity

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Environment.DIRECTORY_MUSIC
import android.os.Environment.getExternalStoragePublicDirectory
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_music.*
import zhet.mus.sound.R
import zhet.mus.sound.adapter.MusicAdapter
import java.io.File

class MusicActivity : AppCompatActivity() {
    companion object {
        private val TAG = MusicActivity::class.java.simpleName
    }

    var menuItemDelete: MenuItem? = null
    private lateinit var fileToDelete: File

    //    var ad: InterstitialAd? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music)
        val files = getExternalStoragePublicDirectory(DIRECTORY_MUSIC).listFiles()
        rvMusic.setHasFixedSize(true)
        rvMusic.adapter = MusicAdapter(files)
//        adViewMusic.adListener = object : AdListener() {
//            override fun onAdLoaded() {
//                adViewMusic.visibility = VISIBLE
//            }
//        }
//        adViewMusic.loadAd(AdRequest.Builder().build())
//        ad = InterstitialAd(this)
//        ad?.adUnitId = getString(R.string.int_id)
//        ad?.loadAd(AdRequest.Builder().build())
        setSupportActionBar(toolbar)
    }

    override fun onBackPressed() {
        if (menuItemDelete != null && menuItemDelete!!.isVisible) {
            menuItemDelete!!.isVisible = false
        } else {
            super.onBackPressed()
//            ad?.show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_music, menu)
        menuItemDelete = menu?.findItem(R.id.action_delete)
        return super.onCreateOptionsMenu(menu)
    }

    fun delete(item: MenuItem) {
        fileToDelete.delete()
        rvMusic.adapter =
            MusicAdapter(getExternalStoragePublicDirectory(DIRECTORY_MUSIC).listFiles())
        item.isVisible = false
    }

    fun setFileAndMenuItemVisibility(file: File) {
        menuItemDelete.let {
            fileToDelete = file
            it?.isVisible = true
            vibrate()
        }
    }

    private fun vibrate() {
        val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v?.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            //deprecated in API 26
            v?.vibrate(500)
        }
    }
}