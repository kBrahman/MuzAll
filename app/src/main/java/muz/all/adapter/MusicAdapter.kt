package muz.all.adapter

import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import muz.all.activity.MusicActivity
import muz.all.databinding.MusicItemBinding
import java.io.File


class MusicAdapter(private val list: Array<File>?, val onClick: (pos: Int) -> Unit) :
    RecyclerView.Adapter<MusicAdapter.MusicVH>() {

    companion object {
        private val TAG = MusicAdapter::class.java.simpleName
    }

    private val retriever = MediaMetadataRetriever()
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

                onClick(adapterPosition)
            }

            binding.root.setOnLongClickListener {
                val activity = it.context as MusicActivity
                activity.setFileAndMenuItemVisibility(list?.get(adapterPosition))
                true
            }
        }
    }
}