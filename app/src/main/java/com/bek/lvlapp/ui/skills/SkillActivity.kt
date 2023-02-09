package com.bek.lvlapp.ui.skills

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.transition.Fade
import android.transition.Transition
import android.transition.TransitionManager
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bek.lvlapp.R
import com.bek.lvlapp.WelcomeActivity
import com.bek.lvlapp.databinding.ActivitySkillBinding
import com.bek.lvlapp.helpers.LevelCalculator
import com.bek.lvlapp.models.Skill
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

class SkillActivity : AppCompatActivity() {

    //View binding
    private lateinit var binding: ActivitySkillBinding

    private lateinit var firebaseAuth: FirebaseAuth


    //Firebase db
    lateinit var database: DatabaseReference
    private val url = "https://lvlapp-ff610-default-rtdb.europe-west1.firebasedatabase.app"
    private val path = "skills"
    private var skill_uid = ""

    //ActionBar
    private lateinit var actionBar: androidx.appcompat.app.ActionBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySkillBinding.inflate(layoutInflater)
        setContentView(binding.root)

        actionBar = supportActionBar!!
        actionBar.setBackgroundDrawable(ColorDrawable(getResources().getColor(R.color.dark_bg)))
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setDisplayShowHomeEnabled(true)

        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            database = Firebase.database(url).reference
        }

        binding.skillIcon.visibility = View.GONE
        binding.skillName.visibility = View.GONE
        binding.skillLevel.visibility = View.GONE
        binding.skillXp.visibility = View.GONE
        binding.skillXpLeft.visibility = View.GONE
        binding.imgLine.visibility = View.GONE
        binding.imgLine2.visibility = View.GONE
        binding.imgLine3.visibility = View.GONE

        val extras = intent.extras
        if (extras != null) {
            skill_uid = extras.getString("skill_uid").toString()
        }

        var skill: Skill

        val skillListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                skill = dataSnapshot.getValue<Skill>()!!
                if(binding != null) {
                    try{
                        skill.xp?.let { LevelCalculator.LevelProgress(it).toFloat() }
                            ?.let { binding.progressBar.setProgress(it, true) }

                        actionBar.title = "Skill: " + skill.name
                        binding.progressBar.progressColor = skill.color!!
                        binding.progressBar.textColor = skill.color!!
                        binding.progressBar.apply()
                        binding.skillIcon.background = ContextCompat.getDrawable(this@SkillActivity, resources.getIdentifier(skill.icon!!, "drawable", "com.bek.lvlapp"))
                        binding.skillIcon.background.setColorFilter(skill.color!!, PorterDuff.Mode.MULTIPLY)
                        binding.skillName.text = skill.name
                        binding.skillName.setTextColor(skill.color!!)
                        binding.skillLevel.text = "LEVEL " + skill.level.toString()
                        binding.skillLevel.setTextColor(skill.color!!)
                        binding.skillXp.text = skill.xp.toString() + " XP"
                        binding.skillXpLeft.text = LevelCalculator.XPAmountToNextLevel(skill.xp!!).toString() + " XP LEFT"
                        binding.skillXpLeft.setTextColor(skill.color!!)

                        val transition: Transition = Fade()
                        transition.setDuration(1000)
                        transition.addTarget(binding.skillIcon)
                        transition.addTarget(binding.skillName)
                        transition.addTarget(binding.skillLevel)
                        transition.addTarget(binding.skillXp)
                        transition.addTarget(binding.skillXpLeft)
                        TransitionManager.beginDelayedTransition(binding.skillLayout, transition)

                        binding.skillIcon.visibility = View.VISIBLE
                        binding.skillName.visibility = View.VISIBLE
                        binding.skillLevel.visibility = View.VISIBLE
                        binding.skillXp.visibility = View.VISIBLE
                        binding.skillXpLeft.visibility = View.VISIBLE
                        binding.imgLine.visibility = View.VISIBLE
                        binding.imgLine2.visibility = View.VISIBLE
                        binding.imgLine3.visibility = View.VISIBLE
                    }catch (e: Exception) {
                        e.printStackTrace()
                    }

                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        }
        database.child(path).child(firebaseUser!!.uid).child(skill_uid).addValueEventListener(skillListener)


    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        overridePendingTransition(R.transition.fade_in, R.transition.fade_out)
        return super.onSupportNavigateUp()
    }

    private fun checkUser() {
        //if user is already logged in go to main activity
        val firebaseUser = firebaseAuth.currentUser
        if(firebaseUser == null){
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        finish()
    }

}