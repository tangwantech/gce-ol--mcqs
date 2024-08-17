package com.example.gceolmcqs.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.gceolmcqs.MCQConstants
import com.example.gceolmcqs.R
import com.example.gceolmcqs.datamodels.PackageData
import com.example.gceolmcqs.datamodels.SubscriptionFormData
import com.example.gceolmcqs.viewmodels.SubscriptionFormDialogFragmentViewModel

private const val SUBJECT_POSITION = "subject_position"
private const val SUBJECT_TITLE = "subject_title"
private const val PACKAGE_DATA = "packageData"

class SubscriptionFormDialogFragment : DialogFragment() {
    private lateinit var viewModel: SubscriptionFormDialogFragmentViewModel
    private lateinit var subscriptionFormButtonClickListener: SubscriptionFormButtonClickListener
//    private lateinit var etMomoNumber: TextInputEditText

    private lateinit var rbMtn: RadioButton
    private lateinit var rbOrange: RadioButton
    private lateinit var rgPaymentMethod: RadioGroup

    private var positiveBtn: Button? = null

    private var currentPackage: PackageData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpViewModel()
        viewModel.setSubjectPosition(
            requireArguments().getInt(
                SUBJECT_POSITION
            )
        )

        viewModel.updateDialogTitle(requireArguments().getString(
            SUBJECT_TITLE)!!)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is SubscriptionFormButtonClickListener) {
            subscriptionFormButtonClickListener = context
        }
    }

    private fun setCurrentPackage(){
        currentPackage = requireArguments().getSerializable(PACKAGE_DATA) as PackageData
    }

    private fun setUpViewModel(){
        viewModel =
            ViewModelProvider(this)[SubscriptionFormDialogFragmentViewModel::class.java]
        setCurrentPackage()
        viewModel.setPackageType(currentPackage?.packageName!!)
        viewModel.setPackagePrice(currentPackage?.price!!)
        viewModel.setPackageDuration(currentPackage?.duration!!)
    }

    private fun initViews(): View{

        val view = requireActivity().layoutInflater.inflate(R.layout.payment_method, null)

        val tvSubject: TextView = view.findViewById(R.id.tvsubject)
        tvSubject.text = requireArguments().getString(SUBJECT_TITLE)!!

        val tvSelectedPackage: TextView = view.findViewById(R.id.tvSelectedPackage)
        tvSelectedPackage.text = currentPackage?.packageName

        val tvPrice: TextView = view.findViewById(R.id.tvPrice)
        tvPrice.text = "${currentPackage?.price} FCFA"

        rgPaymentMethod = view.findViewById(R.id.rgPaymentMethod)

        rbMtn = view.findViewById(R.id.rbMtn)
        rbOrange = view.findViewById(R.id.rbOrange)

//        etMomoNumber = view.findViewById(R.id.etMomoNumber)
        return view
    }

    private fun setUpViewListeners(){

        rgPaymentMethod.setOnCheckedChangeListener { _, id ->
            when(id){
                R.id.rbMtn -> {
//                    println("MTN")
//                    viewModel.setMomoPartner(requireContext().resources.getStringArray(R.array.momo_partners)[0])

                    viewModel.setMomoPartner(MCQConstants.MTN_MOMO)
                }

                R.id.rbOrange -> {
//                    println("ORANGE")
//                    viewModel.setMomoPartner(requireContext().resources.getStringArray(R.array.momo_partners)[1])
                    viewModel.setMomoPartner(MCQConstants.ORANGE_MOMO)
                }

            }
            positiveBtn?.isEnabled = true
        }

//        etMomoNumber.doOnTextChanged { text, _, _, _ ->
//            viewModel.setMomoNumber(text.toString())
//
//        }
    }

    private fun setUpViewObservers(){

        viewModel.isSubscriptionFormFilled.observe(this, Observer {
            positiveBtn?.isEnabled = it
        })
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val view = initViews()
//        setUpViews()
        setUpViewListeners()
//        setUpViewObservers()
        return setUpAlertDialog(view)
    }
    private fun setUpAlertDialog(view: View): Dialog{
        val builder = AlertDialog.Builder(requireContext()).apply {
            setTitle(requireContext().resources.getString(R.string.select_payment_method))
//            setMessage()
            setView(view)
            setPositiveButton(requireContext().resources.getString(R.string.next)){ btn, _ ->
                subscriptionFormButtonClickListener.onSubscriptionFormNextButtonClicked(
                    viewModel.getSubscriptionFormData())
                btn.dismiss()
            }
            setNegativeButton(requireContext().resources.getString(R.string.cancel)) { btn, _ ->
                btn.dismiss()
            }
        }.create()

        builder.setOnShowListener {
            positiveBtn = builder.getButton(AlertDialog.BUTTON_POSITIVE)
//            val positiveBtn = (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
            positiveBtn?.isEnabled = false



        }
        return builder
    }

    companion object {
        fun newInstance(position: Int, subjectName: String, packageData: PackageData?=null): DialogFragment {
            val subscriptionFormDialogFragment = SubscriptionFormDialogFragment()
            val bundle = Bundle().apply {
                putInt(SUBJECT_POSITION, position)
                putString(SUBJECT_TITLE, subjectName)
                putSerializable(PACKAGE_DATA, packageData)
            }
            subscriptionFormDialogFragment.arguments = bundle
            return subscriptionFormDialogFragment
        }
    }


    interface SubscriptionFormButtonClickListener {
        fun onSubscriptionFormNextButtonClicked(subscriptionFormData: SubscriptionFormData)
    }
}