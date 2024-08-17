package com.example.gceolmcqs.datamodels

data class PendingTransactionData(
    val referenceId: String,
    val momoPartner: String,
    val packageName: String,
    val amountToPay: String
)
