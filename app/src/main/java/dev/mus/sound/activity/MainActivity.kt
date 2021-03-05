package dev.mus.sound.activity

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.compose.setContent
import androidx.appcompat.widget.SearchView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.gesture.tapGestureFilter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.facebook.ads.*
import dagger.android.support.DaggerAppCompatActivity
import dev.mus.sound.BuildConfig.ClIENT_ID
import dev.mus.sound.R
import dev.mus.sound.manager.ApiManager
import dev.mus.sound.model.CollectionHolder
import dev.mus.sound.model.Selection
import dev.mus.sound.model.Track
import dev.mus.sound.util.isNetworkConnected
import dev.mus.sound.util.milliSecondsToTime
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URL
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap
import kotlin.concurrent.schedule

class MainActivity : DaggerAppCompatActivity() {

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    private var filteredTracks = mutableListOf<Track>()
    private var selections: List<Selection>? = null
    lateinit var adView: AdView

    var interstitialAd: InterstitialAd? = null
    private lateinit var q: String

    @Inject
    lateinit var mp: MediaPlayer

    @Inject
    lateinit var manager: ApiManager
    private lateinit var loading: MutableState<Boolean>
    private var searching = false
    private lateinit var uiState: MutableState<UIState>
    private val imageCache = HashMap<String, Bitmap?>()
    private val searchCallback = object : Callback<CollectionHolder<Track>> {
        override fun onFailure(call: Call<CollectionHolder<Track>>, t: Throwable) =
            t.printStackTrace()

        override fun onResponse(
            call: Call<CollectionHolder<Track>>,
            response: Response<CollectionHolder<Track>>
        ) {
            val data =
                (response.body()?.collection?.filter { it.media != null }?.toMutableList()
                    ?: mutableListOf())
            filteredTracks.addAll(data)
            uiState.value = UIState.PLAYLIST
            loading.value = false
            Log.i(TAG, "search resp")
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
                if (interstitialAd == null) {
                    uiState.value = UIState.SELECTION
                    loading.value = false
                }
                adView.loadAd()
                Log.i(TAG, "selections resp")
            }
        }

    private val selectionCallback: Callback<List<Track>> = object : Callback<List<Track>> {
        override fun onFailure(call: Call<List<Track>>, t: Throwable) = t.printStackTrace()

        override fun onResponse(call: Call<List<Track>>, response: Response<List<Track>>) {
            filteredTracks = response.body()?.filter {
                it.media.transcodings.isNotEmpty() and it.media.transcodings.any { tr ->
                    tr.url.endsWith("/progressive")
                }
            }?.toMutableList() ?: mutableListOf()
            uiState.value = UIState.PLAYLIST
            loading.value = false
        }
    }

    @Composable
    private fun Player(playerState: MutableState<Track?>, colorPrimary: Color) {
        val track = playerState.value!!
        val showPlayButton = remember { mutableStateOf(false) }
        val isProgressDeterminate = remember { mutableStateOf(false) }
        val progress = remember { mutableStateOf(0F) }
        play(track, showPlayButton, isProgressDeterminate, progress)
        Dialog(onDismissRequest = {
            mp.stop()
            mp.reset()
            playerState.value = null
        }) {
            Column(
                Modifier
                    .background(Color.White)
                    .padding(4.dp)
            ) {
                Text(track.title, fontSize = 20.sp)
                Spacer(Modifier.preferredHeight(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = {
                            if (mp.isPlaying) mp.pause() else mp.start()
                            showPlayButton.value = !mp.isPlaying
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = colorPrimary),
                        modifier = Modifier.preferredWidth(48.dp)
                    ) {
                        Image(
                            painterResource(id = if (showPlayButton.value) R.drawable.ic_play_24 else R.drawable.ic_pause_24),
                            null
                        )
                    }
                    Spacer(Modifier.preferredWidth(4.dp))
                    if (isProgressDeterminate.value) {
                        var w = 0
                        LinearProgressIndicator(progress = progress.value,
                            color = colorPrimary,
                            modifier = Modifier
                                .layout { measurable, constraints ->
                                    val placeable = measurable.measure(constraints)
                                    w = placeable.width
                                    layout(placeable.width, placeable.height) {
                                        placeable.placeRelative(0, 0)
                                    }
                                }
                                .tapGestureFilter {
                                    progress.value = it.x / w
                                    mp.seekTo(
                                        (progress.value * mp.duration).toInt()
                                    )
                                })
                    } else {
                        LinearProgressIndicator(
                            color = colorPrimary,
                        )
                    }
                }
            }
        }
    }

