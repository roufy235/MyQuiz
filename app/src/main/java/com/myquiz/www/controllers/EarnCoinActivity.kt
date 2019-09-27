package com.myquiz.www.controllers

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.myquiz.www.R
import com.myquiz.www.utilities.MyFunctions

@Suppress("UNUSED_PARAMETER")
class EarnCoinActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_earn_coin)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        MyFunctions.transitionZoom(this)
    }

    fun earnGoBack(view : View) {
        finish()
        MyFunctions.transitionZoom(this)
    }
}
