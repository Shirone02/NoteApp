package com.example.noteapp.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapp.R
import com.example.noteapp.listeners.OnItemClickListener
import com.example.noteapp.models.Note

class ListNoteAdapter(
    private val onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<ListNoteAdapter.viewholder>() {

    private val differCallBack = object : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem.id == newItem.id && oldItem.content == newItem.content && oldItem.title == newItem.title
        }

        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallBack)

    inner class viewholder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var time: TextView
        var title: TextView
        var content: TextView

        init {
            title = itemView.findViewById(R.id.tvTitle)
            time = itemView.findViewById(R.id.tvTime)
            content = itemView.findViewById(R.id.tvContent)
        }
    }

    private val selectedItems = mutableSetOf<Note>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListNoteAdapter.viewholder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
        return viewholder(itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ListNoteAdapter.viewholder, position: Int) {
        holder.title.text = differ.currentList[position].title
        if (differ.currentList[position].content.isEmpty()) {
            holder.content.visibility = View.GONE
        } else {
            holder.content.text = differ.currentList[position].content
        }
        holder.time.text = "Last edit: " + differ.currentList[position].time

        holder.itemView.isSelected = selectedItems.contains(differ.currentList[position])

        holder.itemView.setOnClickListener {
            onItemClickListener.onNoteClick(
                differ.currentList[position],
                holder.itemView.isSelected
            )

            if (holder.itemView.isSelected) {
                if (selectedItems.contains(differ.currentList[position])) {
                    selectedItems.remove(differ.currentList[position])
                    holder.itemView.isSelected = false
                }
                notifyItemChanged(position)  // Cập nhật lại item
            }
        }

        holder.itemView.setOnLongClickListener {
            onItemClickListener.onNoteLongClick(differ.currentList[position])
            toggleSelection(differ.currentList[position])
            notifyItemChanged(position)
            true

        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun removeSelectedItems() {
        val newList = differ.currentList.toMutableList().apply {
            removeAll(selectedItems)
        }
        differ.submitList(newList)
        selectedItems.clear()
    }

    fun getSelectedItemsCount(): Int{
        return selectedItems.size + 1
    }

    fun getSelectedItems(): Set<Note> {
        return selectedItems
    }

    fun clearSelection() {
        selectedItems.clear()
        notifyDataSetChanged()
    }

    fun selectAllItem() {
        if (selectedItems.isEmpty()) {
            selectedItems.addAll(differ.currentList)
        } else {
            if (selectedItems.size < differ.currentList.size) {
                selectedItems.clear()
                selectedItems.addAll(differ.currentList)
            } else {
                selectedItems.clear()
            }
        }
        notifyDataSetChanged()
    }

    private fun toggleSelection(note: Note) {
        if (selectedItems.contains(note)) {
            selectedItems.remove(note)
        } else {
            selectedItems.add(note)
        }
        notifyDataSetChanged()
    }

}