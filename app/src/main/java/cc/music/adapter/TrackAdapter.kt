package cc.music.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import cc.music.R
import cc.music.databinding.TrackItemBinding
import cc.music.fragment.PlayerFragment
import cc.music.model.Track
import cc.music.util.TRACK
import com.squareup.picasso.Picasso

class TrackAdapter(private val results: MutableList<Track>?) :
    RecyclerView.Adapter<TrackAdapter.VH>() {

    private val player: DialogFragment = PlayerFragment()

    override fun onCreateViewHolder(group: ViewGroup, p1: Int) =
        VH(TrackItemBinding.inflate(LayoutInflater.from(group.context), group, false))

    override fun getItemCount() = results?.size ?: 0

    override fun onBindViewHolder(holder: VH, position: Int) {
        val track = results?.get(position)
        val binding = holder.binding
        binding.name.text = track?.upload_name
        binding.artist.text = track?.user_name
        binding.duration.text = binding.root.context.getString(R.string.duration, track?.duration)
//        view.releaseDate.text = view.context.getString(R.string.released, track?.releasedate)
        Picasso.get().load(track?.image).into(binding.img)
    }

    fun addData(data: List<Track>?) {
        data?.let {
            val start = results?.size ?: 0
            results?.addAll(it)
            notifyItemRangeInserted(start, results?.size ?: 0)
        }
    }

    fun getAll() = results

    inner class VH(val binding: TrackItemBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            player.showsDialog = false
            binding.root.setOnClickListener {
                val showsDialog = player.showsDialog
                if (showsDialog) {
                    player.dismiss()
                    player.showsDialog = false
                }
                val bundle = Bundle()
                bundle.putSerializable(TRACK, results?.get(adapterPosition))
                player.arguments = bundle
                player.show(
                    (it.context as AppCompatActivity).supportFragmentManager,
                    "player"
                )
                player.showsDialog = true
            }
        }
    }
}
