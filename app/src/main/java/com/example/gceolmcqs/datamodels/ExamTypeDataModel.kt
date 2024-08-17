package com.example.gceolmcqs.datamodels
import java.io.Serializable
data class ExamTypeDataModel(val title: String, val examItems: ArrayList<ExamItemDataModel>):Serializable
