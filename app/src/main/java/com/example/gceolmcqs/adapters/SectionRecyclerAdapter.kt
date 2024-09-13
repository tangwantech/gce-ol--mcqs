package com.example.gceolmcqs.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.gceolmcqs.R
import com.example.gceolmcqs.ResourceImages
import com.example.gceolmcqs.databinding.QuestionCardItemBinding
import com.example.gceolmcqs.datamodels.QuestionWithUserAnswerMarkedData


class SectionRecyclerAdapter(
    private val context: Context,
    private val title: String,
    private val questions: List<QuestionWithUserAnswerMarkedData>,
) : RecyclerView.Adapter<SectionRecyclerAdapter.ViewHolder>() {
    private lateinit var binding: QuestionCardItemBinding

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {



        val questionCard: CardView = view.findViewById(R.id.questionCard)
        val questionLayout: LinearLayout = view.findViewById(R.id.questionLayout)
        val imageLo: CardView = view.findViewById(R.id.imageCardLayout)
        val twoStatementLo: LinearLayout = view.findViewById(R.id.twoStatementsLayout)
        val nonSelectableOptionsLo: LinearLayout =
            view.findViewById(R.id.nonSelectableOptionsLayout)

        val tvQuestion: TextView = view.findViewById(R.id.tvQuestion)
        val imageView: AppCompatImageView = view.findViewById(R.id.imgView)

        private val tvFirstStatement: TextView = view.findViewById(R.id.tvFirstStatement)
        private val tvSecondStatement: TextView = view.findViewById(R.id.tvSecondStatement)
        val twoStatements: ArrayList<TextView> = ArrayList()

        private val tvNonSelectableOption1: TextView =
            view.findViewById(R.id.tvNonSelectableOption1)
        private val tvNonSelectableOption2: TextView =
            view.findViewById(R.id.tvNonSelectableOption2)
        private val tvNonSelectableOption3: TextView =
            view.findViewById(R.id.tvNonSelectableOption3)
        val nonSelectableOptions: ArrayList<TextView> = ArrayList()


//        private val layoutOption1: LinearLayout = view.findViewById(R.id.layoutOption1)
//        private val layoutOption2: LinearLayout = view.findViewById(R.id.layoutOption2)
//        private val layoutOption3: LinearLayout = view.findViewById(R.id.layoutOption3)
//        private val layoutOption4: LinearLayout = view.findViewById(R.id.layoutOption4)


        val selectableOptionsLayout: LinearLayout = view.findViewById(R.id.selectableOptionsLayout)

        val tvUserAnswer: TextView = view.findViewById(R.id.tvUserAnswer)
        val imgRemark: ImageView = view.findViewById(R.id.imgRemark)
        val layoutUserAnswer: LinearLayout = view.findViewById(R.id.userMarkedAnswerLayout)

        val tvCorrectAnswer: TextView = view.findViewById(R.id.tvCorrectAnswer)
        val layoutCorrectAnswer: LinearLayout = view.findViewById(R.id.correctAnswerLayout)
        val layoutExplanationBtn: LinearLayout = view.findViewById(R.id.explanationBtnLayout)

        val explanationBtn: Button = view.findViewById(R.id.explanationBtn)
        val fourAlternativeCorrectionLayout: LinearLayout = view.findViewById(R.id.fourAlternativeCorrectionLayout)
        val fourAlternativesInCorrection: TextView = view.findViewById(R.id.alternativesCorrection)

        init {

            twoStatements.add(tvFirstStatement)
            twoStatements.add(tvSecondStatement)

            nonSelectableOptions.add(tvNonSelectableOption1)
            nonSelectableOptions.add(tvNonSelectableOption2)
            nonSelectableOptions.add(tvNonSelectableOption3)

            selectableOptionsLayout.visibility = View.GONE


        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(context).inflate(R.layout.question_card_item, parent, false)
        binding = QuestionCardItemBinding.bind(view)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        holder.questionCard.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_transition))

        val questionData = questions[position]
        displayOrHideLayout(questionData, holder, position)

        if(title == context.getString(R.string.section_questions)){
            setupSectionQuestions(holder)
        }else if (title == context.resources.getString(R.string.result)){
            setupResult(questionData, holder)

        }else{

            setupCorrection(questionData, holder, position)


        }

    }

    override fun getItemCount(): Int {
        return questions.size
    }

    private fun setupSectionQuestions(holder: ViewHolder){
        holder.layoutUserAnswer.visibility = View.GONE
        holder.layoutCorrectAnswer.visibility = View.GONE
        holder.layoutExplanationBtn.visibility = View.GONE
        holder.selectableOptionsLayout.visibility = View.VISIBLE
    }

    private fun setupResult(questionData: QuestionWithUserAnswerMarkedData, holder: ViewHolder){
        holder.layoutUserAnswer.visibility = View.VISIBLE
        holder.layoutCorrectAnswer.visibility = View.GONE
        holder.layoutExplanationBtn .visibility = View.GONE
        holder.selectableOptionsLayout.visibility = View.GONE

        if (questionData.userSelection == null){
//            holder.layoutUserAnswer.visibility = View.VISIBLE
            holder.tvUserAnswer.text = context.resources.getString(R.string.no_answer_selected)
            holder.imgRemark.setImageResource(R.drawable.ic_baseline_close_24)
            holder.tvUserAnswer.setTextColor(context.resources.getColor(R.color.color_red))
        }else{
//            holder.layoutUserAnswer.visibility = View.VISIBLE
            holder.tvUserAnswer.text = "${questionData.userSelection!!.optionLetter}. ${questionData.userSelection!!.optionSelected}"

            if (questionData.fourOptions != null){
                holder.fourAlternativeCorrectionLayout.visibility = View.VISIBLE
                holder.fourAlternativesInCorrection.text = questionData.fourOptions
            }else{
                holder.fourAlternativeCorrectionLayout.visibility = View.GONE
            }

            if(questionData.userSelection?.remark == null || !questionData.userSelection!!.remark!!){
                holder.imgRemark.setImageResource(R.drawable.ic_baseline_close_24)
                holder.tvUserAnswer.setTextColor(context.resources.getColor(R.color.color_red))
            }else{
                holder.imgRemark.setImageResource(R.drawable.ic_baseline_check_24)
                holder.tvUserAnswer.setTextColor(context.resources.getColor(R.color.color_green))
            }

        }
    }


    private fun setupCorrection(questionData: QuestionWithUserAnswerMarkedData, holder: ViewHolder, position: Int){
        holder.selectableOptionsLayout.visibility = View.GONE
        holder.layoutUserAnswer.visibility = View.GONE
        holder.layoutCorrectAnswer.visibility = View.VISIBLE
        holder.tvCorrectAnswer.text = questionData.correctAnswer
        if (questionData.explanation == null){
            holder.layoutExplanationBtn.visibility = View.GONE
        }else{
            holder.layoutExplanationBtn.visibility = View.VISIBLE
        }
        if (questionData.fourOptions != null){
            holder.fourAlternativeCorrectionLayout.visibility = View.VISIBLE
            holder.fourAlternativesInCorrection.text = questionData.fourOptions
        }else{
            holder.fourAlternativeCorrectionLayout.visibility = View.GONE
        }

        holder.explanationBtn.setOnClickListener {
            if (context is OnExplanationClickListener && questionData.explanation != null){
                context.onExplanationClicked(questionData)
            }
        }
    }

    private fun displayOrHideLayout(questionData: QuestionWithUserAnswerMarkedData, holder: ViewHolder, position: Int){
        if (questionData.question == null) {
            holder.questionLayout.visibility = View.GONE

        } else {

            if(title == "Correction"){
                holder.tvQuestion.text = "${questionData.questionNumber}. ${questionData.question}"
            }else{
                holder.tvQuestion.text = "${position + 1}. ${questionData.question}"
            }

        }

        if (questionData.image == null) {
            holder.imageLo.visibility = View.GONE
        } else {
            holder.imageLo.visibility = View.VISIBLE
            holder.imageView.setImageResource(ResourceImages.images[questionData.image]!!)
//            println(questionData.image!!)

        }

        if (questionData.twoStatements == null) {
            holder.twoStatementLo.visibility = View.GONE
        } else {
            holder.questionLayout.visibility = View.VISIBLE
            holder.tvQuestion.text = "Question ${questionData.questionNumber}"

            questionData.twoStatements!!.forEachIndexed { index, s ->
                holder.twoStatements[index].text = s
            }
        }

        if (questionData.nonSelectableOptions == null) {
            holder.nonSelectableOptionsLo.visibility = View.GONE
        } else {
            questionData.nonSelectableOptions!!.forEachIndexed { index, s ->
                holder.nonSelectableOptions[index].text = s
            }
        }


    }


    interface OnExplanationClickListener{
        fun onExplanationClicked(questionData: QuestionWithUserAnswerMarkedData)
    }



}