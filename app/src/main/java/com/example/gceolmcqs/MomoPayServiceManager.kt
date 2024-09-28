package com.example.gceolmcqs

import com.example.gceolmcqs.datamodels.SubscriptionFormData
import net.compay.android.CamPay
import net.compay.android.models.requests.CollectionRequest
import java.util.UUID
import java.util.concurrent.TimeUnit

class MomoPayServiceManager {
    private val camPay = CamPay.getInstance()

    fun requestToPay(subscriptionFormData: SubscriptionFormData, onPaymentStatusListener: OnPaymentStatusListener){

        val temp = camPay.collect(
            CollectionRequest.CollectionRequestBuilder
                .aCollectionRequest()
                .withAmount(subscriptionFormData.packagePrice)
                .withFrom("237${subscriptionFormData.momoNumber}")
                .withDescription("${subscriptionFormData.subject} ${subscriptionFormData.packageType} subscription")
                .withExternalReference(UUID.randomUUID().toString())
                .withCurrency("XAF")
                .build()
        ).delay(10, TimeUnit.SECONDS) // delay for a minute before checking the transaction status
            .switchMap { collectResponse ->
                println(collectResponse)
                return@switchMap camPay.transactionStatus(collectResponse.reference) //  check the transaction status
            }.subscribe { transactionStatusResponse ->
                println(transactionStatusResponse)
                when (transactionStatusResponse.status){
                    "SUCCESSFUL" -> onPaymentStatusListener.onPaymentSuccessful()
                    "FAILED" -> onPaymentStatusListener.onPaymentFailed()
                    "PENDING" -> onPaymentStatusListener.onPaymentPending()
                }

            }

    }

    interface OnPaymentStatusListener{
        fun onPaymentSuccessful()
        fun onPaymentFailed()
        fun onPaymentPending()

    }

}