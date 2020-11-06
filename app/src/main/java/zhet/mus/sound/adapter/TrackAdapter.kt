package zhet.mus.sound.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_track.view.*
import zhet.mus.sound.R
import zhet.mus.sound.fragment.PlayerFragment
import zhet.mus.sound.model.Track
import zhet.mus.sound.util.TRACK
import zhet.mus.sound.util.milliSecondsToTime

class TrackAdapter(private val results: MutableList<Track>?) :
    RecyclerView.Adapter<TrackAdapter.VH>() {

    private val player: DialogFragment = PlayerFragment()

    override fun onCreateViewHolder(group: ViewGroup, p1: Int) =
        VH(LayoutInflater.from(group.context).inflate(R.layout.item_track, group, false))

    override fun getItemCount() = results?.size ?: 0

    override fun onBindViewHolder(holder: VH, position: Int) {
        val track = results?.get(position)
        val view = holder.itemView
        view.name.text = track?.title
        view.duration.text =
            view.context.getString(R.string.duration, milliSecondsToTime(track?.duration))
        view.uploadDate.text =
            view.context.getString(R.string.uploaded, track?.created_at?.replace("+0000", ""))
        Picasso.get().load(track?.artwork_url ?: track?.user?.avatar_url).into(view.img)
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