    private fun play(
        track: Track,
        showPlayButton: MutableState<Boolean>,
        isProgressDeterminate: MutableState<Boolean>,
        progress: MutableState<Float>
    ) {
        val url = track.media.transcodings.find { it.url.endsWith("/progressive") }?.url
        val urlLocation = "$url?client_id=$ClIENT_ID"
        GlobalScope.launch {
            mp.setDataSource(getStreamLink(urlLocation))
            configureMp(showPlayButton, isProgressDeterminate, progress)
        }
    }

    private fun configureMp(
        showPlayButton: MutableState<Boolean>,
        isProgressDeterminate: MutableState<Boolean>,
        progress: MutableState<Float>
    ) {
        mp.prepareAsync()
        mp.setOnPreparedListener {
            it.start()
            isProgressDeterminate.value = true
            startProgress(progress)
        }
        mp.setOnCompletionListener {
            showPlayButton.value = true
            progress.value = 0F
        }
    }

    private fun startProgress(progress: MutableState<Float>) {
        Timer("progress timer", false).schedule(500L) {
            if (!mp.isPlaying) return@schedule
            progress.value = progress.value + 500F / mp.duration
            startProgress(progress)
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    private fun init() {
        val colorPrimary = Color(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getColor(R.color.colorPrimary)
            } else {
                resources.getColor(R.color.colorPrimary)
            }
        )
        if (isNetworkConnected(this)) {
            setContent {
                uiState = remember { mutableStateOf(UIState.UNDEFINED) }
                loading = remember { mutableStateOf(true) }
                when (uiState.value) {
                    UIState.SELECTION -> LazyColumn(Modifier.padding(start = 4.dp, end = 4.dp)) {
                        items(count = selections!!.size) {
                            val selection = selections!![it]
                            Text(
                                selection.title,
                                fontSize = 20.sp,
                                style = TextStyle(fontWeight = FontWeight.Bold)
                            )
                            LazyRow {
                                items(count = selection.items.collection.size) { i ->
                                    val btp = remember { mutableStateOf<Bitmap?>(null) }
                                    val playlist = selection.items.collection[i]
                                    setBitmap(btp, playlist.calculated_artwork_url)
                                    Box(
                                        Modifier
                                            .padding(start = 4.dp)
                                            .tapGestureFilter {
                                                manager.tracksBy(
                                                    playlist.tracks
                                                        .map { t -> t.id }
                                                        .joinToString(","), selectionCallback)
                                                loading.value = true
                                            }) {
                                        Card(Modifier.preferredSize(150.dp), elevation = 4.dp) {
                                            val bitmap = btp.value?.asImageBitmap()
                                            if (bitmap != null) {
                                                Image(
                                                    bitmap,
                                                    null,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    UIState.PLAYLIST -> {
                        val playerState = remember { mutableStateOf<Track?>(null) }
                        Log.i(TAG, "size=>${filteredTracks.size}")
                        LazyColumn(contentPadding = PaddingValues(4.dp)) {
//                            filteredTracks = mutableListOf()
                            items(count = filteredTracks.size) {
                                Spacer(Modifier.preferredHeight(4.dp))
                                val track = filteredTracks[it]
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .clickable { playerState.value = track }) {
                                    val btp = remember { mutableStateOf<Bitmap?>(null) }
                                    val url = track.artwork_url
                                    if (url != null) {
                                        setBitmap(btp, url)
                                        val bitmap = btp.value?.asImageBitmap()
                                        if (bitmap != null) {
                                            Image(
                                                bitmap,
                                                null,
                                                modifier = Modifier
                                                    .preferredSize(100.dp)
                                            )
                                        }
                                    } else Image(
                                        painterResource(R.drawable.ic_music_note_black_24dp),
                                        null, modifier = Modifier
                                            .preferredSize(100.dp)
                                    )

                                    Spacer(Modifier.width(4.dp))
                                    Column {
                                        Text(track.title, fontSize = 22.sp)
                                        Text(
                                            getString(
                                                R.string.uploaded,
                                                track.created_at.replace(Regex("T.+"), "")
                                            )
                                        )
                                        Text(
                                            getString(
                                                R.string.duration,
                                                milliSecondsToTime(track.duration)
                                            )
                                        )
                                    }
                                }
                                if (it == filteredTracks.size - 1 && !loading.value && searching) {
                                    Log.i(TAG, "filtered size=>${filteredTracks.size}")
                                    loading.value = true
                                    search(q, (filteredTracks.size / 25 + 1) * 25)
                                }
                            }
                        }
                        if (playerState.value != null) Player(playerState, colorPrimary)
                    }
                }
                if (loading.value) Box(Modifier.fillMaxSize()) {
                    CircularProgressIndicator(
                        color = colorPrimary, modifier = Modifier.align(
                            Alignment.Center
                        )
                    )

                }
            }
            getMixedSelections()
            AudienceNetworkAds.initialize(this)
            interstitialAd = InterstitialAd(this, getString(R.string.fb_int_id))
            val conf = interstitialAd?.buildLoadAdConfig()?.withAdListener(adListener)?.build()
            interstitialAd?.loadAd(conf)
            adView = AdView(this, getString(R.string.fb_banner_id), AdSize.BANNER_HEIGHT_50)
            GlobalScope.launch {
                delay(7000)
                interstitialAd = null
                setUI()
                Log.i(TAG, "time out")
            }
        } else setContent {
            Column(
                Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(getString(R.string.no_inet), fontSize = 23.sp)
                Button(
                    ::init,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = colorPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Text(getString(R.string.refresh))
                }
            }
        }
    }

    private fun setBitmap(btp: MutableState<Bitmap?>, url: String) {
        val bitmap = imageCache[url]
        if (bitmap != null) {
            btp.value = bitmap
        } else GlobalScope.launch {
            btp.value = BitmapFactory.decodeStream(URL(url).openConnection().getInputStream())
            imageCache[url] = btp.value
        }
    }

    private fun getBitmapFromVectorDrawable(context: Context, drawableId: Int): Bitmap? {
        val drawable = ContextCompat.getDrawable(context, drawableId)
        val bitmap = Bitmap.createBitmap(
            drawable!!.intrinsicWidth,
            drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight())
        drawable.draw(canvas)
        return bitmap
    }

    private val adListener = object : InterstitialAdListener {
        override fun onInterstitialDisplayed(ad: Ad) {
            Log.e(TAG, "Interstitial ad displayed.")
            setUI()
        }

        override fun onInterstitialDismissed(ad: Ad) {
            Log.e(TAG, "Interstitial ad dismissed.")
        }

        override fun onError(ad: Ad, adError: AdError) {
            interstitialAd = null
            setUI()
            Log.e(TAG, "Interstitial ad failed to load: " + adError.errorMessage)
        }

        override fun onAdLoaded(ad: Ad) {
            Log.d(TAG, "Interstitial ad is loaded and ready to be displayed!")
            this@MainActivity.interstitialAd?.show()
            interstitialAd = null
        }

        override fun onAdClicked(ad: Ad) {
            Log.d(TAG, "Interstitial ad clicked!")
        }

        override fun onLoggingImpression(ad: Ad) {
            Log.d(TAG, "Interstitial ad impression logged!")
        }
    }

    private fun setUI() {
        if (selections != null) {
            uiState.value = UIState.SELECTION
            loading.value = false
        }
    }

    private fun getMixedSelections() = manager.getMixedSelections(selectionsCallback)

    private fun search(q: String, offset: Int) {
        Log.i(TAG, "search")
        loading.value = true
        manager.search(q, offset, searchCallback)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (uiState.value == UIState.SELECTION) onBackPressed()
        else uiState.value = UIState.SELECTION
        searching = false
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        (menu?.findItem(R.id.action_search)?.actionView as SearchView)
            .setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(q: String): Boolean {
                    if (q.isNotBlank()) {
                        this@MainActivity.q = q
                        searching = true
                        filteredTracks.clear()
                        Log.i(TAG, "clear")
                        search(q, 0)
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

    private enum class UIState {
        UNDEFINED,
        SELECTION,
        PLAYLIST,
    }
}
