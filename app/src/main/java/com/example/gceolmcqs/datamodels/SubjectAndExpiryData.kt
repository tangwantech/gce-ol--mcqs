package com.example.gceolmcqs.datamodels

data class SubjectAndExpiryData(
    val position: Int,
    val subjectName: String,
    var expiresOn: String
) : java.io.Serializable
