package dev.mus.sound.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_selection.view.*
import dev.mus.sound.R
import dev.mus.sound.model.Selection

class SelectionsAdapter(
    private val selections: List<Selection>,
    private val click: (ids: String, name: String) -> Unit
) :
    RecyclerView.Adapter<SelectionsAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        LayoutInflater.from(parent.context).inflate(
            R.layout.item_selection, parent, false
        )
    )

    override fun onBindViewHolder(holder: VH, position: Int) {
        val selection = selections[position]
        holder.itemView.selectionName.text = selection.title
        val get = selection.items.collection
        holder.itemView.rvHorizontal.adapter = PlayListAdapter(selection.items.collection, click)
    }

    override fun getItemCount() = selections.size

    class VH(v: View) : RecyclerView.ViewHolder(v)
}