package music.sound.fragment

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.DownloadManager
import android.content.Context.DOWNLOAD_SERVICE
import android.content.DialogInterface
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment.DIRECTORY_MUSIC
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.fragment_player.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import music.sound.activity.MainActivity
import music.sound.activity.MusicActivity
import music.sound.component.DaggerFragmentComponent
import music.sound.manager.ApiManager
import music.sound.model.Track
import music.sound.util.TRACK
import org.json.JSONObject
import z.music.BuildConfig
import z.music.R
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
//    private lateinit var adView: AdView

    @Inject
    lateinit var apiManager: ApiManager
    private val handler: Handler? = Handler()
    private var isPrepared = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
//        adView = AdView(activity, getString(R.string.rec_id), AdSize.RECTANGLE_HEIGHT_250)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) =
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN)
            inflater.inflate(
                R.layout.fragment_player,
                container,
                false
            ) else inflater.inflate(R.layout.fragment_player_api_16, container, false)

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

            name.text = track.title
        } else if (track is File) {
            context?.let { mp.setDataSource(it, Uri.fromFile(track)) }
            download.visibility = GONE
            name.text = track.name
            configureMp()
        }
        setVisibility(GONE)
//        bannerContainer.addView(adView)
//        adView.loadAd()
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
            play?.setImageResource(android.R.drawable.ic_media_play)
            handler?.removeCallbacks(this)
            sb.progress = 0
        }
        sb.setOnSeekBarChangeListener(this)
        play.setOnClickListener {
            if (mp.isPlaying) {
                mp.pause()
                play.setImageResource(android.R.drawable.ic_media_play)
            } else {
                mp.start()
                play.setImageResource(android.R.drawable.ic_media_pause)
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
                context!!,
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
        pbPlayer.visibility = GONE
        startSeekBar()
        isPrepared = true
    }

    private fun startSeekBar() {
        handler?.postDelayed(this, 1000)
    }

    override fun run() {
        val currentPosition = mp.currentPosition
        sb?.progress = currentPosition.times(100).div(mp.duration)
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
        handler?.removeCallbacks(this)
        if (isPrepared) mp.stop()
        mp.release()
        sb.progress = 0
        setVisibility(VISIBLE)
        isPrepared = false
    }
}