package com.example.gceolmcqs.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.gceolmcqs.datamodels.*
import com.google.gson.Gson

class MainActivityViewModel : ViewModel() {
    private val _liveSubjectsAvailable = MutableLiveData<ArrayList<String>>()
    val liveSubjectsAvailable: LiveData<ArrayList<String>> = _liveSubjectsAvailable
    private lateinit var subjectAndFileNameDataListModel: SubjectAndFileNameDataListModel

    fun getSubjectAndFileNameDataAt(position: Int): SubjectAndFileNameData {
        return subjectAndFileNameDataListModel.subjectAndFileNameDataList[position]
    }

    fun setSubjectAndFileNameDataListModel(subjectsDataJsonString: String?) {
        subjectAndFileNameDataListModel =
            Gson().fromJson(subjectsDataJsonString!!, SubjectAndFileNameDataListModel::class.java)
        val subjectAndFile = subjectAndFileNameDataListModel.subjectAndFileNameDataList
        setSubjectNames(subjectAndFile)

    }

    private fun setSubjectNames(temp: ArrayList<SubjectAndFileNameData>) {
        val tempSubjectNames = ArrayList<String>()
        temp.forEach {
            tempSubjectNames.add(it.subject)
        }

        _liveSubjectsAvailable.value = tempSubjectNames
    }
}

