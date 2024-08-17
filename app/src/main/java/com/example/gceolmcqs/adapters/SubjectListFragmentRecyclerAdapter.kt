package com.example.gceolmcqs.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gceolmcqs.R

class SubjectListFragmentRecyclerAdapter(
    private val context: Context,
    private val subjects: List<String>,
    private val clickListener: OnRecyclerViewItemClick
) : RecyclerView.Adapter<SubjectListFragmentRecyclerAdapter.ViewHolder>() {
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvRecyclerViewItem: TextView = view.findViewById(R.id.tvRecyclerViewItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.recycler_item_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvRecyclerViewItem.text = subjects[position]
        holder.tvRecyclerViewItem.setOnClickListener {
            clickListener.onRecyclerViewItemClicked(position)
        }
    }

    override fun getItemCount(): Int {
        return subjects.size
    }

    interface OnRecyclerViewItemClick {
        fun onRecyclerViewItemClicked(position: Int)
    }
}

