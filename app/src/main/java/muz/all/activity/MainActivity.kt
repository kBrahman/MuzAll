package muz.all.activity

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.os.Environment.DIRECTORY_MUSIC
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.End
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.TextFieldDefaults.textFieldColors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.facebook.ads.*
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.coroutines.*
import muz.all.R
import muz.all.manager.ApiManager
import muz.all.model.MuzNativeAd
import muz.all.model.MuzResponse
import muz.all.model.Track
import muz.all.util.AudienceNetworkInitializeHelper
import muz.all.util.isNetworkConnected
import java.io.File
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException
import java.util.*
import java.util.Collections.emptyIterator
import javax.inject.Inject
import kotlin.concurrent.schedule


@ExperimentalFoundationApi
class MainActivity : DaggerAppCompatActivity() {

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private const val REQUEST_CODE_STORAGE = 1
    }

    private var nativeBannerAdLoaded = false
    private lateinit var nativeBannerAd: NativeBannerAd
    private var filteredFiles = mutableStateListOf<File>()
    private var searching = false
    private lateinit var q: MutableState<String>
    private var timeOut = false
    private lateinit var uiState: MutableState<UIState>
    private var loading: MutableState<Boolean>? = null
    private var tracks = mutableListOf<Track>()
    private val imageCache = HashMap<String, Bitmap?>()
    private val disposable = CompositeDisposable()
    private val retriever = MediaMetadataRetriever()
    private var fileToDel: File? = null
    private val value = emptyIterator<File>()
    private lateinit var onPermissionGranted: () -> Unit
    private lateinit var onPermissionDenied: () -> Unit
    private val cScope = CoroutineScope(Dispatchers.Default)

    @Inject
    lateinit var idIterator: Iterator<String>

    @Inject
    lateinit var mp: MediaPlayer

    @Inject
    lateinit var apiManager: ApiManager
    private var isPaused = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AudienceNetworkInitializeHelper.initialize(this)
        nativeBannerAd = NativeBannerAd(this, "2277322472588056_3164186070568354")
        nativeBannerAd.loadAd(
            nativeBannerAd.buildLoadAdConfig().withAdListener(object : NativeAdListener {
                override fun onError(p0: Ad?, adError: AdError?) {
                    if (tracks.isNotEmpty()) loading?.value = false
                    Log.e(TAG, "Native ad failed to load: " + adError?.errorMessage)
                }

                override fun onAdLoaded(p0: Ad?) {
                    Log.d(
                        TAG,
                        "Native ad is loaded and ready to be displayed, loading state=>${loading?.value}"
                    )
                    nativeBannerAdLoaded = true
                    if (tracks.isNotEmpty()) {
                        insertNative(tracks.size, tracks, nativeBannerAd)
                        if (timeOut) loading?.value = false
                        Log.d(TAG, "inserted, is loading=>${loading?.value}")
                    }
                }

                override fun onAdClicked(p0: Ad?) {}
                override fun onLoggingImpression(p0: Ad?) {}

                override fun onMediaDownloaded(p0: Ad?) {
                    Log.e(TAG, "onMediaDownloaded")
                }
            }).build()
        )
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
        val stateVal = if (!isNetworkConnected(this)) {
            updateFileList()
            UIState.MY_MUSIC
        } else {
            disposable += apiManager.getPopular(0).subscribe(::onContentFetched, ::onError)
            UIState.MAIN
        }

        if (stateVal == UIState.MY_MUSIC && ContextCompat.checkSelfPermission(
                this,
                READ_EXTERNAL_STORAGE
            ) != PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(READ_EXTERNAL_STORAGE),
                REQUEST_CODE_STORAGE
            )
            Toast.makeText(this, R.string.no_net, LENGTH_SHORT).show()
            onPermissionGranted = ::init
            onPermissionDenied = ::finish
            return
        }
        setContent {
            MaterialTheme(colors = lightColors(colorPrimary)) {
                q = remember { mutableStateOf("") }
                val showSearchView = remember { mutableStateOf(false) }
                val playerState = remember { mutableStateOf<Any?>(null) }
                Log.i(TAG, "rec setContent")
                Log.i(TAG, "rec ConstraintLayout")
                Column(Modifier.fillMaxHeight()) {
                    loading = remember { mutableStateOf(true) }
                    uiState = remember { mutableStateOf(stateVal) }
                    when (uiState.value) {
                        UIState.MAIN -> MainScreen(
                            Modifier,
                            playerState,
                            colorPrimary,
                            showSearchView
                        )
                        UIState.MY_MUSIC -> MyMusicScreen(playerState, colorPrimary)
                        UIState.DIR_ISSUE -> DirIssueScreen()
                    }
                }


                Log.i(TAG, "finished composing ConstraintLayout")

                if (playerState.value != null) {
                    val adLoaded = remember { mutableStateOf(false) }
                    val ad = AdView(
                        this@MainActivity,
                        "2277322472588056_3164105983909696",
                        AdSize.RECTANGLE_HEIGHT_250
                    ).apply {
                        loadAd((buildLoadAdConfig().withAdListener(object : AdListener {
                            override fun onError(p0: Ad?, p1: AdError?) {
                                Log.i(TAG, "ad failed to load=>${p1?.errorMessage}")
                            }

                            override fun onAdLoaded(p0: Ad?) {
                                adLoaded.value = true
                                Log.i(TAG, "onAdLoaded")
                            }

                            override fun onAdClicked(p0: Ad?) {}

                            override fun onLoggingImpression(p0: Ad?) {}
                        }).build()))
                    }
                    Player(
                        playerState,
                        colorPrimary,
                        uiState.value == UIState.MAIN, ad
                    )
                }
                if (loading?.value == true) Box(Modifier.fillMaxSize()) {
                    CircularProgressIndicator(
                        color = colorPrimary,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
        val interstitialAd = InterstitialAd(this, "2277322472588056_3164068060580155")
        interstitialAd.loadAd(
            interstitialAd.buildLoadAdConfig()
                .withAdListener(object : InterstitialAdListener {
                    override fun onError(p0: Ad?, err: AdError?) {
                        Log.e(TAG, "Interstitial ad failed to load: " + err?.errorMessage);
                    }

                    override fun onAdLoaded(ad: Ad?) {
                        if (!timeOut) interstitialAd.show()
                        timeOut = true
                    }

                    override fun onAdClicked(p0: Ad?) {}
                    override fun onLoggingImpression(p0: Ad?) {}
                    override fun onInterstitialDisplayed(p0: Ad?) {}
                    override fun onInterstitialDismissed(p0: Ad?) {
                        interstitialAd.destroy()
                    }
                }).build()
        )
        setTimer()
    }

    @Composable
    private fun DirIssueScreen() {
//        Text(text = getString(R.string.create_dir_manually))
    }

    @Composable
    private fun Banner(modifier: Modifier, ad: AdView) =
        AndroidView({ ad }, modifier)

    @ExperimentalFoundationApi
    @Composable
    private fun MyMuzAppBar(colorPrimary: Color, showSearchView: MutableState<Boolean>) =
        TopAppBar(contentColor = Color.White, backgroundColor = colorPrimary) {
            Spacer(Modifier.width(4.dp))
            Text(getString(R.string.app_name), fontSize = 21.sp)
            Spacer(Modifier.weight(1F))
            SearchView(showSearchView, Modifier.fillMaxHeight())
            Spacer(Modifier.width(4.dp))
            IconButton(modifier = Modifier, onClick = {
                if (ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        WRITE_EXTERNAL_STORAGE
                    ) == PERMISSION_GRANTED
                ) myMusic()
                else {
                    ActivityCompat.requestPermissions(
                        this@MainActivity, arrayOf(WRITE_EXTERNAL_STORAGE),
                        REQUEST_CODE_STORAGE
                    )
                    onPermissionGranted = ::myMusic
                    onPermissionDenied = {}
                }
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_folder_24dp),
                    contentDescription = getString(R.string.my_music)
                )
            }
        }

    private fun myMusic() {
        uiState.value = UIState.MY_MUSIC
        loading?.value = true
        updateFileList()
    }

    private fun updateFileList() {
        cScope.launch {
            filteredFiles.clear()
            val directory =
                Environment.getExternalStoragePublicDirectory(DIRECTORY_MUSIC)
            Log.i(TAG, "dir=>$directory")
            if (!directory.exists()
                && ContextCompat.checkSelfPermission(
                    this@MainActivity,
                    WRITE_EXTERNAL_STORAGE
                ) != PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(WRITE_EXTERNAL_STORAGE),
                    REQUEST_CODE_STORAGE
                )
                onPermissionGranted = ::updateFileList
                onPermissionDenied = { uiState.value = UIState.MAIN }
            } else {
                if (!directory.exists()) {
                    val create = directory.mkdirs()
                    Log.i(TAG, "dir does not exist. created=$create")
                }
                Log.i(TAG, "dir  exists. is dir=>${directory.isDirectory}")
                val files = directory.listFiles()
                    ?: getExternalFilesDir(DIRECTORY_MUSIC)?.listFiles()

                files?.filter { it.extension == "mp3" || it.extension == "flac" }?.let {
                    filteredFiles.addAll(it)
                }
            }
            Log.i(TAG, "updateFileList")
        }
    }

    @ExperimentalFoundationApi
    @Composable
    private fun SearchView(showSearchView: MutableState<Boolean>, modifier: Modifier) {
        if (showSearchView.value) TextField(
            modifier = modifier,
            value = q.value,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                tracks.clear()
                disposable += apiManager.search(q.value, (tracks.size / 25 + 1) * 25)
                    .subscribe(::onContentFetched, ::onError)
                loading?.value = true
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
            }) else Icon(
            painterResource(id = R.drawable.ic_search),
            contentDescription = getString(R.string.close_search_view),
            modifier.clickable { showSearchView.value = true },
        )
    }

    @Composable
    private fun MyMusicScreen(playerState: MutableState<Any?>, colorPrimary: Color) {
        val width = screenWidth().dp - 20.dp
        val delItemVisible = remember { mutableStateOf(false) }
        TopAppBar(contentColor = Color.White, backgroundColor = colorPrimary) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    if (tracks.isEmpty()) finish()
                    else {
                        Log.i(TAG, "setting state to main")
                        uiState.value = UIState.MAIN
                    }
                }) {
                    Icon(
                        painterResource(id = R.drawable.ic_arrow_back),
                        contentDescription = getString(R.string.back)
                    )
                }
                Text(getString(R.string.my_music), fontSize = 21.sp)
                if (delItemVisible.value) Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = {
                        if (ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                WRITE_EXTERNAL_STORAGE
                            ) == PERMISSION_GRANTED || Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                        )
                            deleteAndUpdate()
                        else {
                            ActivityCompat.requestPermissions(
                                this@MainActivity,
                                arrayOf(WRITE_EXTERNAL_STORAGE), REQUEST_CODE_STORAGE
                            )
                            onPermissionGranted = ::deleteAndUpdate
                            onPermissionDenied = {}
                        }
                        delItemVisible.value = false
                    }) {
                        Icon(
                            painterResource(id = android.R.drawable.ic_menu_delete),
                            contentDescription = getString(R.string.action_delete),
                            modifier = Modifier.scale(.8f),
                            tint = Color.White
                        )
                    }
                }
            }
        }
        Column(Modifier.fillMaxHeight()) {
            var i = 0
            val size = filteredFiles.size
            while (i < size) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(4.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    run loop@{
                        repeat(if (size - i >= 3) 3 else size - i) {
                            val index = i + it
                            val f = filteredFiles[index]
                            Log.i(TAG, "file name=>${f.name}")
                            try {
                                retriever.setDataSource(f.absolutePath)
                            } catch (e: RuntimeException) {
                                e.printStackTrace()
                            }
                            val data = retriever.embeddedPicture
                            Column(
                                Modifier
                                    .width(width / 3)
                                    .combinedClickable(onClick = {
                                        playerState.value = f
                                    }, onLongClick = {
                                        fileToDel = f
                                        vibrateAndShowDelItem(delItemVisible)
                                    }
                                    ),
                                horizontalAlignment = CenterHorizontally
                            ) {
                                val byteArray =
                                    data?.let { d -> BitmapFactory.decodeByteArray(d, 0, d.size) }
                                if (byteArray != null) {
                                    Image(
                                        byteArray.asImageBitmap(),
                                        contentDescription = getString(R.string.music_icon),
                                        modifier = Modifier.clip(CircleShape)
                                    )
                                } else Image(
                                    painterResource(id = R.drawable.ic_music_note_black_24dp),
                                    contentDescription = getString(R.string.my_music),
                                )
                                Text(f.name, Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                            }
                        }
                    }
                    i += 3
                }
            }
        }
        loading?.value = false
    }

    private fun deleteAndUpdate() {
        val deleted = fileToDel?.delete()
        Log.i(TAG, "file to del=>${fileToDel?.name},  deleted=>$deleted")
        filteredFiles.remove(fileToDel)
        updateFileList()
    }

    private fun vibrateAndShowDelItem(delItemVisible: MutableState<Boolean>) {
        val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v?.vibrate(VibrationEffect.createOneShot(70, 250))
        } else {
            v?.vibrate(50)
        }
        delItemVisible.value = true
    }

    private fun download(track: Track) {
        if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED
            || Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        ) {
            val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            val uri = Uri.parse(track.audio)
            val request = DownloadManager.Request(uri)
            val downloadId = downloadManager
                .enqueue(
                    request.setDestinationInExternalPublicDir(
                        DIRECTORY_MUSIC,
                        track.name + ".mp3"
                    )
                )
            registerReceiver(object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent) {
                    val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    if (downloadId == id) updateFileList()
                }
            }, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(WRITE_EXTERNAL_STORAGE),
                REQUEST_CODE_STORAGE
            )
            onPermissionGranted = {
                Log.i(TAG, "onPermissionGranted download")
                download(track)
            }
            onPermissionDenied = {}
        }
    }

    private fun screenWidth() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val metrics = windowManager.currentWindowMetrics
        metrics.bounds.width() / resources.displayMetrics.density
    } else {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        displayMetrics.widthPixels / displayMetrics.density
    }


    @Composable
    private fun MainScreen(
        modifier: Modifier,
        playerState: MutableState<Any?>,
        colorPrimary: Color,
        showSearchView: MutableState<Boolean>
    ) {
        MyMuzAppBar(colorPrimary, showSearchView)
        LazyColumn(
            modifier,
            contentPadding = PaddingValues(4.dp)
        ) {
            items(count = tracks.size) {
                Spacer(Modifier.height(4.dp))
                val item = tracks[it]

                if (item is MuzNativeAd) AndroidAdView()
                else TrackView(playerState, item)
                if (it == tracks.size - 1 && loading?.value == false) {
                    loading?.value = true
                    val offset = (tracks.size / 25 + 1) * 25
                    (if (searching) apiManager.search(q.value, offset)
                    else apiManager.getPopular(offset)).subscribe(::onContentFetched, ::onError)
                }
            }
        }
        Log.i(TAG, "finish comp MainScreen")
    }

    @Composable
    private fun AndroidAdView() {
        val view = View(this@MainActivity)
        val mediaView = MediaView(this)
        Column {
            Row {
                AndroidView(factory = { AdOptionsView(it, nativeBannerAd, null) })
                Text(
                    text = nativeBannerAd.sponsoredTranslation ?: "sponsored",
                    Modifier.padding(2.dp),
                    color = Color.DarkGray,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 12.sp
                )
            }
            Row(Modifier.fillMaxWidth()) {
                AndroidView(factory = { mediaView }, Modifier.size(80.dp))
                Spacer(Modifier.width(4.dp))
                Column {
                    Text(
                        nativeBannerAd.advertiserName ?: "",
                        overflow = TextOverflow.Ellipsis,
                        color = Color.Black,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = nativeBannerAd.adSocialContext ?: "",
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 12.sp
                    )
                }
                Row(Modifier.fillMaxWidth(), End) {
                    Button(
                        { view.performClick() },
                        Modifier
                            .width(80.dp)
                            .height(50.dp)
                    ) {
                        Text(text = nativeBannerAd.adCallToAction ?: "Open", fontSize = 12.sp)
                    }
                    Spacer(Modifier.width(4.dp))
                }
            }
        }
        nativeBannerAd.registerViewForInteraction(view, mediaView)
    }

    @Composable
    private fun TrackView(playerState: MutableState<Any?>, item: Track) =
        Row(
            Modifier
                .fillMaxWidth()
                .clickable { playerState.value = item }) {
            val btp = remember { mutableStateOf<Bitmap?>(null) }
            val url = item.image
            setBitmap(btp, url)
            val bitmap = btp.value?.asImageBitmap()
            if (bitmap != null) {
                Image(bitmap, null, modifier = Modifier.height(100.dp))
            }
            Spacer(Modifier.width(4.dp))
            Column {
                Text(item.name, fontSize = 21.sp)
                Text(item.artist_name)
                Text(getString(R.string.released, item.releasedate))
                Text(getString(R.string.duration, item.duration))
            }
        }


    @Composable
    private fun Player(
        playerState: MutableState<Any?>,
        colorPrimary: Color,
        downloadable: Boolean,
        ad: AdView
    ) {
        Log.i(TAG, "player recomposed")
        val value = playerState.value!!
        val name = if (value is Track) value.name else (value as File).name
        val showPlayButton = remember { mutableStateOf(false) }
        val isProgressDeterminate = remember { mutableStateOf(false) }
        val progress = remember { mutableStateOf(0F) }
        val showPlayErr = remember { mutableStateOf(false) }
        play(value, showPlayButton, isProgressDeterminate, progress, showPlayErr)
        Dialog(onDismissRequest = {
            mp.stop()
            mp.reset()
            playerState.value = null
            ad.destroy()
        }) {
            Column(
                Modifier
                    .background(Color.Transparent)
                    .padding(4.dp)
            ) {
                Banner(Modifier.height(250.dp), ad)
                Column(
                    Modifier
                        .background(Color.White)
                        .width(300.dp)
                ) {
                    Text(name, fontSize = 20.sp)
                    Spacer(Modifier.height(4.dp))
                    Row(
                        Modifier.padding(bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Spacer(Modifier.width(4.dp))
                        Button(
                            onClick = {
                                when {
                                    mp.isPlaying -> {
                                        mp.pause()
                                        showPlayButton.value = true
                                    }
                                    showPlayErr.value -> {
                                        isProgressDeterminate.value = false
                                        showPlayButton.value = false
                                        play(
                                            value,
                                            showPlayButton,
                                            isProgressDeterminate,
                                            progress,
                                            showPlayErr
                                        )
                                    }
                                    else -> {
                                        startProgress(progress)
                                        mp.start()
                                        showPlayButton.value = false
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(backgroundColor = colorPrimary),
                            modifier = Modifier.width(48.dp)
                        ) {
                            Image(
                                painterResource(id = if (showPlayButton.value) android.R.drawable.ic_media_play else R.drawable.ic_pause_24),
                                null
                            )
                        }
                        Spacer(Modifier.width(4.dp))
                        ProgressBar(
                            isProgressDeterminate,
                            progress,
                            colorPrimary,
                            Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(4.dp))
                        if (downloadable) Button(
                            onClick = { download(value as Track) },
                            colors = ButtonDefaults.buttonColors(backgroundColor = colorPrimary),
                            modifier = Modifier
                                .width(48.dp)
                        ) {
                            Image(painterResource(R.drawable.ic_file_download_24dp), null)
                        }
                        Spacer(Modifier.width(4.dp))
                    }
                    if (showPlayErr.value) Text(
                        getString(R.string.could_not_play_track),
                        color = Color.Red
                    )
                }
            }
        }
    }

    @Composable
    private fun ProgressBar(
        isProgressDeterminate: MutableState<Boolean>, progress: MutableState<Float>,
        colorPrimary: Color, modifier: Modifier
    ) {
        if (isProgressDeterminate.value) {
            var w = 0
            LinearProgressIndicator(progress = progress.value,
                color = colorPrimary,
                modifier = modifier
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(constraints)
                        w = placeable.width
                        layout(placeable.width, placeable.height) {
                            placeable.placeRelative(0, 0)
                        }
                    }
                    .pointerInput(key1 = null) {
                        detectTapGestures {
                            progress.value = it.x / w
                            mp.seekTo((progress.value * mp.duration).toInt())
                        }
                    })
        } else {
            LinearProgressIndicator(modifier, color = colorPrimary)
        }
    }

    private fun <T> play(
        t: T, showPlayButton: MutableState<Boolean>, isProgressDeterminate: MutableState<Boolean>,
        progress: MutableState<Float>, showPlayErr: MutableState<Boolean>
    ) {
        try {
            if (t is Track) {
                val url = t.audio
                mp.setDataSource(url)
                showPlayErr.value = false
            } else if (t is File) {
                FirebaseCrashlytics.getInstance().setCustomKey("file", t.path)
                mp.setDataSource(t.path)
            }
        } catch (ex: Exception) {
            isProgressDeterminate.value = true
            showPlayButton.value = true
            showPlayErr.value = true
            Log.i(TAG, "caught exception")
            return
        }
        configureMp(showPlayButton, isProgressDeterminate, progress)
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
            Log.i(TAG, "mp complete")
        }
    }

    private fun startProgress(progress: MutableState<Float>) {
        Timer("progress timer", false).schedule(500L) {
            if (!mp.isPlaying) return@schedule
            progress.value = progress.value + 500F / mp.duration
            startProgress(progress)
        }
    }

    override fun onStop() {
        disposable.clear()
        super.onStop()
    }

    private fun onError(t: Throwable) =
        if (t is SocketTimeoutException || t is UnknownHostException || t is ConnectException) connectionErr(
            t
        ) else t.printStackTrace()

    private fun onContentFetched(response: MuzResponse?) {
        val data = response?.results?.toMutableList() ?: mutableListOf()
        Log.i(TAG, "on content fetched size=>${data.size}")
        if (data.isEmpty() && tracks.isEmpty() && !searching && idIterator.hasNext()) {
            apiManager.clientId = idIterator.next()
            disposable.clear()
            disposable += apiManager.getPopular((tracks.size / 25 + 1) * 25)
                .subscribe(::onContentFetched, ::onError)
        } else if (data.isEmpty() && !searching) {
            loading?.value = false
            showServiceUnavailable()
        } else {
            val count = data.size
            if (nativeBannerAdLoaded) insertNative(count, data, nativeBannerAd)
            tracks.addAll(data)
            if (timeOut && nativeBannerAdLoaded) loading?.value = false
        }
    }

    private fun insertNative(count: Int, tracks: MutableList<Track>, ad: NativeBannerAd?) {
        Log.i(TAG, "insertNative")
        val muzAd = MuzNativeAd(ad)
        val step = 7
        for (i in step until count) {
            val t = tracks[i]
            if (i % step == 0 && t !is MuzNativeAd) {
                tracks.add(i, muzAd)
            }
        }
    }

    private fun setBitmap(btp: MutableState<Bitmap?>, url: String) {
        val bitmap = imageCache[url]
        if (bitmap != null) btp.value = bitmap
        else cScope.launch {
            val v = try {
                BitmapFactory.decodeStream(URL(url).openConnection().getInputStream())
            } catch (ce: IOException) {
                BitmapFactory.decodeResource(resources, R.drawable.ic_music_note_black_24dp)
            }
            withContext(Dispatchers.Main) {
                btp.value = v
            }
            imageCache[url] = btp.value
        }
    }

    private fun setTimer() = cScope.launch {
        delay(7000)
        if (tracks.isNotEmpty()) loading?.value = false
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

    private fun connectionErr(t: Throwable) = setContent {
        t.printStackTrace()
        TopAppBar {
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
            horizontalAlignment = CenterHorizontally
        ) {
            Text(getString(R.string.conn_err))
            Button(onClick = ::init) {
                Text(getString(R.string.refresh))
            }
        }
    }

    private fun showServiceUnavailable() {
        Toast.makeText(this, R.string.service_unavailable, Toast.LENGTH_LONG).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_STORAGE &&
            grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED
        ) onPermissionGranted()
        else if (requestCode == REQUEST_CODE_STORAGE) onPermissionDenied()
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private enum class UIState {
        MAIN, MY_MUSIC, DIR_ISSUE
    }
}
