package cc.music.activity

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Environment.DIRECTORY_MUSIC
import android.os.Environment.getExternalStoragePublicDirectory
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import cc.music.R
import cc.music.adapter.MusicAdapter
import cc.music.databinding.ActivityMusicBinding
import dagger.android.support.DaggerAppCompatActivity
import java.io.File

class MusicActivity : DaggerAppCompatActivity() {
    companion object {
        private val TAG = MusicActivity::class.java.simpleName
    }

    private var menuItemDelete: MenuItem? = null
    private var fileToDelete: File? = null
    private lateinit var binding: ActivityMusicBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMusicBinding.inflate(layoutInflater)
        setContentView(binding.root)
        var directory = getExternalStoragePublicDirectory(DIRECTORY_MUSIC)
        if (!isDirOk(directory)) return
        if (directory.listFiles() == null) {
            directory = File(filesDir, DIRECTORY_MUSIC)
            if (!isDirOk(directory)) return
        }
        val files = directory.listFiles()
            ?.filter {
                it.extension == "mp3"
            }
        binding.rvMusic.setHasFixedSize(true)
        binding.rvMusic.adapter = MusicAdapter(files?.toTypedArray())
        setSupportActionBar(binding.toolbar)
//        adViewMusic.adListener = object : AdListener() {
//            override fun onAdLoaded() {
//                adViewMusic.visibility = VISIBLE
//            }
//        }
//        adViewMusic.loadAd(AdRequest.Builder().build())
    }

    private fun isDirOk(directory: File): Boolean {
        if (!(if (!directory.exists()) directory.mkdirs() else true)) {
            Toast.makeText(this, R.string.err_dir_create, LENGTH_SHORT).show()
            finish()
            return false
        }
        return true
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
        binding.rvMusic.adapter =
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v?.vibrate(VibrationEffect.createOneShot(50, 1))
            } else {
                v?.vibrate(50)
            }
        }
    }
}
