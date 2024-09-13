package com.example.gceolmcqs.viewmodels

import android.os.CountDownTimer
import android.text.format.Time
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.gceolmcqs.MCQConstants
import com.example.gceolmcqs.datamodels.*
import com.example.gceolmcqs.repository.PaperRepository



class SectionFragmentViewModel : ViewModel() {
    private var sectionData: SectionData? = null
    private var userSelections = ArrayList<UserSelection>()
    private val letters: Array<String> = Array(4) { "" }
    private val indexPreviousAndCurrentSelectedOptionOfQuestion =
        IndexPreviousAndCurrentSelectedOptionOfQuestion()
    private val _numberOfQuestionsAnswered: MutableLiveData<Int> = MutableLiveData()
    val numberOfQuestionsAnswered: LiveData<Int> = _numberOfQuestionsAnswered
    private val isQuestionAnswered = MutableLiveData<Boolean>()
    private val sectionQuestionsScores: ArrayList<QuestionScore> = ArrayList()
    private var sectionScore: Int = 0
    private var sectionDuration: Long = 0L
    private lateinit var timer: CountDownTimer
    private val questionIndex = MutableLiveData<Int>()
    private val userMarkedAnswerSheet: ArrayList<QuestionWithUserAnswerMarkedData> = ArrayList()

    private var sectionIndex: Int? = null

    private val isTimeOut = MutableLiveData<Boolean>()

    private val timeRemaining = MutableLiveData<Time>()

    private val isTimeAlmostOut = MutableLiveData<Boolean>()
    private val isTimeAlmostOutStartTime = 20000L

    init {
        letters[0] = "A"
        letters[1] = "B"
        letters[2] = "C"
        letters[3] = "D"
        _numberOfQuestionsAnswered.value = 0
        questionIndex.value = 0
        val t = Time().apply {
            set(0)
        }
        timeRemaining.value = t
        isQuestionAnswered.value = false
        isTimeOut.value = false
        isTimeAlmostOut.value = false


    }

    fun setSectionIndex(index:Int){
        sectionIndex = index
        initSectionData(index)
        setSectionDuration()
        shuffleSectionQuestions()
        initUserMarkedAnswerSheet()
        updateUserMarkedAnswerSheet()
    }

    private fun initSectionData(sectionIndex: Int){
        sectionData = PaperRepository.getSectionDataAt(sectionIndex)
    }

    fun getLetters():Array<String>{
        return letters
    }

    private fun shuffleSectionQuestions(){
        for (i in 0..2){
            sectionData?.questions?.shuffle()
        }

    }

    private fun setSectionDuration() {
        sectionDuration = sectionData!!.numberOfQuestions * MCQConstants.MILLI_SEC_PER_QUESTION
    }

    fun startTimer() {
        timer = object : CountDownTimer(sectionDuration, MCQConstants.COUNT_DOWN_INTERVAL) {
            override fun onTick(p0: Long) {
                val t = Time()
                updateIsTimeAlmostOut(p0)
                t.set(p0)
                timeRemaining.value = t
            }

            override fun onFinish() {
                isTimeOut.value = true
            }

        }.start()
    }

    fun getTimeRemaining(): LiveData<Time> {
        return timeRemaining
    }

    fun updateIsTimeAlmostOut(timeLeft: Long){
        if(timeLeft < MCQConstants.TIME_TO_ANIMATE_TIMER){
            isTimeAlmostOut.value = true
        }
    }

    fun getIsTimeAlmostOut(): LiveData<Boolean>{
        return isTimeAlmostOut
    }

    fun getIsTimeOut(): LiveData<Boolean> {
        return isTimeOut
    }

    fun getNumberOfQuestionsInSection(): Int {
        return sectionData!!.numberOfQuestions
    }


    private fun updateNumberOfQuestionsAnswered() {
        _numberOfQuestionsAnswered.value = _numberOfQuestionsAnswered.value!! + 1
    }

    private fun resetIsQuestionAnswered() {
        isQuestionAnswered.value = false
    }


    private fun initUserMarkedAnswerSheet() {
        initUserSelections()
        initSectionQuestionsScores()
        userMarkedAnswerSheet.clear()
        for (index in 0..< sectionData!!.numberOfQuestions){
            userMarkedAnswerSheet.add(QuestionWithUserAnswerMarkedData((index + 1).toString()))
        }


    }

    private fun initUserSelections() {
        userSelections.clear()
        for (index in 0..< sectionData!!.numberOfQuestions){
            userSelections.add(UserSelection())
        }

    }

    private fun initSectionQuestionsScores() {
        sectionQuestionsScores.clear()
        for (index in 0..< sectionData!!.numberOfQuestions){
            sectionQuestionsScores.add(QuestionScore())
        }

    }

//    fun getQuestion(): QuestionData {
//        when(sectionData!!.title){
//            MCQConstants.SECTION_I, MCQConstants.SECTION_II, MCQConstants.SECTION_V, MCQConstants.SECTION_VI ->{
//                sectionData!!.questions[questionIndex.value!!].selectableOptions.shuffle()
//
//            }
//        }
//        return sectionData!!.questions[questionIndex.value!!]
//    }

    fun getSectionQuestions(): ArrayList<QuestionData>{
        when(sectionData!!.title){
            MCQConstants.SECTION_I, MCQConstants.SECTION_II, MCQConstants.SECTION_V, MCQConstants.SECTION_VI ->{
                for (index in 0..< sectionData!!.numberOfQuestions){
                    sectionData!!.questions[index].selectableOptions.shuffle()
                }
            }
        }
        return sectionData?.questions!!
    }

