package com.bek.lvlapp.helpers

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.bek.lvlapp.WelcomeActivity
import com.google.firebase.auth.FirebaseAuth

class AuthManager() {

    var email: String = ""

    val firebaseAuth = FirebaseAuth.getInstance()

    val firebaseUser = firebaseAuth.currentUser

    fun getUserEmail(): String {
        if(firebaseUser != null){
            email = firebaseUser.email.toString();
        }
        return email
    }

    fun logout(){
        firebaseAuth.signOut()
    }

    fun checkUser(context: Context) {
        //check if user logged in
        val currUser = firebaseAuth.currentUser
        if(currUser == null){
            context.startActivity(Intent(context, WelcomeActivity::class.java))
            val activity = context as Activity?
            activity!!.finish()
        }
    }
}