package com.myquiz.www.controllers

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.Toast
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.ads.AdRequest
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.mxn.soul.flowingdrawer_core.ElasticDrawer
import com.mxn.soul.flowingdrawer_core.FlowingDrawer
import com.myquiz.www.R
import com.myquiz.www.services.DataServices
import com.myquiz.www.utilities.MyFunctions
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.navigation_view.*

@Suppress("UNUSED_PARAMETER")
class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout : FlowingDrawer
    private lateinit var mAuth : FirebaseAuth
    private var currentUser : FirebaseUser? = null
    private lateinit var mRef : DatabaseReference
    private lateinit var mAdView : AdView
    private lateinit var mFirebaseAnalytics : FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mAuth = FirebaseAuth.getInstance()
        mRef = FirebaseDatabase.getInstance().reference
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        currentUser = mAuth.currentUser
        if (currentUser != null) {
            MyFunctions.getUserCoin(currentUser!!) {coinBalance ->
                userCoin.text = if (coinBalance != null) {
                    val coin = "coins($coinBalance)"
                    coin
                } else {
                    ""
                }
            }
        }
        // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713
        MobileAds.initialize(this, "ca-app-pub-4245492336540799~7035199291")
        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
        mAdView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                Toast.makeText(this@MainActivity, "onAdLoaded", Toast.LENGTH_LONG).show()
            }

            override fun onAdFailedToLoad(errorCode : Int) {
                // Code to be executed when an ad request fails.
                Toast.makeText(this@MainActivity, "onAdFailedToLoad ::: $errorCode", Toast.LENGTH_LONG).show()
            }

            override fun onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
                Toast.makeText(this@MainActivity, "onAdOpened", Toast.LENGTH_LONG).show()
            }

            override fun onAdLeftApplication() {
                // Code to be executed when the user has left the app.
                Toast.makeText(this@MainActivity, "onAdLeftApplication", Toast.LENGTH_LONG).show()
            }

            override fun onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
                Toast.makeText(this@MainActivity, "onAdClosed", Toast.LENGTH_LONG).show()
            }
        }

        drawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.setTouchMode(ElasticDrawer.TOUCH_MODE_BEZEL)
        drawerLayout.setOnDrawerStateChangeListener(object : ElasticDrawer.OnDrawerStateChangeListener{
            override fun onDrawerStateChange(oldState: Int, newState: Int) {
                if (newState == ElasticDrawer.STATE_OPEN) {
                    activityIdentifier = ""
                }
                if (newState == ElasticDrawer.STATE_CLOSED) {
                    if (activityIdentifier.isNotEmpty()) {
                        when(activityIdentifier) {
                            "earn" -> {
                                startActivity(Intent(this@MainActivity, EarnCoinActivity::class.java))
                                MyFunctions.transitionZoom(this@MainActivity)
                            }
                            "settings" -> {
                                startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                                MyFunctions.transitionZoom(this@MainActivity)
                            }
                        }
                    }
                }
            }

            override fun onDrawerSlide(openRatio: Float, offsetPixels: Int) {

            }
        })
        closeDrawer.setOnClickListener {
            openDrawer(false)
        }
        updateUi()
    }

    private fun updateUi() {
        navProfileName.text = DataServices.userData.userName
    }


    fun logout(view: View) {
        AlertDialog.Builder(this, R.style.AlertDialogThemeCustom)
            .setMessage("Do you want to logout?")
            .setPositiveButton("Yes") { dialogInterface, i ->
                dialogInterface.dismiss()
                mAuth.signOut()
                finish()
                startActivity(Intent(this, SignInActivity::class.java))
                MyFunctions.transitionZoom(this)
            }
            .setNegativeButton("No") { dialogInterface, i ->
                dialogInterface.dismiss()
            }
            .setCancelable(false)
            .show()

    }

    private fun openDrawer(boolean: Boolean) {
        if (boolean) {
            drawerLayout.openMenu()
           // drawerLayout.openDrawer(GravityCompat.START)
        } else {
            drawerLayout.closeMenu()
            //drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    fun settings(view: View) {
        openDrawer(false)
        if (DataServices.isLogin) {
            activityIdentifier = "settings"
        }
    }
    private var activityIdentifier : String = ""
    fun earnCoinLayoutClicked(view: View) {
        openDrawer(false)
        if (DataServices.isLogin) {
            activityIdentifier = "earn"
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.drawerState == ElasticDrawer.STATE_OPEN) {
            openDrawer(false)
        } else {
            AlertDialog.Builder(this, R.style.AlertDialogThemeCustom)
                .setMessage("Do you want to close " + MyFunctions.appName + "?")
                .setPositiveButton("Yes") { dialogInterface, i ->
                    dialogInterface.dismiss()
                    super.onBackPressed()
                }
                .setNegativeButton("No") { dialogInterface, i ->
                    dialogInterface.dismiss()
                }
                .setCancelable(false)
                .show()
        }
    }

    fun hamMenu(view: View) {
        openDrawer(true)
    }


    fun artAndLitClicked(view: View) {
        val quest = Intent(this, QuestionActivity::class.java)
        quest.putExtra("category", getString(R.string.art_and_literature))
        quest.putExtra("cateCode", "catOne")
        startActivity(quest)
        MyFunctions.transitionZoom(this)
    }

    fun generalKnowledge(view: View) {
        val quest = Intent(this, QuestionActivity::class.java)
        quest.putExtra("category", getString(R.string.general_knowledge))
        quest.putExtra("cateCode", "catTwo")
        startActivity(quest)
        MyFunctions.transitionZoom(this)
    }

    fun history(view: View) {
        val quest = Intent(this, QuestionActivity::class.java)
        quest.putExtra("category", getString(R.string.history))
        quest.putExtra("cateCode", "catThree")
        startActivity(quest)
        MyFunctions.transitionZoom(this)
    }

    fun music(view: View) {
        val quest = Intent(this, QuestionActivity::class.java)
        quest.putExtra("category", getString(R.string.music))
        quest.putExtra("cateCode", "catFour")
        startActivity(quest)
        MyFunctions.transitionZoom(this)
    }

    fun scienceAndNature(view: View) {
        val quest = Intent(this, QuestionActivity::class.java)
        quest.putExtra("category", getString(R.string.science_and_nature))
        quest.putExtra("cateCode", "catFive")
        startActivity(quest)
        MyFunctions.transitionZoom(this)
    }

    fun sport(view: View) {
        val quest = Intent(this, QuestionActivity::class.java)
        quest.putExtra("category", getString(R.string.sport))
        quest.putExtra("cateCode", "catSix")
        startActivity(quest)
        MyFunctions.transitionZoom(this)
    }

    fun TVAndFilms(view: View) {
        val quest = Intent(this, QuestionActivity::class.java)
        quest.putExtra("category", getString(R.string.tv_and_films))
        quest.putExtra("cateCode", "catSeven")
        startActivity(quest)
        MyFunctions.transitionZoom(this)
    }

    fun geography(view: View) {
        val quest = Intent(this, QuestionActivity::class.java)
        quest.putExtra("category", getString(R.string.geography))
        quest.putExtra("cateCode", "catEight")
        startActivity(quest)
        MyFunctions.transitionZoom(this)
    }

    fun computerTrivia(view: View) {
        val quest = Intent(this, QuestionActivity::class.java)
        quest.putExtra("category", getString(R.string.computer_trivia))
        quest.putExtra("cateCode", "catNine")
        startActivity(quest)
        MyFunctions.transitionZoom(this)
    }
}
