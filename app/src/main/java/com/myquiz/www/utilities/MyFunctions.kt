package com.myquiz.www.utilities

import android.content.Context
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.myquiz.www.services.DataServices
import spencerstudios.com.bungeelib.Bungee

object MyFunctions {


    var appName = "My Quiz"
    val defaultCoinForNewUsers : Int = 500
    private val mRef : DatabaseReference = FirebaseDatabase.getInstance().reference

    fun transitionZoom(context: Context) {
        Bungee.zoom(context)
    }


    fun getUserCoin(currentUser : FirebaseUser, complete : (String?) -> Unit) {
        mRef.child("Users").child(currentUser.uid).child("coin").addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                complete(null)
            }
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val value = dataSnapshot.value.toString()
                    DataServices.userCoinBal = value.toInt()
                    complete(value)
                } else {
                    mRef.child("Users").child(currentUser.uid).child("coin").setValue(defaultCoinForNewUsers)
                    DataServices.userCoinBal = defaultCoinForNewUsers
                    complete(defaultCoinForNewUsers.toString())
                }
            }
        })
    }

}