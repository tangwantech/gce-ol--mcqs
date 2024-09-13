package com.example.gceolmcqs.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gceolmcqs.MCQConstants
import com.example.gceolmcqs.R
import com.example.gceolmcqs.ResourceImages
import com.example.gceolmcqs.databinding.QuestionCardItemBinding
import com.example.gceolmcqs.datamodels.QuestionData

class SectionQuestionsRecyclerAdapter(private val context: Context, val title: String,
                                      private var questions: ArrayList<QuestionData>,
                                      private val onAlternativeItemRadioButtonCheckStateChangeListener: OnAlternativeItemRadioButtonCheckStateChangeListener): RecyclerView.Adapter<SectionQuestionsRecyclerAdapter.ViewHolder>() {


    inner class ViewHolder(val binding: QuestionCardItemBinding): RecyclerView.ViewHolder(binding.root){
        init {
            binding.selectableOptionsLayout.rbSelectableOption1.setOnCheckedChangeListener { _, state ->
                if (state) {
                    onAlternativeItemRadioButtonCheckStateChangeListener.onAlternativeItemRadioButtonCheckStateChanged(adapterPosition, 0)
                }
            }

            binding.selectableOptionsLayout.rbSelectableOption2.setOnCheckedChangeListener { _, state ->
                if (state) {
                    onAlternativeItemRadioButtonCheckStateChangeListener.onAlternativeItemRadioButtonCheckStateChanged(adapterPosition, 1)
                }
            }

            binding.selectableOptionsLayout.rbSelectableOption3.setOnCheckedChangeListener { _, state ->
                if (state) {
                    onAlternativeItemRadioButtonCheckStateChangeListener.onAlternativeItemRadioButtonCheckStateChanged(adapterPosition, 2)
                }
            }

            binding.selectableOptionsLayout.rbSelectableOption4.setOnCheckedChangeListener { _, state ->
                if (state) {
                    onAlternativeItemRadioButtonCheckStateChangeListener.onAlternativeItemRadioButtonCheckStateChanged(adapterPosition, 3)
                }
            }


        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding = QuestionCardItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return questions.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        setupQuestionCardItems(position, holder.binding)
    }

    private fun setupQuestionCardItems(adapterPosition: Int, binding: QuestionCardItemBinding){

        binding.correctAnswerLayout.root.visibility = View.GONE
        binding.explanationBtnLayout.root.visibility = View.GONE
        binding.correctAnswerLayout.root.visibility = View.GONE
        binding.userMarkedAnswerLayout.root.visibility = View.GONE

        if (questions[adapterPosition].question == null) {
            binding.questionLayout.main.visibility = View.GONE
        }else{
            binding.questionLayout.main.visibility = View.VISIBLE
            binding.questionLayout.tvQuestion.text = context.getString(R.string.question_value, (adapterPosition + 1).toString(), questions[adapterPosition].question)
        }

        if (questions[adapterPosition].image == null) {
            binding.imageCardLayout.root.visibility = View.GONE
        } else {
            binding.imageCardLayout.root.visibility = View.VISIBLE
            binding.imageCardLayout.imgView.setImageResource(ResourceImages.images[questions[adapterPosition].image]!!)

        }

        if (questions[adapterPosition].twoStatements == null) {
            binding.twoStatementsLayout.root.visibility = View.GONE
        } else {
            binding.twoStatementsLayout.root.visibility = View.VISIBLE
            binding.questionLayout.main.visibility = View.VISIBLE
            binding.questionLayout.tvQuestion.text  = "Question ${adapterPosition + 1}"
            binding.twoStatementsLayout.tvFirstStatement.text = questions[adapterPosition].twoStatements!![0]
            binding.twoStatementsLayout.tvSecondStatement.text = questions[adapterPosition].twoStatements!![1]

        }

        if (questions[adapterPosition].nonSelectableOptions == null) {
           binding.nonSelectableOptionsLayout.root.visibility = View.GONE
        } else {
            binding.nonSelectableOptionsLayout.root.visibility = View.VISIBLE
            binding.nonSelectableOptionsLayout.tvNonSelectableOption1.text = questions[adapterPosition].nonSelectableOptions!![0]
            binding.nonSelectableOptionsLayout.tvNonSelectableOption2.text = questions[adapterPosition].nonSelectableOptions!![1]
            binding.nonSelectableOptionsLayout.tvNonSelectableOption3.text = questions[adapterPosition].nonSelectableOptions!![2]

        }

        binding.selectableOptionsLayout.rbSelectableOption1.text = context.getString(R.string.selectable_alternative, MCQConstants.A, questions[adapterPosition].selectableOptions[0])
        binding.selectableOptionsLayout.rbSelectableOption2.text = context.getString(R.string.selectable_alternative, MCQConstants.B, questions[adapterPosition].selectableOptions[1])
        binding.selectableOptionsLayout.rbSelectableOption3.text = context.getString(R.string.selectable_alternative, MCQConstants.C, questions[adapterPosition].selectableOptions[2])
        binding.selectableOptionsLayout.rbSelectableOption4.text = context.getString(R.string.selectable_alternative, MCQConstants.D, questions[adapterPosition].selectableOptions[3])

    }

    interface OnAlternativeItemRadioButtonCheckStateChangeListener{
        fun onAlternativeItemRadioButtonCheckStateChanged(questionIndex:Int, selectableOptionIndex: Int)
    }
}