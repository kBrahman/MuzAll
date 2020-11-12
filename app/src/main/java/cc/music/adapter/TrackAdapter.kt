package cc.music.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import cc.music.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.track_item.view.*
import cc.music.fragment.PlayerFragment
import cc.music.model.Track
import cc.music.util.TRACK

class TrackAdapter(private val results: MutableList<Track>?) :
    RecyclerView.Adapter<TrackAdapter.VH>() {

    private val player: DialogFragment = PlayerFragment()

    override fun onCreateViewHolder(group: ViewGroup, p1: Int) =
        VH(LayoutInflater.from(group.context).inflate(R.layout.track_item, group, false))

    override fun getItemCount() = results?.size ?: 0

    override fun onBindViewHolder(holder: VH, position: Int) {
        val track = results?.get(position)
        val view = holder.itemView
        view.name.text = track?.upload_name
        view.artist.text = track?.user_name
        view.duration.text = view.context.getString(R.string.duration, track?.duration)
//        view.releaseDate.text = view.context.getString(R.string.released, track?.releasedate)
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

    inner class VH(item: View) : RecyclerView.ViewHolder(item) {
        init {
            player.showsDialog = false
            item.setOnClickListener {
                val showsDialog = player.showsDialog
                if (showsDialog) {
                    player.dismiss()
                    player.showsDialog = false
                }
                val bundle = Bundle()
                bundle.putSerializable(TRACK, results?.get(adapterPosition))
                player.arguments = bundle
                player.show((it.context as AppCompatActivity).supportFragmentManager, "player")
                player.showsDialog = true
            }
        }
    }
}