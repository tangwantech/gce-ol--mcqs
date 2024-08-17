package com.example.gceolmcqs.datamodels

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName="subject_package_table")
data class SubjectPackageData(

    @PrimaryKey(autoGenerate = true)
    var subjectIndex: Int? = null,
//    @ColumnInfo(name="package_index")

    @ColumnInfo(name="subject_name")
    var subjectName: String? = null,

    @ColumnInfo(name="package_name")
    var packageName: String? = null,

    @ColumnInfo(name="activated_on")
    var activatedOn: String? = null,

    @ColumnInfo(name="expires_on")
    var expiresOn: String? = null,

    @ColumnInfo(name="package_status")
    var isPackageActive: Boolean? = null
): Serializable



