package dev.mus.sound.fragment

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.DownloadManager
import android.content.Context.DOWNLOAD_SERVICE
import android.content.DialogInterface
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Environment.DIRECTORY_MUSIC
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.facebook.ads.AdSize
import com.facebook.ads.AdView
import dev.mus.sound.BuildConfig
import dev.mus.sound.R
import dev.mus.sound.activity.MainActivity
import dev.mus.sound.activity.MusicActivity
import dev.mus.sound.component.DaggerFragmentComponent
import dev.mus.sound.databinding.FragmentPlayerBinding
import dev.mus.sound.manager.ApiManager
import dev.mus.sound.model.Track
import dev.mus.sound.util.TRACK
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.net.URL
import java.util.*
import javax.inject.Inject

class PlayerFragment : DialogFragment(), MediaPlayer.OnPreparedListener,
    SeekBar.OnSeekBarChangeListener, Runnable {

    companion object {
        private val TAG = PlayerFragment::class.java.simpleName
    }

    @Inject
    lateinit var mp: MediaPlayer
    private lateinit var adView: AdView

    @Inject
    lateinit var apiManager: ApiManager
    private val handler: Handler = Handler(Looper.myLooper()!!)
    private var isPrepared = false
    private lateinit var binding: FragmentPlayerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
//        adView = AdView(activity, getString(R.string.banner_rec_id), AdSize.RECTANGLE_HEIGHT_250)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlayerBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        DaggerFragmentComponent.create().inject(this)
        val track = arguments?.getSerializable(TRACK)
        if (track is Track) {
            val url = track.media.transcodings.find { it.url.endsWith("/progressive") }?.url
            val urlLocation = url + "?client_id=" + BuildConfig.ClIENT_ID
            GlobalScope.launch {
                val link = getStreamLink(urlLocation)
                mp.setDataSource(link)
                configureMp()
            }

            binding.name.text = track.title
        } else if (track is File) {
            context?.let { mp.setDataSource(it, Uri.fromFile(track)) }
            binding.download.visibility = GONE
            binding.name.text = track.name
            configureMp()
        }
        setVisibility(GONE)
        binding.bannerContainer.addView(adView)
        adView.loadAd()
    }

    private suspend fun getStreamLink(urlLocation: String): String {
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
            binding.play.setImageResource(R.drawable.ic_play_24)
            handler.removeCallbacks(this)
            binding.sb.progress = 0
        }
        binding.sb.setOnSeekBarChangeListener(this)
        binding.play.setOnClickListener {
            if (mp.isPlaying) {
                mp.pause()
                binding.play.setImageResource(R.drawable.ic_play_24)
            } else {
                mp.start()
                binding.play.setImageResource(android.R.drawable.ic_media_pause)
            }
        }
    }

    private fun setVisibility(visibility: Int) {
        if (activity is MainActivity) {
//            (activity as MainActivity).adView.visibility = visibility
        } else if (activity is MusicActivity) {
//            (activity as MusicActivity).adViewMusic.visibility = visibility
        }
    }

    private fun download(track: Track) {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                WRITE_EXTERNAL_STORAGE
            ) == PERMISSION_GRANTED
        ) {
            val downloadManager = context?.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            val uri = Uri.parse(track.stream_url)
            val request = DownloadManager.Request(uri)
            downloadManager.enqueue(
                request.setDestinationInExternalPublicDir(
                    DIRECTORY_MUSIC,
                    track.title + ".mp3"
                )
            )
        } else {
            requestPermissions(arrayOf(WRITE_EXTERNAL_STORAGE), 2)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if ((grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED)) {
            download(arguments?.getSerializable(TRACK) as Track)
        }
    }

    override fun onPrepared(mp: MediaPlayer?) {
        mp?.start()
        binding.pbPlayer.visibility = GONE
        startSeekBar()
        isPrepared = true
    }

    private fun startSeekBar() {
        handler?.postDelayed(this, 1000)
    }

    override fun run() {
        val currentPosition = mp.currentPosition
        binding.sb?.progress = currentPosition.times(100).div(mp.duration)
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