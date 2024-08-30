package com.example.gceolmcqs.datamodels

data class SubjectData(
    val title: String,
    val contents: ArrayList<ExamTypeData>? = null
): java.io.Serializable
