package com.example.gceolmcqs.activities

import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import com.example.gceolmcqs.AssertReader
import com.example.gceolmcqs.MCQConstants
import com.example.gceolmcqs.R
import com.example.gceolmcqs.adapters.SectionNavigationRecyclerViewAdapter
import com.example.gceolmcqs.adapters.SectionRecyclerAdapter
import com.example.gceolmcqs.datamodels.ExamItemData
import com.example.gceolmcqs.datamodels.QuestionWithUserAnswerMarkedData
import com.example.gceolmcqs.datamodels.SectionResultData
import com.example.gceolmcqs.datamodels.UserMarkedAnswersSheetData
import com.example.gceolmcqs.fragments.*
import com.example.gceolmcqs.viewmodels.PaperActivityViewModel

private const val SHOW_INSTRUCTION = "showInstruction"

class PaperActivity : SubscriptionActivity(),
    SectionNavigationRecyclerViewAdapter.OnRecyclerItemClickListener,
    OnCheckPackageExpiredListener,
    OnRetrySectionListener,
    OnNextSectionListener,
    OnGetNumberOfSectionsListener,
    OnGotoSectionCorrectionListener,
    OnRequestToGoToResultListener,
    OnPaperScoreListener,
    OnIsSectionAnsweredListener,
    SectionRecyclerAdapter.OnExplanationClickListener
{

    private lateinit var _viewModel: PaperActivityViewModel
    private lateinit var pref: SharedPreferences

    private lateinit var checkBox: CheckBox
    private lateinit var tvInstruction: TextView

    private var currentSectionFragment: Fragment? = null
    private var subjectName: String? = null
    private var paperDataJsonString: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paper)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        setupViewModel()
