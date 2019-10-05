package muz.all.activity

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Environment.DIRECTORY_MUSIC
import android.os.Environment.getExternalStoragePublicDirectory
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Menu
import android.view.MenuItem
import android.view.View.VISIBLE
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_music.*
import muz.all.R
import muz.all.adapter.MusicAdapter
import java.io.File

class MusicActivity : DaggerAppCompatActivity() {
    companion object {
        private val TAG = MusicActivity::class.java.simpleName
    }

    private var menuItemDelete: MenuItem? = null
    private var fileToDelete: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music)
        val directory = getExternalStoragePublicDirectory(DIRECTORY_MUSIC)
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val files = directory.listFiles()
            .filter {
                !it.name.startsWith(".") && it.name != "jrdonlinemusic.db" && !it.name.endsWith(".pls")
                        && it.name != "jrdonlinemusic.db-journal"
            }

        rvMusic.setHasFixedSize(true)
        rvMusic.adapter = MusicAdapter(files.toTypedArray())
        setSupportActionBar(toolbar)
        adViewMusic.adListener = object : AdListener() {
            override fun onAdLoaded() {
                adViewMusic.visibility = VISIBLE
            }
        }
        adViewMusic.loadAd(AdRequest.Builder().build())
    }

    override fun onBackPressed() {
        if (menuItemDelete != null && menuItemDelete!!.isVisible) {
            menuItemDelete!!.isVisible = false
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_music, menu)
        menuItemDelete = menu?.findItem(R.id.action_delete)
        return super.onCreateOptionsMenu(menu)
    }

    fun delete(item: MenuItem) {
        fileToDelete?.delete()
        rvMusic.adapter =
            MusicAdapter(getExternalStoragePublicDirectory(DIRECTORY_MUSIC).listFiles())
        item.isVisible = false
    }

    fun setFileAndMenuItemVisibility(file: File?) {
        menuItemDelete.let {
            fileToDelete = file
            it?.isVisible = true
            vibrate()
        }
    }

    private fun vibrate() {
        val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            v?.vibrate(50)
        }
    }
}
