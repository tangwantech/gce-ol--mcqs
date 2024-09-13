package com.example.gceolmcqs.adapters

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.gceolmcqs.R
//import com.example.gceolmcq.fragments.OnSectionAnsweredListener

class SectionNavigationRecyclerViewAdapter(
    private val context: Context,
    private val listSections: Array<Bundle>,
    private val listener: OnRecyclerItemClickListener,
    private val sectionsAnswered: List<Boolean>
) :
    RecyclerView.Adapter<SectionNavigationRecyclerViewAdapter.ViewHolder>() {

    private var sectionScores: ArrayList<Int>? = null

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val tvSectionNavItem: TextView = view.findViewById(R.id.tvSectionNavItem)
        val tvSectionNumberOfQuestions: TextView = view.findViewById(R.id.tvSectionNumberOfQuestions)
        val sectionNavItemLayout: LinearLayout = view.findViewById(R.id.sectionNavItemLayout)
//        val imgSectionAnsweredCheck: ImageView = view.findViewById(R.id.imgSectionAnsweredCheck)
        val scoreLo: LinearLayout = view.findViewById(R.id.scoreLo)
        val tvSectionScore: TextView = view.findViewById(R.id.tvSectionScore)

        init{
            sectionNavItemLayout.setOnClickListener{
                listener.onRecyclerItemClick(this.adapterPosition)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.section_nav_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvSectionNavItem.text = listSections[position].getString("sectionName")
        holder.tvSectionNumberOfQuestions.text = "Number of questions: ${listSections[position].getString("numberOfQuestions")}"

        if(sectionsAnswered[position]){
            val numberOfQuestionsInSection = listSections[position].getString("numberOfQuestions")?.toInt()
            val sectionScore = sectionScores!![position]
            val scorePercentage =(( sectionScore.toDouble() / numberOfQuestionsInSection!!.toDouble()) * 100).toInt()

            holder.scoreLo.visibility = View.VISIBLE
//            holder.tvSectionScore.visibility = View.VISIBLE
            holder.tvSectionScore.text = "$sectionScore/$numberOfQuestionsInSection"

            if (scorePercentage > 50){
                holder.tvSectionScore.setTextColor(context.resources.getColor(R.color.color_green))
            }else{
                holder.tvSectionScore.setTextColor(context.resources.getColor(R.color.color_red))
            }
//            holder.sectionNavItemLayout.isEnabled = false



        }else{
            holder.scoreLo.visibility = View.GONE
        }


    }

    override fun getItemCount(): Int {
        return listSections.size
    }

    fun updateSectionScore(sectionScores: ArrayList<Int>){
        this.sectionScores = sectionScores
    }

    interface OnRecyclerItemClickListener{
        fun onRecyclerItemClick(position: Int)
    }
}