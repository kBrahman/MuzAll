package z.music.adapter

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import z.music.R
import z.music.databinding.TrackItemBinding
import z.music.fragment.PlayerFragment
import z.music.model.Track
import z.music.util.TRACK

class TrackAdapter(private val results: MutableList<Track>?) :
    RecyclerView.Adapter<TrackAdapter.VH>() {

    companion object {
        private val TAG = TrackAdapter::class.simpleName
    }

    private val player: DialogFragment = PlayerFragment()

    override fun onCreateViewHolder(group: ViewGroup, p1: Int): VH {
        val binding = TrackItemBinding.inflate(LayoutInflater.from(group.context), group, false)
        return VH(binding)
    }

    override fun getItemCount() = results?.size ?: 0

    override fun onBindViewHolder(holder: VH, position: Int) {
        val track = results?.get(position)
        val b = holder.binding
        b.name.text = track?.track
        b.duration.text =
            b.root.context.getString(R.string.duration, track?.duration)
        b.bitrate.text =
            b.root.context.getString(R.string.bitrate, track?.bitrate)
        if (track?.artistImageUrlSquare100 != null)
            Picasso.get().load(track.artistImageUrlSquare100).into(b.img)
        else b.img.setImageResource(R.mipmap.ic_launcher)
    }

    fun addData(data: List<Track>?) {
        data?.let {
            val start = results?.size ?: 0
            results?.addAll(it)
            notifyItemRangeInserted(start, results?.size ?: 0)
        }
    }

    inner class VH(val binding: TrackItemBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val showsDialog = player.showsDialog
                Log.i(TAG, "shows d=>$showsDialog")
//                if (showsDialog) {
//                    player.dismiss()
//                }
                val bundle = Bundle()
                bundle.putSerializable(TRACK, results?.get(adapterPosition))
                player.arguments = bundle
                player.show((it.context as AppCompatActivity).supportFragmentManager, "player")
            }
        }
    }

}
