package com.example.gceolmcqs.datamodels
import java.io.Serializable
data class CurrentPackageDataModel(
    var packageType: String? = null,
    var packageStatus: String? = null,
    var packageActivatedOn: String? = null,
    var packageExpiresOn: String? = null
):Serializable
