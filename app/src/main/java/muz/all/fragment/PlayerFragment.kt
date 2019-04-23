package muz.all.fragment

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.DownloadManager
import android.content.Context.DOWNLOAD_SERVICE
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Environment.DIRECTORY_MUSIC
import android.os.Handler
import android.support.v4.app.DialogFragment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import com.facebook.ads.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_player.*
import muz.all.R
import muz.all.activity.MainActivity
import muz.all.activity.MusicActivity
import muz.all.component.DaggerFragmentComponent
import muz.all.model.Track
import muz.all.util.TRACK
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import javax.inject.Inject

class PlayerFragment : DialogFragment(), MediaPlayer.OnPreparedListener, SeekBar.OnSeekBarChangeListener, Runnable {

    companion object {
        private val TAG = PlayerFragment::class.java.simpleName
    }

    @set:Inject
    var mp: MediaPlayer? = null

    private val handler = Handler()
    private var isPrepared = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        setStyle(STYLE_NO_TITLE, theme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        inflater.inflate(
            R.layout.fragment_player,
            container,
            false
        )


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        DaggerFragmentComponent.create().inject(this)
        val track = arguments?.getSerializable(TRACK)
        if (track is Track) {
            val audio = track.audio
            mp?.setDataSource(audio)
            name.text = track.name
        } else if (track is File && track.exists()) {
            try {
                val fos = FileInputStream(track)
                mp?.setDataSource(fos.fd, 0, track.length())
                fos.close()
            } catch (ex: FileNotFoundException) {
                Toast.makeText(context, R.string.could_not_play_file, LENGTH_LONG).show()
                return
            }
            download.visibility = GONE
            name.text = track.name
        } else {
            Toast.makeText(context, R.string.could_not_play_file, LENGTH_LONG).show()
            return
        }
        mp?.setOnPreparedListener(this)
        mp?.prepareAsync()
        mp?.setOnCompletionListener {
            play.setImageResource(android.R.drawable.ic_media_play)
            handler.removeCallbacks(this)
            seekBar.progress = 0
        }
        seekBar.setOnSeekBarChangeListener(this)
        play.setOnClickListener {
            if (mp?.isPlaying == true) {
                mp?.pause()
                play.setImageResource(android.R.drawable.ic_media_play)
            } else {
                mp?.start()
                play.setImageResource(android.R.drawable.ic_media_pause)
            }
        }
        download.setOnClickListener {
            download(track as Track)
        }
        setVisibility(GONE)
        val adView = AdView(context, getString(R.string.fb_rec_banner_id), AdSize.RECTANGLE_HEIGHT_250)

        // Find the Ad Container
        val adContainer = view.findViewById<LinearLayout>(R.id.recBannerContainer)
        adContainer.layoutParams.width = (320F.times(context?.resources?.displayMetrics?.density ?: 1.1F)).toInt()

        // Add the ad view to your activity layout
        adContainer.addView(adView)

        // Request an ad
        adView.setAdListener(object : InterstitialAdListener {
            override fun onInterstitialDisplayed(ad: Ad) {}

            override fun onInterstitialDismissed(ad: Ad) {
                ad.destroy()
            }

            override fun onError(ad: Ad, adError: AdError) {
                ad.destroy()
            }

            override fun onAdLoaded(ad: Ad) {
                Log.d(TAG, "Interstitial ad is loaded and ready to be displayed!")

            }

            override fun onAdClicked(ad: Ad) {
                // Ad clicked callback
                Log.d(TAG, "Interstitial ad clicked!")
            }

            override fun onLoggingImpression(ad: Ad) {
                // Ad impression logged callback
                Log.d(TAG, "Interstitial ad impression logged!")
            }
        })
        adView.loadAd()
    }

    private fun setVisibility(visibility: Int) {
        if (activity is MainActivity) {
            (activity as MainActivity).bannerContainer.visibility = visibility
        } else if (activity is MusicActivity) {
            (activity as MusicActivity).bannerContainer.visibility = visibility
        }
    }

    private fun download(track: Track) {
        if (ContextCompat.checkSelfPermission(context!!, WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED) {
            val downloadManager = context?.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            val uri = Uri.parse(track.audio)
            val request = DownloadManager.Request(uri)
            downloadManager.enqueue(request.setDestinationInExternalPublicDir(DIRECTORY_MUSIC, track.name + ".mp3"))
        } else {
            requestPermissions(arrayOf(WRITE_EXTERNAL_STORAGE), 2)
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            download(arguments?.getSerializable(TRACK) as Track)
        }
    }

    override fun onPrepared(mp: MediaPlayer?) {
        isPrepared = true
        mp?.start()
        pbPlayer?.visibility = GONE
        startSeekBar()
    }

    private fun startSeekBar() {
        handler.postDelayed(this, 1000)
    }

    override fun run() {
        val currentPosition = mp?.currentPosition ?: 0
        var dur = mp?.duration ?: 1
        if (dur != 0) dur = currentPosition.times(100).div(dur)
        seekBar?.progress = dur
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

    override fun onDismiss(dialog: DialogInterface?) {
        handler.removeCallbacks(this)
        if (isPrepared) mp?.stop()
        mp?.release()
        seekBar.progress = 0
        setVisibility(VISIBLE)
        isPrepared = false
        super.onDismiss(dialog)
    }
}

