package com.example.gceolmcqs.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.gceolmcqs.*
import com.example.gceolmcqs.activities.*
import com.example.gceolmcqs.datamodels.SectionResultData
import com.example.gceolmcqs.viewmodels.SectionResultFragmentViewModel

private const val SECTION_RESULT_DATA = "Section Result data"
//private const val MINIMUM_PERCENTAGE = 30

class SectionResultFragment : Fragment() {

    private lateinit var sectionResultFragmentViewModel: SectionResultFragmentViewModel
    private lateinit var onRetrySectionListener: OnRetrySectionListener
    private lateinit var onGetNumberOfSectionsListener: OnGetNumberOfSectionsListener
    private lateinit var onNextSectionListener: OnNextSectionListener
    private lateinit var onGotoSectionCorrectionListener: OnGotoSectionCorrectionListener

    private lateinit var onIsSectionAnsweredListener: OnIsSectionAnsweredListener
    private lateinit var onPaperScoreListener: OnPaperScoreListener
    private lateinit var onCheckPackageExpiredListener: OnCheckPackageExpiredListener

    private lateinit var btnRetry: Button
    private lateinit var btnNextSection: Button
    private lateinit var btnCorrection: Button
//    private  var nextSectionIndex: Int? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModel()

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is OnRetrySectionListener) {
            onRetrySectionListener = context
        }
        if (context is OnGetNumberOfSectionsListener) {
            onGetNumberOfSectionsListener = context
        }

        if (context is OnNextSectionListener) {
            onNextSectionListener = context
        }

        if (context is OnGotoSectionCorrectionListener) {
            onGotoSectionCorrectionListener = context
        }

        if (context is OnPaperScoreListener) {
            onPaperScoreListener = context
        }

        if (context is OnIsSectionAnsweredListener) {
            onIsSectionAnsweredListener = context
        }
        if(context is OnCheckPackageExpiredListener){
            onCheckPackageExpiredListener = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_section_result, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        val sectionIndex = sectionResultFragmentViewModel.getSectionIndex()
//        val score = sectionResultFragmentViewModel.getNumberOfCorrectAnswers()

        initViews(view)
        setupViewListeners()
        setupViewObservers()


        val rvFragment = RecyclerViewFragment.newInstance(
            requireContext().resources.getString(R.string.result),
            sectionResultFragmentViewModel.getUserMarkedAnswersSheet()
        )
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.apply {
            replace(R.id.rvFragmentHolder, rvFragment)
            commit()
        }

        updateNextBtnState()


    }

    private fun updateNextBtnState(){
        sectionResultFragmentViewModel.updateNextBtnState()

    }

    private fun initViewModel(){
        sectionResultFragmentViewModel =
            ViewModelProvider(this)[SectionResultFragmentViewModel::class.java]
        sectionResultFragmentViewModel.setResultData(
            requireArguments().getSerializable(
                SECTION_RESULT_DATA
            ) as SectionResultData
        )
    }

    private fun initViews(view: View){
        val tvSectionScore: TextView = view.findViewById(R.id.tvSectionScore)
        tvSectionScore.text =
            "${sectionResultFragmentViewModel.getNumberOfCorrectAnswers()}/${sectionResultFragmentViewModel.getNumberOfQuestions()}"

//        val tvSectionPercentage: TextView = view.findViewById(R.id.tvSectionPercentage)
//        tvSectionPercentage.text =
//            sectionResultFragmentViewModel.getScorePercentage().toString()

        btnRetry = view.findViewById(R.id.btnRetry)
        btnNextSection = view.findViewById(R.id.btnNextSection)
        btnCorrection = view.findViewById(R.id.btnCorrection)
    }

    private fun retrySection(){
//        if(onRetrySectionListener.onGetCurrentSectionRetryCount().value == 0){
//            Toast.makeText(requireContext(), requireContext().resources.getString(R.string.retry_limit_message), Toast.LENGTH_LONG).show()
//            btnRetry.isEnabled = false
//        }else{
//
//            retryDialog(sectionResultFragmentViewModel.getSectionIndex())
//        }
        retryDialog(sectionResultFragmentViewModel.getSectionIndex())
    }

    private fun setupViewListeners(){
        btnRetry.setOnClickListener {
            retrySection()
        }

        btnNextSection.setOnClickListener {
            if(!onCheckPackageExpiredListener.onCheckPackageExpired()){
                onCheckPackageExpiredListener.onShowPackageExpiredDialog()
            }else{
                onNextSectionListener.onNextSection(sectionResultFragmentViewModel.getNextSectionIndex())
            }

        }

        btnCorrection.setOnClickListener {
            if (sectionResultFragmentViewModel.getScorePercentage() < MCQConstants.MINIMUM_PERCENT_SCORE) {
                val alertDialog = AlertDialog.Builder(requireContext())
                alertDialog.apply {
                    setMessage("Get a minimum percentage score of ${MCQConstants.MINIMUM_PERCENT_SCORE} in order to see corrections to all wrong answers")
                    setPositiveButton(resources.getString(R.string.ok)) { p0, _ ->
//                        retrySection()
                        p0.dismiss()
                    }
//                    setNegativeButton(resources.getString(R.string.cancel)){_, _ ->}
//                    setCancelable(false)
                }.create().show()
            } else {
                onGotoSectionCorrectionListener.onGotoSectionCorrection(
                    sectionResultFragmentViewModel.getSectionIndex(),
                    sectionResultFragmentViewModel.getQuestionsWithCorrectAnswer()
                )
            }
        }
    }

    private fun setupViewObservers(){
//        sectionResultFragmentViewModel.nextButtonState.observe(viewLifecycleOwner){
//            btnNextSection.isEnabled = it
//        }

        sectionResultFragmentViewModel.getHasPerfectScore().observe(viewLifecycleOwner){
            btnRetry.isEnabled = !it
            btnNextSection.isEnabled = it
            btnCorrection.isEnabled = !it
            if(it){
                Toast.makeText(requireContext(), requireContext().getString(R.string.excellent), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun retryDialog(sectionIndex: Int){
        val alertDialog = AlertDialog.Builder(requireContext())
        alertDialog.apply {
            setMessage(requireContext().resources.getString(R.string.retry_message))
            setPositiveButton(requireContext().resources.getString(R.string._continue)) { p0, _ ->

                if(!onCheckPackageExpiredListener.onCheckPackageExpired()){
                    onCheckPackageExpiredListener.onShowPackageExpiredDialog()
                }else{

                    onRetrySectionListener.onDecrementCurrentSectionRetryCount()
//                    Toast.makeText(requireContext(), "Retries left: ${onRetrySectionListener.onGetCurrentSectionRetryCount().value}", Toast.LENGTH_LONG).show()
                    onRetrySectionListener.onRetrySection(sectionIndex)
                }

                p0.dismiss()
            }
            setNegativeButton(requireContext().resources.getString(R.string.cancel)){ p0, _ ->
                p0.dismiss()
            }
            setCancelable(false)
        }.create().show()
    }

    override fun onResume() {
        super.onResume()
        requireActivity().title = "${requireContext().resources.getString(R.string.result)} ${requireContext().resources.getStringArray(R.array.sections)[sectionResultFragmentViewModel.getSectionIndex()]}"
    }


    companion object {

        fun newInstance(sectionResultData: SectionResultData, expiresOn: String): Fragment {
            val bundle = Bundle().apply {
                putSerializable(SECTION_RESULT_DATA, sectionResultData)
                putString("expiresOn", expiresOn)
            }
            val sectionResultFragment = SectionResultFragment()
            sectionResultFragment.arguments = bundle
            return sectionResultFragment
        }

    }

}