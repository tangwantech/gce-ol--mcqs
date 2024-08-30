package com.example.gceolmcqs.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.gceolmcqs.datamodels.*
import com.example.gceolmcqs.repository.PaperRepository

class PaperActivityViewModel:ViewModel() {
    private var currentFragmentIndex: Int? = null
    private lateinit var subjectName: String
    private var examItemData: ExamItemData? = null

    fun setExamItemData(examItemData: ExamItemData){
        this.examItemData = examItemData
    }

    fun getExamFileName(): String{
        return examItemData!!.fileName
    }

    fun getExamTitle(): String{
        return examItemData!!.title
    }

    fun setCurrentFragmentIndex(index: Int){
        currentFragmentIndex = index
    }

    fun getCurrentFragmentIndex():Int?{
        return currentFragmentIndex
    }

    fun setSubjectName(subjectName: String) {
        this.subjectName = subjectName
    }

//    fun updateSubjectPackageData(subjectPackageData: SubjectPackageData){
//        _subjectPackage.value = subjectPackageData
//    }

    fun initPaperData(paperDataJsonString: String?){
        PaperRepository.initPaperData(paperDataJsonString)

    }

    fun getUnAnsweredSectionIndexes(): List<Int>{
        return PaperRepository.getUnAnsweredSectionIndexes()
    }

    fun setCurrentSectionIndex(sectionIndex: Int){
        PaperRepository.setCurrentSectionIndex(sectionIndex)
    }

    fun getCurrentSectionIndex(): Int {
        return PaperRepository.getCurrentSectionIndex()
    }

    fun getTotalNumberOfQuestions():Int{
        return PaperRepository.getTotalNumberOfQuestions()
    }

    fun getSectionData(position: Int):SectionData{
        return PaperRepository.getSectionDataAt(position)
    }

    fun getNumberOfSections(): Int{
        return PaperRepository.getNumberOfSections()
    }

    fun updateSectionsScore(sectionIndex: Int, score: Int){
        PaperRepository.updateSectionScoreAt(sectionIndex, score)

    }

    fun resetSectionScore(sectionIndex: Int){
        PaperRepository.resetSectionScoreAt(sectionIndex)
    }

    fun updateIsSectionsAnswered(sectionIndex: Int){
        PaperRepository.updateSectionsAnsweredAt(sectionIndex)
    }

    fun getIsSectionsAnswered(): List<Boolean>{
        return PaperRepository.getSectionsAnswered()
    }


    fun getSectionNumberAt(position: Int): String {
        return PaperRepository.getSectionNumberAt(position)
    }

    fun getIsSectionAnsweredAt(position: Int): Boolean {
        return PaperRepository.getSectionAnsweredAt(position)
    }

    fun decrementCurrentSectionRetryCount(){
        PaperRepository.decrementCurrentSectionRetryCount()

    }

    fun resetCurrentSectionRetryCount(){
        PaperRepository.resetCurrentSectionRetryCount()
    }

    fun getCurrentSectionRetryCount(): LiveData<Int>{
        return PaperRepository.getCurrentSectionRetryCount()
    }

    fun setUserMarkedAnswerSheet(userMarkedAnswersSheetData: UserMarkedAnswersSheetData){
        PaperRepository.setUserMarkedAnswerSheet(userMarkedAnswersSheetData)
    }

    fun getUserMarkedAnswerSheet(): UserMarkedAnswersSheetData {
        return PaperRepository.getUserMarkedAnswerSheet()
    }

    fun setSectionResultData(sectionResultData: SectionResultData){
        PaperRepository.setSectionResultData(sectionResultData)
    }

    fun getSectionResultData(): SectionResultData {
        return PaperRepository.getSectionResultData()
    }

    fun resetPaperRepository(){
        PaperRepository.resetPaperRepo()
    }

}