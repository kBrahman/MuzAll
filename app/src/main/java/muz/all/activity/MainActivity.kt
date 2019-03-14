package muz.all.activity

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Typeface
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
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.startapp.android.publish.ads.banner.BannerListener
import com.startapp.android.publish.adsCommon.StartAppAd
import com.startapp.android.publish.adsCommon.StartAppSDK
import kotlinx.android.synthetic.main.activity_main.*
import muz.all.R
import muz.all.adapter.TrackAdapter
import muz.all.component.DaggerActivityComponent
import muz.all.manager.ApiManager
import muz.all.model.Track
import muz.all.mvp.presenter.MainPresenter
import muz.all.mvp.view.MainView
import javax.inject.Inject


class MainActivity : AppCompatActivity(), MainView {

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private const val SHARED_PREFS_GDPR_SHOWN = "gdpr_shown"
    }

    @Inject
    lateinit var manager: ApiManager
    @Inject
    lateinit var presenter: MainPresenter
    private lateinit var q: String
    private var bannerAdReceived = false
    private var isPaused = false
    override var trackAdapter: TrackAdapter? = null


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
        adView.setBannerListener(object : BannerListener {
            override fun onClick(p0: View?) {}

            override fun onFailedToReceiveAd(view: View?) {
                view?.visibility = GONE
                Log.i(TAG, "failed to receive banner ad")
            }

            override fun onReceiveAd(view: View?) {
                Log.i(TAG, "onReceiveAd, isPaused=>$isPaused")
                if (!isPaused) {
                    adViewLayout.visibility = VISIBLE
                }
                bannerAdReceived = true
            }
        })
        initStartAppSdkAccordingToConsent()
    }

    private fun showGdprDialog(callback: Runnable?) {
        val view = layoutInflater.inflate(muz.all.R.layout.dialog_gdpr, null)
        val dialog = Dialog(this, android.R.style.Theme_Light_NoTitleBar)
        dialog.setContentView(view)

        val medium = Typeface.createFromAsset(assets, "gotham_medium.ttf")
        val book = Typeface.createFromAsset(assets, "gotham_book.ttf")
        (view.findViewById(muz.all.R.id.title) as TextView).typeface = medium
        (view.findViewById(muz.all.R.id.body) as TextView).typeface = book

        val okBtn = view.findViewById<Button>(muz.all.R.id.okBtn)
        okBtn.typeface = medium
        okBtn.setOnClickListener {
            writePersonalizedAdsConsent(true)
            callback?.run()
            dialog.dismiss()
        }

        val cancelBtn = view.findViewById<Button>(muz.all.R.id.cancelBtn)
        cancelBtn.typeface = medium
        cancelBtn.setOnClickListener {
            writePersonalizedAdsConsent(false)
            callback?.run()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun initStartAppSdkAccordingToConsent() {
        if (getPreferences(Context.MODE_PRIVATE).getBoolean(SHARED_PREFS_GDPR_SHOWN, false)) {
            initStartAppSdk()
            return
        }

        showGdprDialog(Runnable {
            initStartAppSdk();
        })
    }

    private fun initStartAppSdk() {
        StartAppSDK.init(this, getString(R.string.app_id), true)
    }

    private fun writePersonalizedAdsConsent(isGranted: Boolean) {
        StartAppSDK.setUserConsent(
            this,
            "pas",
            System.currentTimeMillis(),
            isGranted
        )

        getPreferences(Context.MODE_PRIVATE)
            .edit()
            .putBoolean(SHARED_PREFS_GDPR_SHOWN, true)
            .apply()
    }

    override fun onRetainCustomNonConfigurationInstance() = presenter


    override fun onPause() {
        Log.i(TAG, "onPause")
        adViewLayout.visibility = GONE
        isPaused = true
        super.onPause()
    }

    override fun onResume() {
        Log.i(TAG, "onResume, bannerAdReceived=>$bannerAdReceived")
        super.onResume()
        if (bannerAdReceived) {
            adViewLayout.visibility = VISIBLE
        }
        isPaused = false
    }

    override fun onBackPressed() {
        StartAppAd.onBackPressed(this);
        super.onBackPressed()
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