//        displayPaperInstructionDialog()
        loadFragment()

    }

    private fun setupViewModel(){
        val bundle = intent.getBundleExtra("paperData")
        val examItemData = bundle!!.getSerializable("paperSerializable") as ExamItemData

        _viewModel = ViewModelProvider(this)[PaperActivityViewModel::class.java]
        _viewModel.setExamItemData(examItemData)
        _viewModel.setSubjectName(bundle.getString("subjectName")!!)

//        paperDataJsonString = getJsonFromAssets(_viewModel.getExamFileName())
        paperDataJsonString = AssertReader.getJsonFromAssets(this, _viewModel.getExamFileName())
        _viewModel.initPaperData(paperDataJsonString)
        _viewModel.setCurrentFragmentIndex(0)

        subjectName = bundle.getString("subjectName")
        loadSubjectPackageDataFromLocalDbWhere(subjectName!!)

        this.title = _viewModel.getExamTitle()
    }

    private fun gotoSectionNavigationFragment() {
        this.title = _viewModel.getExamTitle()
        val sectionNavigationFragment = SectionNavigationFragment.newInstance()
        replaceFragment(sectionNavigationFragment, 0)
    }

    private fun gotoSection(sectionIndex: Int) {
        if(currentSectionFragment != null){
            replaceFragment(currentSectionFragment!!, 1)
        }else{
            val sectionFragment =
                SectionFragment.newInstance(
                    sectionIndex,
                    _viewModel.getSectionData(sectionIndex)
                )
            replaceFragment(sectionFragment, 1)
        }
    }

    private fun gotoResult(sectionResultData: SectionResultData) {
        _viewModel.setSectionResultData(sectionResultData)
        val sectionResultFragment = SectionResultFragment.newInstance(
            sectionResultData,
            intent.getBundleExtra("paperData")!!.getString("expiresOn")!!
        )
        replaceFragment(sectionResultFragment, 2)

    }

    private fun gotoSectionCorrection(
        sectionIndex: Int,
        userMarkedAnswersSheetData: UserMarkedAnswersSheetData
    ) {
        _viewModel.setUserMarkedAnswerSheet(userMarkedAnswersSheetData)
        val sectionCorrectionFragment = CorrectionFragment.newInstance(
            sectionIndex,
            userMarkedAnswersSheetData,
            intent.getBundleExtra("paperData")!!.getString("expiresOn")!!
        )
        replaceFragment(sectionCorrectionFragment, 3)

    }

    private fun replaceFragment(fragment: Fragment, fragmentIndex: Int) {
        _viewModel.setCurrentFragmentIndex(fragmentIndex)

        val transaction = supportFragmentManager.beginTransaction()

        transaction.apply {
            replace(R.id.sectionNavigationFragmentHolder, fragment)
            commit()
        }
    }

    override fun onResume() {
        super.onResume()
        title = _viewModel.getExamTitle()
//        loadFragment()

    }


    private fun loadFragment(){
        when(_viewModel.getCurrentFragmentIndex()){
            0 -> gotoSectionNavigationFragment()
            1 -> {
                gotoSection(_viewModel.getCurrentSectionIndex())
            }
            2 -> {
                gotoResult(_viewModel.getSectionResultData())
            }
            3 -> {
                gotoSectionCorrection(_viewModel.getCurrentSectionIndex(), _viewModel.getUserMarkedAnswerSheet())
            }
        }
    }

    private fun resetCurrentSectionFragment(){
        currentSectionFragment = null
    }

    override fun onRecyclerItemClick(position: Int) {
        resetCurrentSectionFragment()
        _viewModel.setCurrentSectionIndex(position)

        if (!getIsPackageActive()) {
            showPackageExpiredDialog()
//                    && !_viewModel.getIsSectionAnsweredAt(position)
        }else{
            gotoSection(position)
        }


    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {

        if (_viewModel.getCurrentFragmentIndex() == 0) {
            super.onBackPressed()
            finish()

        } else {
            gotoSectionNavigationFragment()
        }

    }

    override fun finish() {
        super.finish()
        resetPaperRepository()
    }

    private fun resetPaperRepository(){
        _viewModel.resetPaperRepository()
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
//                finish()
            }

        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRequestToGoToResult(sectionResultData: SectionResultData) {
        gotoResult(sectionResultData)
    }

    override fun onRetrySection(sectionIndex: Int) {
        resetCurrentSectionFragment()
        gotoSection(sectionIndex)
        _viewModel.resetSectionScore(sectionIndex)
    }

    override fun onNextSection(sectionIndex: Int) {
        resetCurrentSectionFragment()
        _viewModel.resetCurrentSectionRetryCount()
        gotoSection(sectionIndex)
    }

    override fun onGetNumberOfSections(): Int {
        return _viewModel.getNumberOfSections()
    }

    override fun onAllSectionsAnswered(): Boolean {
        return _viewModel.getUnAnsweredSectionIndexes().isEmpty()
    }

    override fun onGotoSectionCorrection(
        sectionIndex: Int,
        userMarkedAnswersSheetData: UserMarkedAnswersSheetData
    ) {
        gotoSectionCorrection(sectionIndex, userMarkedAnswersSheetData)
    }

    override fun onUpdatePaperScore(sectionIndex: Int, numberOfCorrectAnswers: Int) {
        _viewModel.updateSectionsScore(sectionIndex, numberOfCorrectAnswers)
    }

    override fun onUpdateIsSectionAnswered(sectionIndex: Int) {
        _viewModel.updateIsSectionsAnswered(sectionIndex)
    }

    override fun onGetSectionsAnswered(): List<Boolean> {
        return _viewModel.getIsSectionsAnswered()
    }

    override fun showPackageExpiredDialog() {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.apply {
            setMessage(resources.getString(R.string.package_expired_message))
            setPositiveButton(resources.getString(R.string.subscribe)) { _, _ ->
                val subjectIndex = intent.getIntExtra(MCQConstants.SUBJECT_INDEX, 0)
//                val subjectPackageData = paperActivityViewModel.getSubjectPackageData()
                setSubjectPackageDataToActivate(subjectIndex, getSubjectPackageData())
            }
            setNegativeButton(resources.getString(R.string.cancel)){_, _ ->}
        }.create().show()
    }

    override fun showPackageActivatedDialog() {
        super.showPackageActivatedDialog()
        loadSubjectPackageDataFromLocalDbWhere(subjectName!!)
//        updateSubjectPackageData()
    }

    override fun onDecrementCurrentSectionRetryCount() {
        _viewModel.decrementCurrentSectionRetryCount()
    }

    override fun onResetCurrentSectionRetryCount() {
        _viewModel.resetCurrentSectionRetryCount()
    }

    override fun onCheckPackageExpired(): Boolean {
        return getIsPackageActive()
    }

    override fun onShowPackageExpiredDialog() {
        showPackageExpiredDialog()
    }

    override fun onGetCurrentSectionRetryCount(): LiveData<Int> {
        return _viewModel.getCurrentSectionRetryCount()
    }

    private fun displayPaperInstructionDialog() {
        pref = getSharedPreferences(resources.getString(R.string.app_name), MODE_PRIVATE)
        val doNotShowThis = pref.getBoolean(SHOW_INSTRUCTION, false)
        if (!doNotShowThis) {
            val editor = pref.edit()
            val instruction = AlertDialog.Builder(this)
            val view = this.layoutInflater.inflate(R.layout.paper_instruction_dialog_lo, null)
            checkBox = view.findViewById(R.id.instructionCheckBox)
            tvInstruction = view.findViewById(R.id.tvPaperInstruction)
            val message: String =
                "${_viewModel.getExamTitle()} ${resources.getStringArray(R.array.paper_instruction)[0]} ${_viewModel.getTotalNumberOfQuestions()} " +
                        "${resources.getStringArray(R.array.paper_instruction)[1]} ${_viewModel.getNumberOfSections()} ${resources.getStringArray(R.array.paper_instruction)[2]}"
            tvInstruction.text = message
            instruction.apply {
                setView(view)
                setPositiveButton("OK") { btnOk, _ ->
                    if (checkBox.isChecked) {
                        editor.apply {
                            putBoolean(SHOW_INSTRUCTION, true)
                        }.apply()
                    }
                    btnOk.dismiss()
                }
            }.create().show()
        }

    }

    private fun showExplanationDialog(questionData: QuestionWithUserAnswerMarkedData){
        val view = layoutInflater.inflate(R.layout.explanation_layout, null)
        val qnTv: TextView = view.findViewById(R.id.questionNumTv)
        val explanationTv: TextView = view.findViewById(R.id.explanationTv)
        qnTv.text = "Question ${questionData.questionNumber}"
        explanationTv.text = questionData.explanation
        val dialog = AlertDialog.Builder(this).apply {
            setView(view)
            setTitle("Explanation")
            setPositiveButton("OK"){_, _ ->}

        }.create()
        dialog.show()
    }

    override fun onExplanationClicked(questionData: QuestionWithUserAnswerMarkedData) {
//        get explanation at position clicked
        println(questionData)
        showExplanationDialog(questionData)

    }

//    override fun onRestartPaper() {
//        resetPaperRepository()
//        gotoSectionNavigationFragment()
//    }
}

