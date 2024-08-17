package com.example.gceolmcqs.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.gceolmcqs.MCQConstants
import com.example.gceolmcqs.R
import com.example.gceolmcqs.datamodels.SubscriptionFormData
import com.example.gceolmcqs.viewmodels.RequestToPayViewModel

private const val SUBSCRIPTION_DATA = "Subscription data"

class RequestToPayDialogFragment : DialogFragment() {
    private lateinit var requestToPayViewModel: RequestToPayViewModel
    private lateinit var onRequestToPayTransactionStatusListener: RequestToPayTransactionStatusListener


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is RequestToPayTransactionStatusListener) {
            onRequestToPayTransactionStatusListener = context
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestToPayViewModel = ViewModelProvider(this)[RequestToPayViewModel::class.java]
        requestToPayViewModel.setSubscriptionFormData(requireArguments()[SUBSCRIPTION_DATA] as SubscriptionFormData)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        val dialogView =
            requireActivity().layoutInflater.inflate(R.layout.fragment_request_to_pay, null)

        val tvRequestToPayMessage: TextView = dialogView.findViewById(R.id.tvRequestToPayMessage)
        val tvRequestToPaySubject: TextView =
            dialogView.findViewById(R.id.tvRequestToPaySubject)
        val tvRequestToPayPackageType: TextView =
            dialogView.findViewById(R.id.tvRequestToPayPackage)
        val tvRequestToPayPackagePrice: TextView =
            dialogView.findViewById(R.id.tvRequestToPayAmount)
//        val layoutInvoice: LinearLayout = dialogView.findViewById(R.id.layoutInvoice)
        val tvRequestToPayTitle: TextView = dialogView.findViewById(R.id.tvRequestToPayTitle)

        builder.setView(dialogView)
        requestToPayViewModel.getMomoPartner().observe(this, Observer{
            if(it == MCQConstants.MTN){
                tvRequestToPayTitle.setBackgroundColor(requireContext().resources.getColor(R.color.mtn))
                tvRequestToPayMessage.text = requireContext().resources.getString(R.string.mtn_request_to_pay_message)
            }else{
                tvRequestToPayTitle.setBackgroundColor(requireContext().resources.getColor(R.color.orange))
                tvRequestToPayTitle.setTextColor(requireContext().resources.getColor(R.color.white))
                tvRequestToPayMessage.text = requireContext().resources.getString(R.string.orange_request_to_pay_message)
            }
        })
        tvRequestToPaySubject.text = requestToPayViewModel.getSubjectName()
        tvRequestToPayPackageType.text = requestToPayViewModel.getPackageType()
        tvRequestToPayPackagePrice.text = "${requestToPayViewModel.getPackagePrice()} FCFA"

        requestToPayViewModel.getIsTransactionSuccessful().observe(this, Observer { isSuccessful ->
            if (isSuccessful) {
                onRequestToPayTransactionStatusListener.onTransactionSuccessful(
                    requestToPayViewModel.getSubjectIndex(),
                    requestToPayViewModel.getPackageType(),
                    requestToPayViewModel.getPackageDuration()
                )
                this.dismiss()
            } else {
                onRequestToPayTransactionStatusListener.onTransactionFailed(requestToPayViewModel.getPackageType())
                this.dismiss()
            }
        })



        this.isCancelable = false
        return builder.create()


    }

    companion object {
        fun newInstance(subscriptionFormData: SubscriptionFormData): DialogFragment {
            val bundle = Bundle().apply {
                putSerializable(SUBSCRIPTION_DATA, subscriptionFormData)
            }
            val requestToPayDialogFragment = RequestToPayDialogFragment()
            requestToPayDialogFragment.arguments = bundle
            return requestToPayDialogFragment
        }

    }

    interface RequestToPayTransactionStatusListener {
        fun onTransactionSuccessful(subjectIndex: Int, packageType: String, packageDuration: Int)
        fun onTransactionFailed(packageType: String)
    }

}