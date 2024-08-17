package com.example.gceolmcqs

import com.example.gceolmcqs.datamodels.SubjectPackageData

class SubjectsPackagesBuilder {
    fun build(availableSubjects: List<String>?): List<SubjectPackageData>{
        val packageDataList = ArrayList<SubjectPackageData>()
        availableSubjects?.forEachIndexed { subjectIndex, subject ->

            packageDataList.add(
                SubjectPackageData(
                    subjectIndex,
                    subject,
                    MCQConstants.NA,
                    MCQConstants.NA,
                    MCQConstants.NA,
                    isPackageActive = false
                )
            )

        }
        return packageDataList

    }
}