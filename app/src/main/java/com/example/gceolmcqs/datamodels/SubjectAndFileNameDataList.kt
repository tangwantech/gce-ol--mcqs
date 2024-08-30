package com.example.gceolmcqs.datamodels


data class SubjectAndFileNameDataListModel(val subjectAndFileNameDataList: ArrayList<SubjectAndFileNameData>): java.io.Serializable

data class SubjectContents(val contents:ArrayList<ExamTypeData>): java.io.Serializable

data class SubjectAndStatusData(val subject: String, val status: String="Active")



