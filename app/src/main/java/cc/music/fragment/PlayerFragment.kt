package cc.music.fragment

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
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import cc.music.R
import cc.music.activity.MainActivity
import cc.music.activity.MusicActivity
import cc.music.component.DaggerFragmentComponent
import cc.music.databinding.FragmentPlayerBinding
import cc.music.model.Track
import cc.music.mvp.presenter.PlayerPresenter
import cc.music.mvp.view.PlayerView
import cc.music.util.TRACK
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import javax.inject.Inject

class PlayerFragment : DialogFragment(), PlayerView, MediaPlayer.OnPreparedListener,
    SeekBar.OnSeekBarChangeListener,
    Runnable {

    companion object {
        private val TAG = PlayerFragment::class.java.simpleName
    }

    @Inject
    lateinit var playerPresenter: PlayerPresenter

    @set:Inject
    var mp: MediaPlayer? = null
    private val handler = Handler(Looper.myLooper()!!)
    private var isPrepared = false
    private lateinit var binding: FragmentPlayerBinding
    override fun showLoading() {
        binding.pbPlayer.visibility = VISIBLE
    }

    override fun hideLoading() {
        binding.pbPlayer.visibility = GONE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        setStyle(STYLE_NO_TITLE, theme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlayerBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        DaggerFragmentComponent.create().inject(this)
        val track = arguments?.getSerializable(TRACK)
        if (track is Track) {
            val audio = track.audio
            Log.i(TAG, "url=>$audio")
            mp?.setDataSource(audio)
            binding.name.text = track.upload_name
        } else if (track is File && track.exists()) {
            try {
                val fos = FileInputStream(track)
                mp?.setDataSource(fos.fd, 0, track.length())
                fos.close()
            } catch (ex: FileNotFoundException) {
                Toast.makeText(context, R.string.could_not_play_file, LENGTH_LONG).show()
                return
            }
            binding.download.visibility = GONE
            binding.name.text = track.name
        } else {
            Toast.makeText(context, R.string.could_not_play_file, LENGTH_LONG).show()
            return
        }
        mp?.setOnPreparedListener(this)
        mp?.prepareAsync()
        mp?.setOnCompletionListener {
            binding.play.setImageResource(R.drawable.ic_play_arrow_24)
            handler.removeCallbacks(this)
            binding.seekBar.progress = 0
        }
        mp?.setOnErrorListener { _, what, extra ->
            Log.i(TAG, "what=>$what; extra=>$extra")
            true
        }
        binding.seekBar.setOnSeekBarChangeListener(this)
        binding.play.setOnClickListener {
            if (mp?.isPlaying == true) {
                mp?.pause()
                binding.play.setImageResource(R.drawable.ic_play_arrow_24)
            } else {
                mp?.start()
                binding.play.setImageResource(R.drawable.ic_pause_24)
            }
        }
        binding.download.setOnClickListener {
            download(track as Track)
        }
//        recBanner.adListener = object : AdListener() {
//            override fun onAdLoaded() {
//                recBanner?.visibility = VISIBLE
//            }
//        }
//        recBanner.loadAd(AdRequest.Builder().build())
        setVisibility(GONE)
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
            val uri = Uri.parse(track.audio)
            val request = DownloadManager.Request(uri)
            downloadManager.enqueue(
                request.setDestinationInExternalPublicDir(
                    DIRECTORY_MUSIC,
                    track.upload_name + ".mp3"
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
        isPrepared = true
        mp?.start()
        hideLoading()
        startSeekBar()
    }

    private fun startSeekBar() {
        handler.postDelayed(this, 1000)
    }

    override fun run() {
        if (mp == null || !isPrepared) return
        val currentPosition = mp?.currentPosition ?: 0
        var dur = mp?.duration ?: 1
        if (dur != 0) dur = currentPosition.times(100).div(dur)
        binding.seekBar.progress = dur
        startSeekBar()
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if (fromUser) {
            seekBar?.progress = progress
            mp?.seekTo(progress.times(mp?.duration ?: 0).div(100))
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {}

    override fun onStopTrackingTouch(seekBar: SeekBar?) {}

    override fun onDismiss(dialog: DialogInterface) {
        handler.removeCallbacks(this)
        if (isPrepared) mp?.stop()
        mp?.release()
        binding.seekBar?.progress = 0
        setVisibility(VISIBLE)
        isPrepared = false
        super.onDismiss(dialog)
    }
}
