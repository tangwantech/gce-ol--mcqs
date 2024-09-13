package com.example.gceolmcqs.activities

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.gceolmcqs.AssertReader
import com.example.gceolmcqs.MCQConstants
import com.example.gceolmcqs.viewmodels.SplashActivityViewModel
import com.example.gceolmcqs.R
import com.example.gceolmcqs.databinding.ActivitySplashBinding
import com.example.gceolmcqs.databinding.TermsOfUseLayoutBinding
import kotlinx.coroutines.*
import java.io.IOException
import java.nio.charset.Charset

class GCEFirstActivity : AppCompatActivity() {
    private val serverRetryLimit = 2
    private lateinit var viewModel: SplashActivityViewModel
    private lateinit var pref: SharedPreferences
    private var termsOfServiceDialog: AlertDialog? = null
    private var initializingAppDialog: AlertDialog? = null
    private lateinit var binding: ActivitySplashBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        supportActionBar?.hide()
        pref = getSharedPreferences(resources.getString(R.string.app_name), MODE_PRIVATE)

//        this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)

        setupViewModel()
//        setServerRetryCount()
        setupObservers()
        checkIsTermsOfServiceAccepted()

    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[SplashActivityViewModel::class.java]
        viewModel.setSubjectAndFileNameDataListModel(AssertReader.getJsonFromAssets(this, "subject_data.json"))
        viewModel.setRepositoryLink(this, getMobileID())
    }


    private fun setupObservers(){
//        viewModel.remoteRepoErrorExceptionRaised().observe(this){isException ->
//            isException?.let {
//                cancelInitializingAppDialog()
//                if(it){
//                    decrementServerRetryCount()
//                    checkRetryCount()
//                }
//            }
//        }

        viewModel.getAreSubjectsPackagesAvailable().observe(this){subjectPackagesAvailable ->
            subjectPackagesAvailable?.let{
                if(it){
                    gotoMainActivity()
                }else{
                    displayServerTimeOutDialog()
                }

            }
        }

    }

//    private fun checkRetryCount(){
//        if (viewModel.getServerRetryCountsLeft() > 0){
//            displayInitializingAppDialog()
//        }else{
////            resetServerRetryCount()
//            displayServerTimeOutDialog()
//        }
//    }

//    private fun setServerRetryCount(){
//        viewModel.setServerRetryCountsLeft(serverRetryLimit)
//    }
//
//    private fun decrementServerRetryCount(){
//        viewModel.decrementServerRetryCount()
//    }
//
//    private fun resetServerRetryCount(){
//        viewModel.resetServerRetryCount(serverRetryLimit)
//
//    }

    private fun checkIsTermsOfServiceAccepted(){
        val termsAccepted = pref.getBoolean(MCQConstants.TERMS_ACCEPTED, false)
        if (termsAccepted){
//            cancelInitializingAppDialog()
            syncSubjectsPackages()
//            gotoMainActivity()
        }else{
            displayInternetConnectionDialog()
        }

    }

    private fun displayInternetConnectionDialog(){
        displayTermsOfServiceDialog()
    }

    private fun displayTermsOfServiceDialog(){
//        val view = LayoutInflater.from(this).inflate(R.layout.terms_of_use_layout, null)
        val dialogBinding = TermsOfUseLayoutBinding.inflate(layoutInflater)
        dialogBinding.btnTerms.setOnClickListener {
            gotoTermsOfServiceActivity()
        }

       dialogBinding.btnPrivacyPolicy.setOnClickListener {
            gotoPrivacyPolicy()
        }


        termsOfServiceDialog = AlertDialog.Builder(this).create()
        termsOfServiceDialog?.setTitle(resources.getString(R.string.agreement))
        termsOfServiceDialog?.setView(dialogBinding.root)
        termsOfServiceDialog?.setButton(AlertDialog.BUTTON_POSITIVE, resources.getString(R.string.accept)) { _, _ ->
            saveTermsOfServiceAcceptedStatus(true)

            checkIsTermsOfServiceAccepted()

        }
        termsOfServiceDialog?.setButton(AlertDialog.BUTTON_NEGATIVE, resources.getString(R.string.decline)) { _, _ ->
            finish()
        }
        termsOfServiceDialog?.setCancelable(false)
        termsOfServiceDialog?.show()
    }

    private fun gotoTermsOfServiceActivity(){
        startActivity(TermsOfServiceActivity.getIntent(this))
    }

    private fun gotoPrivacyPolicy() {
        val uri = Uri.parse(MCQConstants.PRIVACY_POLICY)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.addFlags(
            Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        )

        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(MCQConstants.PRIVACY_POLICY)))
        }
    }

    private fun displayServerTimeOutDialog(){
        val timeoutDialog = AlertDialog.Builder(this).apply {
//            setTitle("GCE OL MCQs failed to Initialize")
            setMessage("GCE OL MCQs failed to Initialize due to connection error. Please ensure you have an active internet connection.")
//            setPositiveButton("Retry"){_, _ ->
//                checkRetryCount()
//                finish()
//
//            }
            setNegativeButton("Exit"){_, _ ->
                finish()
            }
        }.create()
        timeoutDialog.show()
    }

//    private fun cancelInitializingAppDialog(){
//        if (initializingAppDialog != null){
//            initializingAppDialog?.dismiss()
//        }
//
//    }

    private fun gotoMainActivity(){
        CoroutineScope(Dispatchers.IO).launch{
            delay(2000L)
            withContext(Dispatchers.Main){
                val intent = Intent(this@GCEFirstActivity, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
//
            }
        }
    }

    private fun getJsonFromAssets(): String? {
        val charset: Charset = Charsets.UTF_8

        return try {
            val jsonFile = assets.open("subject_data.json")
            val size = jsonFile.available()
            val buffer = ByteArray(size)

            jsonFile.read(buffer)
            jsonFile.close()
            String(buffer, charset)

        } catch (e: IOException) {
            null
        }
    }

    private fun saveTermsOfServiceAcceptedStatus(state: Boolean){
        pref.edit().apply {
            putBoolean(MCQConstants.TERMS_ACCEPTED, state)
            apply()
        }
    }

    @SuppressLint("HardwareIds")
    fun getMobileID(): String {
        return Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
    }

    private fun syncSubjectsPackages(){
        viewModel.synchronizeSubjectsPackageData()
    }
}