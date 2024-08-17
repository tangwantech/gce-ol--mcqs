package com.example.gceolmcqs.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.example.gceolmcqs.MCQConstants
import com.example.gceolmcqs.R
import com.example.gceolmcqs.viewmodels.ActivateTrialPackageFragmentViewModel

class ActivateTrialPackageFragment : Fragment() {

    private lateinit var viewModel: ActivateTrialPackageFragmentViewModel
    private var subjects: ArrayList<String>? = null
    private lateinit var onSubjectsPackagesAvailableListener: OnSubjectsPackagesAvailableListener
    private lateinit var onActivateTrialButtonClickListener: OnActivateTrialButtonClickListener

    private lateinit var subjectsAvailableTv: TextView
    private lateinit var packageDurationTV: TextView
    private lateinit var activateBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is OnSubjectsPackagesAvailableListener){
            onSubjectsPackagesAvailableListener = context
        }

        if(context is OnActivateTrialButtonClickListener){
            onActivateTrialButtonClickListener = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_activate_trial_package, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
//        setMobileId()
        setSubjects()
        setRepository()

        initViews(view)
        setupViewsListeners()
        setViewObservers()

    }

    private fun setSubjects(){
        arguments?.let {
            val subjects = it.getStringArrayList(MCQConstants.SUBJECTS)!!
//            println(subjects)
            viewModel.setSubjectNames(subjects)
        }

    }

    private fun initViewModel(){
        viewModel = ViewModelProvider(this)[ActivateTrialPackageFragmentViewModel::class.java]
    }

    private fun setRepository(){
        arguments?.let {
            val id = it.getString(MCQConstants.MOBILE_ID)!!
            viewModel.setRepositoryLink(requireContext(), id)
        }

    }

    private fun initViews(view: View){
        subjectsAvailableTv = view.findViewById(R.id.subjectsAvailableTv)
        packageDurationTV = view.findViewById(R.id.trialPackageDurationTv)
        packageDurationTV.text = "${MCQConstants.TRIAL_DURATION} Hours"
        activateBtn = view.findViewById(R.id.activateButton)
    }

    private fun setupViewsListeners(){
        activateBtn.setOnClickListener {
//            println("Activate button clicked")
            onActivateTrialButtonClickListener.onActivateTrialButtonClicked()
            viewModel.readSubjectsPackagesByMobileIdFromRemoteRepo()


        }
    }

    private fun setViewObservers(){
        viewModel.getAreSubjectsPackagesAvailable().observe(viewLifecycleOwner){
            it?.let {
                onSubjectsPackagesAvailableListener.onSubjectsPackagesAvailable(it)
            }
        }

        viewModel.subjectsAvailable.observe(viewLifecycleOwner){
            val subjects = it.joinToString(",")
            subjectsAvailableTv.text = subjects
        }

        viewModel.remoteRepoErrorMessage.observe(viewLifecycleOwner){
            println(it)
        }
    }


    companion object {
        @JvmStatic
        fun newInstance(subjects: ArrayList<String>, mobileId: String) =
            ActivateTrialPackageFragment().apply {
                arguments = Bundle().apply {
                    putStringArrayList(MCQConstants.SUBJECTS, subjects)
                    putString(MCQConstants.MOBILE_ID, mobileId)
                }
            }
    }

    interface OnSubjectsPackagesAvailableListener{
        fun onSubjectsPackagesAvailable(isAvailable: Boolean)
    }

    interface OnActivateTrialButtonClickListener{
        fun onActivateTrialButtonClicked()
    }
}