package cc.music.adapter

import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import cc.music.activity.MusicActivity
import cc.music.databinding.MusicItemBinding
import cc.music.fragment.PlayerFragment
import cc.music.util.TRACK
import java.io.File


class MusicAdapter(private val list: Array<File>?) :
    RecyclerView.Adapter<MusicAdapter.MusicVH>() {

    companion object {
        private val TAG = MusicAdapter::class.java.simpleName
    }

    private val retriever = MediaMetadataRetriever()
    private val player = PlayerFragment()

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int) =
        MusicVH(MusicItemBinding.inflate(LayoutInflater.from(p0.context), p0, false))

    override fun getItemCount() = list?.size ?: 0

    override fun onBindViewHolder(holder: MusicVH, i: Int) {
        val file = list?.get(i)
        holder.binding.musicName.text = file?.name
        try {
            retriever.setDataSource(file?.absolutePath)
        } catch (e: RuntimeException) {
            e.printStackTrace()
        }
        val data = retriever.embeddedPicture;
        if (data != null) {
            val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
            holder.binding.imgMusicItem.setImageBitmap(bitmap)
        }
    }

    inner class MusicVH(val binding: MusicItemBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val bundle = Bundle()
                bundle.putSerializable(TRACK, list?.get(adapterPosition))
                player.arguments = bundle
                player.show((it.context as AppCompatActivity).supportFragmentManager, "player")
            }

            binding.root.setOnLongClickListener {
                val activity = it.context as MusicActivity
                activity.setFileAndMenuItemVisibility(list?.get(adapterPosition))
                true
            }
        }
    }
}