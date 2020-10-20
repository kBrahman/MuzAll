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
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.WindowManager
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import z.music.BuildConfig
import z.music.R
import z.music.adapter.TrackAdapter
import z.music.err.TokenExpiredException
import z.music.manager.ApiManager
import z.music.model.Token
import z.music.model.TrackList
import z.music.util.TOKEN
import z.music.util.isNetworkAvailable
import java.io.IOException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.inject.Inject


class MainActivity : DaggerAppCompatActivity() {

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    private var currentToken: String? = null

    //    lateinit var adView: AdView
    private var timeOut = false

    //    var ad: InterstitialAd? = null
    private lateinit var q: String

    @Inject
    lateinit var manager: ApiManager

    @Inject
    lateinit var sharedPreferences: SharedPreferences
    private var loading = false
    private var trackAdapter: TrackAdapter? = null
    private var searching = false

    private fun getHash(token: String?): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append(token)
        stringBuilder.append(
            xor(
                String(Base64.decode(BuildConfig.C_EQ, 0)),
                BuildConfig.C
            )
        )
        return cipher(stringBuilder.toString());
    }

    private fun cipher(paramString: String): String {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isNetworkAvailable(this)) {
            init()
        } else {
            setContentView(R.layout.activity_no_inet)
        }
    }

    fun refresh(view: View) {
        if (isNetworkAvailable(this)) init()
    }


    private fun init() {
        currentToken = sharedPreferences.getString(TOKEN, null)
        Log.i(TAG, "tok=>$currentToken")
        if (currentToken == null) {
            getToken()
        } else {
            getTop(currentToken!!)
        }
        setContentView(R.layout.activity_main)
        rv.setHasFixedSize(true)
        rv.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrolled(
                recyclerView: RecyclerView,
                dx: Int,
                dy: Int
            ) {
                val layoutManager =
                    recyclerView.layoutManager as LinearLayoutManager
                if (layoutManager.findLastVisibleItemPosition() == layoutManager.itemCount - 1 && !loading) {
                    pb.visibility = VISIBLE
                    loading = true
                    if (searching) {
                        search(q, currentToken!!)
                    } else {
                        getTop(currentToken!!)
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

    private fun onToken(token: Token) =
        manager.getAccessToken(token.token, getHash(token.token)).subscribe(::onAccessToken)


    private fun onAccessToken(token: Token) {
        this.currentToken = token.token
        sharedPreferences.edit().putString(TOKEN, this.currentToken).apply()
        getTop(currentToken!!)
    }

    private fun onResult(result: TrackList) {
        Log.i(TAG, "list=>$result")
        loading = result.page == result.pagesCount
        val tracks = result.tracks.filter { it.playbackEnabled }
        if (trackAdapter == null && timeOut) {
            trackAdapter = TrackAdapter(tracks.toMutableList())
            rv.adapter = trackAdapter
            pb.visibility = GONE
        } else if (trackAdapter == null) {
            trackAdapter = TrackAdapter(tracks.toMutableList())
        } else {
            trackAdapter?.addData(tracks)
            pb.visibility = GONE
        }
//        if (timeOut) {
//            setAdapterAndBanner()
//        }
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

    private fun getTop(token: String) =
        manager.getTop(token, ((trackAdapter?.itemCount ?: 0) / 20 + 1))
            .subscribe(::onResult, ::onErr)

    private fun onErr(t: Throwable) = when (t) {
        is TokenExpiredException -> onTokenExpired()
        else -> throw IOException()
    }


    private fun onTokenExpired() {
        Log.i(TAG, "onTokenExpired")
        getToken().dispose()
        getToken()
    }

    private fun search(q: String, token: String) =
        manager.search(q, token, ((trackAdapter?.itemCount ?: 0) / 20 + 1))?.subscribe(::onResult)


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        (menu?.findItem(R.id.action_search)?.actionView as SearchView)
            .setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(q: String): Boolean {
                    if (q.isNotBlank()) {
                        this@MainActivity.q = q
                        trackAdapter = null
                        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                        pb.visibility = VISIBLE
                        search(q, currentToken!!)
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
