package com.example.gceolmcqs.viewmodels

import androidx.lifecycle.ViewModel
import com.example.gceolmcqs.datamodels.ExamItemData
import com.example.gceolmcqs.datamodels.ExamTypeData

class ExamTypeFragmentViewModel: ViewModel() {
    private lateinit var examTypeData: ExamTypeData

    fun setExamTypeData(examTypeData: ExamTypeData){
        this.examTypeData = examTypeData
    }

    fun getExamTypeItemsData(): ArrayList<ExamItemData>{
        return examTypeData.examItems
    }
    fun getExamItemDataAt(position: Int):ExamItemData{
        return examTypeData.examItems[position]
    }
}