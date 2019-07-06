package muz.all.adapter

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.track_item.view.*
import muz.all.R
import muz.all.fragment.PlayerFragment
import muz.all.model.Track
import muz.all.util.TRACK

class TrackAdapter(private val results: MutableList<Track>?) : androidx.recyclerview.widget.RecyclerView.Adapter<TrackAdapter.VH>() {

    private val player: DialogFragment = PlayerFragment()

    override fun onCreateViewHolder(group: ViewGroup, p1: Int) =
        VH(LayoutInflater.from(group.context).inflate(R.layout.track_item, group, false))

    override fun getItemCount() = results?.size ?: 0

    override fun onBindViewHolder(holder: VH, position: Int) {
        val track = results?.get(position)
        val view = holder.itemView
        view.name.text = track?.name
        view.artist.text = track?.artist_name
        view.duration.text = view.context.getString(R.string.duration, track?.duration)
        view.releaseDate.text = view.context.getString(R.string.released, track?.releasedate)
        Picasso.get().load(track?.image).into(view.img)
    }

    fun addData(data: List<Track>?) {
        data?.let {
            val start = results?.size ?: 0
            results?.addAll(it)
            notifyItemRangeInserted(start, results?.size ?: 0)
        }
    }

    fun getAll() = results

    inner class VH(item: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(item) {
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
