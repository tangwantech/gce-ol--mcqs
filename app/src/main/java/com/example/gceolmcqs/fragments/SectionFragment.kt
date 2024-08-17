package com.example.gceolmcqs.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.gceolmcqs.activities.OnRequestToGoToResultListener
import com.example.gceolmcqs.R
import com.example.gceolmcqs.ResourceImages
import com.example.gceolmcqs.datamodels.SectionDataModel
import com.example.gceolmcqs.viewmodels.SectionFragmentViewModel

private const val SECTION_DATA = "Section data"
private const val SECTION_INDEX = "Section index"
class SectionFragment : Fragment(), OnClickListener {
    private lateinit var onRequestToGoToResultListener: OnRequestToGoToResultListener

    private lateinit var viewModel: SectionFragmentViewModel

    private lateinit var svQuestion: ScrollView

    private lateinit var tvTimer: TextView
    private lateinit var tvCurrentQuestionNumberOfTotal: TextView

    private lateinit var btnNextQuestion: Button
    private lateinit var btnResult: Button

    private lateinit var imageLo: CardView
    private lateinit var twoStatementLo: LinearLayout
    private lateinit var nonSelectableOptionsLo: LinearLayout

    private lateinit var tvQuestion: TextView
    private lateinit var questionLayout: LinearLayout

    private lateinit var imageView: AppCompatImageView

    private lateinit var tvFirstStatement: TextView
    private lateinit var tvSecondStatement: TextView
    private val twoStatements: ArrayList<TextView> = ArrayList()

    private lateinit var tvNonSelectableOption1: TextView
    private lateinit var tvNonSelectableOption2: TextView
    private lateinit var tvNonSelectableOption3: TextView
    private val nonSelectableOptions: ArrayList<TextView> = ArrayList()


    private lateinit var tvSelectableOption1: TextView
    private lateinit var tvSelectableOption2: TextView
    private lateinit var tvSelectableOption3: TextView
    private lateinit var tvSelectableOption4: TextView
    private val selectableOptions: ArrayList<TextView> = ArrayList()


    private lateinit var layoutOption1: LinearLayout
    private lateinit var layoutOption2: LinearLayout
    private lateinit var layoutOption3: LinearLayout
    private lateinit var layoutOption4: LinearLayout
    private val optionsLayouts: ArrayList<LinearLayout> = ArrayList()

    private lateinit var cardSelectableOption1: CardView
    private lateinit var cardSelectableOption2: CardView
    private lateinit var cardSelectableOption3: CardView
    private lateinit var cardSelectableOption4: CardView
    private val cardSelectableLayouts: ArrayList<CardView> = ArrayList()

    private var fadeInOut: Animation? = null
    private var fadeTransition: Animation? = null
    private var fadeScale: Animation? = null

    private var isPositiveBtnClicked = false

//    private var preLayoutOption: LinearLayout? = null
//    private var currentLayoutOption: LinearLayout? = null
//
    private var background: Drawable? = null
    private var textColor: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupViewModel()
        initTransitions()

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is OnRequestToGoToResultListener){
            onRequestToGoToResultListener = context
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_section, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)

