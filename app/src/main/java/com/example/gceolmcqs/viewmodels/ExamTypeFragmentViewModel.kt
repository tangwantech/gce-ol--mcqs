package com.example.gceolmcqs.viewmodels

import androidx.lifecycle.ViewModel
import com.example.gceolmcqs.datamodels.ExamItemDataModel
import com.example.gceolmcqs.datamodels.ExamTypeDataModel

class ExamTypeFragmentViewModel: ViewModel() {
    private lateinit var examTypeDataModel: ExamTypeDataModel

    fun setExamTypeData(examTypeDataModel: ExamTypeDataModel){
        this.examTypeDataModel = examTypeDataModel
    }

    fun getExamTypeItemsData(): ArrayList<ExamItemDataModel>{
        return examTypeDataModel.examItems
    }
    fun getExamItemDataAt(position: Int):ExamItemDataModel{
        return examTypeDataModel.examItems[position]
    }
}