    private fun updateUserMarkedAnswerSheet() {
        sectionData!!.questions.forEachIndexed { index, questionDataModel ->
            userMarkedAnswerSheet[index].questionNumber = (index + 1).toString()
            userMarkedAnswerSheet[index].question = questionDataModel.question
            userMarkedAnswerSheet[index].image = questionDataModel.image
            userMarkedAnswerSheet[index].twoStatements = questionDataModel.twoStatements
            userMarkedAnswerSheet[index].nonSelectableOptions =
                questionDataModel.nonSelectableOptions
//            userMarkedAnswerSheet[index].fourOptions = questionDataModel.selectableOptions.toString()
            userMarkedAnswerSheet[index].explanation = questionDataModel.explanation

        }
    }
    private fun setQuestionsCorrectAnswers(){
        sectionData!!.questions.forEachIndexed { index, questionDataModel ->
            questionDataModel.selectableOptions.forEachIndexed { optionIndex, s ->
                if (s == questionDataModel.wordAnswer) {
                    userMarkedAnswerSheet[index].correctAnswer =
                        "${letters[optionIndex]}. ${questionDataModel.wordAnswer}"
                }
            }
        }

    }

    fun getSectionTitle(): String {
        return sectionData!!.title
    }

//    fun updateQuestionAlternativeSelected(questionIndex: Int, selectableOptionIndex: Int){
//        updateUserSelection(questionIndex, selectableOptionIndex)
//    }

    fun updateUserSelection(questionIndex: Int, optionSelectedIndex: Int) {
        val userSelection = UserSelection(
            letters[optionSelectedIndex],
            sectionData!!.questions[questionIndex].selectableOptions[optionSelectedIndex]
        )
        userSelections[questionIndex] = userSelection

        appendLetterToFourOptions(questionIndex)

        userMarkedAnswerSheet[questionIndex].userSelection = userSelection

//        updateIndexQuestionOptionSelected(optionSelectedIndex)
//        if (!isQuestionAnswered.value!!) {
//            updateIsQuestionAnswered()
//
//        }
        updateNumberOfQuestionsAnswered()
        evaluateUserSelections(questionIndex)
    }

    private fun appendLetterToFourOptions(questionIndex: Int){
        var optionsWithLetterPrepended = ""
        sectionData!!.questions[questionIndex].selectableOptions.forEachIndexed { index, s ->
            optionsWithLetterPrepended += "${letters[index]}. $s\n"
        }
        userMarkedAnswerSheet[questionIndex].fourOptions = optionsWithLetterPrepended
//        userMarkedAnswerSheet[questionIndex.value!!].fourOptions = sectionDataModel!!.questions[questionIndex.value!!].selectableOptions.joinToString("\n")
    }

    private fun evaluateUserSelections(questionIndex: Int) {
        if (userSelections[questionIndex].optionSelected == sectionData!!.questions[questionIndex].wordAnswer) {
            sectionQuestionsScores[questionIndex].score = 1
            userMarkedAnswerSheet[questionIndex].userSelection!!.remark = true

        } else {
            sectionQuestionsScores[questionIndex].score = 0
            userMarkedAnswerSheet[questionIndex].userSelection!!.remark = false
        }

        sumSectionQuestionScores()


    }


    private fun sumSectionQuestionScores() {
        sectionScore = sectionQuestionsScores.count { it.score == 1 }
    }

    private fun updateIndexQuestionOptionSelected(indexOptionSelected: Int) {

        if (indexPreviousAndCurrentSelectedOptionOfQuestion.indexCurrentItem == null) {
            indexPreviousAndCurrentSelectedOptionOfQuestion.indexCurrentItem = indexOptionSelected
        }

        indexPreviousAndCurrentSelectedOptionOfQuestion.indexCurrentItem?.let {
            if (indexOptionSelected != it) {
                indexPreviousAndCurrentSelectedOptionOfQuestion.indexPreviousItem = it
                indexPreviousAndCurrentSelectedOptionOfQuestion.indexCurrentItem =
                    indexOptionSelected

            }
        }

    }


    fun getIndexPreviousAndCurrentItemOfQuestion(): IndexPreviousAndCurrentSelectedOptionOfQuestion {
        return indexPreviousAndCurrentSelectedOptionOfQuestion
    }

    private fun resetIndexPreviousAndCurrentItemOfQuestion() {
        indexPreviousAndCurrentSelectedOptionOfQuestion.indexCurrentItem = null
        indexPreviousAndCurrentSelectedOptionOfQuestion.indexPreviousItem = null
    }

    fun getSectionResultData():SectionResultData{
        setQuestionsCorrectAnswers()
        val percentage = ((sectionScore.toDouble() / sectionData!!.numberOfQuestions.toDouble()) * 100).toInt()
        val scoreData = ScoreData(sectionScore, sectionData!!.numberOfQuestions, percentage)
        val userMarkedAnswersSheetData = UserMarkedAnswersSheetData(userMarkedAnswerSheet)
        return SectionResultData(sectionIndex!!, scoreData, userMarkedAnswersSheetData)
    }

    fun getSectionDirections(): String {

        return sectionData?.directions!!
    }

    fun getSectionIndex(): String {
        return sectionIndex.toString()
    }


}