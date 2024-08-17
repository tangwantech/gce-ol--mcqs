package com.example.gceolmcqs.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.gceolmcqs.R
import com.example.gceolmcqs.datamodels.SubjectPackageData

private const val SUBJECT_PACKAGE_DATA = "subject package data"
class SubjectPackageDetailsDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        val subjectPackageData = requireArguments()[SUBJECT_PACKAGE_DATA]!! as SubjectPackageData

        val dialogView = requireActivity().layoutInflater.inflate(R.layout.fragment_subject_package_details, null)

        val packageDialogTitle = dialogView.findViewById(R.id.tvSubjectPackageDetailsTitle) as TextView
        packageDialogTitle.text = "${subjectPackageData.subjectName}"

        val tvSubjectPackage: TextView = dialogView.findViewById(R.id.tvSubjectPackage)
        tvSubjectPackage.text = subjectPackageData.packageName

        val tvPackageActivationDate: TextView = dialogView.findViewById(R.id.tvSubjectPackageActivationDate)
        tvPackageActivationDate.text = subjectPackageData.activatedOn


        val tvPackageExpiryDate: TextView  = dialogView.findViewById(R.id.tvSubjectPackageExpiryDate)
        tvPackageExpiryDate.text = subjectPackageData.expiresOn

        builder.setView(dialogView)
        builder.setPositiveButton("Ok") {_, _ ->
            this.dismiss()
        }
        return builder.create()
    }

    companion object {

        fun newInstance(subjectPackageData: SubjectPackageData): DialogFragment {
            val bundle = Bundle()
            bundle.putSerializable(SUBJECT_PACKAGE_DATA, subjectPackageData)
            val subjectPackageDetailsFragment = SubjectPackageDetailsDialogFragment()
            subjectPackageDetailsFragment.arguments = bundle
            return subjectPackageDetailsFragment
        }

    }
}