package com.example.gceolmcqs.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import com.example.gceolmcqs.MCQConstants
import com.example.gceolmcqs.MomoPayService

import com.example.gceolmcqs.R
import com.example.gceolmcqs.datamodels.PackageData
import com.example.gceolmcqs.datamodels.SubjectPackageData
import com.example.gceolmcqs.datamodels.SubscriptionFormData
import com.example.gceolmcqs.fragments.PackagesDialogFragment
import com.example.gceolmcqs.fragments.SubscriptionFormDialogFragment

//import com.example.gceolmcq.momoPay.MomoPayService
import com.example.gceolmcqs.viewmodels.SubscriptionActivityViewModel
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.*

abstract class SubscriptionActivity: AppCompatActivity(), SubscriptionFormDialogFragment.SubscriptionFormButtonClickListener, PackagesDialogFragment.PackageDialogListener{
    private val SUBSCRIPTION_ACTIVITY = "subscriptionActivity"
    private var processingAlertDialog: AlertDialog? = null
    private var requestToPayDialog: AlertDialog? = null
    private var failedToActivatePackageDialog: AlertDialog? = null
    private var activatingPackageDialog: AlertDialog? = null
    private var packageActivatedDialog: AlertDialog? = null
    private var paymentReceivedDialog: AlertDialog? = null
    private var activatingTrialPackageDialog: AlertDialog? = null
//    private var packagesDialog: DialogFragment? = null
    private var currentRefNum: String? = null

