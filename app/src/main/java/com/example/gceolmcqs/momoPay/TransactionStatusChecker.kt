package com.example.gceolmcqs.momoPay

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.gceolmcqs.MCQConstants
import com.example.gceolmcqs.MomoPayService
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class TransactionStatusChecker {
    private val transactionStatusChanged = MutableLiveData<String?>()

    fun getTransactionStatusChanged(): LiveData<String?>{
        println("getting transaction status........")
        return transactionStatusChanged
    }

    fun testPaySuccessful(){

        transactionStatusChanged.value = MCQConstants.SUCCESSFUL


    }

    fun checkTransactionStatus(token: String, transactionId: String, transactionStatusChanged: MutableLiveData<String?>, paymentStatus: PaymentStatus, delayBy: Long=3000){

        runBlocking {
            transactionStatusChanged.postValue(MCQConstants.PENDING)
            while (transactionStatusChanged.value == MCQConstants.PENDING){
                val client = OkHttpClient().newBuilder()
                    .build()
                val request: Request = Request.Builder()
                    .url("${ MCQConstants.TRANSACTION_STATUS_URL }$transactionId/")

                    .addHeader(MCQConstants.AUTHORIZATION, "${MCQConstants.TOKEN} $token")
                    .addHeader(MCQConstants.CONTENT_TYPE, MCQConstants.APPLICATION_JSON)
                    .build()
                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
//                        transactionStatusChanged.postValue(null)
                        transactionStatusChanged.postValue(null)
                        paymentStatus.onPaymentFailed()
                    }
                    override fun onResponse(call: Call, response: Response) {
                        try{
                            val responseBody = response.body?.string()
//                    println(responseBody)
                            val jsonResponse = JSONObject(responseBody!!)
                            transactionStatusChanged.postValue(jsonResponse[MomoPayService.STATUS].toString())
                            paymentStatus.onPaymentStatusChanged(jsonResponse[MomoPayService.STATUS].toString())

                        }catch (e: JSONException){
                            transactionStatusChanged.postValue(null)
//                    isTransactionSuccessful.postValue(false)
//                            transactionStatusChanged.postValue(null)
                        }

                    }

                })
                delay(delayBy)

            }
        }

    }

    interface PaymentStatus{
        fun onPaymentStatusChanged(status:String)
        fun onPaymentFailed()

    }
}