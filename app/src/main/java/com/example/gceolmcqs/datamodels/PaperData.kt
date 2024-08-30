package com.example.gceolmcqs.datamodels

data class PaperData(
    val privateTitle: String,
    val publicTitle: String,
    val numberOfQuestions: Int,
    val numberOfSections: Int,
    val sections: ArrayList<SectionData>
) : java.io.Serializable

data class SectionData(
    val title: String,
    val numberOfQuestions: Int,
    val directions: String,
    val questions: ArrayList<QuestionData>
) : java.io.Serializable

data class QuestionData(
    val questionNumber: String,
    val question: String?,
    val image: String?,
    val twoStatements: ArrayList<String>?,
    val nonSelectableOptions: ArrayList<String>?,
    val selectableOptions: ArrayList<String>,
    val wordAnswer: String,
    val explanation: String?
) : java.io.Serializable

data class UserSectionQuestionAnswers(
    val sectionTitle: String,
    val userSelections: ArrayList<UserSelection>
)

data class UserSelection(
    var optionLetter: String? = null,
    var optionSelected: String? = null,
    var remark: Boolean? = null
) : java.io.Serializable

data class IndexPreviousAndCurrentSelectedOptionOfQuestion(
    var indexPreviousItem: Int? = null,
    var indexCurrentItem: Int? = null
)

data class QuestionScore(var questionNumberIndex: Int? = null, var score: Int = 0)

data class QuestionWithUserAnswerMarkedData(
    var questionNumber: String,
    var question: String? = null,
    var image: String? = null,
    var twoStatements: ArrayList<String>? = null,
    var nonSelectableOptions: ArrayList<String>? = null,
    var fourOptions: String? = null,
    var userSelection: UserSelection? = null,
    var correctAnswer: String? = null,
    var explanation: String? = null
) : java.io.Serializable

data class UserMarkedAnswersSheetData(val questionsWithUserAnswerMarkedData: List<QuestionWithUserAnswerMarkedData>) :
    java.io.Serializable

data class ScoreData(
    val numberOfCorrectAnswers: Int,
    val numberOfQuestions: Int,
    val percentage: Int
) : java.io.Serializable

data class SectionResultData(
    val sectionIndex: Int,
    val scoreData: ScoreData,
    val userMarkedAnswersSheet: UserMarkedAnswersSheetData
) : java.io.Serializable


