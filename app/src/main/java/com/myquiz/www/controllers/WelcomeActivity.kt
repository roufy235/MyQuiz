package com.myquiz.www.controllers

import android.content.Intent
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.google.firebase.auth.FirebaseAuth
import com.myquiz.www.R
import com.myquiz.www.services.DataServices
import com.myquiz.www.utilities.MyFunctions

class WelcomeActivity : AppCompatActivity() {

    private lateinit var mAuth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        mAuth = FirebaseAuth.getInstance()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val w = window
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        }

        YoYo.with(Techniques.ZoomIn)
            .duration(1500)
            .repeat(0)
            .playOn(findViewById(R.id.quizAppName))

        Handler().postDelayed({
            if (DataServices.getIsLogin(this)) {
                if (mAuth.currentUser != null) {
                    startActivity(Intent(this, MainActivity::class.java))
                } else {
                    startActivity(Intent(this, SignInActivity::class.java))
                }
            } else {
                startActivity(Intent(this, SignInActivity::class.java))
            }
            finish()
            MyFunctions.transitionZoom(this)
        }, 3000)
    }
}
