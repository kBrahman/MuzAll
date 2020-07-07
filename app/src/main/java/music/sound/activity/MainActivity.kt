package music.sound.activity

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
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import kotlinx.android.synthetic.main.activity_main.*
import music.sound.R
import music.sound.adapter.TrackAdapter
import music.sound.component.DaggerActivityComponent
import music.sound.manager.ApiManager
import music.sound.model.CollectionHolder
import music.sound.model.Selection
import music.sound.model.Track
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    private var timeOut = false
    var ad: InterstitialAd? = null
    private lateinit var q: String

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
            Log.i(TAG, "response=>$response")
            val collection = response.body()?.collection?.filter { it.media != null }
            if (trackAdapter == null && timeOut) {
                trackAdapter = TrackAdapter(collection?.toMutableList())
                rv.adapter = trackAdapter
                initBanner()
            } else if (trackAdapter == null) {
                trackAdapter = TrackAdapter(collection?.toMutableList())
            } else {
                trackAdapter?.addData(collection?.toMutableList())
            }
            pb.visibility = GONE
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

                val found =
                    response.body()?.collection?.find { it.urn == "soundcloud:selections:charts-top" }
                Log.i(TAG, "found=>$response")
                val tracks = found?.items?.collection?.get(0)?.tracks
                val ids = tracks?.map { it.id }?.joinToString(",")
                manager.tracksBy(ids, topTrackCallback)
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
            title = "Top ${filtered?.size}"
            trackAdapter = TrackAdapter(
                filtered?.toMutableList()
            )
            rv.adapter = trackAdapter
            pb.visibility = GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val component = DaggerActivityComponent.create()
        component.inject(this)
        rv.setHasFixedSize(true)
        rv.addOnScrollListener(object :
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
                        pb.visibility = VISIBLE
                        offset += 25
                        search(q, offset)
                        loading = true
                    }
                }
            }
        })
        setSupportActionBar(toolbar)
        adView.loadAd(AdRequest.Builder().build())
        ad = InterstitialAd(this)
        ad?.adUnitId = getString(R.string.int_id)
        ad?.adListener = object : AdListener() {
            override fun onAdLoaded() {
                ad?.show()
                timeOut = true
            }

            override fun onAdFailedToLoad(p0: Int) {
                timeOut = true
            }

            override fun onAdClosed() {
                timeOut = true
                initBanner()
            }
        }
        ad?.loadAd(AdRequest.Builder().build())
        getMixedSelections()
        setTimer()
    }

    private fun initBanner() {
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                adView.visibility = VISIBLE
            }
        }
    }

    private fun setTimer() {
        Timer().schedule(object : TimerTask() {
            override fun run() {
                timeOut = true
                if (trackAdapter != null) {
                    runOnUiThread {
                        rv.adapter = trackAdapter
                        pb.visibility = GONE
                        ad = null
                    }
                }
                Log.i(TAG, "time out")
            }
        }, 6000L)
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
                        pb.visibility = VISIBLE
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
