package com.bek.lvlapp

import android.app.ActionBar
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bek.lvlapp.databinding.ActivityWelcomeBinding
import com.bek.lvlapp.models.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.victor.loading.rotate.RotateLoading
import java.time.LocalDateTime

class WelcomeActivity : AppCompatActivity() {

    private lateinit var account: GoogleSignInAccount
    private lateinit var googleSignInClient: GoogleSignInClient

    //View binding
    private lateinit var binding: ActivityWelcomeBinding

    private lateinit var firebaseAuth: FirebaseAuth

    //todo add popup
    private lateinit var dialogBuilder: AlertDialog.Builder
    private lateinit var dialog: AlertDialog

    //Firebase db
    lateinit var database: DatabaseReference
    private val url = "https://lvlapp-ff610-default-rtdb.europe-west1.firebasedatabase.app"
    private val path = "users"

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

//        createGoogleRequest()
        database = Firebase.database(url).reference

//        binding.btnGoogle.setOnClickListener{
//            signInGoogle()
//        }

    }

    private fun signInGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, Companion.RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == RC_SIGN_IN){

            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try{
                account = task.getResult(ApiException::class.java)!!
                Log.d(ContentValues.TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken)
            } catch (e: ApiException){
                Log.d(ContentValues.TAG, "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        loadingBar("Signing up")
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task->
                if(task.isSuccessful){
                    val user = firebaseAuth.currentUser
                    Log.d(ContentValues.TAG, "singInWithCredentian:success")
                    updateUI(user)
                }
                else{
                    Log.d(ContentValues.TAG, "singInWithCredentian:fail", task.exception)
                    updateUI(null)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        if(user != null){
            val dbuser = User(user!!.uid, account.displayName, user.email)
            dbuser.created_at = LocalDateTime.now().toString()
            dbuser.updated_at = LocalDateTime.now().toString()

            database.child(path).child(dbuser.uid.toString()).setValue(dbuser).addOnSuccessListener { e->
                val intent = Intent(applicationContext, MainActivity::class.java)
                startActivity(intent)
            }.addOnFailureListener{ e->
                Toast.makeText(this, "Error while creating the user: $e", Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun createGoogleRequest(){
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("784694790345-ai5vaq01nnur87889h4q5duasnric75l.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun checkUser() {
        //if user is already logged in go to main activity
        val firebaseUser = firebaseAuth.currentUser
        if(firebaseUser != null){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
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

    companion object {
        const val RC_SIGN_IN = 1001
    }
}