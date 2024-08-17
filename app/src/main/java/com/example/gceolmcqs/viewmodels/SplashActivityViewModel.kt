package com.example.gceolmcqs.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.gceolmcqs.datamodels.SubjectAndFileNameData
import com.example.gceolmcqs.datamodels.SubjectAndFileNameDataListModel
import com.example.gceolmcqs.repository.RepositoriesLinker
import com.google.gson.Gson

class SplashActivityViewModel : ViewModel() {
    private lateinit var repositoriesLinker: RepositoriesLinker
    private val iSubjectPackageAvailable = MutableLiveData<String>()
    private val _liveSubjectNames = MutableLiveData<ArrayList<String>>()
    val liveSubjectNames: LiveData<ArrayList<String>> = _liveSubjectNames

    private var _serverRetryCountLeft = 4

    private val _remoteRepoErrorExceptionRaised = MutableLiveData<Boolean>()
    val remoteRepoErrorExceptionRaised: LiveData<Boolean> = _remoteRepoErrorExceptionRaised


    private lateinit var subjectAndFileNameDataListModel: SubjectAndFileNameDataListModel

    fun getSubjectAndFileNameDataAt(position: Int): SubjectAndFileNameData {
        return subjectAndFileNameDataListModel.subjectAndFileNameDataList[position]
    }


    fun setSubjectAndFileNameDataListModel(subjectsDataJsonString: String?) {
        subjectAndFileNameDataListModel =
            Gson().fromJson(subjectsDataJsonString!!, SubjectAndFileNameDataListModel::class.java)

        setSubjectNames()

    }

    private fun setSubjectNames() {
        val tempSubjectNames = ArrayList<String>()
        val subjectAndFileList = subjectAndFileNameDataListModel.subjectAndFileNameDataList
        subjectAndFileList.forEach {
            tempSubjectNames.add(it.subject)
        }

        _liveSubjectNames.value = tempSubjectNames
    }

    fun setRepositoryLink(context: Context, mobileId: String){
        repositoriesLinker = RepositoriesLinker().apply {
            setLocalRepo(context, mobileId)
        }
    }

    fun getAreSubjectsPackagesAvailable(): LiveData<Boolean?> {
        return repositoriesLinker.getAreSubjectsPackagesAvailable()
    }


    fun synchronizeSubjectsPackageData(){
        repositoriesLinker.getLocalRepository().syncSubjectsPackageDataFromLocalDbOffLine(
            _liveSubjectNames.value!!,
            repositoriesLinker.getRemoteRepository())
    }

    fun remoteRepoErrorExceptionRaised(): LiveData<Boolean>{
        return repositoriesLinker.getRemoteRepository().remoteRepoErrorExceptionRaised
    }

    fun getServerRetryCountsLeft(): Int{
        return _serverRetryCountLeft
    }

    fun setServerRetryCountsLeft(limit: Int){
        _serverRetryCountLeft = limit
    }

    fun decrementServerRetryCount(){
        _serverRetryCountLeft =- 1
    }

    fun resetServerRetryCount(limit: Int){
        _serverRetryCountLeft = limit
    }



}