package com.example.gceolmcqs

import android.os.CountDownTimer
import android.text.format.Time
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class SubscriptionCountDownTimer(private val position: Int ) {
    private lateinit var timer: CountDownTimer
    private val isTimeOut = MutableLiveData<Boolean>()

    private val timeRemaining = MutableLiveData<Time>()
    private val isTimeAlmostOut = MutableLiveData<Boolean>()

    fun startTimer(duration:Long, onTimeRemainingListener: OnTimeRemainingListener) {
        timer = object : CountDownTimer(duration, MCQConstants.COUNT_DOWN_INTERVAL) {
            override fun onTick(p0: Long) {
                val t = Time()
                updateIsTimeAlmostOut(p0)
                t.set(p0)
                timeRemaining.value = t
                onTimeRemainingListener.onTimeRemaining(getExpiresIn(t))
            }

            override fun onFinish() {
                val t = Time().apply { set(0) }
                onTimeRemainingListener.onTimeRemaining(getExpiresIn(t))
                onTimeRemainingListener.onExpired()
                isTimeOut.value = true
            }

        }.start()
    }

    fun getExpiresIn(timeLeft: Time): String {
        var timeRemainingInSeconds = timeLeft.toMillis(true) / 1000
        val secondsInHours = 3600
        val secondsInMinutes = 60

        val hoursRemaining = timeRemainingInSeconds / secondsInHours
        timeRemainingInSeconds %= secondsInHours
        val minutesRemaining = timeRemainingInSeconds / secondsInMinutes
        timeRemainingInSeconds %= secondsInMinutes

        return if(hoursRemaining > 0){
            "${hoursRemaining.toString().padStart(2, '0')} Hrs : ${
                minutesRemaining.toString().padStart(2, '0')
            } Mins : ${timeRemainingInSeconds.toString().padStart(2, '0')} s"
        }else if (minutesRemaining > 0){
            "${minutesRemaining.toString().padStart(2, '0')
            } Mins : ${timeRemainingInSeconds.toString().padStart(2, '0')} s"
        }else{
            "${timeRemainingInSeconds.toString().padStart(2, '0')} s"
        }
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

    interface OnTimeRemainingListener{
        fun onTimeRemaining(expiresIn:String)
        fun onExpired()
    }

}