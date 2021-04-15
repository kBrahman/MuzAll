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
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.TextFieldDefaults.textFieldColors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension.Companion.fillToConstraints
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.firebase.crashlytics.FirebaseCrashlytics
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


class MainActivity : DaggerAppCompatActivity() {

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private const val REQUEST_CODE_STORAGE = 1
    }

    private var filteredFiles = mutableStateListOf<File>()
    private var searching = false
    private lateinit var q: MutableState<String>
    private var timeOut = false
    private lateinit var uiState: MutableState<UIState>
    private var loadingState: MutableState<Boolean>? = null
    private var tracks = mutableListOf<Track>()
    private val imageCache = HashMap<String, Bitmap?>()
    private val disposable = CompositeDisposable()
    private val retriever = MediaMetadataRetriever()
    var fileToDel: File? = null
    private val value = emptyIterator<File>()
    private lateinit var onPermissionGranted: () -> Unit
    private lateinit var onPermissionDenied: () -> Unit

    @Inject
    lateinit var idIterator: Iterator<String>

    @Inject
    lateinit var mp: MediaPlayer

    @Inject
    lateinit var apiManager: ApiManager
    private var isPaused = false

    @ExperimentalFoundationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }

    @ExperimentalFoundationApi
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

        if (stateVal == UIState.MY_MUSIC && ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(READ_EXTERNAL_STORAGE), REQUEST_CODE_STORAGE)
            Toast.makeText(this, R.string.no_net, LENGTH_SHORT).show()
            onPermissionGranted = ::init
            onPermissionDenied = ::finish
            return
        }

        setContent {
            uiState = remember { mutableStateOf(stateVal) }
            loadingState = remember { mutableStateOf(true) }
            q = remember { mutableStateOf("") }
            val showSearchView = remember { mutableStateOf(false) }
            val playerState = remember { mutableStateOf<Any?>(null) }
            val scrollState = rememberLazyListState()
            ConstraintLayout(Modifier.fillMaxHeight()) {
                val bannerRef = createRef()
                Column(Modifier.constrainAs(createRef()) {
                    top.linkTo(parent.top)
                    bottom.linkTo(bannerRef.top)
                    height = fillToConstraints
                }) {
                    when (uiState.value) {
                        UIState.MAIN -> MainScreen(playerState, colorPrimary, showSearchView, scrollState)
                        UIState.MY_MUSIC -> MyMusicScreen(playerState, colorPrimary)
                        UIState.DIR_ISSIUE -> DirIssueScreen()
                    }
                }
                if (playerState.value == null && timeOut) {
                    Banner(Modifier.constrainAs(bannerRef) {
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }, AdSize.BANNER)
                }
            }
            if (playerState.value != null) Player(playerState, colorPrimary, uiState.value == UIState.MAIN)
            if (loadingState?.value == true) Box(Modifier.fillMaxSize()) {
                CircularProgressIndicator(color = colorPrimary,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

        }
        MobileAds.initialize(this) {}
        InterstitialAd.load(
            this,
            getString(if (BuildConfig.DEBUG) R.string.int_test_id else R.string.int_id),
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(TAG, adError.message)
                    timeOut = true
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    if (!timeOut) ad.show(this@MainActivity)
                    timeOut = true
                }
            })
        setTimer()
    }

    @Composable
    private fun DirIssueScreen() {
//        Text(text = getString(R.string.create_dir_manually))
    }

    @Composable
    private fun Banner(modifier: Modifier, size: AdSize) = AndroidView({
        AdView(it).apply {
            adUnitId = if (BuildConfig.DEBUG) "ca-app-pub-3940256099942544/6300978111" else getString(R.string.banner_id)
            adSize = size
            loadAd(AdRequest.Builder().build())
        }
    }, modifier)

    @ExperimentalFoundationApi
    @Composable
    private fun MyMuzAppBar(colorPrimary: Color, showSearchView: MutableState<Boolean>) {
        TopAppBar(contentColor = Color.White, backgroundColor = colorPrimary) {
            ConstraintLayout(Modifier.fillMaxSize()) {
                val (btnMyMusic, title) = createRefs()
                Text(
                    getString(R.string.app_name),
                    fontSize = 21.sp,
                    modifier = Modifier.constrainAs(title) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    })
                SearchView(showSearchView, Modifier
                    .fillMaxHeight()
                    .constrainAs(createRef()) {
                        end.linkTo(btnMyMusic.start)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        if (showSearchView.value) start.linkTo(title.end, 16.dp)
                    })
                IconButton(modifier = Modifier.constrainAs(btnMyMusic) {
                    end.linkTo(parent.end)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }, onClick = {
                    if (ContextCompat.checkSelfPermission(this@MainActivity, WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED) myMusic()
                    else {
                        ActivityCompat.requestPermissions(this@MainActivity, arrayOf(WRITE_EXTERNAL_STORAGE),
                            REQUEST_CODE_STORAGE)
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
        }
    }

    private fun myMusic() {
        uiState.value = UIState.MY_MUSIC
        loadingState?.value = true
        updateFileList()
    }

    private fun updateFileList() {
        GlobalScope.launch {
            filteredFiles.clear()
            val directory =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)

            Log.i(TAG, "dir=>$directory")
            if (!directory.exists()
                && ContextCompat.checkSelfPermission(this@MainActivity, WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this@MainActivity, arrayOf(WRITE_EXTERNAL_STORAGE), REQUEST_CODE_STORAGE)
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
                loadingState?.value = true
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
            }) else Icon(
            painterResource(id = R.drawable.ic_search),
            contentDescription = getString(R.string.close_search_view),
            modifier.clickable { showSearchView.value = true },
        )
    }

    @ExperimentalFoundationApi
    @Composable
    private fun MyMusicScreen(playerState: MutableState<Any?>, colorPrimary: Color) {
        val width = screenWidth().dp - 20.dp
        val delItemVisible = remember { mutableStateOf(false) }
        Log.i(TAG, "w=>$width")
        TopAppBar(contentColor = Color.White, backgroundColor = colorPrimary) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    if (tracks.isEmpty()) finish()
                    uiState.value = UIState.MAIN
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
                        if (ContextCompat.checkSelfPermission(this@MainActivity,
                                WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED || Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                        )
                            deleteAndUpdate()
                        else {
                            ActivityCompat.requestPermissions(this@MainActivity,
                                arrayOf(WRITE_EXTERNAL_STORAGE), REQUEST_CODE_STORAGE)
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
                                    },
                                        onLongClick = {
                                            fileToDel = f
                                            vibrateAndShowDelItem(delItemVisible)
                                        }),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (data != null) Image(
                                    BitmapFactory.decodeByteArray(data, 0, data.size)
                                        .asImageBitmap(),
                                    contentDescription = getString(R.string.music_icon),
                                    modifier = Modifier.clip(CircleShape)
                                )
                                else Image(
                                    painterResource(id = R.drawable.ic_music_note_black_24dp),
                                    contentDescription = getString(R.string.my_music),
                                )
                                Text(f.name)
                            }
                        }
                    }
                    i += 3
                }
            }
        }
        loadingState?.value = false
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
                .enqueue(request.setDestinationInExternalPublicDir(Environment.DIRECTORY_MUSIC, track.name + ".mp3"))
            registerReceiver(object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent) {
                    val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    if (downloadId == id) updateFileList()
                }
            }, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(WRITE_EXTERNAL_STORAGE), REQUEST_CODE_STORAGE)
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


    @ExperimentalFoundationApi
    @Composable
    private fun MainScreen(playerState: MutableState<Any?>, colorPrimary: Color, showSearchView: MutableState<Boolean>, scrollState: LazyListState) {
        if (loadingState?.value != true) MyMuzAppBar(colorPrimary, showSearchView)
        LazyColumn(contentPadding = PaddingValues(4.dp), state = scrollState) {
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
                    Spacer(Modifier.width(4.dp))
                    Column {
                        Text(track.name, fontSize = 21.sp)
                        Text(track.artist_name)
                        Text(getString(R.string.released, track.releasedate))
                        Text(getString(R.string.duration, track.duration))
                    }
                }
                if (it == tracks.size - 1 && loadingState?.value == false) {
                    loadingState?.value = true
                    val offset = (tracks.size / 25 + 1) * 25
                    (if (searching) apiManager.search(q.value, offset)
                    else apiManager.getPopular(offset)).subscribe(::onContentFetched, ::onError)
                }
            }
        }
    }

    @Composable
    private fun Player(
        playerState: MutableState<Any?>,
        colorPrimary: Color,
        downloadable: Boolean
    ) {
        val value = playerState.value!!
        val name = if (value is Track) value.name else (value as File).name
        val showPlayButton = remember { mutableStateOf(false) }
        val isProgressDeterminate = remember { mutableStateOf(false) }
        val progress = remember { mutableStateOf(0F) }
        play(value, showPlayButton, isProgressDeterminate, progress)
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
                Banner(Modifier, AdSize.MEDIUM_RECTANGLE)
                Text(name, fontSize = 20.sp)
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = {
                            if (mp.isPlaying) mp.pause() else mp.start()
                            showPlayButton.value = !mp.isPlaying
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
                    ProgressBar(isProgressDeterminate, progress, colorPrimary, Modifier.weight(1f))
                    Spacer(Modifier.width(4.dp))
                    if (downloadable) Button(
                        onClick = { download(value as Track) },
                        colors = ButtonDefaults.buttonColors(backgroundColor = colorPrimary),
                        modifier = Modifier
                            .width(48.dp)
                    ) {
                        Image(painterResource(R.drawable.ic_file_download_24dp), null)
                    }
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
        t: T,
        showPlayButton: MutableState<Boolean>,
        isProgressDeterminate: MutableState<Boolean>,
        progress: MutableState<Float>
    ) {
        if (t is Track) {
            FirebaseCrashlytics.getInstance().setCustomKey("track", t.toString())
            val url = t.audio
            val urlLocation = "$url?client_id=${apiManager.clientId}"
            mp.setDataSource(urlLocation)
        } else if (t is File) {
            FirebaseCrashlytics.getInstance().setCustomKey("file", t.path)
            mp.setDataSource(t.path)
        }
        GlobalScope.launch {
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

    override fun onStop() {
        disposable.clear()
        super.onStop()
    }

    @ExperimentalFoundationApi
    private fun onError(t: Throwable) =
        if (t is SocketTimeoutException || t is UnknownHostException || t is ConnectException) connectionErr() else t.printStackTrace()

    @ExperimentalFoundationApi
    private fun onContentFetched(response: MuzResponse?) {
        Log.i(TAG, "on content fetched")
        if (response?.results?.isEmpty() == true && tracks.isEmpty() && !searching && idIterator.hasNext()) {
            apiManager.clientId = idIterator.next()
            disposable.clear()
            disposable += apiManager.getPopular((tracks.size / 25 + 1) * 25)
                .subscribe(::onContentFetched, ::onError)
        } else if (response?.results?.isEmpty() == true && !searching) {
            loadingState?.value = false
            showServiceUnavailable()
        } else {
            tracks.addAll(response?.results ?: emptyList())
            if (timeOut) loadingState?.value = false
        }
    }

    private fun setBitmap(btp: MutableState<Bitmap?>, url: String) {
        val bitmap = imageCache[url]
        if (bitmap != null) btp.value = bitmap
        else GlobalScope.launch {
            try {
                btp.value = BitmapFactory.decodeStream(URL(url).openConnection().getInputStream())
            } catch (ce: IOException) {
                btp.value = BitmapFactory.decodeResource(resources, R.drawable.ic_music_note_black_24dp)
            }
            imageCache[url] = btp.value
        }
    }

    private fun setTimer() = GlobalScope.launch {
        delay(7000)
        if (tracks.isNotEmpty()) loadingState?.value = false
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

    @ExperimentalFoundationApi
    private fun connectionErr() = setContent {
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

    private fun showServiceUnavailable() {
        Toast.makeText(this, R.string.service_unavailable, Toast.LENGTH_LONG).show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED) onPermissionGranted()
        else onPermissionDenied()
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private enum class UIState {
        MAIN, MY_MUSIC, DIR_ISSIUE
    }
}
