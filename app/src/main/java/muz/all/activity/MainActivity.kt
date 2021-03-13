package muz.all.activity

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.activity.compose.setContent
import androidx.appcompat.widget.SearchView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.TextFieldDefaults.textFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import muz.all.BuildConfig
import muz.all.R
import muz.all.manager.ApiManager
import muz.all.model.MuzResponse
import muz.all.model.Track
import muz.all.util.isNetworkConnected
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException
import java.util.*
import javax.inject.Inject


class MainActivity : DaggerAppCompatActivity() {

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private const val REQUEST_CODE_STORAGE_READ = 1
    }

    private var searching = false
    private lateinit var q: MutableState<String>
    private var timeOut = false
    private lateinit var uiState: MutableState<UIState>
    private lateinit var loadingState: MutableState<Boolean>
    private var tracks = mutableListOf<Track>()
    private val imageCache = HashMap<String, Bitmap?>()
    private val disposable = CompositeDisposable()

    @Inject
    lateinit var idIterator: Iterator<String>

    @Inject
    lateinit var mp: MediaPlayer

    @Inject
    lateinit var apiManager: ApiManager
    private var isPaused = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }

    private fun init() {
        val colorPrimary = Color(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getColor(R.color.colorPrimary)
            } else {
                resources.getColor(R.color.colorPrimary)
            }
        )
        val stateVal = if (!isNetworkConnected(this)) UIState.MY_MUSIC else {
            disposable += apiManager.getPopular(0).subscribe(::onContentFetched, ::onError)
            Log.i(TAG, "get popular")
            UIState.MAIN
        }
        if (stateVal == UIState.MY_MUSIC && ContextCompat
                .checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(READ_EXTERNAL_STORAGE),
                REQUEST_CODE_STORAGE_READ
            )
            Toast.makeText(this, R.string.no_net, LENGTH_SHORT).show()
            return
        }
        setContent {
            uiState = remember { mutableStateOf(stateVal) }
            loadingState = remember { mutableStateOf(true) }
            q = remember { mutableStateOf("") }
            val showSearchView = remember { mutableStateOf(false) }
            Column {
                TopAppBar(
                    backgroundColor = colorPrimary,
                    contentColor = Color.White
                ) {
                    ConstraintLayout(Modifier.fillMaxSize()) {
                        val (fB, title) = createRefs()
                        Text(
                            getString(R.string.app_name),
                            fontSize = 21.sp,
                            modifier = Modifier.constrainAs(title) {
                                top.linkTo(parent.top)
                                bottom.linkTo(parent.bottom)
                            })
                        SearchView(showSearchView,
                            Modifier
                                .fillMaxHeight()
                                .constrainAs(createRef()) {
                                    end.linkTo(fB.start)
                                    top.linkTo(parent.top)
                                    bottom.linkTo(parent.bottom)
                                    if (showSearchView.value) start.linkTo(title.end, 16.dp)
                                }
                        )
                        IconButton(modifier = Modifier.constrainAs(fB) {
                            end.linkTo(parent.end)
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                        },
                            onClick = { uiState.value = UIState.MY_MUSIC }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_folder_24dp),
                                contentDescription = getString(R.string.my_music)
                            )
                        }
                    }
                }
                when (uiState.value) {
                    UIState.MAIN -> MainScreen()
                    UIState.MY_MUSIC -> MyMusicScreen()
                }
            }
            if (loadingState.value) Box(Modifier.fillMaxSize()) {
                CircularProgressIndicator(
                    color = colorPrimary, modifier = Modifier.align(
                        Alignment.Center
                    )
                )

            }
        }
        MobileAds.initialize(this) {}
        InterstitialAd.load(this,
            getString(if (BuildConfig.DEBUG) R.string.int_test_id else R.string.int_id),
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(TAG, adError.message)
                    timeOut = true
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Ad was loaded.")
                    if (!timeOut) ad.show(this@MainActivity)
                    timeOut = true
                }
            })
    }

    @Composable
    private fun SearchView(showSearchView: MutableState<Boolean>, modifier: Modifier) {
        if (showSearchView.value) TextField(modifier = modifier,
            value = q.value,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                loadingState.value = true
                tracks.clear()
                apiManager.search(q.value, (tracks.size / 25 + 1) * 25)
                    .subscribe(::onContentFetched, ::onError)
            }),
            singleLine = true,
            shape = MaterialTheme.shapes.large,
            colors = textFieldColors(
                cursorColor = Color.White,
                backgroundColor = Color.Transparent,
                focusedIndicatorColor = Color.White
            ),
            textStyle = TextStyle(fontSize = 18.sp),
            onValueChange = { q.value = it },
            trailingIcon = {
                Icon(
                    painterResource(id = android.R.drawable.ic_menu_close_clear_cancel),
                    contentDescription = getString(R.string.close_search_view),
                    Modifier.clickable {
                        showSearchView.value = false
                        q.value = ""
                    },
                    tint = Color.White
                )
            }
        ) else Icon(
            painterResource(id = R.drawable.ic_search),
            contentDescription = getString(R.string.close_search_view),
            modifier.clickable { showSearchView.value = true },
        )

