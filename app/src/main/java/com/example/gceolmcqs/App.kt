package com.example.gceolmcqs

import android.app.Application
import com.parse.Parse
//import net.compay.android.CamPay

//import net.compay.android.CamPay

class App: Application() {

    override fun onCreate() {
        super.onCreate()
        Parse.initialize(
            Parse.Configuration.Builder(this)
                .applicationId(getString(R.string.back4app_app_id))
                // if defined
                .clientKey(getString(R.string.back4app_client_key))
                .server(getString(R.string.back4app_server_url))
                .build()
        )

//        CamPay.init(
//            getString(R.string.campay_app_user_name),
//            getString(R.string.campay_app_pass_word),
//            CamPay.Environment.DEV // environment
//        )


    }

}