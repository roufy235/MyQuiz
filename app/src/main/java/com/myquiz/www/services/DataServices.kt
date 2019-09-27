package com.myquiz.www.services

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.myquiz.www.models.QuestionOptionsModel
import com.myquiz.www.models.UserData

object DataServices {

    private lateinit var prefs : SharedPreferences
    var isLogin : Boolean = false
    var userData : UserData = UserData()
    var userCoinBal : Int = 0

    //quiz questioms
    val question : ArrayList<QuestionOptionsModel> = ArrayList()
    var questionIsOnGoing : Boolean = false


    fun saveToPref(context: Context) {
        prefs = context.getSharedPreferences("MyQuizApp", Context.MODE_PRIVATE)
        if (isLogin) {
            val dataStr = Gson().toJson(userData)
            prefs.edit().apply {
                putString("userData", dataStr)
                putString("email", userData.userEmail)
                putBoolean("isLogin", isLogin)
                apply()
            }
        } else {
            prefs.edit().apply {
                putString("userData", "")
                putBoolean("isLogin", isLogin)
                apply()
            }
        }
    }

    fun getEmail(context: Context) : String? {
        prefs = context.getSharedPreferences("MyQuizApp", Context.MODE_PRIVATE)
        return prefs.getString("email", "")
    }

    private fun initializeVars(context: Context) {
        prefs = context.getSharedPreferences("MyQuizApp", Context.MODE_PRIVATE)
        val dataStr = prefs.getString("userData", "")
        if (dataStr != null) {
            userData = Gson().fromJson(dataStr.toString(), UserData::class.java)
        }
    }

    fun getIsLogin(context: Context) : Boolean {
        prefs = context.getSharedPreferences("MyQuizApp", Context.MODE_PRIVATE)
        isLogin = prefs.getBoolean("isLogin", false)
        if (isLogin) {
            initializeVars(context)
        }
        return isLogin
    }

}