package com.myquiz.www.controllers

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.CardView
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.myquiz.www.R
import com.myquiz.www.models.QuestionOptionsModel
import com.myquiz.www.services.DataServices
import com.myquiz.www.utilities.MyFunctions
import kotlinx.android.synthetic.main.activity_question.*

@Suppress("UNUSED_PARAMETER")
class QuestionActivity : AppCompatActivity() {


    private  var timer : MyTimer? = null
    private val timeToCountDownMilli : Long = 120000 //2 mins
    private val timeToCountDownInterval : Long = 1000 //1 sec
    private lateinit var cateCode : String
    private lateinit var category : String
    private lateinit var mRef : DatabaseReference
    private var curentUser : FirebaseUser? = null
    private lateinit var mAuth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question)
        val bundle = intent.extras
        if (bundle != null) {
            category = bundle.getString("category", "")
            cateCode = bundle.getString("cateCode", "")
            questCat.text = category
        }
        DataServices.question.clear()
        mRef = FirebaseDatabase.getInstance().reference
        mAuth = FirebaseAuth.getInstance()
        curentUser = mAuth.currentUser
        /*
        val uploadQuestOptions : ArrayList<QuestionOptionsModel> = ArrayList()
        uploadQuestOptions.add(QuestionOptionsModel("Edward Lear", "Ernest B. Adames", "Celina J. Torres", "Braiwaithe",
            "Braiwaithe",
            "In the Adrian Mole Diaries, what is the surname of his girlfriend?"))


        uploadQuestOptions.add(QuestionOptionsModel("Nevermore", "Joel M. Brewer", "Richard Yates", "Susan C. Sharp",
            "Nevermore",
            "What word does the bird constantly repeat in Edgar Allan Poe`s classic poem The Raven?"))

        uploadQuestOptions.add(QuestionOptionsModel("George L. Ratcliffe", "Laputa", "Edwin T. Cork", "Sandra J. McCormick",
            "Laputa",
            "In Gullivers Travels, what is the name of the flying island?"))

        uploadQuestOptions.add(QuestionOptionsModel("Jason E. Thomas", "Linda R. Kuss", "Compton Mackenzie", "Claudia T. Delk",
            "Compton Mackenzie",
            "Who was the author of Whisky Galore?"))

        uploadQuestOptions.add(QuestionOptionsModel("Alejandro B. Soto", "Francis L. Scarborough", "Michael P. Stirling", "Richard III's",
            "Richard III's",
            "According to Shakespeare, whose horse was called White Surrey?"))

        uploadQuestOptions.add(QuestionOptionsModel("Bertha R. Carter", "Dawn French", "Martha J. Merritt", "Robin W. Sheeley",
            "Dawn French",
            "Who has written a series of letters entitled `Dear Fatty` in the form of an autobiography?"))


        for (counter in 0 until uploadQuestOptions.size) {
            val key = mRef.child("Questions").child(cateCode).push().key
            if (key != null) {
                mRef.child("Questions").child(cateCode).child(key).setValue(uploadQuestOptions[counter])
            }
        }

        */
        getQuestion()
    }


    override fun onDestroy() {
        super.onDestroy()
        cancelCountDown()
        if (getQuestionRef != null && getQuestionRefChildEventListener != null) {
            getQuestionRef!!.removeEventListener(getQuestionRefChildEventListener!!)
        }
    }

    private var getQuestionRef : DatabaseReference? = null
    private var getQuestionRefChildEventListener : ChildEventListener? = null
    private fun getQuestion() {
        mRef.child("Questions").child(cateCode).orderByChild("hasAnswered/"+curentUser!!.uid).equalTo(null).limitToLast(6).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    getQuestionRef = mRef.child("Questions").child(cateCode)
                    getQuestionRefChildEventListener = getQuestionRef!!.orderByChild("hasAnswered/"+curentUser!!.uid).equalTo(null).limitToLast(6).addChildEventListener(object : ChildEventListener{
                        override fun onCancelled(p0: DatabaseError) {
                            //Toast.makeText(this@QuestionActivity, "onCancelled", Toast.LENGTH_LONG).show()
                        }

                        override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                            //Toast.makeText(this@QuestionActivity, "onChildMoved", Toast.LENGTH_LONG).show()
                        }

                        override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                            //Toast.makeText(this@QuestionActivity, "onChildChanged", Toast.LENGTH_LONG).show()
                        }

                        override fun onChildAdded(dataSnapshot : DataSnapshot, p1: String?) {
                            if (dataSnapshot.exists() && dataSnapshot.childrenCount > 0) {
                                val question = dataSnapshot.getValue(QuestionOptionsModel::class.java)
                                if (question != null) {
                                    question.questionKey = dataSnapshot.key.toString()
                                    DataServices.question.add(question)
                                    dismissLoader()

                                    quizCounterTextFunc(1)
                                    Handler().postDelayed({
                                        quizBody(0)
                                    }, 1000)
                                } else {
                                    Toast.makeText(this@QuestionActivity, "question is null", Toast.LENGTH_LONG).show()
                                }
                            } else {
                                noQuestion()
                            }
                        }

                        override fun onChildRemoved(p0: DataSnapshot) {
                            //Toast.makeText(this@QuestionActivity, "onChildRemoved", Toast.LENGTH_LONG).show()
                        }

                    })
                } else {
                    noQuestion()
                }
            }
        })
    }

    private fun noQuestion() {
        emptyBox.visibility = View.VISIBLE
        emptyText.visibility = View.VISIBLE

        progressBar2.visibility = View.GONE
        progressbarText.visibility = View.GONE
        quizLayout.visibility = View.GONE
    }

    private fun dismissLoader() {
        quizLayout.visibility = View.VISIBLE
        progressBar2.visibility = View.GONE
        progressbarText.visibility = View.GONE
    }

    private fun quizCounterTextFunc(num : Int) {
        val counterText = "$num/" + DataServices.question.size
        quizCounterText.text = counterText
    }

    private fun startTimer(){
        if (timer == null) {
            timer = MyTimer(timeToCountDownMilli)
        }
        timer?.start()
    }


    private fun quizBody(num : Int) {
        if (num < DataServices.question.size) {
            DataServices.questionIsOnGoing = true
            isUserSelectAnOption = false
            questDashText.text = DataServices.question[num].question
            YoYo.with(Techniques.SlideInRight)
                .duration(1000)
                .repeat(0)
                .playOn(questDashText)
            cardAText.text = DataServices.question[num].a
            cardBText.text = DataServices.question[num].b
            cardCText.text = DataServices.question[num].c
            cardDText.text = DataServices.question[num].d
            startTimer()
        } else {
            noQuestion()
        }

    }

    private fun cancelCountDown() {
        if (timer != null) timer!!.cancel()
    }




    private fun resetUi() {
        cardAText.text = ""
        cardBText.text = ""
        cardCText.text = ""
        cardDText.text = ""
        questDashText.text = ""
        timerText.text = ""

        cardA.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white))
        cardB.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white))
        cardC.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white))
        cardD.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white))

        cardAText.setTextColor(ContextCompat.getColor(this, R.color.blackLight))
        cardBText.setTextColor(ContextCompat.getColor(this, R.color.blackLight))
        cardCText.setTextColor(ContextCompat.getColor(this, R.color.blackLight))
        cardDText.setTextColor(ContextCompat.getColor(this, R.color.blackLight))
    }


    private fun getCorrectAnswer() {
        when(DataServices.question[num].answer) {
            DataServices.question[num].a -> {
                cardA.setCardBackgroundColor(ContextCompat.getColor(this, R.color.green))
                cardAText.setTextColor(ContextCompat.getColor(this, R.color.white))
                YoYo.with(Techniques.ZoomIn)
                    .duration(1000)
                    .repeat(1)
                    .playOn(cardA)
            }
            DataServices.question[num].b -> {
                cardB.setCardBackgroundColor(ContextCompat.getColor(this, R.color.green))
                cardBText.setTextColor(ContextCompat.getColor(this, R.color.white))
                YoYo.with(Techniques.ZoomIn)
                    .duration(1000)
                    .repeat(1)
                    .playOn(cardB)
            }
            DataServices.question[num].c -> {
                cardC.setCardBackgroundColor(ContextCompat.getColor(this, R.color.green))
                cardCText.setTextColor(ContextCompat.getColor(this, R.color.white))
                YoYo.with(Techniques.ZoomIn)
                    .duration(1000)
                    .repeat(1)
                    .playOn(cardC)
            }
            DataServices.question[num].d -> {
                cardD.setCardBackgroundColor(ContextCompat.getColor(this, R.color.green))
                cardDText.setTextColor(ContextCompat.getColor(this, R.color.white))
                YoYo.with(Techniques.ZoomIn)
                    .duration(1000)
                    .repeat(1)
                    .playOn(cardD)
            }
            else -> {

            }
        }
    }

    private var num = 0
    private fun checkAnswer(cardView : CardView?, text : TextView?) {
        isUserSelectAnOption = true
        cancelCountDown()
        if (cardView != null && text != null) {
            if (text.text.toString() == DataServices.question[num].answer) {
                cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.green))
                text.setTextColor(ContextCompat.getColor(this, R.color.white))
                YoYo.with(Techniques.ZoomIn)
                    .duration(1000)
                    .repeat(1)
                    .playOn(cardView)
            } else {

                getCorrectAnswer()
                cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.red))
                text.setTextColor(ContextCompat.getColor(this, R.color.white))
                YoYo.with(Techniques.Wobble)
                    .duration(700)
                    .repeat(1)
                    .playOn(cardView)
            }
        } else {
            getCorrectAnswer()
        }

        if (curentUser != null) {
            val questFuid = DataServices.question[num].questionKey
            mRef.child("Questions").child(cateCode).child(questFuid).child("hasAnswered").child(curentUser!!.uid).setValue(true)
        }
        Handler().postDelayed({
            resetUi()
            num += 1
            if (num < DataServices.question.size) {
                quizCounterTextFunc(num + 1)
                quizBody(num)
            } else {
                DataServices.questionIsOnGoing = false
                cancelCountDown()
                Toast.makeText(this, "no more question", Toast.LENGTH_LONG).show()
            }

        }, 4000)
    }


    private var isUserSelectAnOption : Boolean = false
    fun optionAClicked(view: View) {
        if (!isUserSelectAnOption) {
            checkAnswer(cardA, cardAText)
        }
    }

    fun optionBClicked(view: View) {
        if (!isUserSelectAnOption) {
            checkAnswer(cardB, cardBText)
        }
    }

    fun optionCClicked(view: View) {
        if (!isUserSelectAnOption) {
            checkAnswer(cardC, cardCText)
        }
    }

    fun optionDClicked(view: View) {
        if (!isUserSelectAnOption) {
            checkAnswer(cardD, cardDText)
        }
    }

    override fun onBackPressed() {
        if (DataServices.questionIsOnGoing) {
            AlertDialog.Builder(this, R.style.AlertDialogThemeCustom)
                .setMessage("You have an active quiz section, do you want to cancel it?")
                .setPositiveButton("Yes") { dialogInterface, i ->
                    dialogInterface.dismiss()
                    DataServices.questionIsOnGoing = false
                    super.onBackPressed()
                    MyFunctions.transitionZoom(this)
                }
                .setNegativeButton("No") { dialogInterface, i ->
                    dialogInterface.dismiss()
                }
                .setCancelable(false)
                .show()
        } else {
            super.onBackPressed()
            MyFunctions.transitionZoom(this)
        }
    }

    fun navQuestionGoBack(view: View) {
        if (DataServices.questionIsOnGoing) {
            AlertDialog.Builder(this, R.style.AlertDialogThemeCustom)
                .setMessage("You have an active quiz section, do you want to cancel it?")
                .setPositiveButton("Yes") { dialogInterface, i ->
                    dialogInterface.dismiss()
                    DataServices.questionIsOnGoing = false
                    finish()
                    MyFunctions.transitionZoom(this)
                }
                .setNegativeButton("No") { dialogInterface, i ->
                    dialogInterface.dismiss()
                }
                .setCancelable(false)
                .show()
        } else {
            finish()
            MyFunctions.transitionZoom(this)
        }
    }


    inner class MyTimer(millis : Long) : CountDownTimer(millis, timeToCountDownInterval) {
        override fun onFinish() {
            if (!isUserSelectAnOption) {
                checkAnswer(null, null)
            }
        }

        override fun onTick(millisUntilFinished : Long) {
            val secLong = millisUntilFinished / 1000
            val min = (secLong / 60).toInt().toString()
            val sec = if ((secLong % 60).toInt() > 9) {
                (secLong % 60).toInt().toString()
            } else {
                "0" +(secLong % 60).toInt().toString()
            }
            val timeRemaining = "$min : $sec"
            timerText.text = timeRemaining
        }
    }
}