interface OnRequestToGoToResultListener {
    fun onRequestToGoToResult(sectionResultData: SectionResultData)
}

interface OnRetrySectionListener {
    fun onRetrySection(sectionIndex: Int)
    fun onDecrementCurrentSectionRetryCount()
    fun onGetCurrentSectionRetryCount(): LiveData<Int>
}

interface OnNextSectionListener {
    fun onNextSection(sectionIndex: Int)
    fun onResetCurrentSectionRetryCount()

}

interface OnGetNumberOfSectionsListener {
    fun onGetNumberOfSections(): Int
    fun onAllSectionsAnswered(): Boolean
}

interface OnGotoSectionCorrectionListener {
    fun onGotoSectionCorrection(
        sectionIndex: Int,
        userMarkedAnswersSheetData: UserMarkedAnswersSheetData
    )
}

interface OnPaperScoreListener {
    fun onUpdatePaperScore(sectionIndex: Int, numberOfCorrectAnswers: Int)
//    fun onGetPaperScore(): Int
}

interface OnIsSectionAnsweredListener {
    fun onUpdateIsSectionAnswered(sectionIndex: Int)
    fun onGetSectionsAnswered(): List<Boolean>
}

interface OnCheckPackageExpiredListener{
    fun onCheckPackageExpired():Boolean
    fun onShowPackageExpiredDialog()
}





