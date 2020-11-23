package dev.mus.sound.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import dev.mus.sound.databinding.ItemPlaylistBinding
import dev.mus.sound.model.PlayList

class PlayListAdapter(
    private val playlists: List<PlayList>,
    private val click: (ids: String, name: String) -> Unit
) :
    RecyclerView.Adapter<PlayListAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemPlaylistBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val pl = playlists[position]
        holder.pos = position
        holder.binding.namePlaylist.text = pl.short_title
        Picasso.get().load(pl.calculated_artwork_url).into(holder.binding.imgPlayList)
    }

    override fun getItemCount() = playlists.size

    inner class VH(val binding: ItemPlaylistBinding) : RecyclerView.ViewHolder(binding.root) {
        var pos: Int = -1

        init {
            binding.root.setOnClickListener {
                val ids = playlists[pos].tracks.map { it.id }.joinToString(",")
                click(ids, binding.namePlaylist.text.toString())
            }
        }
    }
}