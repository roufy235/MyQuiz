package com.myquiz.www.controllers

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.myquiz.www.R
import com.myquiz.www.services.DataServices
import com.myquiz.www.utilities.MyFunctions
import kotlinx.android.synthetic.main.activity_settings.*

@Suppress("UNUSED_PARAMETER")
class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        settingEmail.text = DataServices.userData.userEmail
        settingName.text = DataServices.userData.userName.capitalize()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        MyFunctions.transitionZoom(this)
    }


    fun settingsGoBack(view : View) {
        finish()
        MyFunctions.transitionZoom(this)
    }
}
