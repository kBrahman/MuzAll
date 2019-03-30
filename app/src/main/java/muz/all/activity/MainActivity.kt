package muz.all.activity

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.VISIBLE
import android.widget.FrameLayout
import android.widget.Toast
import com.ironsource.mediationsdk.ISBannerSize
import com.ironsource.mediationsdk.IronSource
import com.ironsource.mediationsdk.IronSourceBannerLayout
import com.ironsource.mediationsdk.logger.IronSourceError
import com.ironsource.mediationsdk.sdk.InterstitialListener
import kotlinx.android.synthetic.main.activity_main.*
import muz.all.R
import muz.all.adapter.TrackAdapter
import muz.all.component.DaggerActivityComponent
import muz.all.manager.ApiManager
import muz.all.model.Track
import muz.all.mvp.presenter.MainPresenter
import muz.all.mvp.view.MainView
import javax.inject.Inject


class MainActivity : AppCompatActivity(), MainView, InterstitialListener {

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    @Inject
    lateinit var manager: ApiManager
    @Inject
    lateinit var presenter: MainPresenter
    private var isPaused = false
    override var trackAdapter: TrackAdapter? = null
    internal lateinit var banner: IronSourceBannerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val component = DaggerActivityComponent.create()
        component.inject(this)
        if (lastCustomNonConfigurationInstance != null) {
            presenter = lastCustomNonConfigurationInstance as MainPresenter
        }
        presenter.view = this
        rv.setHasFixedSize(true)
        rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                if (layoutManager.findLastVisibleItemPosition() == layoutManager.itemCount - 1) {
                    presenter.onScrolled()
                }
            }
        })
        setSupportActionBar(toolbar)
        IronSource.init(this, "8cf4e74d")
        IronSource.loadInterstitial();
        IronSource.setInterstitialListener(this)
        val layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        banner = IronSource.createBanner(this, ISBannerSize.BANNER)
        IronSource.loadBanner(banner)
        bannerContainer.addView(banner, 0, layoutParams)
    }

    override fun onDestroy() {
        super.onDestroy()
        IronSource.destroyBanner(banner)
    }

    override fun onInterstitialAdLoadFailed(p0: IronSourceError?) {
        Log.i(TAG, p0.toString())
    }

    override fun onInterstitialAdClosed() {}

    override fun onInterstitialAdShowFailed(p0: IronSourceError?) {}

    override fun onInterstitialAdClicked() {}

    override fun onInterstitialAdReady() = IronSource.showInterstitial()


    override fun onInterstitialAdOpened() {
        Log.i(TAG, "int should have opened")
    }

    override fun onInterstitialAdShowSucceeded() {}

    override fun onRetainCustomNonConfigurationInstance() = presenter

    override fun onPause() {
        isPaused = true
        super.onPause()
        IronSource.onPause(this)
    }

    override fun onResume() {
        super.onResume()
        isPaused = false
        IronSource.onResume(this)
    }

    override fun show(tracks: MutableList<Track>?) {
        trackAdapter = TrackAdapter(tracks)
        rv.adapter = trackAdapter
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
        if (ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) == PERMISSION_GRANTED) {
            startActivity(Intent(this, MusicActivity::class.java))
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(READ_EXTERNAL_STORAGE), 1)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 1 && (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            openMusic(null)
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