    private lateinit var viewModel: SubscriptionActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupViewModel()
        setupViewObservers()
//        resumeTransaction()
    }

    open override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }

    private fun resetAllDialogs(){
        processingAlertDialog = null
        requestToPayDialog = null
        failedToActivatePackageDialog = null
        activatingTrialPackageDialog = null
        packageActivatedDialog = null
        paymentReceivedDialog = null
        activatingTrialPackageDialog = null
    }


    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[SubscriptionActivityViewModel::class.java]
        viewModel.setRepositoryLink(this, getMobileID())
        viewModel.setMobileId(getMobileID())
        viewModel.setMomoPayService(MomoPayService(this))

    }

    private fun setupViewObservers() {
        viewModel.getAreSubjectsPackagesAvailable().observe(this){areAvailable ->

            areAvailable?.let{
                if (it){
                    if(activatingPackageDialog != null){
                        cancelActivatingPackageDialog()
                    }
                    if (activatingTrialPackageDialog != null){
                        dismissActivatingTrialPackageDialog()
                    }
                    if (packageActivatedDialog == null){
                        showPackageActivatedDialog()
                    }

                }

            }

        }

        viewModel.getRemoteRepoErrorEncountered().observe(this){
            println("Error encountered: $it")
        }

        viewModel.transactionId.observe(this){
            currentRefNum = it
        }
        setMomoPayFlow()

    }

    private fun setMomoPayFlow(){
//        viewModel.getTransactionStatus().observe(this)

        viewModel.currentTransaction.observe(this){
            setCurrentTransactionSharedPref(it)
        }
        viewModel.transactionStatus.observe(this) {
//            it.status?.let
            it?.let{status ->
                when(status) {
                    MCQConstants.PENDING -> {
                        if (processingAlertDialog != null){
                            cancelProcessingRequestDialog()
                        }
                        if (failedToActivatePackageDialog != null){
                            cancelFailedToActivateDialog()
                        }

                        if (requestToPayDialog == null){
                            showRequestUserToPayDialog()
                        }
                        updateCurrentTransactionSharedPref(getCurrentTransactionFromSharedPref(), MCQConstants.PENDING, false)
                    }
                    MCQConstants.SUCCESSFUL -> {
                        cancelProcessingAndRequestToPayDialogs()
//                        if (processingAlertDialog != null){
//                            cancelProcessingRequestDialog()
//                        }
//                        if (requestToPayDialog != null){
//                            cancelRequestToPayDialog()
//                        }

                        if(paymentReceivedDialog == null){
                            showPaymentReceivedDialog()
                            viewModel.resetTransactionStatus()
                        }
                        if (failedToActivatePackageDialog != null){
                            cancelFailedToActivateDialog()
                        }

                        CoroutineScope(Dispatchers.IO).launch {
                            delay(2000)
                            withContext(Dispatchers.Main){
                                if(paymentReceivedDialog != null){
                                    cancelPaymentReceivedDialog()
                                }
                                if(activatingPackageDialog == null){
                                    showActivatingPackageDialog()
                                    activateUserPackage()
                                }
                            }
                        }
                    }
                    MCQConstants.FAILED -> {
                        cancelProcessingAndRequestToPayDialogs()
//                        if (processingAlertDialog != null){
//                            cancelProcessingRequestDialog()
//                        }
//                        if (requestToPayDialog != null){
//                            cancelRequestToPayDialog()
//                        }
                        if(failedToActivatePackageDialog == null){
                            showTransactionFailedDialog()
                            updateCurrentTransactionSharedPref(getCurrentTransactionFromSharedPref(), MCQConstants.FAILED, false)
                            resetMomoPayService()
                        }
                    }
                }
            }


        }

        viewModel.getIsPaymentSystemAvailable().observe(this){isPaymentSystemAvailable ->
            isPaymentSystemAvailable?.let {
                if(!it){
                    Toast.makeText(this, "Payment system is temporarily unavailable. Please try again later", Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    open fun showPackageExpiredDialog(){
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.apply {
            setMessage(resources.getString(R.string.package_expired_message))
            setPositiveButton("Ok") { _, _ ->

            }
        }.create().show()
    }

    private fun showProcessingRequestDialog() {
        processingAlertDialog = AlertDialog.Builder(this).create()
        processingAlertDialog?.apply {
            setCancelable(false)
        }
        processingAlertDialog?.setMessage(resources.getString(R.string.processing_request))
        processingAlertDialog?.show()
    }

    private fun cancelProcessingRequestDialog() {
        processingAlertDialog?.dismiss()
        processingAlertDialog = null
    }

    private fun showActivatingPackageDialog() {
        activatingPackageDialog = AlertDialog.Builder(this).create()
        activatingPackageDialog?.apply {
            setCancelable(false)
        }
        activatingPackageDialog?.setMessage(resources.getString(R.string.activating_package_message))
        activatingPackageDialog?.show()
//        activateUserPackage()
    }

    private fun cancelActivatingPackageDialog() {
        activatingPackageDialog?.dismiss()
        activatingPackageDialog = null
    }

    private fun showSubscriptionForm(position: Int, subjectName: String, packageData: PackageData?) {
        val subscriptionFormDialogFragment = SubscriptionFormDialogFragment.newInstance(
            position,
            subjectName,
            packageData
        )
        subscriptionFormDialogFragment.isCancelable = false
        subscriptionFormDialogFragment.show(supportFragmentManager, "subscription_form_dialog")
    }

    private fun showRequestUserToPayDialog() {

        val dialogView = layoutInflater.inflate(R.layout.fragment_request_to_pay, null)
        val tvRequestToPayTitle: TextView = dialogView.findViewById(R.id.tvRequestToPayTitle)
        val tvRequestToPayMessage: TextView = dialogView.findViewById(R.id.tvRequestToPayMessage)
        val tvRequestToPaySubject: TextView =
            dialogView.findViewById(R.id.tvRequestToPaySubject)
        val tvRequestToPayPackageType: TextView =
            dialogView.findViewById(R.id.tvRequestToPayPackage)
        val tvRequestToPayPackagePrice: TextView =
            dialogView.findViewById(R.id.tvRequestToPayAmount)
        val tvTransactionId: TextView = dialogView.findViewById(R.id.tvTransactionId)

        if (viewModel.subscriptionData.value?.momoPartner!! == MCQConstants.MTN_MOMO) {
            tvRequestToPayTitle.setBackgroundColor(resources.getColor(R.color.mtn))
            tvRequestToPayMessage.text = resources.getString(R.string.mtn_request_to_pay_message)

        } else {
            tvRequestToPayTitle.setBackgroundColor(resources.getColor(R.color.orange))
            tvRequestToPayMessage.text = resources.getString(R.string.orange_request_to_pay_message)
        }

        tvRequestToPaySubject.text = viewModel.subscriptionData.value?.subject
        tvRequestToPayPackageType.text = viewModel.subscriptionData.value?.packageType
        tvRequestToPayPackagePrice.text = "${viewModel.subscriptionData.value?.packagePrice} FCFA"
//        tvTransactionId.text = "Reference Number: $currentRefNum"

        requestToPayDialog = AlertDialog.Builder(this).create()
        requestToPayDialog?.apply {
            setCancelable(false)
        }
        requestToPayDialog?.setView(dialogView)
        requestToPayDialog?.show()

    }

    private fun cancelRequestToPayDialog() {
//        requestToPayDialog.cancel()
        requestToPayDialog?.dismiss()
        requestToPayDialog =  null
    }

    private fun cancelProcessingAndRequestToPayDialogs(){
        if(processingAlertDialog != null){
            cancelProcessingRequestDialog()
        }

        if(requestToPayDialog != null){
            cancelRequestToPayDialog()
        }
    }

    open fun showPackageActivatedDialog() {
        val view = layoutInflater.inflate(
            R.layout.package_activation_successful_dialog,
            null
        )
        val tvPackageActivationSuccessful: TextView =
            view.findViewById(R.id.tvPackageActivationSuccessful)
        tvPackageActivationSuccessful.text =
            "${viewModel.subscriptionData.value?.packageType} ${resources.getString(R.string.activated_successfully)}"
        val packageActivatedDialog = AlertDialog.Builder(this).apply {
            setView(view)
            setPositiveButton("Ok"){ _, _ ->
            packageActivatedDialog = null
            }
        }.create()
        packageActivatedDialog.show()
        updateCurrentTransactionSharedPref(getCurrentTransactionFromSharedPref(), MCQConstants.SUCCESSFUL, true)

    }

    private fun showTransactionFailedDialog() {

        failedToActivatePackageDialog = AlertDialog.Builder(this).create()
        val view = this.layoutInflater.inflate(R.layout.package_activation_failed_dialog, null)
        val tvFailedMessage: TextView = view.findViewById(R.id.tvPackageActivationFailed)
        tvFailedMessage.text =
            "${resources.getString(R.string.failed_to_activate_package)} ${viewModel.subscriptionData.value?.packageType} "

        failedToActivatePackageDialog?.setView(view)
        failedToActivatePackageDialog?.setButton(AlertDialog.BUTTON_POSITIVE, "Ok") { _, _ ->
//            d.dismiss()
//            failedToActivatePackageDialog = null
            cancelFailedToActivateDialog()

        }
        failedToActivatePackageDialog?.show()

    }

    private fun cancelFailedToActivateDialog(){
        failedToActivatePackageDialog?.dismiss()
        failedToActivatePackageDialog = null
    }

    private fun showPaymentReceivedDialog(){
        paymentReceivedDialog = AlertDialog.Builder(this).create()
        paymentReceivedDialog?.setMessage(resources.getString(R.string.payment_received))
        paymentReceivedDialog?.show()
    }

    private fun cancelPaymentReceivedDialog(){
        paymentReceivedDialog?.dismiss()
        paymentReceivedDialog = null
    }

    fun showActivatingTrialPackageDialog(){
        activatingTrialPackageDialog = AlertDialog.Builder(this).create()
        activatingTrialPackageDialog?.setMessage("Activating trial package...")
        activatingTrialPackageDialog?.setCancelable(false)
        activatingTrialPackageDialog?.show()
    }

    fun dismissActivatingTrialPackageDialog(){
        activatingTrialPackageDialog?.dismiss()
        activatingTrialPackageDialog = null
    }

    private fun displayInvoice(subscriptionData: SubscriptionFormData){
        val view = LayoutInflater.from(this).inflate(R.layout.subscription_summary_layout, null)
        val subjectName: TextView = view.findViewById(R.id.invoiceSubjectNameTv)
        val packageName: TextView = view.findViewById(R.id.invoicePackageNameTv)
        val packagePrice: TextView = view.findViewById(R.id.invoicePackagePriceTv)
        val momoNumber: TextView = view.findViewById(R.id.invoiceMomoNumberTv)

        subjectName.text = "${subscriptionData.subject}"
        packageName.text = "${subscriptionData.packageType}"
        packagePrice.text = "${subscriptionData.packagePrice} FCFA"
        momoNumber.text = "${subscriptionData.momoNumber}"
        val dialog = AlertDialog.Builder(this)
        dialog.apply{
            setMessage(resources.getString(R.string.verify_payment_info))
            setView(view)
            setCancelable(false)
            setPositiveButton(resources.getString(R.string.pay)  ){btn, _ ->
                viewModel.setSubscriptionData(subscriptionData)
                showProcessingRequestDialog()
                viewModel.initiatePayment()
                btn.dismiss()
            }
            setNegativeButton(resources.getString(R.string.cancel)){btn, _ ->
                btn.dismiss()
            }
        }.create()
        dialog.show()
    }

    private fun displayPackagesDialog(){
        val packagesDialog = PackagesDialogFragment.newInstance()
        packagesDialog.show(supportFragmentManager, null)
    }

    private fun displayEnterMomoNumberDialog(subscriptionFormData: SubscriptionFormData){
        val view = layoutInflater.inflate(R.layout.momo_number_dialog, null)
        val etMoMoNumber: TextInputEditText = view.findViewById(R.id.etMomoNumber)
        val dialog = AlertDialog.Builder(this).create()
        dialog.setTitle(resources.getString(R.string.enter_number))
        dialog.setView(view)
        dialog.setCancelable(false)
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, resources.getString(R.string.next)){_, _ ->
            displayInvoice(subscriptionFormData)
        }
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE,resources.getString(R.string.cancel)){_, _ ->}
        dialog.show()

        val btnPositive = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        btnPositive.isEnabled = false

        etMoMoNumber.doOnTextChanged { text, _, _, _ ->
            if(text.toString().isNotEmpty() && text.toString().length == 9){
                subscriptionFormData.momoNumber = text.toString()
                btnPositive.isEnabled = true

            }else{
                btnPositive.isEnabled = false
            }
        }
    }

    private fun activateUserPackage() {
        viewModel.activateSubjectPackage()
    }

    fun activateTrialPackage(position: Int, subjectName: String){
        viewModel.activateSubjectTrialPackage(position, subjectName)
    }

    fun getActivatedPackageIndex(): LiveData<Int> {
        return viewModel.getIndexOfActivatedPackage()
    }

    override fun onSubscriptionFormNextButtonClicked(subscriptionFormData: SubscriptionFormData) {
        displayEnterMomoNumberDialog(subscriptionFormData)
//        displayInvoice(subscriptionFormData)
    }



    private fun resetMomoPayService() {
        viewModel.resetMomoPayService()
    }

    fun setSubjectPackageDataToActivate(position: Int, subjectPackageData: SubjectPackageData){
        viewModel.setSubjectPackageDataToActivate(subjectPackageData)
//        showSubscriptionForm(position, subjectPackageData.subjectName!!)
        displayPackagesDialog()
    }

    fun getActivatedSubjectPackageData(): SubjectPackageData{
        return viewModel.subjectPackageDataToActivated.value!!
    }

    fun loadSubjectPackageDataFromLocalDbWhere(subjectName: String){
        viewModel.loadSubjectPackageDataFromLocalDbWhere(subjectName)
    }

    fun getIsPackageActive(): Boolean{
        return viewModel.checkSubjectPackageExpiry()
    }

    fun getSubjectPackageData(): SubjectPackageData{
        return viewModel.getSubjectPackageData()
    }


    @SuppressLint("HardwareIds")
    fun getMobileID(): String {
        return Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
    }

    private fun setCurrentTransactionSharedPref(bundle: Bundle?){
        val pref = getSharedPreferences(SUBSCRIPTION_ACTIVITY, MODE_PRIVATE)
        pref.edit().apply {
            putInt(MCQConstants.SUBJECT_INDEX, bundle?.getInt(MCQConstants.SUBJECT_INDEX, 0)!!)
            putString(MCQConstants.SUBJECT_NAME, bundle.getString(MCQConstants.SUBJECT_NAME))
            putInt(MCQConstants.PACKAGE_DURATION, bundle.getInt(MCQConstants.PACKAGE_DURATION))
            putString(MCQConstants.PACKAGE_NAME, bundle.getString(MCQConstants.PACKAGE_NAME))
            putString(MCQConstants.PACKAGE_PRICE, bundle.getString(MCQConstants.PACKAGE_PRICE))
            putString(MCQConstants.MOMO_PARTNER, bundle.getString(MCQConstants.MOMO_PARTNER))
            putBoolean(MCQConstants.IS_ACTIVE, bundle.getBoolean(MCQConstants.IS_ACTIVE))
            putString(MCQConstants.TOKEN, bundle.getString(MCQConstants.TOKEN))
            putString(MCQConstants.TRANSACTION_ID, bundle.getString(MCQConstants.TRANSACTION_ID))
            putString(MCQConstants.TRANSACTION_STATUS, bundle.getString(MCQConstants.TRANSACTION_STATUS))
            apply()
        }
    }

    private fun updateCurrentTransactionSharedPref(bundle: Bundle?, transactionStatus: String, isPackageActive: Boolean){
        val pref = getSharedPreferences(SUBSCRIPTION_ACTIVITY, MODE_PRIVATE)
        pref.edit().apply {
            putInt(MCQConstants.SUBJECT_INDEX, bundle?.getInt(MCQConstants.SUBJECT_INDEX, 0)!!)
            putString(MCQConstants.SUBJECT_NAME, bundle.getString(MCQConstants.SUBJECT_NAME))
            putInt(MCQConstants.PACKAGE_DURATION, bundle.getInt(MCQConstants.PACKAGE_DURATION))
            putString(MCQConstants.PACKAGE_NAME, bundle.getString(MCQConstants.PACKAGE_NAME))
            putString(MCQConstants.PACKAGE_PRICE, bundle.getString(MCQConstants.PACKAGE_PRICE))
            putString(MCQConstants.MOMO_PARTNER, bundle.getString(MCQConstants.MOMO_PARTNER))
            putBoolean(MCQConstants.IS_ACTIVE, isPackageActive)
            putString(MCQConstants.TOKEN, bundle.getString(MCQConstants.TOKEN))
            putString(MCQConstants.TRANSACTION_ID, bundle.getString(MCQConstants.TRANSACTION_ID))
            putString(MCQConstants.TRANSACTION_STATUS, transactionStatus)
            apply()
        }
    }

    private fun getCurrentTransactionFromSharedPref():Bundle{
        val pref = getSharedPreferences(SUBSCRIPTION_ACTIVITY, MODE_PRIVATE)
        val bundle = Bundle().apply {
            putInt(MCQConstants.SUBJECT_INDEX, pref.getInt(MCQConstants.SUBJECT_INDEX, 0))
            putString(MCQConstants.SUBJECT_NAME, pref.getString(MCQConstants.SUBJECT_NAME, null))
            putString(MCQConstants.PACKAGE_NAME, pref.getString(MCQConstants.PACKAGE_NAME, null))
            putInt(MCQConstants.PACKAGE_DURATION, pref.getInt(MCQConstants.PACKAGE_DURATION, 0))
            putString(MCQConstants.PACKAGE_PRICE, pref.getString(MCQConstants.PACKAGE_PRICE, null))
            putString(MCQConstants.MOMO_PARTNER, pref.getString(MCQConstants.MOMO_PARTNER, null))
            putBoolean(MCQConstants.IS_ACTIVE, pref.getBoolean(MCQConstants.IS_ACTIVE, false))
            putString(MCQConstants.TOKEN, pref.getString(MCQConstants.TOKEN, null))
            putString(MCQConstants.TRANSACTION_ID, pref.getString(MCQConstants.TRANSACTION_ID, null))
            putString(MCQConstants.TRANSACTION_STATUS, pref.getString(MCQConstants.TRANSACTION_STATUS, null))
        }
        return bundle
    }

    private fun paymentBackup(bundle: Bundle){
        val subscriptionFormData = SubscriptionFormData().apply {
            subjectPosition = bundle.getInt(MCQConstants.SUBJECT_INDEX)
            subject = bundle.getString(MCQConstants.SUBJECT_NAME)
            packageType = bundle.getString(MCQConstants.PACKAGE_NAME)
            packageDuration = bundle.getInt(MCQConstants.PACKAGE_DURATION)
            packagePrice = bundle.getString(MCQConstants.PACKAGE_PRICE)
            momoPartner = bundle.getString(MCQConstants.MOMO_PARTNER)
        }

        val tokenTransactionIdBundle: Bundle = Bundle().apply {
            putString(MCQConstants.TOKEN, bundle.getString(MCQConstants.TOKEN))
            putString(MCQConstants.TRANSACTION_ID, bundle.getString(MCQConstants.TRANSACTION_ID))
        }

        viewModel.setSubscriptionData(subscriptionFormData)
        showProcessingRequestDialog()
        viewModel.initiatePayment(tokenTransactionIdBundle = tokenTransactionIdBundle)

    }


    private fun resumeTransaction(){
        val bundle = getCurrentTransactionFromSharedPref()
        val currentTransactionStatus = bundle.getString(MCQConstants.TRANSACTION_STATUS)
        val packageStatus = bundle.getBoolean(MCQConstants.IS_ACTIVE)
        println(currentTransactionStatus)
        if(currentTransactionStatus == MCQConstants.PENDING){
            if(!packageStatus){
                paymentBackup(bundle)
            }
        }
    }

    override fun onPackageDialogNextButtonClicked(packageData: PackageData?) {
        showSubscriptionForm(viewModel.subjectPackageDataToActivated.value?.subjectIndex!!, viewModel.subjectPackageDataToActivated.value?.subjectName!!, packageData)
//        println(packageData)

    }

    override fun onPackageDialogCancelButtonClicked() {
//        packagesDialog = null
    }

}