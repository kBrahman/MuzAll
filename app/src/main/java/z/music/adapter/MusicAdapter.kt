package z.music.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import z.music.R
import z.music.activity.MusicActivity
import z.music.databinding.MusicItemBinding
import z.music.fragment.PlayerFragment
import z.music.model.Track
import z.music.util.TRACK


class MusicAdapter(private val list: List<Track>) : RecyclerView.Adapter<MusicAdapter.MusicVH>() {

    companion object {
        private val TAG = MusicAdapter::class.java.simpleName
    }

    private val player = PlayerFragment()

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int) =
        MusicVH(MusicItemBinding.inflate(LayoutInflater.from(p0.context), p0, false))

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: MusicVH, i: Int) {
        val track = list[i]
        holder.binding.musicName.text = track.track
        if (track.artistImageUrlSquare100 != null)
            Picasso.get().load(track.artistImageUrlSquare100).into(holder.binding.musicImg)
        else holder.binding.musicImg.setImageResource(R.mipmap.ic_launcher)
    }

    inner class MusicVH(val binding: MusicItemBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val bundle = Bundle()
                bundle.putSerializable(TRACK, list[adapterPosition])
                player.arguments = bundle
                player.show((it.context as AppCompatActivity).supportFragmentManager, "player")
            }

            binding.root.setOnLongClickListener {
                val activity = it.context as MusicActivity
                activity.setFileAndMenuItemVisibility(list[adapterPosition])
                true
            }
        }
    }
}