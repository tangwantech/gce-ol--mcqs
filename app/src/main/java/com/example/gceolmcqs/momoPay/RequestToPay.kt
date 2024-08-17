package com.example.gceolmcqs.momoPay

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.gceolmcqs.MCQConstants
import com.example.gceolmcqs.MomoPayService
import com.example.gceolmcqs.R
import com.example.gceolmcqs.datamodels.SubscriptionFormData
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class RequestToPay(private val context: Context) {
    private val isTransactionIdAvailable = MutableLiveData<Boolean?>()
//    private val transactionId = MutableLiveData<String?>()

    fun pay(token: String, subscriptionFormData: SubscriptionFormData, transactionId:MutableLiveData<String?>, transactionIdStatus: TransactionIdStatus){
        val client = OkHttpClient().newBuilder()
            .build()
        val mediaType = MCQConstants.APPLICATION_JSON.toMediaTypeOrNull()
        val requestBody: RequestBody = RequestBody.create(
            mediaType,
            "{\"${MCQConstants.AMOUNT}\":\"${subscriptionFormData.packagePrice}\",\"from\":\"237${subscriptionFormData.momoNumber}\",\"${MCQConstants.DESCRIPTION}\":\"${subscriptionFormData.subject} ${subscriptionFormData.packageType} ${MCQConstants.SUBSCRIPTION}\",\"${MCQConstants.EXTERNAL_REFERENCE}\": \"\"}"
        )
        val request: Request = Request.Builder()
            .url(context.resources.getString(R.string.campay_requestToPay_url))
            .method(MCQConstants.POST, requestBody)
            .addHeader(MCQConstants.AUTHORIZATION, "${MCQConstants.TOKEN} ${token}")
            .addHeader(MCQConstants.CONTENT_TYPE, MCQConstants.APPLICATION_JSON)
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                transactionId.postValue(null)
                transactionIdStatus.onTransactionIdFailed()
//                isTransactionIdAvailable.postValue(false)

            }
            override fun onResponse(call: Call, response: Response) {
                try {
                    val responseBody = response.body?.string()
                    val json = JSONObject(responseBody!!)
                    transactionId.postValue(json[MomoPayService.REFERENCE_ID].toString())
                    transactionIdStatus.onTransactionIdAvailable(json[MomoPayService.REFERENCE_ID].toString())
//                    isTransactionIdAvailable.postValue(true)

                }catch (e: JSONException){
                    transactionId.postValue(null)
                    transactionIdStatus.onTransactionIdFailed()
//                    println("Failed retrieving transaction reference id due to ${e.message}")
//                    isTransactionSuccessful.postValue(false)
//                    isTransactionIdAvailable.postValue(false)
                }


            }

        })
    }

    fun getIsTransactionIdAvailable(): LiveData<Boolean?>{
        return isTransactionIdAvailable
    }

    interface TransactionIdStatus{
        fun onTransactionIdAvailable(transactionId:String)
        fun onTransactionIdFailed()
    }
}