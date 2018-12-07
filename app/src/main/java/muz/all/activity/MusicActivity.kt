package muz.all.activity

import android.app.Activity
import android.os.Bundle
import android.os.Environment.DIRECTORY_MUSIC
import android.os.Environment.getExternalStoragePublicDirectory
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_music.*
import muz.all.R
import muz.all.adapter.MusicAdapter

class MusicActivity : AppCompatActivity() {
    companion object {
        private val TAG = MusicActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music)
        val files = getExternalStoragePublicDirectory(DIRECTORY_MUSIC).listFiles()
        Log.i(TAG, files.size.toString())
        rvMusic.setHasFixedSize(true)
        rvMusic.adapter = MusicAdapter(files)
    }
}
