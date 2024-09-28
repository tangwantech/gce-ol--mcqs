package com.example.gceolmcqs.viewmodels

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.gceolmcqs.ActivationExpiryDatesGenerator
import com.example.gceolmcqs.MCQConstants
import com.example.gceolmcqs.MomoPayService
import com.example.gceolmcqs.MomoPayServiceManager

import com.example.gceolmcqs.SubjectPackageActivator
import com.example.gceolmcqs.datamodels.SubjectPackageData
import com.example.gceolmcqs.datamodels.SubscriptionFormData

//import com.example.gceolmcq.momoPay.MomoPayService
import com.example.gceolmcqs.repository.RepositoriesLinker


class SubscriptionActivityViewModel: ViewModel() {
    private lateinit var momoPay: MomoPayService
//    private lateinit var momoPay2: com.example.gceolmcq.momoPay.MomoPayService
    private var mobileId: String? = null


//    private lateinit var localDatabase: GceOLMcqDatabase

    private val _subjectPackageDataToActivated = MutableLiveData<SubjectPackageData?>()
    val subjectPackageDataToActivated: LiveData<SubjectPackageData?> = _subjectPackageDataToActivated

    private val _activatedPackageIndexChangedAt = MutableLiveData<Int>()
    val activatedPackageIndexChangedAt: LiveData<Int> = _activatedPackageIndexChangedAt

    private val _subscriptionData = MutableLiveData<SubscriptionFormData>()
    val subscriptionData: LiveData<SubscriptionFormData> = _subscriptionData

    private lateinit var repositoriesLinker: RepositoriesLinker
    private val _transactionStatus = MutableLiveData<String?>()
    val transactionStatus: LiveData<String?> = _transactionStatus

    private val _transactionId = MutableLiveData<String?>()
    val transactionId: LiveData<String?> = _transactionId

    private val _refNumber = MutableLiveData<String?>()
    val refNumber: LiveData<String?> = _refNumber

    private val _currentTransaction = MutableLiveData<Bundle?>()
    val currentTransaction: LiveData<Bundle?> = _currentTransaction



    fun setSubjectPackageDataToActivate(packageData: SubjectPackageData){
        _subjectPackageDataToActivated.value = packageData
    }

    fun setRepositoryLink(context: Context, mobileId: String){
        repositoriesLinker = RepositoriesLinker().apply {
            setLocalRepo(context, mobileId)
        }
    }
    fun getAreSubjectsPackagesAvailable(): LiveData<Boolean?>{
        return repositoriesLinker.getAreSubjectsPackagesAvailable()
    }

    fun getIndexOfActivatedPackage():LiveData<Int>{
        return repositoriesLinker.getIndexOfActivatedPackage()
    }

    fun getRemoteRepoErrorEncountered():LiveData<Boolean>{
        return repositoriesLinker.getRemoteRepository().remoteRepoErrorExceptionRaised
    }

    fun setMomoPayService(momoPayService: MomoPayService){
        momoPay = momoPayService

    }

    fun setMobileId(mobileID: String) {
        this.mobileId = mobileID
    }

    fun setSubscriptionData(subscriptionFormData: SubscriptionFormData){
        _subscriptionData.value = subscriptionFormData
        initCurrentTransaction(subscriptionFormData)

    }

    private fun initCurrentTransaction(subscriptionFormData: SubscriptionFormData){
        _currentTransaction.value = Bundle().apply {
            putInt(MCQConstants.SUBJECT_INDEX, subscriptionFormData.subjectPosition!!)
            putString(MCQConstants.SUBJECT_NAME, subscriptionFormData.subject)
            putString(MCQConstants.PACKAGE_NAME, subscriptionFormData.packageType)
            putInt(MCQConstants.PACKAGE_DURATION, subscriptionFormData.packageDuration!!)
            putString(MCQConstants.PACKAGE_PRICE, subscriptionFormData.packagePrice)
            putString(MCQConstants.MOMO_PARTNER, subscriptionFormData.momoPartner)
//            putBoolean(MCQConstants.IS_ACTIVE, false)
//            putString(MCQConstants.TOKEN, null)
//            putString(MCQConstants.TRANSACTION_ID, null)
//            putString(MCQConstants.TRANSACTION_STATUS, null)
        }
    }

    private fun updateCurrentTransactionToken(token: String){
        val bundle = _currentTransaction.value
        bundle?.putString(MCQConstants.TOKEN, token)
        _currentTransaction.postValue(bundle)
    }

    private fun updateCurrentTransactionId(transactionId: String){
        val bundle = _currentTransaction.value
        bundle?.putString(MCQConstants.TRANSACTION_ID, transactionId)
        _currentTransaction.postValue(bundle)
    }

