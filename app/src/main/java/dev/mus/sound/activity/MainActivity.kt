package dev.mus.sound.activity

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.widget.SearchView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.*
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.gesture.tapGestureFilter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URL
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap


class MainActivity : DaggerAppCompatActivity() {

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    private lateinit var filteredTracks: List<Track>
    private lateinit var selections: List<Selection>
    private var selectionsAdapter: SelectionsAdapter? = null
    lateinit var adView: AdView
    private var timeOut = false

    var ad: InterstitialAd? = null
    private lateinit var q: String
    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var manager: ApiManager
    private var offset: Int = 0
    private var loading = false
    private var trackAdapter: TrackAdapter? = null
    private var searching = false
    private lateinit var uiState: MutableState<UIState>
    private val imageCache = HashMap<String, Bitmap?>()
    private val callback = object : Callback<CollectionHolder<Track>> {
        override fun onFailure(call: Call<CollectionHolder<Track>>, t: Throwable) =
            t.printStackTrace()

        override fun onResponse(
            call: Call<CollectionHolder<Track>>,
            response: Response<CollectionHolder<Track>>
        ) {
            val collection = response.body()?.collection?.filter { it.media != null }
            if (trackAdapter == null && timeOut) {
                trackAdapter = TrackAdapter(collection?.toMutableList(), ::play)
                binding.rv.adapter = trackAdapter
            } else if (trackAdapter == null) {
                trackAdapter = TrackAdapter(collection?.toMutableList(), ::play)
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
                title = getString(R.string.mixed_selections)
                selections =
                    response.body()?.collection?.filter { it.items.collection.any { e -> !e.tracks.isNullOrEmpty() } }
                        ?: emptyList()
                uiState.value = UIState.SELECTION
                selections.let {
                    selectionsAdapter = SelectionsAdapter(it) { ids, name ->
                        binding.pb.visibility = VISIBLE
                        manager.tracksBy(ids, selectionCallback)
                        title = name
                    }
                }
                if (timeOut) {
                    setAdapterAndBanner()
                }
            }
        }

    private val selectionCallback: Callback<List<Track>> = object : Callback<List<Track>> {
        override fun onFailure(call: Call<List<Track>>, t: Throwable) = t.printStackTrace()

        override fun onResponse(call: Call<List<Track>>, response: Response<List<Track>>) {
            filteredTracks = response.body()?.filter {
                it.media.transcodings.isNotEmpty() and it.media.transcodings.any { tr ->
                    tr.url.endsWith("/progressive")
                }
            } ?: emptyList()
            uiState.value = UIState.PLAYLIST
//            trackAdapter = TrackAdapter(filtered?.toMutableList(), ::play)
//            binding.rv.adapter = trackAdapter
//            binding.pb.visibility = GONE
        }
    }

    private fun play(track: Track?) {
        setContent {
            Dialog(onDismissRequest = { /*TODO*/ }) {
                Text(text = "TEST")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val colorPrimary = Color(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    getColor(R.color.colorPrimary)
                } else {
                    resources.getColor(R.color.colorPrimary)
                }
            )
            uiState = remember { mutableStateOf(UIState.PB) }
            when (uiState.value) {
                UIState.PB -> Box(Modifier.fillMaxSize()) {
                    CircularProgressIndicator(
                        color = colorPrimary, modifier = Modifier.align(
                            Alignment.Center
                        )
                    )

                }
                UIState.SELECTION -> LazyColumn(Modifier.padding(start = 4.dp, end = 4.dp)) {
                    items(items = selections) {
                        Text(
                            it.title,
                            fontSize = 20.sp,
                            style = TextStyle(fontWeight = FontWeight.Bold)
                        )
                        LazyRow {
                            items(items = it.items.collection) { playlist ->
                                val btp = remember { mutableStateOf<Bitmap?>(null) }
                                getBitmap(btp, playlist.calculated_artwork_url)
                                Box(
                                    Modifier
                                        .padding(start = 4.dp)
                                        .tapGestureFilter {
                                            manager.tracksBy(
                                                playlist.tracks
                                                    .map { t -> t.id }
                                                    .joinToString(","), selectionCallback)
                                            uiState.value = UIState.PB
                                        }) {
                                    Card(Modifier.preferredSize(150.dp), elevation = 4.dp) {
                                        val bitmap = btp.value?.asImageBitmap()
                                        if (bitmap != null) {
                                            Image(bitmap, modifier = Modifier.fillMaxSize())
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                UIState.PLAYLIST -> {
                    LazyColumn {
                        items(filteredTracks) {
                            Row {

                            }
                        }
                    }
                }
            }
        }
//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//        binding.rv.setHasFixedSize(true)
//        binding.rv.addOnScrollListener(object :
//            androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
//            override fun onScrolled(
//                recyclerView: androidx.recyclerview.widget.RecyclerView,
//                dx: Int,
//                dy: Int
//            ) {
//                val layoutManager =
//                    recyclerView.layoutManager as androidx.recyclerview.widget.LinearLayoutManager
//                if (layoutManager.findLastVisibleItemPosition() == layoutManager.itemCount - 1) {
//                    if (!loading && searching) {
//                        binding.pb.visibility = VISIBLE
//                        offset += 25
//                        search(q, offset)
//                        loading = true
//                    }
//                }
//            }
//        })
//        setSupportActionBar(binding.toolbar)
        getMixedSelections()
        setTimer()
        AudienceNetworkAds.initialize(this)
        ad = InterstitialAd(this, getString(R.string.fb_int_id))
        val conf = ad?.buildLoadAdConfig()?.withAdListener(value)?.build()
        ad?.loadAd(conf)
        adView = AdView(this, getString(R.string.fb_banner_id), AdSize.BANNER_HEIGHT_50)
//        binding.bannerContainer.addView(adView)
    }

    private fun getBitmap(btp: MutableState<Bitmap?>, url: String) {
        val bitmap = imageCache[url]
        if (bitmap != null) {
            btp.value = bitmap
        } else GlobalScope.launch {
            btp.value = BitmapFactory.decodeStream(URL(url).openConnection().getInputStream())
            imageCache[url] = btp.value
        }

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
            timeOut = true
            Log.e(TAG, "Interstitial ad failed to load: " + adError.errorMessage)
        }

        override fun onAdLoaded(ad: Ad) {
            Log.d(TAG, "Interstitial ad is loaded and ready to be displayed!")
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
//        binding.rv.adapter = selectionsAdapter
//        binding.pb.visibility = GONE
        adView.loadAd()
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

    private enum class UIState() {
        PB,
        SELECTION,
        PLAYLIST
    }
}
