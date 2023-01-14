package com.aitu.lvlapp

import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aitu.lvlapp.databinding.ActivityWelcomeBinding
import com.google.firebase.auth.FirebaseAuth

class WelcomeActivity : AppCompatActivity() {

    //View binding
    private lateinit var binding: ActivityWelcomeBinding

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        binding.btnSignUp.setOnClickListener{
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Login
        binding.btnLogin.setOnClickListener{
            startActivity(Intent(this, LoginActivity::class.java))
        }

    }
    private fun checkUser() {
        //if user is already logged in go to main activity
        val firebaseUser = firebaseAuth.currentUser
        if(firebaseUser != null){


            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}