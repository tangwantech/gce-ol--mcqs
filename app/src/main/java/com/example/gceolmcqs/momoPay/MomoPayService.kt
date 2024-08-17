package com.example.gceolmcqs.momoPay

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.gceolmcqs.datamodels.SubscriptionFormData

class MomoPayService(private val context: Context) {
    private val tokenGenerator = TokenGenerator(context)
    private val requestToPay = RequestToPay(context)
    private val transactionStatusChecker = TransactionStatusChecker()

    private val token = MutableLiveData<String?>()
    private val transactionId = MutableLiveData<String?>()
    private val transactionStatusChanged = MutableLiveData<String?>()

    fun generateToken(){
        tokenGenerator.generate(token, object: TokenGenerator.TokenStatus{
            override fun onTokenAvailable(token: String) {
                this@MomoPayService.token.postValue(token)
            }

            override fun onTokenFailed() {
                this@MomoPayService.token.postValue(null)
            }

        })
    }

    fun getIsTokenAvailable(): LiveData<Boolean?>{
        return tokenGenerator.getIsTokenAvailable()
    }

    fun testPaySuccessful(){
        transactionStatusChecker.testPaySuccessful()
    }

    fun pay(subscriptionFormData: SubscriptionFormData){
        requestToPay.pay(token.value!!, subscriptionFormData, transactionId, object: RequestToPay.TransactionIdStatus{
            override fun onTransactionIdAvailable(transactionId: String) {
                println(transactionId)
                this@MomoPayService.transactionId.postValue(transactionId)
            }

            override fun onTransactionIdFailed() {
                this@MomoPayService.transactionId.postValue(null)
            }

        })
    }

    fun getIsTransactionIdAvailable(): LiveData<Boolean?>{
        return requestToPay.getIsTransactionIdAvailable()
    }

    fun checkTransactionStatus(){
        transactionStatusChecker.checkTransactionStatus(token.value!!, transactionId.value!!, transactionStatusChanged, object:TransactionStatusChecker.PaymentStatus{
            override fun onPaymentStatusChanged(status: String) {
                this@MomoPayService.transactionStatusChanged.postValue(status)
            }

            override fun onPaymentFailed() {
                this@MomoPayService.transactionStatusChanged.postValue(null)
            }

        })
    }

    fun getTransactionStatusChanged(): LiveData<String?>{
        return transactionStatusChanged
    }

    fun getTransactionId(): LiveData<String?>{
        return transactionId
    }

    fun getToken(): LiveData<String?>{
        return token
    }
}