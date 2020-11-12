package cc.music.activity

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.VISIBLE
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import cc.music.R
import cc.music.adapter.TrackAdapter
import cc.music.model.AppViewModel
import cc.music.model.Track
import cc.music.mvp.presenter.MainPresenter
import cc.music.mvp.view.MainView
import cc.music.util.isNetworkConnected
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import javax.inject.Inject


class MainActivity : DaggerAppCompatActivity(), MainView {

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    private var timeOut = true
    private var finish = false

    @Inject
    lateinit var presenter: MainPresenter

    @Inject
    lateinit var viewModel: AppViewModel
    private var isPaused = false
    override var trackAdapter: TrackAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isNetworkConnected(this) && ContextCompat.checkSelfPermission(
                this,
                WRITE_EXTERNAL_STORAGE
            ) == PERMISSION_GRANTED
        ) {
            Toast.makeText(this, R.string.no_net, LENGTH_SHORT).show()
            openMusic(null)
            finish()
            return
        } else if (!isNetworkConnected(this)) {
            openMusic(null)
            finish = true
        }
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
//        val ad = InterstitialAd(this)
//        ad.adUnitId = getString(R.string.int_id)
//        ad.loadAd(AdRequest.Builder().build())
//        ad.adListener = object : AdListener() {
//            override fun onAdFailedToLoad(p0: Int) {
//                timeOut = true
//                Log.i(TAG, "ad failed=>$p0")
//            }
//
//            override fun onAdClosed() {
//                timeOut = true
//            }
//
//            override fun onAdLoaded() {
//                if (!timeOut) ad.show()
//            }
//        }
        if (viewModel.tracks != null) {
            presenter.results = viewModel.tracks?.value?.toMutableList()
            timeOut = true
        } else {
            setTimer()
        }
        presenter.view = this
        rv.setHasFixedSize(true)
        rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(
                recyclerView: RecyclerView,
                dx: Int,
                dy: Int
            ) {
                val layoutManager =
                    recyclerView.layoutManager as androidx.recyclerview.widget.LinearLayoutManager
                if (layoutManager.findLastVisibleItemPosition() == layoutManager.itemCount - 1) {
                    presenter.onScrolled()
                }
            }
        })
    }

    private fun setTimer() {
        Timer().schedule(object : TimerTask() {
            override fun run() {
                timeOut = true
                if (trackAdapter != null) {
                    runOnUiThread { setAdapter() }
                }
                Log.i(TAG, "time out")
            }
        }, 6000L)
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
        trackAdapter = TrackAdapter(tracks)
        if (timeOut) {
            setAdapter()
        }
        viewModel.tracks = MutableLiveData<List<Track>>(trackAdapter?.getAll())
    }

    private fun setAdapter() {
        hideLoading()
        rv.adapter = trackAdapter
//        adView.adListener = object : AdListener() {
//            override fun onAdLoaded() {
//                adView.visibility = VISIBLE
//            }
//        }
//        adView.loadAd(AdRequest.Builder().build())
    }

    override fun addAndShow(tracks: List<Track>?) {
        trackAdapter?.addData(tracks)
        viewModel.tracks = MutableLiveData<List<Track>>(tracks)
    }

    override fun showLoading() {
        pb.visibility = VISIBLE
    }

    override fun hideLoading() {
        pb.visibility = View.GONE
    }

    override fun showServiceUnavailable() {
        Toast.makeText(this, R.string.service_unavailable, Toast.LENGTH_LONG).show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        (menu?.findItem(R.id.action_search)?.actionView as SearchView)
            .setOnQueryTextListener(object : SearchView.OnQueryTextListener {
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
            ActivityCompat.requestPermissions(this, arrayOf(WRITE_EXTERNAL_STORAGE), 1)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1 && (grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED)) {
            openMusic(null)
            if (finish) finish()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}