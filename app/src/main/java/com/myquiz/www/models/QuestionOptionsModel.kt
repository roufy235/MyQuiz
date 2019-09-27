package com.myquiz.www.models

import com.google.gson.annotations.SerializedName

data class QuestionOptionsModel(
    @SerializedName("A")
    val a : String = "",
    @SerializedName("B")
    val b : String = "",
    @SerializedName("C")
    val c : String = "",
    @SerializedName("D")
    val d : String = "",
    @SerializedName("Answer")
    val answer : String = "",
    @SerializedName("Question")
    val question : String = "",
    var questionKey : String = ""
)