//            IconButton(
//            modifier = modifier.padding((0).dp),
//            onClick = { showSearchView.value = true }) {
//            Image(
//                painter = painterResource(
//                    id = android.R.drawable.ic_menu_search
//                ),
//                contentDescription = getString(R.string.my_music),
//                contentScale = FixedScale(0.7F),
//                colorFilter = ColorFilter.tint(Color.White)
//            )
//        }
    }

    @Composable
    private fun MyMusicScreen() {

    }

    @Composable
    private fun MainScreen() {
        val playerState = remember { mutableStateOf<Track?>(null) }
        LazyColumn(contentPadding = PaddingValues(4.dp)) {
            items(count = tracks.size) {
                Spacer(Modifier.height(4.dp))
                val track = tracks[it]
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { playerState.value = track }) {
                    val btp = remember { mutableStateOf<Bitmap?>(null) }
                    val url = track.image
                    setBitmap(btp, url)
                    val bitmap = btp.value?.asImageBitmap()
                    if (bitmap != null) {
                        Image(bitmap, null, modifier = Modifier.height(100.dp))
                    }
//                    else Image(
//                        painterResource(R.drawable.ic_music_note_black_24dp),
//                        null, modifier = Modifier
//                            .preferredSize(100.dp)
//                    )

                    Spacer(Modifier.width(4.dp))
                    Column {
                        Text(track.name, fontSize = 21.sp)
                        Text(track.artist_name)
                        Text(getString(R.string.released, track.releasedate))
                        Text(getString(R.string.duration, track.duration))
                    }
                }
                if (it == tracks.size - 1 && !loadingState.value) {
                    loadingState.value = true
                    val offset = (tracks.size / 25 + 1) * 25
                    (if (searching) apiManager.search(q.value, offset)
                    else apiManager.getPopular(offset)).subscribe(::onContentFetched, ::onError)
                }
            }
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
        val url = track.audio
        val urlLocation = "$url?client_id=${apiManager.clientId}"
        GlobalScope.launch {
            mp.setDataSource(url)
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

    override fun onStop() {
        disposable.clear()
        super.onStop()
    }

    private fun onError(t: Throwable) =
        if (t is SocketTimeoutException || t is UnknownHostException || t is ConnectException)
            connectionErr() else t.printStackTrace()

    private fun onContentFetched(response: MuzResponse?) {
        Log.i(TAG, "on content fetched")
        if (response?.results?.isEmpty() == true && tracks.isEmpty() && !searching && idIterator.hasNext()) {
            apiManager.clientId = idIterator.next()
            disposable.clear()
            disposable += apiManager.getPopular((tracks.size / 25 + 1) * 25)
                .subscribe(::onContentFetched, ::onError)
        } else if (response?.results?.isEmpty() == true && !searching) {
            loadingState.value = false
            showServiceUnavailable()
        } else {
            tracks.addAll(response?.results ?: emptyList())
            loadingState.value = false
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

    private fun setTimer() = GlobalScope.launch {
        delay(7000)
        timeOut = true
    }

    override fun onPause() {
        isPaused = true
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        isPaused = false
    }

    fun connectionErr() = setContent {
        val colorPrimary = Color(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getColor(R.color.colorPrimary)
            } else {
                resources.getColor(R.color.colorPrimary)
            }
        )
        TopAppBar(backgroundColor = colorPrimary) {
            Column(
                Modifier
                    .fillMaxHeight()
                    .padding(start = 4.dp), verticalArrangement = Arrangement.Center
            ) {
                Text(getString(R.string.app_name), color = Color.White, fontSize = 20.sp)
            }
        }
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(getString(R.string.conn_err))
            Button(onClick = ::init) {
                Text(getString(R.string.refresh))
            }
        }
    }

    fun showServiceUnavailable() {
        Toast.makeText(this, R.string.service_unavailable, Toast.LENGTH_LONG).show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        (menu?.findItem(R.id.action_search)?.actionView as SearchView).setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(q: String): Boolean {
                if (q.isNotBlank()) {

                }
                return true
            }

            override fun onQueryTextChange(p0: String?) = false

        })
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_CODE_STORAGE_READ -> if (grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED)
                init()
            else finish()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private enum class UIState {
        MAIN,
        MY_MUSIC
    }
}
