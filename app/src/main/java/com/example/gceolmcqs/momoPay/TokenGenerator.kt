package com.example.gceolmcqs.momoPay

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.gceolmcqs.MCQConstants
import com.example.gceolmcqs.MomoPayService
import com.example.gceolmcqs.R
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class TokenGenerator(private val context: Context) {
    private val isTokenAvailable = MutableLiveData<Boolean?>()
//    private val token = MutableLiveData<String?>()

    fun generate(token: MutableLiveData<String?>, tokenStatus: TokenStatus){
        val client = OkHttpClient().newBuilder().build();
        val mediaType: MediaType? = MCQConstants.APPLICATION_JSON.toMediaTypeOrNull()
        val requestBody: RequestBody = RequestBody.create(
            mediaType,
            "{\n    \"${MCQConstants.USER_NAME}\": \"${context.resources.getString(R.string.campay_app_user_name)}\",\n    \"${MCQConstants.PASS_WORD}\": \"${context.resources.getString(
                R.string.campay_app_pass_word)}\"\n}"
        );
        val request = Request.Builder()
            .url(context.resources.getString(R.string.campay_token_url))
            .method(MCQConstants.POST, requestBody)
            .addHeader(MCQConstants.CONTENT_TYPE, MCQConstants.APPLICATION_JSON)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("failed generating token due to ${e.message}")
//                isTokenAvailable.postValue(false)
                token.postValue(null)
                tokenStatus.onTokenFailed()

            }

            override fun onResponse(call: Call, response: Response) {
                try{
                    val responseBody = response.body?.string()
                    println(responseBody)
                    val json = JSONObject(responseBody!!)
//                    isTokenAvailable.postValue(true)
                    token.postValue(json[MomoPayService.TOKEN].toString())
                    tokenStatus.onTokenAvailable(json[MomoPayService.TOKEN].toString())


                }catch (e: JSONException){
                    token.postValue(null)
                    tokenStatus.onTokenFailed()
//                    isTransactionSuccessful.postValue(false)
//                    isTokenAvailable.postValue(false)
//                    println("failed getting token from server due to ${e.message}")
                }

            }

        })
    }

//    fun getToken(): LiveData<String?> {
//        return token
//    }

    fun getIsTokenAvailable(): LiveData<Boolean?>{
        return isTokenAvailable
    }
    interface TokenStatus{
        fun onTokenAvailable(token: String)
        fun onTokenFailed()
    }

}