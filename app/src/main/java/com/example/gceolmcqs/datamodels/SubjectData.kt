package com.example.gceolmcqs.datamodels

data class SubjectData(
    val title: String,
    val contents: ArrayList<ExamTypeDataModel>? = null
): java.io.Serializable
