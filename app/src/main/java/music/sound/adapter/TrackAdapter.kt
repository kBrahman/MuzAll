package music.sound.adapter

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.track_item.view.*
import music.sound.R
import music.sound.fragment.PlayerFragment
import music.sound.model.Track
import music.sound.util.TRACK
import music.sound.util.milliSecondsToTime

class TrackAdapter(private val results: MutableList<Track>?) : androidx.recyclerview.widget.RecyclerView.Adapter<TrackAdapter.VH>() {

    private val player: DialogFragment = PlayerFragment()

    override fun onCreateViewHolder(group: ViewGroup, p1: Int) =
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN)
            VH(LayoutInflater.from(group.context).inflate(R.layout.track_item, group, false))
        else VH(LayoutInflater.from(group.context).inflate(R.layout.track_item_api_16, group, false))

    override fun getItemCount() = results?.size ?: 0

    override fun onBindViewHolder(holder: VH, position: Int) {
        val track = results?.get(position)
        val view = holder.itemView
        view.name.text = track?.title
        view.duration.text = view.context.getString(R.string.duration, milliSecondsToTime(track?.duration))
        view.uploadDate.text = view.context.getString(R.string.uploaded, track?.created_at?.replace("+0000", ""))
        Picasso.get().load(track?.artwork_url ?: track?.user?.avatar_url).into(view.img)
    }

    fun addData(data: List<Track>?) {
        data?.let {
            val start = results?.size ?: 0
            results?.addAll(it)
            notifyItemRangeInserted(start, results?.size ?: 0)
        }
    }

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
