package muz.all.adapter

import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.media.MediaMetadataRetriever
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.music_item.view.*
import muz.all.R
import muz.all.activity.MusicActivity
import muz.all.fragment.PlayerFragment
import muz.all.util.TRACK
import java.io.File


class MusicAdapter(private val list: Array<File>?) : androidx.recyclerview.widget.RecyclerView.Adapter<MusicAdapter.MusicVH>() {

    companion object {
        private val TAG = MusicAdapter::class.java.simpleName
    }

    private val retriever = MediaMetadataRetriever()
    private val player = PlayerFragment()

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int) =
        MusicVH(LayoutInflater.from(p0.context).inflate(R.layout.music_item, p0, false))

    override fun getItemCount() = list?.size ?: 0

    override fun onBindViewHolder(holder: MusicVH, i: Int) {
        val file = list?.get(i)
        holder.itemView.musicName.text = file?.name
        try {
            retriever.setDataSource(file?.absolutePath)
        } catch (e: RuntimeException) {
            e.printStackTrace()
        }
        val data = retriever.embeddedPicture;
        if (data != null) {
            val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
            holder.itemView.imgMusicItem.setImageBitmap(bitmap)
//            holder.itemView.imgMusicItem.bitmap = bitmap
//            holder.itemView.imgMusicItem.draw(Canvas())
        }
    }

    inner class MusicVH(item: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(item) {
        init {
            item.setOnClickListener {
                val bundle = Bundle()
                bundle.putSerializable(TRACK, list?.get(adapterPosition))
                player.arguments = bundle
                player.show((it.context as AppCompatActivity).supportFragmentManager, "player")
            }

            item.setOnLongClickListener {
                val activity = it.context as MusicActivity
                activity.setFileAndMenuItemVisibility(list?.get(adapterPosition))
                Log.i(TAG, "on long click")
                true
            }
        }
    }
}