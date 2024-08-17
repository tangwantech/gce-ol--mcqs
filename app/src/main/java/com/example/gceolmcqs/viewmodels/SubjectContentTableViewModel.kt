package com.example.gceolmcqs.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gceolmcqs.ActivationExpiryDatesGenerator
import com.example.gceolmcqs.datamodels.ExamItemDataModel
import com.example.gceolmcqs.datamodels.ExamTypeDataModel
import com.example.gceolmcqs.datamodels.SubjectData
import com.example.gceolmcqs.datamodels.SubjectPackageData
import com.example.gceolmcqs.roomDB.GceOLMcqDatabase
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SubjectContentTableViewModel : ViewModel() {
    private lateinit var subjectName: String
    private lateinit var subjectData: SubjectData
    private val examTitles: ArrayList<String?> = ArrayList()
    private val examContents: HashMap<Int, ArrayList<String>?> = HashMap()
    private val examContentsFileNames: HashMap<String, String> = HashMap()
    private val isSubjectPackageActive = MutableLiveData<Boolean>()

    private lateinit var gceOLMcqDatabase: GceOLMcqDatabase

    private val _subjectPackageData = MutableLiveData<SubjectPackageData>()
    val subjectPackageData: LiveData<SubjectPackageData> = _subjectPackageData

    fun initDatabase(context: Context) {
        gceOLMcqDatabase = GceOLMcqDatabase.getDatabase(context)
    }

    fun initSubjectContentsData(jsonFile: String) {
        subjectData = Gson().fromJson(jsonFile, SubjectData::class.java)

        setExamTitles()
    }

    fun querySubjectPackageDataFromLocalDatabaseAtSubjectName(subjectName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val subjectPackageData =
                gceOLMcqDatabase.subjectPackageDao().findBySubjectName(subjectName)
            _subjectPackageData.postValue(subjectPackageData)
        }
    }

    private fun setExamTitles() {
        subjectData.contents!!.forEachIndexed { index, examTypeData ->
            examTitles.add(examTypeData.title)
            setExamContents(examTypeData, index)
        }


    }

    fun getExamTypeDataAt(position: Int): ExamTypeDataModel {
        return (subjectData.contents!![position])
    }

    fun getExamTitles(): ArrayList<String?> {
        return examTitles
    }

    fun getExamTypesCount(): Int {
        return (subjectData.contents!!.size)
    }

    private fun setExamContents(examTypeDataModel: ExamTypeDataModel, index: Int) {
        val examContents: ArrayList<String> = ArrayList()

        examTypeDataModel.examItems.forEach {
            examContents.add(it.title)
            setExamContentsFileNames(it)
        }
        this.examContents[index] = examContents

    }

    private fun setExamContentsFileNames(examItemDataModel: ExamItemDataModel) {
        examContentsFileNames[examItemDataModel.title] = examItemDataModel.fileName
    }

    fun getIsPackageActive(): LiveData<Boolean> {
        return isSubjectPackageActive
    }

    fun setSubjectName(subjectTitle: String) {
        subjectName = subjectTitle
    }
    fun getPackageStatus(): Boolean{
        return ActivationExpiryDatesGenerator().checkExpiry(_subjectPackageData.value!!.activatedOn!!, _subjectPackageData.value!!.expiresOn!!)
    }
}