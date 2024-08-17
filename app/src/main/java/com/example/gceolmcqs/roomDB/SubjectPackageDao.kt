package com.example.gceolmcqs.roomDB

import androidx.room.*
import com.example.gceolmcqs.datamodels.SubjectPackageData
//import com.example.gceolmcq.datamodels.TestData

@Dao
interface SubjectPackageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(subjectPackageData: SubjectPackageData)

    @Update()
    fun update(subjectPackageData: SubjectPackageData): Int

    @Query("DELETE FROM subject_package_table")
    fun deleteAll()

    //
    @Query("SELECT * FROM subject_package_table")
    fun getAllSubjectsPackages(): List<SubjectPackageData>

    @Query("SELECT * FROM subject_package_table WHERE subject_name LIKE :subjectName")
    fun findBySubjectName(subjectName: String): SubjectPackageData
}