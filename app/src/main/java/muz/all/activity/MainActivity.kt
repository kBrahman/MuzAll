package muz.all.activity

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.VISIBLE
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.activity.compose.setContent
import androidx.appcompat.widget.SearchView
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import muz.all.BuildConfig
import muz.all.R
import muz.all.adapter.TrackAdapter
import muz.all.databinding.ActivityMainBinding
import muz.all.fragment.PlayerFragment
import muz.all.manager.ApiManager
import muz.all.model.AppViewModel
import muz.all.model.Track
import muz.all.mvp.presenter.MainPresenter
import muz.all.mvp.view.MainView
import muz.all.util.TRACK
import muz.all.util.isNetworkConnected
import java.util.*
import javax.inject.Inject


class MainActivity : DaggerAppCompatActivity(), MainView {

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private val REQUEST_CODE_STORAGE_READ = 1
    }

    private var timeOut = false
    private var finish = false
    private lateinit var uiState: MutableState<UIState>
    private lateinit var loadingState: MutableState<Boolean>
    private var tracks = mutableListOf<Track>()

    @Inject
    lateinit var presenter: MainPresenter

    @Inject
    lateinit var apiManager: ApiManager

    @Inject
    lateinit var viewModel: AppViewModel
    private var isPaused = false
    override var trackAdapter: TrackAdapter? = null
    internal lateinit var binding: ActivityMainBinding

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
            apiManager.getPopular(0)
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
            when (uiState.value) {
                UIState.MAIN -> MainScreen()
            }
            if (loadingState.value) Box(Modifier.fillMaxSize()) {
                CircularProgressIndicator(
                    color = colorPrimary, modifier = Modifier.align(
                        Alignment.Center
                    )
                )

            }
        }
        if (!isNetworkConnected(this)) {
            openMusic(null)
            finish = true
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
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
        if (viewModel.tracks != null) {
            presenter.results = viewModel.tracks?.value?.toMutableList()
        } else {
            setTimer()
        }
        presenter.view = this
        binding.rv.setHasFixedSize(true)
        binding.rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                if (layoutManager.findLastVisibleItemPosition() == layoutManager.itemCount - 1) {
                    presenter.onScrolled()
                }
            }
        })
    }

    @Composable
    private fun MainScreen() {
        LazyColumn(contentPadding = PaddingValues(4.dp)) {
            items(count = tracks.size) {
                Spacer(Modifier.height(4.dp))
                val track = tracks[it]
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
                if (it == tracks.size - 1 && !loading.value && searching) {
                    loading.value = true
                    search(q, (tracks.size / 25 + 1) * 25)
                }
            }
        }
    }

    private fun setTimer() = GlobalScope.launch {
        delay(7000)
        timeOut = true
        if (trackAdapter != null) {
            runOnUiThread { setAdapterAndBanner() }
        }
    }

    override fun onRetainCustomNonConfigurationInstance() = presenter

    override fun onPause() {
        isPaused = true
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        isPaused = false
    }

    override fun show(tracks: MutableList<Track>?) {
        trackAdapter = TrackAdapter(tracks) {
            val bundle = Bundle()
            bundle.putSerializable(TRACK, it)
            val player = PlayerFragment()
            player.arguments = bundle
            player.show(supportFragmentManager, "player")
            player.showsDialog = true
        }
        Log.i(TAG, "get popular")
        if (timeOut) {
            setAdapterAndBanner()
        }
        viewModel.tracks = MutableLiveData<List<Track>>(trackAdapter?.getAll())
    }

    private fun setAdapterAndBanner() {
        hideLoading()
        binding.rv.adapter = trackAdapter
        binding.adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                Log.i(TAG, "ad loaded")
                if (supportFragmentManager.findFragmentByTag("player")?.isVisible != true) {
                    binding.adView.visibility = VISIBLE
                }
            }
        }
        binding.adView.loadAd(AdRequest.Builder().build())
    }

    override fun addAndShow(tracks: List<Track>?) {
        trackAdapter?.addData(tracks)
        viewModel.tracks = MutableLiveData<List<Track>>(tracks)
    }

    override fun connectionErr() = setContent {
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

    override fun showLoading() {
        binding.pb.visibility = VISIBLE
    }

    override fun hideLoading() {
        binding.pb.visibility = View.GONE
    }

    override fun showServiceUnavailable() {
        Toast.makeText(this, R.string.service_unavailable, Toast.LENGTH_LONG).show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        (menu?.findItem(R.id.action_search)?.actionView as SearchView).setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(q: String): Boolean {
                if (q.isNotBlank()) {
                    presenter.onQueryTextSubmit(q)
                    trackAdapter = null
                }
                return true
            }

            override fun onQueryTextChange(p0: String?) = false

        })
        return true
    }

    fun openMusic(item: MenuItem?) {
        if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED) {
            startActivity(Intent(this, MusicActivity::class.java))
        } else {
        }
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
        UNDEFINED,
        MAIN,
        MY_MUSIC
    }
}
