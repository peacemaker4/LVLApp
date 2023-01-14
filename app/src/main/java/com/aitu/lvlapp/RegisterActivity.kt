package com.aitu.lvlapp

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.aitu.lvlapp.databinding.ActivityRegisterBinding
import com.aitu.lvlapp.models.User
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    //View binding
    private lateinit var binding: ActivityRegisterBinding


    //Firebase
    private lateinit var firebaseAuth: FirebaseAuth
    private var username = ""
    private var email = ""
    private var password = ""
    private var passwordConfirm = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        binding.registerBtn.setOnClickListener{
            validateData();
        }
    }
    private fun validateData(){
        username = binding.usernameInput.text.toString().trim()
        email = binding.emailInput.text.toString().trim()
        password = binding.passwordInput.text.toString().trim()
        passwordConfirm = binding.passwordConfirmInput.text.toString().trim()

        if(TextUtils.isEmpty(username)){
            binding.usernameInput.error = "Enter username"
        }
        else if(username.length < 3){
            binding.usernameInput.error = "Username should be at least 3 characters long"
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.emailInput.error = "Invalid email"
        }
        else if(TextUtils.isEmpty(password)){
            binding.passwordInput.error = "Enter password"
        }
        else if(password.length < 8){
            binding.passwordInput.error = "Password length should be at least 8 symbols"
        }
        else if(password != passwordConfirm){
            binding.passwordInput.error = "Passwords do not match"
            binding.passwordConfirmInput.error = "Passwords do not match"
        }
        else{
            firebaseSignUp()
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    private fun firebaseSignUp() {

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {

                val firebaseUser = firebaseAuth.currentUser
                val email = firebaseUser!!.email

                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener{
                    e ->
                Toast.makeText(this, "Register failed due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        if(firebaseUser != null){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}