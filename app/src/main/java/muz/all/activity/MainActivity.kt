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
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import kotlinx.android.synthetic.main.activity_main.*
import muz.all.BuildConfig
import muz.all.R
import muz.all.adapter.TrackAdapter
import muz.all.component.DaggerActivityComponent
import muz.all.manager.ApiManager
import muz.all.model.MuzResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    @Inject
    lateinit var manager: ApiManager
    private var offset: Int = 0
    private var loading = false
    private var trackAdapter: TrackAdapter? = null
    private var searching = false
    private lateinit var q: String
    private val idIterator = listOf(
        BuildConfig.CLIENT_ID_2,
        BuildConfig.CLIENT_ID_3,
        BuildConfig.CLIENT_ID_4,
        BuildConfig.CLIENT_ID_5,
        BuildConfig.CLIENT_ID_6
    ).iterator()
    private val callback = object : Callback<MuzResponse> {
        override fun onFailure(call: Call<MuzResponse>, t: Throwable) = t.printStackTrace()

        override fun onResponse(call: Call<MuzResponse>, response: Response<MuzResponse>) {
            Log.i(TAG, response.body().toString())
            if (response.body()?.results?.isEmpty() == true && !searching && idIterator.hasNext()) {
                pb.visibility = VISIBLE
                manager.clientId = idIterator.next()
                manager.getPopular(offset, this)
            } else if (response.body()?.results?.isEmpty() == true && !searching) {
                pb.visibility = GONE
                Toast.makeText(this@MainActivity, R.string.service_unavailable, LENGTH_LONG).show()
            } else if (trackAdapter == null) {
                pb.visibility = GONE
                trackAdapter = TrackAdapter(response.body()?.results?.toMutableList())
                rv.adapter = trackAdapter
            } else {
                pb.visibility = GONE
                trackAdapter?.addData(response.body()?.results)
            }
            loading = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val component = DaggerActivityComponent.create()
        component.inject(this)
        getPopular(offset)
        rv.setHasFixedSize(true)
        rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                if (layoutManager.findLastVisibleItemPosition() == layoutManager.itemCount - 1) {
                    if (!loading) {
                        loading = true
                        pb.visibility = VISIBLE
                        offset += 25
                        if (searching) search(q, offset) else getPopular(offset)
                    }
                }
            }
        })
        setSupportActionBar(toolbar)
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                adView.visibility = VISIBLE
            }
        }
        adView.loadAd(AdRequest.Builder().build())
    }

    private fun getPopular(offset: Int) {
        manager.getPopular(offset, callback)
    }

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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 1 && (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            openMusic(null)
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
