package z.music.adapter

import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.music_item.view.*
import z.music.activity.MusicActivity
import z.music.fragment.PlayerFragment
import z.music.util.TRACK
import z.music.R
import java.io.File


class MusicAdapter(private val list: Array<File>) : RecyclerView.Adapter<MusicAdapter.MusicVH>() {

    companion object {
        private val TAG = MusicAdapter::class.java.simpleName
    }

    private val retriever = MediaMetadataRetriever()
    private val player = PlayerFragment()

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int) =
        MusicVH(LayoutInflater.from(p0.context).inflate(R.layout.music_item, p0, false))

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: MusicVH, i: Int) {
        val file = list[i]
        holder.itemView.musicName.text = file.name
        try {
            retriever.setDataSource(file.absolutePath)
        } catch (e: RuntimeException) {
            e.printStackTrace()
        }
        val data = retriever.embeddedPicture;
        if (data != null) {
            val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size);
            holder.itemView.musicImg.setImageBitmap(bitmap)
        }
    }

    inner class MusicVH(item: View) : RecyclerView.ViewHolder(item) {
        init {
            item.setOnClickListener {
                val bundle = Bundle()
                bundle.putSerializable(TRACK, list[adapterPosition])
                player.arguments = bundle
                player.show((it.context as AppCompatActivity).supportFragmentManager, "player")
            }

            item.setOnLongClickListener {
                val activity = it.context as MusicActivity
                activity.setFileAndMenuItemVisibility(list[adapterPosition])
                Log.i(TAG, "on long click")
                true
            }
        }
    }
}