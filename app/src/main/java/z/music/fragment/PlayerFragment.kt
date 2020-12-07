package z.music.fragment

import android.content.DialogInterface
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.SeekBar
import com.facebook.ads.AdSize
import com.facebook.ads.AdView
import dagger.android.support.DaggerDialogFragment
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import z.music.BuildConfig
import z.music.R
import z.music.activity.MainActivity
import z.music.activity.MusicActivity
import z.music.databinding.FragmentPlayerBinding
import z.music.db.Db
import z.music.manager.ApiManager
import z.music.model.Track
import z.music.util.TOKEN
import z.music.util.TRACK
import z.music.util.VISIBILITY_BUTTON_ADD
import java.net.URL
import java.util.*
import javax.inject.Inject

class PlayerFragment : DaggerDialogFragment(), MediaPlayer.OnPreparedListener,
    SeekBar.OnSeekBarChangeListener, Runnable {

    companion object {
        private val TAG = PlayerFragment::class.java.simpleName
    }

    private lateinit var track: Track
    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var mp: MediaPlayer

    @Inject
    lateinit var sharedPreferences: SharedPreferences
    private lateinit var adView: AdView

    @Inject
    lateinit var apiManager: ApiManager
    private val handler: Handler = Handler(Looper.myLooper()!!)
    private var isPrepared = false

    @Inject
    lateinit var db: Db

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        adView = AdView(activity, getString(R.string.rec_id), AdSize.RECTANGLE_HEIGHT_250)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        track = arguments?.getSerializable(TRACK) as Track
        GlobalScope.launch {
            val link = getStreamLink(
                BuildConfig.SERVER + TRACK + "/" + track.id + "/play?access_token=" + sharedPreferences.getString(
                    TOKEN, null
                )
            )
            mp.setDataSource(link)
            configureMp()
        }
        binding.name.text = track.track

        setVisibility(GONE)
        binding.bannerContainer.addView(adView)
        adView.loadAd()
        binding.addRm.visibility = arguments?.getInt(VISIBILITY_BUTTON_ADD) ?: VISIBLE
        checkTrackAdded(track)
    }

    private fun checkTrackAdded(track: Track) {
        GlobalScope.launch {
            track.isAdded = db.trackDao().isAdded(track.id)
            if (track.isAdded) {
                activity?.runOnUiThread { binding.addRm.setImageResource(R.drawable.ic_check_24) }
            }
        }
    }

    private fun getStreamLink(urlLocation: String): String {
        val connection = URL(urlLocation).openConnection()
        connection.connect()
        val stream = connection.getInputStream()
        val s = Scanner(stream).useDelimiter("\\A")
        val result = if (s.hasNext()) s.next() else ""
        return JSONObject(result).getString("url")
    }

    private fun configureMp() {
        mp.setOnPreparedListener(this)
        mp.prepareAsync()
        mp.setOnCompletionListener {
            binding.play.setImageResource(R.drawable.ic_play_arrow_24)
            handler.removeCallbacks(this)
            binding.sb.progress = 0
        }
        binding.sb.setOnSeekBarChangeListener(this)
        binding.play.setOnClickListener {
            if (mp.isPlaying) {
                mp.pause()
                binding.play.setImageResource(R.drawable.ic_play_arrow_24)
            } else {
                mp.start()
                binding.play.setImageResource(R.drawable.ic_pause_24)
            }
        }
    }

    private fun setVisibility(visibility: Int) {
        if (activity is MainActivity) {
            (activity as MainActivity).adView.visibility = visibility
        } else if (activity is MusicActivity) {
//            (activity as MusicActivity).adViewMusic.visibility = visibility
        }
    }

    override fun onPrepared(mp: MediaPlayer?) {
        mp?.start()
        binding.pbPlayer.visibility = GONE
        startSeekBar()
        isPrepared = true
        binding.addRm.setOnClickListener {
            if (!track.isAdded) {
                track.isAdded = true
                GlobalScope.launch {
                    db.trackDao().insert(track)
                    Log.i(TAG, "all=>${db.trackDao().all()}")
                }
                binding.addRm.setImageResource(R.drawable.ic_check_24)
            }

        }
    }

    private fun startSeekBar() {
        handler.postDelayed(this, 1000)
    }

    override fun run() {
        val currentPosition = mp.currentPosition
        binding.sb.progress = currentPosition.times(100).div(mp.duration)
        startSeekBar()
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if (fromUser) {
            seekBar?.progress = progress
            mp.seekTo(progress.times(mp.duration).div(100))
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {}

    override fun onStopTrackingTouch(seekBar: SeekBar?) {}

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        handler.removeCallbacks(this)
        if (isPrepared) mp.stop()
        mp.release()
        binding.sb.progress = 0
        setVisibility(VISIBLE)
        isPrepared = false
    }
}