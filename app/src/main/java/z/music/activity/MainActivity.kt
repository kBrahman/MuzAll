package z.music.activity

//import com.facebook.ads.*
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import z.music.R
import z.music.adapter.TrackAdapter
import z.music.manager.ApiManager
import z.music.model.CollectionHolder
import z.music.model.Selection
import z.music.model.Token
import z.music.model.Track
import z.music.util.TOKEN
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.inject.Inject


class MainActivity : DaggerAppCompatActivity() {

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    //    lateinit var adView: AdView
    private var timeOut = false

    //    var ad: InterstitialAd? = null
    private lateinit var q: String

    @Inject
    lateinit var manager: ApiManager

    @Inject
    lateinit var sharedPreferences: SharedPreferences
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
            val collection = response.body()?.collection?.filter { it.media != null }
            if (trackAdapter == null && timeOut) {
                trackAdapter = TrackAdapter(collection?.toMutableList())
                rv.adapter = trackAdapter
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
                val tracks = found?.items?.collection?.get(0)?.tracks
                val ids = tracks?.map { it.id }?.joinToString(",")
                manager.tracksBy(ids, topTrackCallback)
            }
        }

    private val tokenClb = object : Callback<Token> {
        override fun onResponse(call: Call<Token>, response: Response<Token>) {


        }

        override fun onFailure(call: Call<Token>, t: Throwable) = t.printStackTrace()


    }

    private fun getAccessToken(token: String?): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append(token)
        stringBuilder.append(
            xor(
                String(Base64.decode("NQxQBQICUCEZHVY=", 0)),
                "sdfr34egth4523de2e"
            )
        )
        return cipher(stringBuilder.toString());
    }

    fun cipher(paramString: String): String {
        var paramString = paramString
        return try {
            val messageDigest = MessageDigest.getInstance("MD5")
            messageDigest.update(paramString.toByteArray())
            val arrayOfByte: ByteArray = messageDigest.digest()
            val stringBuffer = StringBuffer()
            for (i in arrayOfByte.indices) {
                paramString = Integer.toHexString(arrayOfByte[i].toInt() and 0xFF)
                while (paramString.length < 2) {
                    val stringBuilder = java.lang.StringBuilder()
                    stringBuilder.append("0")
                    stringBuilder.append(paramString)
                    paramString = stringBuilder.toString()
                }
                stringBuffer.append(paramString)
            }
            stringBuffer.toString()
        } catch (noSuchAlgorithmException: NoSuchAlgorithmException) {
            //            h.a(a, noSuchAlgorithmException);
            ""
        }
    }

    private fun xor(paramString1: String, paramString2: String): String? {
        val arrayOfChar1 = paramString1.toCharArray()
        val arrayOfChar2 = paramString2.toCharArray()
        val j = arrayOfChar1.size
        val k = arrayOfChar2.size
        val arrayOfChar3 = CharArray(j)
        for (i in 0 until j) arrayOfChar3[i] =
            (arrayOfChar1[i].toInt() xor arrayOfChar2[i % k].toInt()).toChar()
        return String(arrayOfChar3)
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
            if (timeOut) {
                setAdapterAndBanner()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val token = sharedPreferences.getString(TOKEN, null)
        if (token == null) {
            getToken()
        } else {
            getTop()
        }
        setContentView(R.layout.activity_main)
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
        setTimer()
//        AudienceNetworkAds.initialize(this)
//        ad = InterstitialAd(this, getString(R.string.int_id))
//        adView = AdView(this, getString(R.string.fb_banner_id), AdSize.BANNER_HEIGHT_50)
//        bannerContainer.addView(adView)
//        ad?.setAdListener(object : InterstitialAdListener {
//            override fun onInterstitialDisplayed(ad: Ad) {
//                // Interstitial ad displayed callback
//                Log.e(TAG, "Interstitial ad displayed.")
//            }
//
//            override fun onInterstitialDismissed(ad: Ad) {
//                // Interstitial dismissed callback
//                Log.e(TAG, "Interstitial ad dismissed.")
//            }
//
//            override fun onError(ad: Ad, adError: AdError) {
//                // Ad error callback
//                Log.e(
//                    TAG,
//                    "Interstitial ad failed to load: " + adError.errorMessage
//                )
//            }
//
//            override fun onAdLoaded(ad: Ad) {
//                // Interstitial ad is loaded and ready to be displayed
//                Log.d(
//                    TAG,
//                    "Interstitial ad is loaded and ready to be displayed!"
//                )
//                // Show the ad
//                this@MainActivity.ad?.show()
//            }
//
//            override fun onAdClicked(ad: Ad) {
//                // Ad clicked callback
//                Log.d(TAG, "Interstitial ad clicked!")
//            }
//
//            override fun onLoggingImpression(ad: Ad) {
//                // Ad impression logged callback
//                Log.d(TAG, "Interstitial ad impression logged!")
//            }
//        })
//        ad?.loadAd();
    }

    private fun getToken() = manager.getToken().subscribe(::onToken) { e -> e.printStackTrace() }

    private fun onToken(token: Token) {
        val t = token.token
        val accessToken = getAccessToken(t)
        Log.i(TAG, "token=>$t,  acc_token=>$accessToken")
//        manager.auth(t, accessToken)
    }

    private fun setTimer() {
        Timer().schedule(object : TimerTask() {
            override fun run() {
                timeOut = true
                if (trackAdapter != null) {
                    runOnUiThread {
                        setAdapterAndBanner()
//                        ad = null
                    }
                }
                Log.i(TAG, "time out")
            }
        }, 7000L)
    }

    private fun setAdapterAndBanner() {
        rv.adapter = trackAdapter
        pb.visibility = GONE
//        adView.loadAd()
    }

    private fun getTop() = manager.getMixedSelections(selectionsCallback)


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
