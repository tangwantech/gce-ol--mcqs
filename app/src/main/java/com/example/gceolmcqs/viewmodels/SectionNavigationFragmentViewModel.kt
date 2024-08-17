package com.example.gceolmcqs.viewmodels

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.gceolmcqs.repository.PaperRepository

class SectionNavigationFragmentViewModel : ViewModel() {

//    fun getSectionNames(): Array<String>? {
//        return PaperRepository.getSectionNames()
//    }

    fun getSectionNameBundleList():Array<Bundle>?{
        return PaperRepository.getSectionNameBundleList()
    }

    fun getNumberOfSections(): Int {
        return PaperRepository.getNumberOfSections()
    }

    fun getSectionsAnswered(): List<Boolean> {
        return PaperRepository.getSectionsAnswered()
    }

    fun getNumberOfSectionsAnswered(): LiveData<Int> {
        return PaperRepository.getNumberOfSectionsAnswered()
    }

    fun getPaperScore(): LiveData<Int> {
        return PaperRepository.getPaperScore()
    }

    fun getTotalNumberOfQuestions(): Int {
        return PaperRepository.getTotalNumberOfQuestions()
    }

    fun getPaperGrade():LiveData<String?>{
        return PaperRepository.getPaperGrade()
    }

    fun getPaperPercentage():LiveData<Int>{
        return PaperRepository.getPaperPercentage()
    }

    fun getAreAllSectionsAnswered(): LiveData<Boolean>{
        return PaperRepository.getAreAllSectionsAnswered()
    }

    fun getSectionsScores(): ArrayList<Int>{
        return PaperRepository.getSectionsScores()
    }

    fun resetPaperRepo(){
        PaperRepository.resetPaperRepo()
    }


}