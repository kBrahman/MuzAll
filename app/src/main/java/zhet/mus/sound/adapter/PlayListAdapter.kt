package zhet.mus.sound.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_playlist.view.*
import zhet.mus.sound.R
import zhet.mus.sound.model.PlayList

class PlayListAdapter(
    private val playlists: List<PlayList>,
    private val click: (ids: String, name: String) -> Unit
) :
    RecyclerView.Adapter<PlayListAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        LayoutInflater.from(parent.context).inflate(
            R.layout.item_playlist, parent, false
        )
    )

    override fun onBindViewHolder(holder: VH, position: Int) {
        val pl = playlists[position]
        holder.pos = position
        holder.itemView.namePlaylist.text = pl.short_title
        Picasso.get().load(pl.calculated_artwork_url).into(holder.itemView.imgPlayList)
    }

    override fun getItemCount() = playlists.size

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        var pos: Int = -1

        init {
            v.setOnClickListener {
                val ids = playlists[pos].tracks.map { it.id }.joinToString(",")
                click(ids, it.namePlaylist.text.toString())
            }
        }
    }
}