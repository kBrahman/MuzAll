package dev.mus.sound.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import dev.mus.sound.R
import dev.mus.sound.databinding.ItemTrackBinding
import dev.mus.sound.model.Track
import dev.mus.sound.util.milliSecondsToTime

class TrackAdapter(
    private val results: MutableList<Track>?,
    private val onClick: (track: Track?) -> Unit
) :
    RecyclerView.Adapter<TrackAdapter.VH>() {
    override fun onCreateViewHolder(group: ViewGroup, p1: Int) =
        VH(ItemTrackBinding.inflate(LayoutInflater.from(group.context), group, false))

    override fun getItemCount() = results?.size ?: 0

    override fun onBindViewHolder(holder: VH, position: Int) {
        val track = results?.get(position)
        val binding = holder.binding
        binding.name.text = track?.title
        binding.duration.text =
            binding.root.context.getString(R.string.duration, milliSecondsToTime(track?.duration))
        binding.uploadDate.text =
            binding.root.context.getString(
                R.string.uploaded,
                track?.created_at?.replace("+0000", "")
            )
        Picasso.get().load(track?.artwork_url ?: track?.user?.avatar_url).into(binding.img)
    }

    fun addData(data: List<Track>?) {
        data?.let {
            val start = results?.size ?: 0
            results?.addAll(it)
            notifyItemRangeInserted(start, results?.size ?: 0)
        }
    }

    inner class VH(val binding: ItemTrackBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                onClick(results?.get(adapterPosition))
//                val bundle = Bundle()
//                bundle.putSerializable(TRACK, results?.get(adapterPosition))
//                player.arguments = bundle
//                player.show((it.context as AppCompatActivity).supportFragmentManager, "player")
            }
        }
    }

}
