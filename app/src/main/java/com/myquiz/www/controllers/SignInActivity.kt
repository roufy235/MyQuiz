package com.myquiz.www.controllers

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.View
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.myquiz.www.R
import com.myquiz.www.models.UserData
import com.myquiz.www.services.DataServices
import com.myquiz.www.utilities.MyFunctions
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_sign_in.*

@Suppress("UNUSED_PARAMETER")
class SignInActivity : AppCompatActivity() {

    private lateinit var mAuth : FirebaseAuth
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mRef : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        mAuth = FirebaseAuth.getInstance()
        mRef = FirebaseDatabase.getInstance().reference

        logEmail.setText(DataServices.getEmail(this))

        val googleSignInOptions = GoogleSignInOptions
            .Builder()
            .requestIdToken("826872573086-65moc20k9s025v4fpcvs33gs5ctu3kvi.apps.googleusercontent.com")
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)
    }

    private fun updateFireBase(data : UserData) {
        DataServices.isLogin = true
        DataServices.saveToPref(this)
        mRef.child("Users").child(data.userFuid).child("name").setValue(data.userName)
        mRef.child("Users").child(data.userFuid).child("email").setValue(data.userEmail)
        mRef.child("Users").child(data.userFuid).child("profileUrl").setValue(data.profileUrl)
        mRef.child("Users").child(data.userFuid).child("userFuid").setValue(data.userFuid)
    }

    fun createAccountBtnClicked(view: View) {
        startActivity(Intent(this, SignUpActivity::class.java))
        finish()
        MyFunctions.transitionZoom(this)
    }

    fun loginBtnClickedBtn(view : View) {
        val required = "required"
        val email = logEmail.text.toString()
        val password = logPassword.text.toString()
        if (email.isNotEmpty()) {
            if (password.isNotEmpty()) {
                val dialog = SpotsDialog
                    .Builder()
                    .setContext(this)
                    .setMessage("logging in.....")
                    .setTheme(R.style.Custom)
                    .build()
                    .apply {
                        show()
                    }
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    dialog.dismiss()
                    if (task.isSuccessful) {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                        MyFunctions.transitionZoom(this)
                    } else {
                        Snackbar.make(main_layoutS, "Authentication Failed.", Snackbar.LENGTH_LONG).show()
                    }
                }
            } else {
                logPassword.error = required
            }
        } else {
            logEmail.error = required
        }
    }

    private val rcSignIn = 200
    fun gmailLoginLayoutClicked(view: View) {
        progressBarSignIn.visibility = View.VISIBLE
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, rcSignIn)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == rcSignIn) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                progressBarSignIn.visibility = View.GONE
                Toast.makeText(this, "google sign in failed", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct : GoogleSignInAccount) {
            Toast.makeText(this, "firebaseAuthWithGoogle:" + acct.id!!, Toast.LENGTH_LONG)

            val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
            mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        //Toast.makeText(this, "signInWithCredential:success", Toast.LENGTH_LONG).show()
                        val user = mAuth.currentUser
                        if (user != null) {
                            DataServices.userData = UserData(
                                user.uid,
                                user.email.toString(),
                                user.displayName.toString(),
                                ""
                            )
                            updateFireBase(DataServices.userData)
                            progressBarSignIn.visibility = View.GONE
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                            MyFunctions.transitionZoom(this)
                    } else {
                        // If sign in fails, display a message to the user.
                            progressBarSignIn.visibility = View.GONE
                        Toast.makeText(this, "signInWithCredential:failure", Toast.LENGTH_LONG).show()
                        Snackbar.make(main_layoutS, "Authentication Failed.", Snackbar.LENGTH_LONG).show()
                    }
                }
        }
    }
}
