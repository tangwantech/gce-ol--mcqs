package com.example.gceolmcqs

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.gceolmcqs.datamodels.SubscriptionFormData
import com.example.gceolmcqs.datamodels.TransactionStatus

import kotlinx.coroutines.*
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

import javax.net.ssl.*
import java.security.cert.X509Certificate


class MomoPayService(private val context: Context) {
    companion object {
        const val REFERENCE_ID = "reference"
        const val TOKEN = "token"
        const val STATUS = "status"
        const val SUCCESSFUL = "SUCCESSFUL"
    }


    private lateinit var client: OkHttpClient

    private var subscriptionFormData: SubscriptionFormData? = null
    private var isTransactionSuccessful = MutableLiveData<Boolean?>()
    private var transactionStatus = MutableLiveData<TransactionStatus>()
    private val _isPaymentSystemAvailable = MutableLiveData<Boolean?>(true)
    val isPaymentSystemAvailable: LiveData<Boolean?> = _isPaymentSystemAvailable


    fun initiatePayment(
        subscriptionFormData: SubscriptionFormData,
        transactionStatusListener: TransactionStatusListener,
        tokenTransactionIdBundle: Bundle? = null
    ) {

        this.subscriptionFormData = subscriptionFormData
//        generateAccessToken(transactionStatusListener)

        testUpdateTransactionSuccessful(transactionStatusListener)

    }

    private fun generateAccessToken(transactionStatusListener: TransactionStatusListener) {

        // Create a custom TrustManager that trusts the server's SSL certificate
        val trustAllCertificates = object : X509TrustManager {
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
        }

// Create an SSLContext with the custom TrustManager to use in your network calls
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(
            null,
            arrayOf<TrustManager>(trustAllCertificates),
            java.security.SecureRandom()
        )

// Apply the SSLContext to your HTTP client (assuming you're using Retrofit or similar)

        client = OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustAllCertificates)
            .hostnameVerifier { _, _ -> true }
            .build()

        val requestBody = FormBody.Builder()
            .add(MCQConstants.USER_NAME, context.getString(R.string.campay_app_user_name))
            .add(MCQConstants.PASS_WORD, context.getString(R.string.campay_app_pass_word))
            .build()

        val request = Request.Builder()
            .url(context.getString(R.string.campay_token_url))
            .post(requestBody)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("failed generating token due to ${e.message}")
                transactionStatusListener.onTransactionFailed()
                call.cancel()

            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val responseBody = response.body?.string()
                    println(responseBody)
                    val json = JSONObject(responseBody!!)
                    val transaction = TransactionStatus()
                    val tokenString = json[TOKEN].toString()
                    transaction.token = tokenString
                    transactionStatusListener.onTransactionTokenAvailable(tokenString)
                    requestToPay(
                        transaction,
                        subscriptionFormData?.packagePrice,
                        subscriptionFormData?.momoNumber,
                        transactionStatusListener
                    )
                } catch (e: JSONException) {
                    transactionStatusListener.onTransactionFailed()
                    call.cancel()
                }

            }

        })
    }

    fun requestToPay(
        transaction: TransactionStatus,
        amountToPay: String?,
        momoNumber: String?,
        transactionStatusListener: TransactionStatusListener
    ) {

        val requestBody = FormBody.Builder()
            .add(MCQConstants.AMOUNT, "$amountToPay")
            .add(MCQConstants.FROM, "${MCQConstants.COUNTRY_CODE}$momoNumber")
            .add(
                MCQConstants.DESCRIPTION,
                "${subscriptionFormData?.subject} ${subscriptionFormData?.packageType} ${MCQConstants.SUBSCRIPTION}"
            ).build()

        val request = Request.Builder()
            .url(context.getString(R.string.campay_requestToPay_url))
            .post(requestBody)
            .addHeader(MCQConstants.AUTHORIZATION, "${MCQConstants.TOKEN} ${transaction.token}")
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
//                println("failed initiating request to pay ...... $transaction due to ${e.message}")
//                }
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val responseBody = response.body?.string()
                    val json = JSONObject(responseBody!!)
                    val refIdString = json[REFERENCE_ID].toString()
                    transaction.refId = refIdString
                    transactionStatusListener.onTransactionIdAvailable(refIdString)
                    runBlocking {
                        transaction.status = MCQConstants.PENDING
                        while (transaction.status!! == MCQConstants.PENDING) {
                            checkTransactionStatus(transaction, transactionStatusListener)
                            delay(MCQConstants.STATUS_CHECK_DURATION)
                        }
                    }

                } catch (e: JSONException) {
                    println("Inside json exception of on response of request to pay")
                    transactionStatusListener.onTransactionFailed()
                }

            }

        })
    }


    fun checkTransactionStatus(
        transaction: TransactionStatus,
        transactionStatusListener: TransactionStatusListener
    ) {
//        val client = OkHttpClient().newBuilder().build()
        val request: Request = Request.Builder()
            .url("${MCQConstants.TRANSACTION_STATUS_URL}${transaction.refId}/")
            .addHeader(MCQConstants.AUTHORIZATION, "${MCQConstants.TOKEN} ${transaction.token}")
            .addHeader(MCQConstants.CONTENT_TYPE, MCQConstants.APPLICATION_JSON)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                transactionStatusListener.onTransactionFailed()
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val responseBody = response.body?.string()
                    val jsonResponse = JSONObject(responseBody!!)
                    transaction.status = jsonResponse[STATUS].toString()
//                    println(responseBody)
                    when (jsonResponse[STATUS].toString()) {
                        MCQConstants.PENDING -> {
                            transactionStatusListener.onTransactionPending()
                        }

                        MCQConstants.SUCCESSFUL -> {

                            transactionStatusListener.onTransactionSuccessful()
                        }
                        MCQConstants.FAILED -> {
                            transactionStatusListener.onTransactionFailed()
                        }
                    }

                } catch (e: JSONException) {
                    transactionStatusListener.onTransactionFailed()
                }
            }

        })

    }

    private fun testUpdateTransactionSuccessful(transactionStatusListener: TransactionStatusListener) {
        isTransactionSuccessful.value = true
        transactionStatus.value = TransactionStatus(status = SUCCESSFUL)
        transactionStatusListener.onTransactionSuccessful()
    }


    fun reset() {
        isTransactionSuccessful.postValue(null)
        transactionStatus.postValue(TransactionStatus())
        subscriptionFormData = null
    }

    interface TransactionStatusListener{
        fun onTransactionTokenAvailable(token: String?)
        fun onTransactionIdAvailable(transactionId: String?)
        fun onReferenceNumberAvailable(refNum:String?)
        fun onTransactionPending()
        fun onTransactionFailed()
        fun onTransactionSuccessful()
    }


}