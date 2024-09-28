package com.example.gceolmcqs

import android.app.Application
import com.example.gceolmcqs.datamodels.CampayCredential
import com.google.gson.Gson
import com.parse.Parse
import net.compay.android.CamPay

class App: Application() {

    override fun onCreate() {
        super.onCreate()
        initParse()
        initCampay()

    }

    private fun initParse(){
        Parse.initialize(
            Parse.Configuration.Builder(this)
                .applicationId(getString(R.string.back4app_app_id))
                // if defined
                .clientKey(getString(R.string.back4app_client_key))
                .server(getString(R.string.back4app_server_url))
                .build()
        )
    }

    private fun initCampay() {
        val jsonString = AssertReader.getJsonFromAssets(this, "campay_credentials.json")
        val campayCredential = Gson().fromJson(jsonString, CampayCredential::class.java)
        CamPay.init(
            campayCredential.username,
            campayCredential.password,
            CamPay.Environment.DEV // environment
        )
    }


}