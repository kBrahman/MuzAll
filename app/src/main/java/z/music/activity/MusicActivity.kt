package z.music.activity

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Menu
import android.view.MenuItem
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import z.music.R
import z.music.adapter.MusicAdapter
import z.music.databinding.ActivityMusicBinding
import z.music.db.Db
import z.music.model.Track
import javax.inject.Inject

class MusicActivity : DaggerAppCompatActivity() {
    companion object {
        private val TAG = MusicActivity::class.java.simpleName
    }

    @Inject
    lateinit var db: Db
    private lateinit var binding: ActivityMusicBinding

    var menuItemDelete: MenuItem? = null
    private lateinit var trackToDelete: Track

    //    var ad: InterstitialAd? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMusicBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.rvMusic.setHasFixedSize(true)
        GlobalScope.launch {
            val all = db.trackDao().all()
            runOnUiThread { binding.rvMusic.adapter = MusicAdapter(all) }
        }
        setActionBar(binding.toolbar)
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
        GlobalScope.launch {
            db.trackDao().delete(trackToDelete)
            binding.rvMusic.adapter =
                MusicAdapter(db.trackDao().all())
            item.isVisible = false
        }

    }

    fun setFileAndMenuItemVisibility(track: Track) {
        menuItemDelete.let {
            trackToDelete = track
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
