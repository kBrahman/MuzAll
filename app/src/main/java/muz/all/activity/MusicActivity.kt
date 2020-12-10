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
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import dagger.android.support.DaggerAppCompatActivity
import muz.all.R
import muz.all.adapter.MusicAdapter
import muz.all.databinding.ActivityMusicBinding
import muz.all.fragment.PlayerFragment
import muz.all.util.TRACK
import java.io.File
import javax.inject.Inject

class MusicActivity : DaggerAppCompatActivity() {
    companion object {
        private val TAG = MusicActivity::class.java.simpleName
    }

    @Inject
    lateinit var player: PlayerFragment

    private var menuItemDelete: MenuItem? = null
    private var fileToDelete: File? = null
    internal lateinit var binding: ActivityMusicBinding

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
        binding.rvMusic.adapter = MusicAdapter(files?.toTypedArray()) {
            onItemClick(files, it)
        }
        setSupportActionBar(binding.toolbar)
        binding.adViewMusic.adListener = object : AdListener() {
            override fun onAdLoaded() {
                binding.adViewMusic.visibility = VISIBLE
            }
        }
        binding.adViewMusic.loadAd(AdRequest.Builder().build())
    }

    private fun onItemClick(files: List<File>?, it: Int) {
        val bundle = Bundle()
        bundle.putSerializable(TRACK, files?.get(it))
        player.arguments = bundle
        player.show(supportFragmentManager, "player")
        player.showsDialog = true
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
        val list = getExternalStoragePublicDirectory(DIRECTORY_MUSIC).listFiles()?.filter {
            it.extension == "mp3"
        }
        binding.rvMusic.adapter =
            MusicAdapter(list?.toTypedArray()) {
                onItemClick(list, it)
            }
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
