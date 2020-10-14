package z.music.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.track_item.view.*
import z.music.R
import z.music.fragment.PlayerFragment
import z.music.model.Track
import z.music.util.TRACK

class TrackAdapter(private val results: MutableList<Track>?) :
    RecyclerView.Adapter<TrackAdapter.VH>() {

    private val player: DialogFragment = PlayerFragment()

    override fun onCreateViewHolder(group: ViewGroup, p1: Int) =
        VH(LayoutInflater.from(group.context).inflate(R.layout.track_item, group, false))

    override fun getItemCount() = results?.size ?: 0

    override fun onBindViewHolder(holder: VH, position: Int) {
        val track = results?.get(position)
        val view = holder.itemView
        view.name.text = track?.track
        view.duration.text =
            view.context.getString(R.string.duration, track?.duration)
        view.bitrate.text =
            view.context.getString(R.string.bitrate, track?.bitrate)
        if (track?.artistImageUrlSquare100 != null)
            Picasso.get().load(track.artistImageUrlSquare100).into(view.img)
    }

    fun addData(data: List<Track>?) {
        data?.let {
            val start = results?.size ?: 0
            results?.addAll(it)
            notifyItemRangeInserted(start, results?.size ?: 0)
        }
    }

    inner class VH(item: View) : RecyclerView.ViewHolder(item) {
        init {
            item.setOnClickListener {
                if (player.fragmentManager != null && player.showsDialog) {
                    player.dismiss()
                }
                val bundle = Bundle()
                bundle.putSerializable(TRACK, results?.get(adapterPosition))
                player.arguments = bundle
                player.show((it.context as AppCompatActivity).supportFragmentManager, "player")
            }
        }
    }

}
