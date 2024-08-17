package com.example.gceolmcqs.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.gceolmcqs.repository.RepositoriesLinker

class ActivateTrialPackageFragmentViewModel: ViewModel() {

    private lateinit var repositoriesLinker: RepositoriesLinker

    private val _remoteRepoErrorMessage = MutableLiveData<String>()
    val remoteRepoErrorMessage: LiveData<String> = _remoteRepoErrorMessage

    private val _liveSubjectsAvailable = MutableLiveData<List<String>>()
    val subjectsAvailable: LiveData<List<String>> = _liveSubjectsAvailable

    fun setRepositoryLink(context: Context, mobileId: String){
        repositoriesLinker = RepositoriesLinker().apply {
            setLocalRepo(context, mobileId)
        }
    }

    fun getAreSubjectsPackagesAvailable(): LiveData<Boolean?>{
        return repositoriesLinker.getAreSubjectsPackagesAvailable()
    }

    fun setSubjectNames(subjectNames: List<String>) {
        _liveSubjectsAvailable.value = subjectNames
    }

    fun readSubjectsPackagesByMobileIdFromRemoteRepo() {
        repositoriesLinker.getRemoteRepository().readUserSubjectsPackagesFromRemoteRepoAtMobileId(_liveSubjectsAvailable.value)

    }

}