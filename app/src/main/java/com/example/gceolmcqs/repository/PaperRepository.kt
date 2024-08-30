package com.example.gceolmcqs.repository

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.gceolmcqs.MCQConstants
import com.example.gceolmcqs.datamodels.PaperData
import com.example.gceolmcqs.datamodels.SectionData
import com.example.gceolmcqs.datamodels.SectionResultData
import com.example.gceolmcqs.datamodels.UserMarkedAnswersSheetData
import com.google.gson.Gson

class PaperRepository {
    companion object{
        private var paperData: PaperData? = null
        private var currentSectionIndex = 0

        private var sectionsAnsweredData: ArrayList<Boolean> = ArrayList()
        private var paperScore = MutableLiveData<Int>(0)

        private var sectionsScores = MutableLiveData<ArrayList<Int>>(ArrayList())

        private var sectionsAnsweredCount = MutableLiveData<Int>(0)

        private var currentSectionRetryCount = MutableLiveData<Int>(MCQConstants.SECTION_RETRY_LIMIT)

        private var unAnsweredSectionIndexes: ArrayList<Int> = ArrayList()

        private var userMarkedAnswersSheetData: UserMarkedAnswersSheetData? = null
        private var sectionResultData: SectionResultData? = null

        private val paperGrade = MutableLiveData<String?>(null)
        private val paperPercentage = MutableLiveData<Int>(0)

        private val areAllSectionsAnswered = MutableLiveData<Boolean>(false)


        fun initPaperData(paperDataJsonString: String?){
            if(paperDataJsonString != null){
                paperData = Gson().fromJson(paperDataJsonString, PaperData::class.java)
                initSectionsAnswered()
                initSectionScores()
            }

        }

        fun getUnAnsweredSectionIndexes(): List<Int>{
            return unAnsweredSectionIndexes
        }

        fun setCurrentSectionIndex(sectionIndex: Int){
            currentSectionIndex = sectionIndex
        }

        fun getCurrentSectionIndex(): Int = currentSectionIndex

        fun resetPaperRepo(){
//            paperDataModel = null
            currentSectionIndex = 0
            sectionsAnsweredData = ArrayList()
            paperScore.value = 0
            sectionsScores.value = ArrayList()
            sectionsAnsweredCount.value = 0
            currentSectionRetryCount.value = MCQConstants.SECTION_RETRY_LIMIT
            unAnsweredSectionIndexes = ArrayList()
            userMarkedAnswersSheetData = null
            sectionResultData = null
            paperScore.value = 0
            paperGrade.value = null
            paperPercentage.value = 0
            areAllSectionsAnswered.value = false
            initSectionScores()
            initSectionsAnswered()
        }

        private fun initSectionScores(){
            for(sectionIndex in 0..paperData!!.numberOfSections){
                sectionsScores.value!!.add(0)
            }
        }

        private fun initSectionsAnswered(){
            for(sectionIndex in 0..paperData!!.numberOfSections){
                sectionsAnsweredData.add(false)
            }

        }

        fun getSectionDataAt(position: Int): SectionData {
            return paperData!!.sections[position]
        }

        fun getNumberOfSections(): Int{
            return paperData!!.numberOfSections
        }

        fun updateSectionScoreAt(sectionIndex: Int, score: Int){
//            updateSectionsAnsweredAt(sectionIndex)
            sectionsScores.value!![sectionIndex] = score
            updatePaperScore(sectionsScores.value!!)

        }

        fun getSectionsScores(): ArrayList<Int>{
            return sectionsScores.value!!
        }

        fun resetSectionScoreAt(sectionIndex: Int){
            updateSectionScoreAt(sectionIndex, 0)
        }


        private fun updatePaperScore(sectionScores: List<Int>){
            paperScore.value = sectionScores.sum()
            updateGrade()

        }

        fun getPaperScore(): LiveData<Int>{
            return paperScore
        }

        fun updateSectionsAnsweredAt(sectionIndex: Int){
            sectionsAnsweredData[sectionIndex] = true
            updateSectionsAnsweredCount()
        }

        private fun updateSectionsAnsweredCount(){
            sectionsAnsweredCount.value = sectionsAnsweredData.count { it }

        }

        fun getSectionsAnswered(): List<Boolean>{
            return sectionsAnsweredData
        }

        fun getSectionNumberAt(position: Int): String {
            return getSectionNames()!![position]
        }

        fun getSectionAnsweredAt(position: Int): Boolean {
            return sectionsAnsweredData[position]
        }

        fun decrementCurrentSectionRetryCount(){
            currentSectionRetryCount.value = currentSectionRetryCount.value?.minus(1)

        }

        fun resetCurrentSectionRetryCount(){
            currentSectionRetryCount.value = MCQConstants.SECTION_RETRY_LIMIT
        }

        fun getCurrentSectionRetryCount(): LiveData<Int> {
            return currentSectionRetryCount
        }

        fun setUserMarkedAnswerSheet(userMarkedAnswersSheetData: UserMarkedAnswersSheetData){
            this.userMarkedAnswersSheetData = userMarkedAnswersSheetData
        }

        fun getUserMarkedAnswerSheet(): UserMarkedAnswersSheetData = userMarkedAnswersSheetData!!

        fun setSectionResultData(sectionResultData: SectionResultData){
            this.sectionResultData = sectionResultData
        }

        fun getSectionResultData(): SectionResultData = sectionResultData!!

        fun getTotalNumberOfQuestions():Int{
            return paperData!!.numberOfQuestions
        }

        fun getNumberOfSectionsAnswered():LiveData<Int> {
            return sectionsAnsweredCount
        }

        fun getSectionNames(): Array<String>?{
            var sectionNames: Array<String>? = null
            paperData?.let {
                sectionNames = Array(it.sections.size){""}
                it.sections.forEachIndexed { index, sectionDataModel ->
                    sectionNames!![index] = sectionDataModel.title

                }
            }

            return sectionNames

        }
        fun getSectionNameBundleList(): Array<Bundle>?{
            var sectionNameBundleList: Array<Bundle>? = null
            paperData?.let {
                sectionNameBundleList = Array(it.sections.size){ Bundle() }
                it.sections.forEachIndexed { index, sectionDataModel ->
                    sectionNameBundleList!![index].apply {
                        putString("sectionName", sectionDataModel.title)
                        putString("numberOfQuestions", sectionDataModel.numberOfQuestions.toString())
                    }

                }
            }

            return sectionNameBundleList

        }

        private fun updateGrade() {
            if(getNumberOfSectionsAnswered().value == getNumberOfSections()){
                updateAreAllSectionsAnswered()
                paperPercentage.value = (paperScore.value!!.toDouble() / getTotalNumberOfQuestions().toDouble() * 100).toInt()
                paperGrade.value = when(paperPercentage.value){
                    in 75..100 -> {"A Grade"}
                    in 65..74 -> {"B Grade"}
                    in 50.. 64 -> {"C Grade"}
                    in 40..49 -> {"D Grade"}
                    in 30..39 -> {"E Grade"}
                    else->{"U Grade"}
                }

            }
        }

        fun getPaperGrade():LiveData<String?>{
            return paperGrade
        }

        fun getPaperPercentage():LiveData<Int>{
            return paperPercentage
        }
        private fun updateAreAllSectionsAnswered(){
            areAllSectionsAnswered.value = true
        }

        fun getAreAllSectionsAnswered(): LiveData<Boolean>{
            return areAllSectionsAnswered
        }

    }



}