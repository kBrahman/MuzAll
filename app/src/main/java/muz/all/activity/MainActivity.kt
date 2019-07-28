package muz.all.activity

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import kotlinx.android.synthetic.main.activity_main.*
import muz.all.R
import muz.all.adapter.TrackAdapter
import muz.all.component.DaggerActivityComponent
import muz.all.model.Track
import muz.all.mvp.presenter.MainPresenter
import muz.all.mvp.view.MainView
import java.util.*
import javax.inject.Inject

class MainActivity : AppCompatActivity(), MainView {

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    private var timeOut = false
    @Inject
    lateinit var presenter: MainPresenter
    private var isPaused = false
    override var trackAdapter: TrackAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val component = DaggerActivityComponent.create()
        component.inject(this)

        setSupportActionBar(toolbar)
        val ad = InterstitialAd(this)
        ad.adUnitId = getString(R.string.int_id)
        ad.loadAd(AdRequest.Builder().build())
        ad.adListener = object : AdListener() {
            override fun onAdFailedToLoad(p0: Int) {
                Log.i(TAG, "ad failed=>$p0")
                timeOut = true
            }

            override fun onAdClosed() {
                timeOut = true
            }

            override fun onAdLoaded() {
                if (!timeOut) ad.show()
            }
        }
        if (lastCustomNonConfigurationInstance != null) {
            presenter = lastCustomNonConfigurationInstance as MainPresenter
            timeOut = true
        } else {
            setTimer()
        }
        presenter.view = this
        rv.setHasFixedSize(true)
        rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager as androidx.recyclerview.widget.LinearLayoutManager
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
    }

    private fun setAdapter() {
        hideLoading()
        rv.adapter = trackAdapter
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                adView.visibility = VISIBLE
            }
        }
        adView.loadAd(AdRequest.Builder().build())
    }

    override fun addAndShow(tracks: List<Track>?) {
        trackAdapter?.addData(tracks)
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 1 && (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            openMusic(null)
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
