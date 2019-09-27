package com.myquiz.www.models

import com.google.gson.annotations.SerializedName

data class UserData(
    @SerializedName("userFuid")
    val userFuid : String = "",
    @SerializedName("email")
    val userEmail : String = "",
    @SerializedName("name")
    val userName : String = "",
    @SerializedName("profileUrl")
    val profileUrl : String = ""
)