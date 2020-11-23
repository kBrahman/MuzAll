package dev.mus.sound.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.mus.sound.databinding.ItemSelectionBinding
import dev.mus.sound.model.Selection

class SelectionsAdapter(
    private val selections: List<Selection>,
    private val click: (ids: String, name: String) -> Unit
) :
    RecyclerView.Adapter<SelectionsAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemSelectionBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val selection = selections[position]
        holder.binding.selectionName.text = selection.title
        holder.binding.rvHorizontal.adapter = PlayListAdapter(selection.items.collection, click)
    }

    override fun getItemCount() = selections.size

    class VH(val binding: ItemSelectionBinding) : RecyclerView.ViewHolder(binding.root)
}