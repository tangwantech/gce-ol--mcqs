package com.example.gceolmcqs.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class RepositoriesLinker {
    private lateinit var localRepository: LocalRepository
    private lateinit var remoteRepository: RemoteRepository
    private lateinit var _areSubjectsPackagesAvailable: MutableLiveData<Boolean?>


    fun setLocalRepo(context: Context, mobileId: String?){
        localRepository = LocalRepository(context)
        initAreSubjectsPackagesAvailable()
        setRemoteRepo(mobileId)

    }

    private fun initAreSubjectsPackagesAvailable(){
        _areSubjectsPackagesAvailable = localRepository.getAreSubjectsPackagesAvailable()
    }

    fun getAreSubjectsPackagesAvailable(): LiveData<Boolean?> {
        return _areSubjectsPackagesAvailable
    }

    fun getIndexOfActivatedPackage(): LiveData<Int>{
        return localRepository.indexOfActivatedPackage
    }

    private fun setRemoteRepo(mobileId: String?){
        remoteRepository = RemoteRepository(localRepository)
        remoteRepository.setMobileId(mobileId!!)
    }

    fun getLocalRepository(): LocalRepository{
        return localRepository
    }

    fun getRemoteRepository(): RemoteRepository{
        return remoteRepository
    }
}