    private fun updateCurrentTransactionStatus(transactionStatus: String){
        val bundle = _currentTransaction.value
        bundle?.putString(MCQConstants.TRANSACTION_STATUS, transactionStatus)
        _currentTransaction.postValue(bundle)
    }

    fun activateSubjectPackage() {
        val subjectIndex = _subscriptionData.value?.subjectPosition!!
        val subjectName = _subscriptionData.value?.subject!!
        val packageType = _subscriptionData.value?.packageType!!
        val packageDuration = _subscriptionData.value?.packageDuration!!
        val activatedSubjectPackage = SubjectPackageActivator.activateSubjectPackage(subjectName, subjectIndex, packageType, packageDuration)
        updateActivatedPackageInRemoteRepo(activatedSubjectPackage, subjectIndex)

    }

    fun activateSubjectTrialPackage(subjectIndex: Int, subjectName: String){
        val subscriptionForm = SubscriptionFormData(
            subjectPosition = subjectIndex,
            subject = subjectName,
            packageType = MCQConstants.TRIAL,
            packageDuration = MCQConstants.TRIAL_DURATION
        )
        _subscriptionData.value = subscriptionForm
        activateSubjectPackage()
        
    }


    fun initiatePayment(tokenTransactionIdBundle: Bundle?=null){
//        setSubscriptionData(subscriptionFormData)
        println(tokenTransactionIdBundle)

//        MomoPayServiceManager().requestToPay(subscriptionData.value!!, object: MomoPayServiceManager.OnPaymentStatusListener{
//            override fun onPaymentSuccessful() {
//                println("Transaction successful.....")
//                _transactionStatus.postValue(MCQConstants.SUCCESSFUL)
//            }
//
//            override fun onPaymentFailed() {
//                println("Transaction successful.....")
//                _transactionStatus.postValue(MCQConstants.FAILED)
//            }
//
//            override fun onPaymentPending() {
//                println("Transaction successful.....")
//                _transactionStatus.postValue(MCQConstants.PENDING)
//            }
//
//        })

        momoPay.initiatePayment(subscriptionData.value!!, object: MomoPayService.TransactionStatusListener{
            override fun onTransactionTokenAvailable(token: String?) {
                println(token)
                updateCurrentTransactionToken(token!!)
            }

            override fun onTransactionIdAvailable(transactionId: String?) {
                println("Transaction id: $transactionId")
//                _transactionId.postValue(transactionId)
                updateCurrentTransactionId(transactionId!!)

            }

            override fun onReferenceNumberAvailable(refNum: String?) {
                _refNumber.postValue(refNum)
            }

            override fun onTransactionPending() {
                println("Transaction pending......")
                _transactionStatus.postValue(MCQConstants.PENDING)
                updateCurrentTransactionStatus(MCQConstants.PENDING)


            }

            override fun onTransactionFailed() {
                println("Transaction failed.......")
                _transactionStatus.postValue(MCQConstants.FAILED)
                updateCurrentTransactionStatus(MCQConstants.FAILED)

            }

            override fun onTransactionSuccessful() {
                println("Transaction successful.....")
                _transactionStatus.postValue(MCQConstants.SUCCESSFUL)
                updateCurrentTransactionStatus(MCQConstants.SUCCESSFUL)

            }

        }, tokenTransactionIdBundle = tokenTransactionIdBundle)

    }


    fun resetMomoPayService() {
        momoPay.reset()

//        momoPay = null

    }
    fun resetTransactionStatus(){
        _transactionStatus.value = null
    }

    fun getIsPaymentSystemAvailable():LiveData<Boolean?>{
        return momoPay.isPaymentSystemAvailable
    }

    private fun updateActivatedPackageInRemoteRepo(subjectPackageData: SubjectPackageData, position: Int){
        repositoriesLinker.getRemoteRepository().updateActivatedPackageInRemoteRepo(subjectPackageData, position)
    }

    fun loadSubjectPackageDataFromLocalDbWhere(subjectName: String){
        repositoriesLinker.getLocalRepository().getSubjectPackageDataFromLocalDbWhereSubjectName(subjectName)
    }

    fun checkSubjectPackageExpiry(): Boolean{
        val activatedOn = repositoriesLinker.getLocalRepository().subjectPackageData.value?.activatedOn!!
        val expiresOn = repositoriesLinker.getLocalRepository().subjectPackageData.value?.expiresOn!!
        return ActivationExpiryDatesGenerator().checkExpiry(activatedOn, expiresOn)
    }

    fun getSubjectPackageData(): SubjectPackageData{
        return repositoriesLinker.getLocalRepository().subjectPackageData.value!!
    }

}
