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
    private var sectionDataModel: SectionDataModel? = null
    private var userSelections = ArrayList<UserSelection>()
    private val letters: Array<String> = Array(4) { "" }
    private val indexPreviousAndCurrentSelectedOptionOfQuestion =
        IndexPreviousAndCurrentSelectedOptionOfQuestion()
    private val numberOfQuestionsAnswered: MutableLiveData<Int> = MutableLiveData()
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
        numberOfQuestionsAnswered.value = 0
        questionIndex.value = 0
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
        sectionDataModel = PaperRepository.getSectionDataAt(sectionIndex)
    }

    fun getLetters():Array<String>{
        return letters
    }

    private fun shuffleSectionQuestions(){
        for (i in 0..2){
            sectionDataModel?.questions?.shuffle()
        }

    }

    private fun setSectionDuration() {
        sectionDuration = sectionDataModel!!.numberOfQuestions * MCQConstants.MILLI_SEC_PER_QUESTION
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
        return sectionDataModel!!.numberOfQuestions
    }

    fun getNumberOfQuestionsAnswered(): LiveData<Int> {
        return numberOfQuestionsAnswered
    }

    private fun updateNumberOfQuestionsAnswered() {
        numberOfQuestionsAnswered.value = numberOfQuestionsAnswered.value!!.plus(1)
    }

    fun incrementQuestionIndex() {
        questionIndex.value = questionIndex.value!!.plus(1)
        resetIsQuestionAnswered()
        resetIndexPreviousAndCurrentItemOfQuestion()

    }

    fun getQuestionIndex(): LiveData<Int> {
        return questionIndex
    }

    private fun updateIsQuestionAnswered() {
        isQuestionAnswered.value = true
        updateNumberOfQuestionsAnswered()
    }

    private fun resetIsQuestionAnswered() {
        isQuestionAnswered.value = false
    }

    fun getIsQuestionAnswered(): LiveData<Boolean> {
        return isQuestionAnswered
    }

    private fun initUserMarkedAnswerSheet() {
        this.sectionDataModel!!.questions.forEachIndexed { questionIndex, _ ->
            initUserSelections()
            initSectionQuestionsScores()
            userMarkedAnswerSheet.add(QuestionWithUserAnswerMarkedData((questionIndex + 1).toString()))
        }

    }

    private fun initUserSelections() {
        userSelections.add(UserSelection())
    }

    private fun initSectionQuestionsScores() {
        sectionQuestionsScores.add(QuestionScore())
    }

    fun getQuestion(): QuestionDataModel {
        when(sectionDataModel!!.title){
            MCQConstants.SECTION_I, MCQConstants.SECTION_II, MCQConstants.SECTION_V, MCQConstants.SECTION_VI ->{
                sectionDataModel!!.questions[questionIndex.value!!].selectableOptions.shuffle()

            }
        }
        return sectionDataModel!!.questions[questionIndex.value!!]
    }

    private fun updateUserMarkedAnswerSheet() {
        sectionDataModel!!.questions.forEachIndexed { index, questionDataModel ->
            userMarkedAnswerSheet[index].questionNumber = (index + 1).toString()
            userMarkedAnswerSheet[index].question = questionDataModel.question
            userMarkedAnswerSheet[index].image = questionDataModel.image
            userMarkedAnswerSheet[index].twoStatements = questionDataModel.twoStatements
            userMarkedAnswerSheet[index].nonSelectableOptions =
                questionDataModel.nonSelectableOptions
            userMarkedAnswerSheet[index].explanation = questionDataModel.explanation

        }
    }
    private fun setQuestionsCorrectAnswers(){
        sectionDataModel!!.questions.forEachIndexed { index, questionDataModel ->
            questionDataModel.selectableOptions.forEachIndexed { optionIndex, s ->
                if (s == questionDataModel.wordAnswer) {
                    userMarkedAnswerSheet[index].correctAnswer =
                        "${letters[optionIndex]}. ${questionDataModel.wordAnswer}"
                }
            }
        }

    }

    fun getSectionTitle(): String {
        return sectionDataModel!!.title
    }

    fun updateUserSelection(optionSelectedIndex: Int) {
        val userSelection = UserSelection(
            letters[optionSelectedIndex],
            sectionDataModel!!.questions[questionIndex.value!!].selectableOptions[optionSelectedIndex]
        )
        userSelections[questionIndex.value!!] = userSelection

        appendLetterToFourOptions()

        userMarkedAnswerSheet[questionIndex.value!!].userSelection = userSelection

        updateIndexQuestionOptionSelected(optionSelectedIndex)
        if (!isQuestionAnswered.value!!) {
            updateIsQuestionAnswered()

        }
        evaluateUserSelections(questionIndex.value!!)
    }

    private fun appendLetterToFourOptions(){
        var optionsWithLetterPrepended = ""
        sectionDataModel!!.questions[questionIndex.value!!].selectableOptions.forEachIndexed { index, s ->
            optionsWithLetterPrepended += "${letters[index]}. $s\n"
        }
        userMarkedAnswerSheet[questionIndex.value!!].fourOptions = optionsWithLetterPrepended
//        userMarkedAnswerSheet[questionIndex.value!!].fourOptions = sectionDataModel!!.questions[questionIndex.value!!].selectableOptions.joinToString("\n")
    }

    private fun evaluateUserSelections(questionIndex: Int) {
        if (userSelections[questionIndex].optionSelected == sectionDataModel!!.questions[questionIndex].wordAnswer) {
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
        val percentage = ((sectionScore.toDouble() / sectionDataModel!!.numberOfQuestions.toDouble()) * 100).toInt()
        val scoreData = ScoreData(sectionScore, sectionDataModel!!.numberOfQuestions, percentage)
        val userMarkedAnswersSheetData = UserMarkedAnswersSheetData(userMarkedAnswerSheet)
        return SectionResultData(sectionIndex!!, scoreData, userMarkedAnswersSheetData)
    }

    fun getSectionDirections(): String {

        return sectionDataModel?.directions!!
    }

    fun getSectionIndex(): String {
        return sectionIndex.toString()
    }


}