package com.example.gceolmcqs.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.gceolmcqs.datamodels.*
import com.example.gceolmcqs.repository.PaperRepository

class SectionResultFragmentViewModel: ViewModel() {
    private lateinit var sectionResultData: SectionResultData
    private lateinit var correctionData: UserMarkedAnswersSheetData
    private  val hasPerfectScore = MutableLiveData<Boolean>()

    private val _nextButtonState = MutableLiveData<Boolean>(false)
    val nextButtonState: LiveData<Boolean> = _nextButtonState

    private var nextSectionIndex = 0

    fun updateNextBtnState(){
        nextSectionIndex = getSectionIndex() + 1

        while(nextSectionIndex < PaperRepository.getNumberOfSections() && PaperRepository.getSectionAnsweredAt(nextSectionIndex)){
            nextSectionIndex += 1
        }

        if (nextSectionIndex < PaperRepository.getNumberOfSections()){
            _nextButtonState.value = true
        }
    }

    fun getNextSectionIndex(): Int{
        return nextSectionIndex
    }
    fun setResultData(sectionResultData: SectionResultData){
        this.sectionResultData = sectionResultData
        checkForPerfectScore()
        updateSectionAnsweredAt(getSectionIndex())
        updateSectionScoreAt(getSectionIndex(), getNumberOfCorrectAnswers())
    }

    fun getNumberOfCorrectAnswers(): Int{
        return sectionResultData.scoreData.numberOfCorrectAnswers
    }

    fun getNumberOfQuestions(): Int{
        return sectionResultData.scoreData.numberOfQuestions
    }

    fun getScorePercentage(): Int{
        return sectionResultData.scoreData.percentage
    }

    private fun updateSectionScoreAt(sectionIndex: Int, sectionScore: Int){
        PaperRepository.updateSectionScoreAt(sectionIndex, sectionScore)
    }

    private fun updateSectionAnsweredAt(sectionIndex: Int){
        PaperRepository.updateSectionsAnsweredAt(sectionIndex)
    }
    private fun checkForPerfectScore(){
        hasPerfectScore.value = sectionResultData.scoreData.percentage == 100
    }
    fun getHasPerfectScore(): LiveData<Boolean>{
        return hasPerfectScore
    }
//    fun getScoreData(): ScoreData {
//        return sectionResultData.scoreData
//    }
    fun getSectionIndex(): Int{
        return sectionResultData.sectionIndex
    }

    fun getUserMarkedAnswersSheet(): UserMarkedAnswersSheetData {
        println("User marked answer sheet: ${sectionResultData.userMarkedAnswersSheet}")
        return sectionResultData.userMarkedAnswersSheet
    }
    fun getQuestionsWithCorrectAnswer(): UserMarkedAnswersSheetData {
        val questionsWithCorrectAnswer = ArrayList<QuestionWithUserAnswerMarkedData>()
        sectionResultData.userMarkedAnswersSheet.questionsWithUserAnswerMarkedData.forEachIndexed { _, questionWithUserAnswerMarkedData ->
            if(questionWithUserAnswerMarkedData.userSelection == null || !questionWithUserAnswerMarkedData.userSelection!!.remark!!){
                questionsWithCorrectAnswer.add(questionWithUserAnswerMarkedData)
            }
        }

        return UserMarkedAnswersSheetData(questionsWithCorrectAnswer)
    }
}