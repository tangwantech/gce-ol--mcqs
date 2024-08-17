package com.example.gceolmcqs.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.gceolmcqs.SubjectPackageDataSynchronizer
import com.example.gceolmcqs.datamodels.SubjectPackageData
import com.example.gceolmcqs.roomDB.GceOLMcqDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LocalRepository(private val context: Context){
    private val localDatabase: GceOLMcqDatabase = GceOLMcqDatabase.getDatabase(context)
    private val _areSubjectsPackagesAvailable = MutableLiveData<Boolean?>()

    private val _indexOfActivatedPackage = MutableLiveData<Int>()
    val indexOfActivatedPackage: LiveData<Int> = _indexOfActivatedPackage

    private val _subjectPackageData = MutableLiveData<SubjectPackageData>()
    val subjectPackageData: LiveData<SubjectPackageData> = _subjectPackageData

    fun getAreSubjectsPackagesAvailable(): MutableLiveData<Boolean?>{
        return _areSubjectsPackagesAvailable
    }

    fun insertUserSubjectsPackageDataToLocalDB(
        tempSubjectPackageDataList: List<SubjectPackageData>?,
        subjectIndex: Int?=null
    ){
        tempSubjectPackageDataList?.let {subjectPackageDataList ->

            CoroutineScope(Dispatchers.IO).launch {
                localDatabase.subjectPackageDao().deleteAll()
                subjectPackageDataList.forEach {
                    localDatabase.subjectPackageDao().insert(it)
                }
                withContext(Dispatchers.Main){
                    _areSubjectsPackagesAvailable.value = subjectPackageDataList.isNotEmpty()
                    subjectIndex?.let {
                        _indexOfActivatedPackage.value = it
                    }
                }

            }
        }
    }

    fun getSubjectPackageDataFromLocalDbWhereSubjectName(subjectName: String){
        CoroutineScope(Dispatchers.IO).launch{
            val tempSubjectPackageData = localDatabase.subjectPackageDao().findBySubjectName(subjectName)
            withContext(Dispatchers.Main){
                _subjectPackageData.value = tempSubjectPackageData
            }
        }

    }

    fun syncSubjectsPackageDataFromLocalDbOffLine(subjectNames: List<String>, remoteRepository: RemoteRepository){
        CoroutineScope(Dispatchers.IO).launch {
            val subjectsData = ArrayList<SubjectPackageData>()
            val tempSubjectPackageData = localDatabase.subjectPackageDao().getAllSubjectsPackages()
            if(tempSubjectPackageData.isEmpty()){
                remoteRepository.readUserSubjectsPackagesFromRemoteRepoAtMobileId(subjectNames)
            }else{
                withContext(Dispatchers.Main){
                    subjectsData.addAll(tempSubjectPackageData)
                    val synchronizedData = SubjectPackageDataSynchronizer.syncSubjectPackageList(subjectsData, subjectNames)
                    insertUserSubjectsPackageDataToLocalDB(synchronizedData)
                    remoteRepository.syncSubjectsPackageDataInRemoteRepo(synchronizedData)
                }

            }

        }

    }


    interface SubjectPackagesListener{
        fun onSubjectPackagesAvailable(isAvailable: Boolean)
    }
}