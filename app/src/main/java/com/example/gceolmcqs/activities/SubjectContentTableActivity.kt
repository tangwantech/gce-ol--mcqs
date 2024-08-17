package com.example.gceolmcqs.activities

import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.example.gceolmcqs.MCQConstants
import com.example.gceolmcqs.R
import com.example.gceolmcqs.adapters.SubjectContentTableViewPagerAdapter
import com.example.gceolmcqs.datamodels.SubjectAndFileNameData
import com.example.gceolmcqs.datamodels.SubjectPackageData
import com.example.gceolmcqs.fragments.ExamTypeFragment
import com.example.gceolmcqs.viewmodels.SubjectContentTableViewModel
import com.google.android.material.tabs.TabLayout
import java.io.IOException
import java.nio.charset.Charset


class SubjectContentTableActivity : AppCompatActivity(), ExamTypeFragment.OnPackageExpiredListener, ExamTypeFragment.OnContentAccessDeniedListener{

    private lateinit var subjectContentTableViewModel: SubjectContentTableViewModel
    private var subjectTitle: String? = null
    private lateinit var tabLayout: TabLayout
    private var selectedTab: TabLayout.Tab? = null
    private lateinit var viewPager: ViewPager
    private lateinit var alertDialog: AlertDialog.Builder
    private lateinit var pref: SharedPreferences
    private var currentTabIndex  = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subject_content_table)
        pref = getSharedPreferences(SUBJECT_CONTENT_TABLE, MODE_PRIVATE)
        setAlertDialog()
        initActivityViews()
        initViewModel()
        setupActivityViewListeners()
        setupViewObservers()
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

    }
    private fun initActivityViews(){
        tabLayout = findViewById(R.id.homeTab)
        viewPager = findViewById(R.id.homeViewPager)
    }

    private fun initViewModel(){
        val subjectAndFileNameData = intent.getBundleExtra("subject_and_file_name_bundle")!!
            .getSerializable("subject_and_file_name_data")!! as SubjectAndFileNameData

        subjectTitle = subjectAndFileNameData.subject

        subjectContentTableViewModel =
            ViewModelProvider(this)[SubjectContentTableViewModel::class.java]

        subjectContentTableViewModel.setSubjectName(subjectTitle!!)

        subjectContentTableViewModel.initDatabase(this)

        getJsonFromAssets(subjectAndFileNameData.fileName)?.let {
            subjectContentTableViewModel.initSubjectContentsData(it)
        }

    }

    private fun setupViewObservers(){
        subjectContentTableViewModel.getIsPackageActive().observe(this, Observer {
            if (!it) {
                showAlertDialog()
            }
        })

        subjectContentTableViewModel.subjectPackageData.observe(this, Observer{subjectPackageData ->
            setUpSubjectContentTab(subjectPackageData)
        })
    }

    private fun setAlertDialog(){
        alertDialog = AlertDialog.Builder(this)
        alertDialog.apply {
            setMessage(resources.getString(R.string.package_expired_message))
            setPositiveButton("Ok") { _, _ ->
                exitActivity()
            }
            setCancelable(false)
        }.create()
    }

    private fun showAlertDialog(){
        alertDialog.show()
    }

    private fun exitActivity() {
        this.finish()
    }

    private fun setUpSubjectContentTab(subjectPackageData: SubjectPackageData) {
        val subjectIndex = intent.getIntExtra(MCQConstants.SUBJECT_INDEX, 0)

        val tabIndex = pref.getInt(TAB_INDEX, 0)
        val tabFragments: ArrayList<Fragment> = ArrayList()

        for (fragmentIndex in 0 until subjectContentTableViewModel.getExamTypesCount()) {
            val fragment =
                ExamTypeFragment.newInstance(
                    subjectContentTableViewModel.getExamTypeDataAt(
                        fragmentIndex
                    ),
                    subjectTitle!!,
                    subjectPackageData.expiresOn!!,
                    subjectPackageData.packageName!!,
                    subjectIndex

                )
            tabFragments.add(fragment)
        }

        val viewPagerAdapter = SubjectContentTableViewPagerAdapter(
            this.supportFragmentManager,
            tabFragments,
            subjectContentTableViewModel.getExamTitles()
        )
        viewPager.adapter = viewPagerAdapter
        viewPager.currentItem = tabIndex
        tabLayout.setupWithViewPager(viewPager)


//         = viewPager.currentItem


    }

    private fun setupActivityViewListeners(){
        tabLayout.setOnTabSelectedListener(object: TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentTabIndex = tab?.position!!
                saveSelectedTab(currentTabIndex)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}

        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
    }

    override fun onResume() {
        super.onResume()
        title = subjectTitle
        subjectContentTableViewModel.querySubjectPackageDataFromLocalDatabaseAtSubjectName(subjectTitle!!)

    }

    override fun onDestroy() {
        super.onDestroy()
        saveSelectedTab(0)
    }

    private fun getJsonFromAssets(fileName: String): String? {
        val charset: Charset = Charsets.UTF_8

        return try {
            val jsonFile = assets.open(fileName)
            val size = jsonFile.available()
            val buffer = ByteArray(size)

            jsonFile.read(buffer)
            jsonFile.close()
            String(buffer, charset)

        } catch (e: IOException) {
            null
        }
    }

    override fun onShowPackageExpired() {
        showAlertDialog()
    }

    override fun onCheckIfPackageHasExpired(): Boolean {
        return subjectContentTableViewModel.getPackageStatus()
    }

    override fun onContentAccessDenied() {
        val contentAccessDeniedDialog = AlertDialog.Builder(this)
        contentAccessDeniedDialog.apply {
            setMessage(resources.getString(R.string.content_access_denied_Message))
            setPositiveButton("Ok") { d, _->
                d.dismiss()
            }
        }.create().show()
    }

    private fun saveSelectedTab(index: Int){
        pref.edit().apply {
            putInt(TAB_INDEX, index)
//            apply()
        }.apply()
    }

    companion object{
        private const val SUBJECT_CONTENT_TABLE = "subject content table"
        private const val TAB_INDEX = "tab index"
    }
}

