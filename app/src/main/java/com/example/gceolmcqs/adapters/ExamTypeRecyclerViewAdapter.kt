package com.example.gceolmcqs.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gceolmcqs.R
import com.example.gceolmcqs.datamodels.ExamItemData


class ExamTypeRecyclerViewAdapter(
    private val context: Context,
    private val examItemsData: ArrayList<ExamItemData>,
    private val listener: OnRecyclerItemClickListener
    ) : RecyclerView.Adapter<ExamTypeRecyclerViewAdapter.ViewHolder>(){


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        val tvItem: TextView = view.findViewById(R.id.tvRecyclerViewItem)
        private val crossFade = AnimationUtils.loadAnimation(context, R.anim.cross_fade)
        init {
            tvItem.setOnClickListener {
                listener.onRecyclerItemClick(adapterPosition)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(context).inflate(R.layout.recycler_item_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
      holder.tvItem.text = examItemsData[position].title
    }

    override fun getItemCount(): Int {
        return examItemsData.size
    }

    interface OnRecyclerItemClickListener{
        fun onRecyclerItemClick(position: Int)
    }

}