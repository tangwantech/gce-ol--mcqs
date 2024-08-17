package com.example.gceolmcqs

import com.example.gceolmcqs.datamodels.ActivationExpiryDates
import com.example.gceolmcqs.datamodels.SubjectPackageData

class SubjectPackageDataSynchronizer {
    companion object{
        fun syncSubjectPackageList(
            packageDataList: ArrayList<SubjectPackageData>,
            liveSubjectsAvailable: List<String>?
//        expiryDataList: ArrayList<String>
        ): List<SubjectPackageData> {
            liveSubjectsAvailable?.let{
                val subjectNamesInPackageDataList = getSubjectNamesFromPackageDataList(packageDataList)
                if (it.size > packageDataList.size) {
                    val activationExpiryDates =
                        ActivationExpiryDatesGenerator.generateTrialActivationExpiryDates(
                            MCQConstants.HOURS,
                            MCQConstants.TRIAL_DURATION
                        )
                    val subjectsToAdd = getSubjectsNotInPackageDataList(it, subjectNamesInPackageDataList)
                    val newTrialPackages = getNewTrialPackages(subjectsToAdd, subjectNamesInPackageDataList.size, activationExpiryDates)

//                    for (index in packageDataList.size until it.size) {
//
//                        val subjectPackageData = SubjectPackageData(
//                            index,
//                            it[index],
//                            "TRIAL",
//                            activationExpiryDates.activatedOn,
//                            activationExpiryDates.expiresOn
//                        )
//
//
////                expiryDataList.add(subjectPackageData.expiresOn!!)
//                    }
                    packageDataList.addAll(newTrialPackages)

                }

                if(it.size < packageDataList.size){
//                    val subjectNamesInPackageDataList = getSubjectNamesFromPackageDataList(packageDataList)
                    subjectNamesInPackageDataList.forEachIndexed { index, subjectNameInPackageDataList ->
                        if(!it.contains(subjectNameInPackageDataList)){
                            packageDataList.removeAt(index)
                        }
                    }
                }
            }

            return packageDataList
        }

        private fun getSubjectNamesFromPackageDataList(subjectPackages: List<SubjectPackageData>):List<String>{
            val subjectNames = ArrayList<String>()
            subjectPackages.forEach {
                subjectNames.add(it.subjectName!!)
            }
            return subjectNames
        }

        private fun getSubjectsNotInPackageDataList(subjectsAvailable: List<String>, subjectsInPackageDataList: List<String>):List<String>{
            val tempSubjects: ArrayList<String> = ArrayList()
            subjectsAvailable.forEach {
                if(it !in subjectsInPackageDataList){
                    tempSubjects.add(it)
                }
            }
            return tempSubjects
        }

        private fun getNewTrialPackages(subjectsToAdd:List<String>,
                                        sizeSubjectsPackageDataList:Int, activationExpiry:ActivationExpiryDates): List<SubjectPackageData> {
            val tempSubjectPackages = ArrayList<SubjectPackageData>()
            subjectsToAdd.forEachIndexed { index, subjectName ->

                    val subjectPackageData = SubjectPackageData(
                        index + sizeSubjectsPackageDataList,
                        subjectName,
                        "TRIAL",
                        activationExpiry.activatedOn,
                        activationExpiry.expiresOn
                    )
                    tempSubjectPackages.add(subjectPackageData)
                }
            return tempSubjectPackages


        }





    }
}