//        startTimer()
        btnNextQuestion.setOnClickListener(this)
        btnResult.setOnClickListener(this)
        setupViewObservers()
        displayDirectionsDialog()


    }

    private fun displayDirections(){
        val view = requireActivity().layoutInflater.inflate(R.layout.section_directions, null)
        val tvDirections: TextView = view.findViewById(R.id.tvSectionInstruction)
        tvDirections.text = viewModel.getSectionDirections()
        val directionsCheckBox: CheckBox = view.findViewById(R.id.instructionCheckBox)
        directionsCheckBox.setOnCheckedChangeListener { _, state ->
            saveDirectionsCheckBoxState(state)
        }
        val dialog = AlertDialog.Builder(requireContext()).apply{
            setTitle("Directions")
            setView(view)
            setPositiveButton(requireContext().resources.getString(R.string.ok)){_, _ ->
                isPositiveBtnClicked = true
                startTimer()
            }
            setCancelable(false)
        }.create()

        dialog.show()
    }

    private fun saveDirectionsCheckBoxState(state: Boolean){
        val pref = requireActivity().getSharedPreferences(
            viewModel.getSectionIndex(),
            AppCompatActivity.MODE_PRIVATE
        )
        pref.edit().apply{
           putBoolean("checkState", state)
        }.apply()
    }

    private fun getDirectionsCheckBoxState(): Boolean {
        val pref = requireActivity().getSharedPreferences(
            viewModel.getSectionIndex(),
            AppCompatActivity.MODE_PRIVATE
        )
        return pref.getBoolean("checkState", false)
    }

    private fun initTransitions(){
        fadeInOut = AnimationUtils.loadAnimation(requireContext(), R.anim.cross_fade)
        fadeTransition = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_transition)
        fadeScale = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_scale)
    }

    private fun setupViewModel(){
        viewModel = ViewModelProvider(this)[SectionFragmentViewModel::class.java]
        viewModel.setSectionIndex(requireArguments().getInt(SECTION_INDEX))
    }


    @SuppressLint("SetTextI18n")
    private fun setupViewObservers(){
        viewModel.getTimeRemaining().observe(viewLifecycleOwner){
            tvTimer.text =
                "${it.minute.toString().padStart(2, '0')}:${it.second.toString().padStart(2, '0')}"

        }

        viewModel.getIsTimeAlmostOut().observe(viewLifecycleOwner){
            if(it){
//                tvTimer.setTextColor(requireContext().resources.getColor(R.color.color_accent))
                tvTimer.startAnimation(fadeInOut)
            }

        }

        viewModel.getIsTimeOut().observe(viewLifecycleOwner){
            if(it){
                val alertDialog = AlertDialog.Builder(requireContext())
                alertDialog.apply {
                    setMessage("Timeout")
                    setPositiveButton("Ok") { _,_ ->
                        onRequestToGoToResultListener.onRequestToGoToResult(viewModel.getSectionResultData())
                    }
                    setCancelable(false)
                }.create().show()
                disableSelectableOptions()
                btnResult.isEnabled = true
                btnNextQuestion.isEnabled = false
            }

        }


        viewModel.getQuestionIndex()
            .observe(viewLifecycleOwner, Observer { questionIndex ->
//                svQuestion.startAnimation(fadeInOut)

                setAnimationOnQuestionViewItems()
                tvCurrentQuestionNumberOfTotal.text =
                    "${questionIndex + 1} of ${viewModel.getNumberOfQuestionsInSection()}"

                if (questionIndex + 1 == viewModel.getNumberOfQuestionsInSection()) {
                    btnNextQuestion.isEnabled = false
                }

                val questionData = viewModel.getQuestion()


                if (questionData.question == null) {
                    questionLayout.visibility = View.GONE
                } else {
                    animateQuestionLo()
                    tvQuestion.text = questionData.question
                }

                if (questionData.image == null) {
                    imageLo.visibility = View.GONE
                } else {
                    animateImageLo()
                    imageLo.visibility = View.VISIBLE
                    imageView.setImageResource(ResourceImages.images[questionData.image]!!)

                }

                if (questionData.twoStatements == null) {
                    twoStatementLo.visibility = View.GONE
                } else {
                    questionData.twoStatements.forEachIndexed { index, s ->
                        twoStatements[index].text = s
                    }
                    animateTwoStatementsLo()
                }

                if (questionData.nonSelectableOptions == null) {
                    nonSelectableOptionsLo.visibility = View.GONE
                } else {
                    questionData.nonSelectableOptions.forEachIndexed { index, s ->
                        nonSelectableOptions[index].text = s
                    }
                    animateNonSelectableOptionsLo()
                }

                questionData.selectableOptions.forEachIndexed { index, s ->
                    selectableOptions[index].text = "${viewModel.getLetters()[index]}. $s"
                }
                animateCardSelectableLayouts()
//                animateSelectableOptionsLo()

                viewModel.getIsQuestionAnswered()
                    .observe(viewLifecycleOwner, Observer { isQuestionAnswered ->
                        when (isQuestionAnswered) {
                            true -> {
                                btnNextQuestion.isEnabled = true
                                if (questionIndex + 1 == viewModel.getNumberOfQuestionsInSection()) {
                                    btnNextQuestion.isEnabled = false
                                }
                            }
                            else -> {
                                btnNextQuestion.isEnabled = false
                            }
                        }
                    })


            })

        viewModel.getNumberOfQuestionsAnswered()
            .observe(viewLifecycleOwner, Observer {
                if (it == viewModel.getNumberOfQuestionsInSection()) {
                    btnResult.isEnabled = true
                }
            })
    }

    private fun startTimer() {
        viewModel.startTimer()
    }

    private fun nextQuestion() {
        resetSelectedQuestionOptionBackground()
//        resetAllSelectableOptions()
        viewModel.incrementQuestionIndex()

    }

    private fun initViews(view: View) {

        svQuestion = view.findViewById(R.id.svQuestion)
        tvTimer = view.findViewById(R.id.tvTimer)

        tvCurrentQuestionNumberOfTotal = view.findViewById(R.id.tvCurrentQuestionOfTotal)

        btnResult = view.findViewById(R.id.btnResult)
        btnNextQuestion = view.findViewById(R.id.btnNext)

        questionLayout = view.findViewById(R.id.questionLayout)
        imageLo = view.findViewById(R.id.imageCardLayout)
        twoStatementLo = view.findViewById(R.id.twoStatementsLayout)
        nonSelectableOptionsLo = view.findViewById(R.id.nonSelectableOptionsLayout)

        tvQuestion = view.findViewById(R.id.tvQuestion)
        imageView = view.findViewById(R.id.imgView)
        tvFirstStatement = view.findViewById(R.id.tvFirstStatement)
        tvSecondStatement = view.findViewById(R.id.tvSecondStatement)
        twoStatements.add(tvFirstStatement)
        twoStatements.add(tvSecondStatement)

        tvNonSelectableOption1 = view.findViewById(R.id.tvNonSelectableOption1)
        tvNonSelectableOption2 = view.findViewById(R.id.tvNonSelectableOption2)
        tvNonSelectableOption3 = view.findViewById(R.id.tvNonSelectableOption3)
        nonSelectableOptions.add(tvNonSelectableOption1)
        nonSelectableOptions.add(tvNonSelectableOption2)
        nonSelectableOptions.add(tvNonSelectableOption3)

        tvSelectableOption1 = view.findViewById(R.id.tvSelectableOption1)
        textColor = tvSelectableOption1.currentTextColor

        tvSelectableOption2 = view.findViewById(R.id.tvSelectableOption2)
        tvSelectableOption3 = view.findViewById(R.id.tvSelectableOption3)
        tvSelectableOption4 = view.findViewById(R.id.tvSelectableOption4)
        selectableOptions.add(tvSelectableOption1)
        selectableOptions.add(tvSelectableOption2)
        selectableOptions.add(tvSelectableOption3)
        selectableOptions.add(tvSelectableOption4)

//        selectableOptions.forEachIndexed { index, _ ->
//            selectableOptions[index].background = context?.resources?.getDrawable(R.drawable.default_background)
//        }

        layoutOption1 = view.findViewById(R.id.layoutOption1)
        background = layoutOption1.background

        layoutOption1.setOnClickListener(this)
        layoutOption2 = view.findViewById(R.id.layoutOption2)
        layoutOption2.setOnClickListener(this)

        layoutOption3 = view.findViewById(R.id.layoutOption3)
        layoutOption3.setOnClickListener(this)

        layoutOption4 = view.findViewById(R.id.layoutOption4)
        layoutOption4.setOnClickListener(this)
        optionsLayouts.add(layoutOption1)
        optionsLayouts.add(layoutOption2)
        optionsLayouts.add(layoutOption3)
        optionsLayouts.add(layoutOption4)

        cardSelectableOption1 = view.findViewById(R.id.cardSelectableOption1)
        cardSelectableOption2 = view.findViewById(R.id.cardSelectableOption2)
        cardSelectableOption3 = view.findViewById(R.id.cardSelectableOption3)
        cardSelectableOption4 = view.findViewById(R.id.cardSelectableOption4)
        cardSelectableLayouts.add(cardSelectableOption1)
        cardSelectableLayouts.add(cardSelectableOption2)
        cardSelectableLayouts.add(cardSelectableOption3)
        cardSelectableLayouts.add(cardSelectableOption4)

    }

    override fun onResume() {
        super.onResume()
        requireActivity().title = viewModel.getSectionTitle()
//        displayDirectionsDialog()


    }

    private fun displayDirectionsDialog(){
        if(!getDirectionsCheckBoxState() && !isPositiveBtnClicked){
            displayDirections()
        }else{
            startTimer()
        }
    }

    private fun setAnimationOnQuestionViewItems(){
//        svQuestion.startAnimation(fadeTransition)

    }
    private fun animateQuestionLo(){
        questionLayout.startAnimation(fadeScale)
    }

    private fun animateImageLo(){
        imageLo.startAnimation(fadeScale)
    }
    private fun animateTwoStatementsLo(){
        twoStatementLo.startAnimation(fadeScale)
    }
    private fun animateNonSelectableOptionsLo(){
        nonSelectableOptionsLo.startAnimation(fadeScale)
    }
    private fun animateSelectableOptionsLo(){
        optionsLayouts.forEach {
            it.startAnimation(fadeScale)
        }
    }

    private fun animateCardSelectableLayouts(){
        cardSelectableLayouts.forEach {
            it.startAnimation(fadeScale)
        }
    }

    companion object {

        @JvmStatic
        fun newInstance(sectionIndex: Int, sectionData: SectionDataModel): Fragment {
            val bundle = Bundle().apply {
                putSerializable(SECTION_DATA, sectionData)
                putInt(SECTION_INDEX, sectionIndex)
            }
            val sectionFragment = SectionFragment()
            sectionFragment.arguments = bundle

            return sectionFragment
        }
    }

    override fun onClick(p0: View?) {
        when (p0!!.id) {
            R.id.btnNext -> {
                nextQuestion()
            }
            R.id.btnResult -> {

                onRequestToGoToResultListener.onRequestToGoToResult(viewModel.getSectionResultData())
            }
//            R.id.layoutOption1, R.id.layoutOption2, R.id.layoutOption3, R.id.layoutOption4 -> {
//                when (p0.id) {
//                    R.id.layoutOption1 -> {
//
//                        updateOptionSelected(p0,0)
//                    }
//                    R.id.layoutOption2 -> {
//                        updateOptionSelected(p0, 1)
//                    }
//                    R.id.layoutOption3 -> {
//                        updateOptionSelected(p0, 2)
//                    }
//                    R.id.layoutOption4 -> {
//                        updateOptionSelected(p0, 3)
//                    }
//                }
//            }
            R.id.layoutOption1 -> {

                updateOptionSelected(p0,0)
            }
            R.id.layoutOption2 -> {
                updateOptionSelected(p0, 1)
            }
            R.id.layoutOption3 -> {
                updateOptionSelected(p0, 2)
            }
            R.id.layoutOption4 -> {
                updateOptionSelected(p0, 3)
            }
        }
    }

    private fun disableSelectableOptions(){
        optionsLayouts.forEach {
            it.isEnabled = false
        }

    }

    private fun updateOptionSelected(view: View, optionSelectedIndex: Int) {

        changeSelectedQuestionOptionBackground(view, optionSelectedIndex)

    }


    private fun changeSelectedQuestionOptionBackground(view: View, optionSelectedIndex: Int) {
        viewModel.updateUserSelection(optionSelectedIndex)
//        if(background == null){
//            textColor = selectableOptions[optionSelectedIndex].textColors
////            println("Initial background: $background")
//
//
//
//        }
        optionsLayouts.forEachIndexed { index, _ ->
            if( index != optionSelectedIndex){
                optionsLayouts[index].background = requireContext().resources.getDrawable(R.drawable.default_background)
//                optionsLayouts[optionSelectedIndex].background = background
//                selectableOptions[index].setTextColor(resources.getColor(R.color.color_primary_text_default))
                textColor?.let{
                    selectableOptions[index].setTextColor(it)
                }

            }else{


//                if (context?.packageManager?.getActivityInfo(requireActivity().componentName, 0)?.theme != android.R.style.Theme_Light){
//
//                    selectableOptions[optionSelectedIndex].setTextColor(resources.getColor(androidx.appcompat.R.color.primary_text_default_material_light))
//                }
                optionsLayouts[optionSelectedIndex].background = requireContext().resources.getDrawable(R.drawable.selected_drawable)
                selectableOptions[optionSelectedIndex].setTextColor(resources.getColor(R.color.color_primary_text_default))

            }

        }


//        println("Initial background2: $background")

    }


    private fun resetSelectedQuestionOptionBackground() {
//

        optionsLayouts.forEachIndexed { index, _ ->
//            optionsLayouts[index].background = requireContext().resources.getDrawable(R.drawable.default_background)
            optionsLayouts[index].background = background
//            selectableOptions[index].setTextColor(resources.getColor(R.color.color_primary_text_default))
            textColor?.let{
                selectableOptions[index].setTextColor(it)
            }
        }
//        background = null
//        textColor = null
    }



}
