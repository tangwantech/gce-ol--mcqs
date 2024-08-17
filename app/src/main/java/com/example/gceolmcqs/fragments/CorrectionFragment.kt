package com.example.gceolmcqs.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.gceolmcqs.*
import com.example.gceolmcqs.activities.*
import com.example.gceolmcqs.datamodels.UserMarkedAnswersSheetData
import com.example.gceolmcqs.viewmodels.CorrectionFragmentViewModel

private const val CORRECTION_DATA = "Correction data"
private const val SECTION_INDEX = "index"

class CorrectionFragment : Fragment() {
    private lateinit var onNextSectionListener: OnNextSectionListener
    private lateinit var onRetrySectionListener: OnRetrySectionListener
    private lateinit var onGetNumberOfSectionsListener: OnGetNumberOfSectionsListener
    private lateinit var onCheckPackageExpiredListener: OnCheckPackageExpiredListener
    private lateinit var onIsSectionAnsweredListener: OnIsSectionAnsweredListener
    private lateinit var viewModel: CorrectionFragmentViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().title = context.resources.getString(R.string.correction)
        if (context is OnNextSectionListener) {
            onNextSectionListener = context
        }

        if (context is OnRetrySectionListener) {
            onRetrySectionListener = context
        }

        if(context is OnGetNumberOfSectionsListener){
            onGetNumberOfSectionsListener = context
        }

        if(context is OnCheckPackageExpiredListener){
            onCheckPackageExpiredListener = context
        }

        if (context is OnIsSectionAnsweredListener) {
            onIsSectionAnsweredListener = context
        }

    }

    private fun initViewModel(){
        viewModel = ViewModelProvider(requireActivity())[CorrectionFragmentViewModel::class.java]
        viewModel.setUserMarkedAnswersSheetData(requireArguments().getSerializable(CORRECTION_DATA) as UserMarkedAnswersSheetData)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_correction, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        val sectionIndex = requireArguments().getInt(SECTION_INDEX)

        val rvCorrectionFragment = RecyclerViewFragment.newInstance(
            requireActivity().title.toString(),
            viewModel.getUserMarkedAnswersSheetData()
//            requireArguments().getSerializable(CORRECTION_DATA) as UserMarkedAnswersSheetData
        )

        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.apply {
            replace(R.id.holderRvFragmentCorrection, rvCorrectionFragment)
            commit()
        }

//        val btnRetry: Button = view.findViewById(R.id.btnRetryCorrection)
//        btnRetry.setOnClickListener {
//            onRetrySectionListener.onRetrySection(sectionIndex)
//        }

        val btnNextSection: Button = view.findViewById(R.id.btnNextSection)
        var nextSectionIndex = sectionIndex + 1

        while(nextSectionIndex < onGetNumberOfSectionsListener.onGetNumberOfSections() && onIsSectionAnsweredListener.onGetSectionsAnswered()[nextSectionIndex]){
            nextSectionIndex += 1
        }

        if(nextSectionIndex < onGetNumberOfSectionsListener.onGetNumberOfSections()){
            btnNextSection.isEnabled = true
        }
        btnNextSection.setOnClickListener {
            if(!onCheckPackageExpiredListener.onCheckPackageExpired()){
                onCheckPackageExpiredListener.onShowPackageExpiredDialog()
            }else{
                onNextSectionListener.onNextSection(nextSectionIndex)
            }

        }

    }

    override fun onResume() {
        super.onResume()
        requireActivity().title = "${requireContext().resources.getString(R.string.correction)} ${requireContext().resources.getStringArray(R.array.sections)[requireArguments().getInt(SECTION_INDEX)]}"
    }

    companion object {

        fun newInstance(sectionIndex: Int, userMarkedAnswersSheetData: UserMarkedAnswersSheetData, expiresOn: String): Fragment {
            val bundle = Bundle()
            bundle.putString("expiresOn", expiresOn)
            bundle.putSerializable(CORRECTION_DATA, userMarkedAnswersSheetData)
            bundle.putInt(SECTION_INDEX, sectionIndex)
            val correctionFragment = CorrectionFragment()
            correctionFragment.arguments = bundle
            return correctionFragment
        }

    }
}