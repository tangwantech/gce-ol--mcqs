package com.example.gceolmcqs.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gceolmcqs.activities.OnRequestToGoToResultListener
import com.example.gceolmcqs.R
import com.example.gceolmcqs.adapters.SectionQuestionsRecyclerAdapter
import com.example.gceolmcqs.databinding.FragmentSectionBinding
import com.example.gceolmcqs.datamodels.SectionData
import com.example.gceolmcqs.viewmodels.SectionFragmentViewModel

private const val SECTION_DATA = "Section data"
private const val SECTION_INDEX = "Section index"
class SectionFragment : Fragment(), SectionQuestionsRecyclerAdapter.OnAlternativeItemRadioButtonCheckStateChangeListener{
    private lateinit var onRequestToGoToResultListener: OnRequestToGoToResultListener

    private lateinit var viewModel: SectionFragmentViewModel

    private lateinit var binding: FragmentSectionBinding

    private var fadeInOut: Animation? = null
    private var fadeTransition: Animation? = null
    private var fadeScale: Animation? = null

    private var isPositiveBtnClicked = false


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
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSectionBinding.inflate(inflater, container, false)
        return binding.root
//        return inflater.inflate(R.layout.fragment_section, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewListeners()
        setupViewObservers()
//        displayDirectionsDialog()
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



    private fun setupSectionRecyclerView(){

        val layoutMan = LinearLayoutManager(requireContext()).apply {
            orientation = LinearLayoutManager.VERTICAL
        }
        binding.recyclerView.layoutManager = layoutMan

        binding.recyclerView.addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))

        val sectionQuestionsAdapter = SectionQuestionsRecyclerAdapter(requireContext(), viewModel.getSectionTitle(), viewModel.getSectionQuestions(), this)
        binding.recyclerView.adapter = sectionQuestionsAdapter
        binding.recyclerView.setItemViewCacheSize(viewModel.getNumberOfQuestionsInSection())
        binding.recyclerView.setHasFixedSize(true)
    }

    @SuppressLint("SetTextI18n") // Suppress lint warning for hardcoded text concatenation
    private fun setupViewObservers() {// Set up observers for LiveData objects from the ViewModel

        // Observe the remaining time and update the timer TextView
        viewModel.getTimeRemaining().observe(viewLifecycleOwner) {
            binding.timerLayout.tvTimer.text = requireContext().getString(
                R.string.time_left_value,
                it.minute.toString().padStart(2, '0'),
                it.second.toString().padStart(2, '0')
            )
        }

        // Observe the "time almost out" flag and start animation if true
        viewModel.getIsTimeAlmostOut().observe(viewLifecycleOwner) {
            if (it) {
                binding.timerLayout.tvTimer.startAnimation(fadeInOut)
            }
        }

        // Observe the "timeout" flag and show alert dialog if true
        viewModel.getIsTimeOut().observe(viewLifecycleOwner) {if (it) {
            val alertDialog = AlertDialog.Builder(requireContext())
            alertDialog.apply {
                setMessage("Timeout")
                setPositiveButton("Ok") { _, _ ->
                    // Trigger callback to navigate to result screen
                    onRequestToGoToResultListener.onRequestToGoToResult(viewModel.getSectionResultData())
                }
                setCancelable(false)
            }.create().show()
            binding.btnResult.isEnabled = true // Enable "Result" button
        }
        }

        // Observe the number of answered questions and enable "Result" button if all questions are answered
        viewModel.numberOfQuestionsAnswered
            .observe(viewLifecycleOwner) {
                println("Number of questions answered: $it")
                if (it == viewModel.getNumberOfQuestionsInSection()) {
                    binding.btnResult.isEnabled = true // Enable "Result" button
                }
            }
    }

    private fun setupViewListeners(){
        binding.btnResult.setOnClickListener {
            onRequestToGoToResultListener.onRequestToGoToResult(viewModel.getSectionResultData())
        }
    }

    private fun startTimer() {
        viewModel.startTimer()
    }

    override fun onResume() {
        super.onResume()
        requireActivity().title = viewModel.getSectionTitle()
        setupSectionRecyclerView()
        startTimer()
//        displayDirectionsDialog()


    }

    private fun displayDirectionsDialog(){
        if(!getDirectionsCheckBoxState() && !isPositiveBtnClicked){
            displayDirections()
        }else{
            startTimer()
        }
    }

//    private fun setAnimationOnQuestionViewItems(){
////        svQuestion.startAnimation(fadeTransition)
//
//    }
//    private fun animateQuestionLo(){
//        questionLayout.startAnimation(fadeScale)
//    }
//
//    private fun animateImageLo(){
//        imageLo.startAnimation(fadeScale)
//    }
//    private fun animateTwoStatementsLo(){
//        twoStatementLo.startAnimation(fadeScale)
//    }
//    private fun animateNonSelectableOptionsLo(){
//        nonSelectableOptionsLo.startAnimation(fadeScale)
//    }
//    private fun animateSelectableOptionsLo(){
//        optionsLayouts.forEach {
//            it.startAnimation(fadeScale)
//        }
//    }
//
//    private fun animateCardSelectableLayouts(){
//        cardSelectableLayouts.forEach {
//            it.startAnimation(fadeScale)
//        }
//    }

    companion object {

        @JvmStatic
        fun newInstance(sectionIndex: Int, sectionData: SectionData): Fragment {
            val bundle = Bundle().apply {
                putSerializable(SECTION_DATA, sectionData)
                putInt(SECTION_INDEX, sectionIndex)
            }
            val sectionFragment = SectionFragment()
            sectionFragment.arguments = bundle

            return sectionFragment
        }
    }


    override fun onAlternativeItemRadioButtonCheckStateChanged(
        questionIndex: Int,
        selectableOptionIndex: Int
    ) {
        viewModel.updateUserSelection(questionIndex, selectableOptionIndex)
    }


}
