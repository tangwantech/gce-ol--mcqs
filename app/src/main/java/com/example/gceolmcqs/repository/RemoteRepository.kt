package com.example.gceolmcqs.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.gceolmcqs.MCQConstants
import com.example.gceolmcqs.SubjectPackageActivator
import com.example.gceolmcqs.SubjectPackageDataSynchronizer
import com.example.gceolmcqs.datamodels.SubjectPackageData
import com.parse.ParseObject
import com.parse.ParseQuery
import org.json.JSONArray
import org.json.JSONObject

class RemoteRepository(private val localRepository: LocalRepository) {
    private var userRemoteParseObject = MutableLiveData<ParseObject?>(null)
    private val _remoteRepoErrorExceptionRaised = MutableLiveData<Boolean>()
    val remoteRepoErrorExceptionRaised: LiveData<Boolean> = _remoteRepoErrorExceptionRaised

    private val _areSubjectsPackagesAvailable = localRepository.getAreSubjectsPackagesAvailable()
//
    private var mobileId: String? = null

    fun setMobileId(id: String){
        mobileId = id
        println("Mobile device id: $id")
    }

    private fun insertUserSubjectsPackageDataToRemoteRepo(subjectPackageDataList: List<SubjectPackageData>?, subjectsAvailable: List<String>?){

        println("Subject package to insert to remote repo: $subjectPackageDataList")
        subjectPackageDataList?.let {
            if(userRemoteParseObject.value == null){
                println("Creating new user")
                userRemoteParseObject.value = ParseObject(MCQConstants.PARSE_CLASS)
                println("New user: ${userRemoteParseObject.value}")
            }
            val jsonArray = JSONArray()
            it.forEachIndexed { _, subjectPackageData ->
                val jsonObject = JSONObject()
                jsonObject.apply {
                    put(MCQConstants.SUBJECT_INDEX, subjectPackageData.subjectIndex)
                    put(MCQConstants.SUBJECT_NAME, subjectPackageData.subjectName)
                    put(MCQConstants.PACKAGE_NAME, subjectPackageData.packageName)
                    put(MCQConstants.ACTIVATED_ON, subjectPackageData.activatedOn)
                    put(MCQConstants.EXPIRES_ON, subjectPackageData.expiresOn)
                }
                jsonArray.put(jsonObject)

            }

            userRemoteParseObject.value?.put(MCQConstants.MOBILE_ID, mobileId!!)
            userRemoteParseObject.value?.put(MCQConstants.SUBJECTS_PACKAGES, jsonArray)

            userRemoteParseObject.value?.saveInBackground {e ->
//                println("Saving......")
                if (e == null) {
                    println("Saving to remote repo was successsful")
                    readUserSubjectsPackagesFromRemoteRepoAtMobileId(subjectsAvailable)
                } else {
                    println("Saving to remote repo failed")
                    _remoteRepoErrorExceptionRaised.value = true
                }
            }
        }
    }

    fun readUserSubjectsPackagesFromRemoteRepoAtMobileId(subjectsAvailable: List<String>?=null, position: Int?=null)  {

        val query: ParseQuery<ParseObject> = ParseQuery(MCQConstants.PARSE_CLASS)
        query.whereEqualTo(MCQConstants.MOBILE_ID, mobileId)

        query.findInBackground { objects, e ->
            val packageDataList = ArrayList<SubjectPackageData>()
            if (e == null) {

                if (objects.isNotEmpty()) {
                    userRemoteParseObject.value = objects[0]


                    val jsonArraySubjectPackages: JSONArray? =
                        userRemoteParseObject.value!!.getJSONArray(MCQConstants.SUBJECTS_PACKAGES)
                    for (index in 0 until jsonArraySubjectPackages!!.length()) {

                        val jsonObject = jsonArraySubjectPackages.getJSONObject(index)

                        val subjectPackageData = SubjectPackageData(
                            subjectIndex = jsonObject.getInt(MCQConstants.SUBJECT_INDEX),
                            subjectName = jsonObject.getString(MCQConstants.SUBJECT_NAME),
                            packageName = jsonObject.getString(MCQConstants.PACKAGE_NAME),
                            activatedOn = jsonObject.getString(MCQConstants.ACTIVATED_ON),
                            expiresOn = jsonObject.getString(MCQConstants.EXPIRES_ON)
                        )
                        packageDataList.add(subjectPackageData)

                    }
                    val synchronizedData = SubjectPackageDataSynchronizer.syncSubjectPackageList(packageDataList, subjectsAvailable)
                    insertUserSubjectsPackageDataToLocalDB(synchronizedData, position)


                } else {
                    println("Data not in remote repo")
                    println("Activating trial package")
                    val activatedPackages = SubjectPackageActivator.activateTrialPackageForAllSubjectsAvailable(subjectsAvailable)
//                    insertUserSubjectsPackageDataToRemoteRepo(activatedPackages, subjectsAvailable)
//                    val subjectsPackages = SubjectsPackagesBuilder().apply{}.build(subjectsAvailable)
                    insertUserSubjectsPackageDataToRemoteRepo(activatedPackages, subjectsAvailable)
                }

            } else {
                _remoteRepoErrorExceptionRaised.value = true
                _areSubjectsPackagesAvailable.value = false
                println("Failed to activate trial package ${e.localizedMessage?.toString()}")
            }
        }
    }

