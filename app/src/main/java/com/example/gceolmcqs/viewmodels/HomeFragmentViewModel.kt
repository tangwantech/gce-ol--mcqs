package com.example.gceolmcqs.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gceolmcqs.ActivationExpiryDatesGenerator
import com.example.gceolmcqs.MCQConstants
import com.example.gceolmcqs.datamodels.SubjectPackageData
import com.example.gceolmcqs.repository.LocalRepository
import com.example.gceolmcqs.roomDB.GceOLMcqDatabase
import kotlinx.coroutines.*

class HomeFragmentViewModel: ViewModel() {
    private lateinit var gceOLMcqDatabase: GceOLMcqDatabase
    private lateinit var localRepo: LocalRepository
    private val _subjectPackageDataList = MutableLiveData<ArrayList<SubjectPackageData>?>(ArrayList())
    val subjectPackageDataList: LiveData<ArrayList<SubjectPackageData>?> = _subjectPackageDataList

    private val _packageExpiredIndex = MutableLiveData<Int>()
    val packageExpiredIndex: LiveData<Int> = _packageExpiredIndex


    fun initGceOLMcqDatabase(context: Context){
        gceOLMcqDatabase = GceOLMcqDatabase.getDatabase(context)
    }

//    fun initLocalRepo(context: Context){
//        localRepo = LocalRepository(context)
//    }

    fun initSubjectPackagesDataListFromLocalDatabase(){
        viewModelScope.launch(Dispatchers.IO) {
            _subjectPackageDataList.value!!.clear()

            val tempSubjectPackageDataList1 = gceOLMcqDatabase.subjectPackageDao().getAllSubjectsPackages()

            println("TempSubjectPackageList: $tempSubjectPackageDataList1")
            val tempSubjectPackageDataList2 = ArrayList<SubjectPackageData>()
            if (tempSubjectPackageDataList1.isNotEmpty()){
                tempSubjectPackageDataList1.forEachIndexed { index, subjectPackageData ->
                    tempSubjectPackageDataList2.add(subjectPackageData)
                    if(subjectPackageData.packageName != MCQConstants.NA){
                        val isActive = ActivationExpiryDatesGenerator().checkExpiry(subjectPackageData.activatedOn!!, subjectPackageData.expiresOn!!)

                        tempSubjectPackageDataList2[index].isPackageActive = isActive
                        println("Is package Active: ${subjectPackageData.isPackageActive}")
                    }

                }
            }
            _subjectPackageDataList.postValue(tempSubjectPackageDataList2)
        }
    }

//    fun checkPackageExpiry(position: Int){
//
//        viewModelScope.launch(Dispatchers.IO) {
////            println(_subjectPackageDataList.value)
//
//            var notExpired = ActivationExpiryDatesGenerator().checkExpiry(_subjectPackageDataList.value!![position].activatedOn!!, _subjectPackageDataList.value!![position].expiresOn!!)
//            while (notExpired){
//                delay(1000)
//                notExpired = ActivationExpiryDatesGenerator().checkExpiry(_subjectPackageDataList.value!![position].activatedOn!!, _subjectPackageDataList.value!![position].expiresOn!!)
//
//            }
//
//            withContext(Dispatchers.Main){
//                this@HomeFragmentViewModel._subjectPackageDataList.value!![position].isPackageActive = notExpired
//                _packageExpiredIndex.value = position
//
//            }
//
//        }
//    }

}