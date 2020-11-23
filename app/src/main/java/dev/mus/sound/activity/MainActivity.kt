package dev.mus.sound.activity

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.facebook.ads.*
import dagger.android.support.DaggerAppCompatActivity
import dev.mus.sound.R
import dev.mus.sound.adapter.SelectionsAdapter
import dev.mus.sound.adapter.TrackAdapter
import dev.mus.sound.databinding.ActivityMainBinding
import dev.mus.sound.manager.ApiManager
import dev.mus.sound.model.CollectionHolder
import dev.mus.sound.model.Selection
import dev.mus.sound.model.Track
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import javax.inject.Inject


class MainActivity : DaggerAppCompatActivity() {

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    private var selectionsAdapter: SelectionsAdapter? = null
//    lateinit var adView: AdView
    private var timeOut = true

    var ad: InterstitialAd? = null
    private lateinit var q: String
    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var manager: ApiManager
    private var offset: Int = 0
    private var loading = false
    private var trackAdapter: TrackAdapter? = null
    private var searching = false

    private val callback = object : Callback<CollectionHolder<Track>> {
        override fun onFailure(call: Call<CollectionHolder<Track>>, t: Throwable) =
            t.printStackTrace()

        override fun onResponse(
            call: Call<CollectionHolder<Track>>,
            response: Response<CollectionHolder<Track>>
        ) {
            val collection = response.body()?.collection?.filter { it.media != null }
            if (trackAdapter == null && timeOut) {
                trackAdapter = TrackAdapter(collection?.toMutableList())
                binding.rv.adapter = trackAdapter
            } else if (trackAdapter == null) {
                trackAdapter = TrackAdapter(collection?.toMutableList())
            } else {
                trackAdapter?.addData(collection?.toMutableList())
            }
            binding.pb.visibility = GONE
            loading = false
        }
    }

    private val selectionsCallback: Callback<CollectionHolder<Selection>> =
        object : Callback<CollectionHolder<Selection>> {
            override fun onFailure(call: Call<CollectionHolder<Selection>>, t: Throwable) =
                t.printStackTrace()

            override fun onResponse(
                call: Call<CollectionHolder<Selection>>,
                response: Response<CollectionHolder<Selection>>
            ) {
                val withTracks =
                    response.body()?.collection?.filter { it.items.collection.any { e -> !e.tracks.isNullOrEmpty() } }

                title = getString(R.string.mixed_selections)

                withTracks?.let {
                    selectionsAdapter = SelectionsAdapter(it) { ids, name ->
                        binding.pb.visibility = VISIBLE
                        manager.tracksBy(ids, topTrackCallback)
                        title = name
                    }
                }
                if (timeOut) {
                    setAdapterAndBanner()
                }
            }
        }

    private val topTrackCallback: Callback<List<Track>> = object : Callback<List<Track>> {
        override fun onFailure(call: Call<List<Track>>, t: Throwable) = t.printStackTrace()

        override fun onResponse(call: Call<List<Track>>, response: Response<List<Track>>) {
            val filtered = response.body()?.filter {
                it.media.transcodings.isNotEmpty() and it.media.transcodings.any { tr ->
                    tr.url.endsWith("/progressive")
                }
            }
            trackAdapter = TrackAdapter(
                filtered?.toMutableList()
            )
            binding.rv.adapter = trackAdapter
            binding.pb.visibility = GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.rv.setHasFixedSize(true)
        binding.rv.addOnScrollListener(object :
            androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(
                recyclerView: androidx.recyclerview.widget.RecyclerView,
                dx: Int,
                dy: Int
            ) {
                val layoutManager =
                    recyclerView.layoutManager as androidx.recyclerview.widget.LinearLayoutManager
                if (layoutManager.findLastVisibleItemPosition() == layoutManager.itemCount - 1) {
                    if (!loading && searching) {
                        binding.pb.visibility = VISIBLE
                        offset += 25
                        search(q, offset)
                        loading = true
                    }
                }
            }
        })
        setSupportActionBar(binding.toolbar)
        getMixedSelections()
        setTimer()
        AudienceNetworkAds.initialize(this)
//        ad = InterstitialAd(this, getString(R.string.fb_int_id))
        val conf = ad?.buildLoadAdConfig()?.withAdListener(value)?.build()
        ad?.loadAd(conf)
//        adView = AdView(this, getString(R.string.fb_banner_id), AdSize.BANNER_HEIGHT_50)
//        binding.bannerContainer.addView(adView)
    }

    private val value = object : InterstitialAdListener {
        override fun onInterstitialDisplayed(ad: Ad) {
            // Interstitial ad displayed callback
            Log.e(TAG, "Interstitial ad displayed.")
        }

        override fun onInterstitialDismissed(ad: Ad) {
            // Interstitial dismissed callback
            Log.e(TAG, "Interstitial ad dismissed.")
        }

        override fun onError(ad: Ad, adError: AdError) {
            // Ad error callback
            Log.e(
                TAG,
                "Interstitial ad failed to load: " + adError.errorMessage
            )
        }

        override fun onAdLoaded(ad: Ad) {
            // Interstitial ad is loaded and ready to be displayed
            Log.d(
                TAG,
                "Interstitial ad is loaded and ready to be displayed!"
            )
            // Show the ad
            this@MainActivity.ad?.show()
        }

        override fun onAdClicked(ad: Ad) {
            // Ad clicked callback
            Log.d(TAG, "Interstitial ad clicked!")
        }

        override fun onLoggingImpression(ad: Ad) {
            // Ad impression logged callback
            Log.d(TAG, "Interstitial ad impression logged!")
        }
    }

    private fun setTimer() {
        Timer().schedule(object : TimerTask() {
            override fun run() {
                timeOut = true
                if (selectionsAdapter != null) {
                    runOnUiThread {
                        setAdapterAndBanner()
                        ad = null
                    }
                }
                Log.i(TAG, "time out")
            }
        }, 7000L)
    }

    private fun setAdapterAndBanner() {
        Log.i(TAG, "setAdapterAndBanner")
        binding.rv.adapter = selectionsAdapter
        binding.pb.visibility = GONE
//        adView.loadAd()
    }

    private fun getMixedSelections() = manager.getMixedSelections(selectionsCallback)


    private fun search(q: String, offset: Int) {
        manager.search(q, offset, callback)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        (menu?.findItem(R.id.action_search)?.actionView as SearchView)
            .setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(q: String): Boolean {
                    if (q.isNotBlank()) {
                        this@MainActivity.q = q
                        offset = 0
                        trackAdapter = null
                        binding.pb.visibility = VISIBLE
                        search(q, offset)
                        searching = true
                    }
                    return true
                }

                override fun onQueryTextChange(p0: String?) = false

            })
        return true
    }

    fun openMusic(item: MenuItem?) {
        if (ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) == PERMISSION_GRANTED) {
            startActivity(Intent(this, MusicActivity::class.java))
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(READ_EXTERNAL_STORAGE), 1)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1 && (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            openMusic(null)
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
