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
import kotlinx.android.synthetic.main.activity_sign_up.*

@Suppress("UNUSED_PARAMETER")
class SignUpActivity : AppCompatActivity() {

    private lateinit var mAuth : FirebaseAuth
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mRef : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        mAuth = FirebaseAuth.getInstance()
        mRef = FirebaseDatabase.getInstance().reference

        val googleSignInOptions = GoogleSignInOptions
            .Builder()
            .requestIdToken("826872573086-65moc20k9s025v4fpcvs33gs5ctu3kvi.apps.googleusercontent.com")
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)
    }

    private fun updateFireBase(data : UserData, complete : (Boolean) -> Unit) {
        DataServices.isLogin = true
        DataServices.saveToPref(this)
        val map = HashMap<String, String>()
        map["name"] = data.userName
        map["email"] = data.userEmail
        map["userFuid"] = data.userFuid
        map["profileUrl"] = data.profileUrl
        mRef.child("Users").child(data.userFuid).setValue(map).addOnCompleteListener {
            complete(true)
        }.addOnFailureListener{
            complete(false)
        }
    }

    fun signUpBtnClicked(view: View) {
        val required = "required"
        val name = regName.text.toString()
        val email = regEmail.text.toString()
        val password = regPassword.text.toString()
        if (name.isNotEmpty()) {
            if (email.isNotEmpty()) {
                if (password.isNotEmpty()) {
                    createAccountBtn.isEnabled = false
                    val dialog = SpotsDialog
                        .Builder()
                        .setContext(this)
                        .setMessage("please wait while we set up your account")
                        .setTheme(R.style.Custom)
                        .build()
                        .apply {
                            show()
                        }
                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            DataServices.userData = UserData(
                                task.result!!.user.uid,
                                email,
                                name,
                                ""
                            )
                            updateFireBase(DataServices.userData) {
                                createAccountBtn.isEnabled = true
                                dialog.dismiss()
                                if (it) {
                                    startActivity(Intent(this, MainActivity::class.java))
                                    finish()
                                    MyFunctions.transitionZoom(this)
                                } else {
                                    val map = HashMap<String, String>()
                                    map["name"] = name
                                    map["email"] = email
                                    mRef.child("Users").child(task.result!!.user.uid).setValue(map)
                                }
                            }
                        } else {
                            dialog.dismiss()
                            createAccountBtn.isEnabled = true
                            Snackbar.make(main_layout, "Registration Failed.", Snackbar.LENGTH_LONG).show()
                        }
                    }

                } else {
                    regPassword.error = required
                }
            } else {
                regEmail.error = required
            }
        } else {
            regName.error = required
        }
    }

    private val rcSignIN = 200
    fun gMailRegSignUp(view : View) {
        progressBar.visibility = View.VISIBLE
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, rcSignIN)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == rcSignIN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                progressBar.visibility = View.GONE
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

                        updateFireBase(DataServices.userData) {
                            progressBar.visibility = View.GONE
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                            MyFunctions.transitionZoom(this)
                        }
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "signInWithCredential:failure", Toast.LENGTH_LONG).show()
                    Snackbar.make(main_layout, "Authentication Failed.", Snackbar.LENGTH_LONG).show()
                }
            }
    }
}
