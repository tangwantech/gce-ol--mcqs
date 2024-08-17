package com.example.gceolmcqs.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.webkit.WebView
import com.example.gceolmcqs.MCQConstants
import com.example.gceolmcqs.R

class TermsOfServiceActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms_of_service)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "Terms of use of service"
        initViews()
    }
    private fun initViews(){
        val webView: WebView = findViewById(R.id.webView)
        webView.loadUrl(MCQConstants.TERMS_URL)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
    companion object{
        fun getIntent(context: Context): Intent {
            return Intent(context, TermsOfServiceActivity::class.java)
        }
    }
}