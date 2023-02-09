package com.bek.lvlapp

import android.app.ActionBar
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bek.lvlapp.databinding.ActivityRegisterBinding
import com.bek.lvlapp.models.User
import com.github.johnpersano.supertoasts.library.Style
import com.github.johnpersano.supertoasts.library.SuperActivityToast
import com.github.johnpersano.supertoasts.library.utils.PaletteUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.victor.loading.rotate.RotateLoading
import java.time.LocalDateTime

class RegisterActivity : AppCompatActivity() {

    //View binding
    private lateinit var binding: ActivityRegisterBinding


    //Firebase
    private lateinit var firebaseAuth: FirebaseAuth
    private var username = ""
    private var email = ""
    private var password = ""
    private var passwordConfirm = ""

    //todo add popup
    private lateinit var dialogBuilder: AlertDialog.Builder
    private lateinit var dialog: AlertDialog

    //Firebase db
    lateinit var database: DatabaseReference
    private val url = "https://lvlapp-ff610-default-rtdb.europe-west1.firebasedatabase.app"
    private val path = "users"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        database = Firebase.database(url).reference

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
        loadingBar("Signing up")
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {

                val firebaseUser = firebaseAuth.currentUser
                val email = firebaseUser!!.email

                val user = User(firebaseUser!!.uid, username, email)
                user.created_at = LocalDateTime.now().toString()
                user.updated_at = LocalDateTime.now().toString()

                database.child(path).child(user.uid.toString()).setValue(user).addOnSuccessListener { e->
                }.addOnFailureListener{ e->
                    dialog.cancel()
                    SuperActivityToast.create(this, Style(), Style.TYPE_STANDARD)
                        .setText("Error while creating account: ${e.message}")
                        .setDuration(Style.DURATION_LONG)
                        .setFrame(Style.FRAME_LOLLIPOP)
                        .setColor(PaletteUtils.getSolidColor(PaletteUtils.MATERIAL_RED))
                        .setAnimations(Style.ANIMATIONS_POP).show()
                }

                finish()
            }
            .addOnFailureListener{
                    e ->
                dialog.cancel()
                SuperActivityToast.create(this, Style(), Style.TYPE_STANDARD)
                    .setText("Register failed: ${e.message}")
                    .setDuration(Style.DURATION_LONG)
                    .setFrame(Style.FRAME_LOLLIPOP)
                    .setColor(PaletteUtils.getSolidColor(PaletteUtils.MATERIAL_RED))
                    .setAnimations(Style.ANIMATIONS_POP).show()
            }
    }

    private fun loadingBar(text: String ){
        dialogBuilder = AlertDialog.Builder(binding.root.context)
        var popupView = layoutInflater.inflate(R.layout.loading_popup, null)

        val loadingbar = popupView.findViewById<RotateLoading>(R.id.loading_bar)
        val loadingtext = popupView.findViewById<TextView>(R.id.loading_text)
        loadingtext.text = text

        dialogBuilder.setView(popupView)
        dialog = dialogBuilder.create()
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
        val window: Window? = dialog.window
        if (window != null) {
            window.setLayout(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT)
            loadingbar.start()
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