    fun updateActivatedPackageInRemoteRepo(subjectPackageData: SubjectPackageData, position: Int){
        val query: ParseQuery<ParseObject> = ParseQuery(MCQConstants.PARSE_CLASS)
        query.whereEqualTo(MCQConstants.MOBILE_ID, mobileId)
        query.limit = 1
        query.findInBackground { objects, e ->
            if (e == null) {
                val parseObject = objects[0]

//                val jsonArray = JSONArray()
                val jsonArray = parseObject.getJSONArray(MCQConstants.SUBJECTS_PACKAGES)
                val jsonObject = jsonArray?.getJSONObject(position)
//                val jsonObject = JSONObject()
                jsonObject?.apply {
                    put(MCQConstants.SUBJECT_INDEX, subjectPackageData.subjectIndex)
                    put(MCQConstants.SUBJECT_NAME, subjectPackageData.subjectName)
                    put(MCQConstants.PACKAGE_NAME, subjectPackageData.packageName)
                    put(MCQConstants.ACTIVATED_ON, subjectPackageData.activatedOn)
                    put(MCQConstants.EXPIRES_ON, subjectPackageData.expiresOn)
                }

                jsonArray?.put(position, jsonObject)
                parseObject.put(MCQConstants.SUBJECTS_PACKAGES, jsonArray!!)
                parseObject.saveInBackground {
                    if (it == null) {
                        println("Updated successfully")
                        readUserSubjectsPackagesFromRemoteRepoAtMobileId(position=position)
                    } else {
                        _remoteRepoErrorExceptionRaised.value = true
                    }
                }
            }
        }
    }

    private fun insertUserSubjectsPackageDataToLocalDB(tempSubjectPackageDataList: List<SubjectPackageData>?, position: Int?){
        localRepository.insertUserSubjectsPackageDataToLocalDB(tempSubjectPackageDataList, position)
    }

    fun syncSubjectsPackageDataInRemoteRepo(subjectPackageDataList: List<SubjectPackageData>?){
        val query: ParseQuery<ParseObject> = ParseQuery(MCQConstants.PARSE_CLASS)
        query.whereEqualTo(MCQConstants.MOBILE_ID, mobileId)
        query.findInBackground { objects, e ->
            if (e == null) {

                if (objects.isNotEmpty()) {
                    val parseObject = objects[0]
                    val jsonArray = JSONArray()

                    subjectPackageDataList?.forEach {
                        val jsonObject = JSONObject()
                        jsonObject.apply {
                            put(MCQConstants.SUBJECT_INDEX, it.subjectIndex)
                            put(MCQConstants.SUBJECT_NAME, it.subjectName)
                            put(MCQConstants.PACKAGE_NAME, it.packageName)
                            put(MCQConstants.ACTIVATED_ON, it.activatedOn)
                            put(MCQConstants.EXPIRES_ON, it.expiresOn)
                        }
                        jsonArray.put(jsonObject)
                    }

                    parseObject.put(MCQConstants.MOBILE_ID, mobileId!!)
                    parseObject.put(MCQConstants.SUBJECTS_PACKAGES, jsonArray)

                    parseObject.saveInBackground {_ -> }

                }

            }
        }

    }


}