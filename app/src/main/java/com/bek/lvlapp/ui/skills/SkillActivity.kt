package com.bek.lvlapp.ui.skills

import android.app.ActionBar
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.transition.Fade
import android.transition.Transition
import android.transition.TransitionManager
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bek.lvlapp.R
import com.bek.lvlapp.WelcomeActivity
import com.bek.lvlapp.databinding.ActivitySkillBinding
import com.bek.lvlapp.helpers.AuthManager
import com.bek.lvlapp.helpers.LevelCalculator
import com.bek.lvlapp.models.Skill
import com.github.johnpersano.supertoasts.library.Style
import com.github.johnpersano.supertoasts.library.SuperActivityToast
import com.github.johnpersano.supertoasts.library.utils.PaletteUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.victor.loading.rotate.RotateLoading

class SkillActivity(bcontext: Context? = null) : AppCompatActivity() {

    //View binding
    private lateinit var binding: ActivitySkillBinding

    val authManager = AuthManager()

    val bcontext = bcontext

    //Firebase db
    lateinit var database: DatabaseReference
    private val url = "https://lvlapp-ff610-default-rtdb.europe-west1.firebasedatabase.app"
    private val path = "skills"
    private var skill_uid = ""
    private lateinit var currentSkill: Skill

    //skill delete popup
    private lateinit var dialogBuilder: AlertDialog.Builder
    private lateinit var dialog: AlertDialog

    //ActionBar
    private lateinit var actionBar: androidx.appcompat.app.ActionBar
    private lateinit var skillListener: ValueEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySkillBinding.inflate(layoutInflater)
        setContentView(binding.root)

        actionBar = supportActionBar!!
        actionBar.setBackgroundDrawable(ColorDrawable(getResources().getColor(R.color.dark_bg)))
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setDisplayShowHomeEnabled(true)


        authManager.checkUser(this)
        val firebaseUser = authManager.firebaseUser
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

        skillListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                skill = dataSnapshot.getValue<Skill>()!!
                if (binding != null) {
                    try {
                        currentSkill = skill
                        currentSkill.uid = dataSnapshot.key
                        skill.xp?.let { LevelCalculator.LevelProgress(it).toFloat() }
                            ?.let { binding.progressBar.setProgress(it, true) }

                        actionBar.title = "Skill: " + skill.name
                        binding.progressBar.progressColor = skill.color!!
                        binding.progressBar.textColor = skill.color!!
                        binding.progressBar.apply()
                        binding.skillIcon.background = ContextCompat.getDrawable(
                            this@SkillActivity,
                            resources.getIdentifier(skill.icon!!, "drawable", "com.bek.lvlapp")
                        )
                        binding.skillIcon.background.setColorFilter(
                            skill.color!!,
                            PorterDuff.Mode.MULTIPLY
                        )
                        binding.skillName.text = skill.name
                        binding.skillName.setTextColor(skill.color!!)
                        binding.skillLevel.text = "LEVEL " + skill.level.toString()
                        binding.skillLevel.setTextColor(skill.color!!)
                        binding.skillXp.text = skill.xp.toString() + " XP"
                        binding.skillXpLeft.text =
                            LevelCalculator.XPAmountToNextLevel(skill.xp!!).toString() + " XP LEFT"
                        binding.skillXpLeft.setTextColor(skill.color!!)

                        val transition: Transition = Fade()
                        transition.setDuration(750)
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
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        }
        database.child(path).child(firebaseUser!!.uid).child(skill_uid).addValueEventListener(skillListener)


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_edit_button, menu)
        menuInflater.inflate(R.menu.main_delete_button, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.action_edit){
            val intent = Intent(this, SkillsAddActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra("skill", Gson().toJson(currentSkill))
            this.startActivity(intent)
            this.overridePendingTransition(R.anim.fade_in, R.anim.no_animation)
        }
        else if(item.itemId == R.id.action_delete){
            deleteConfirm("Delete","Are you sure you want to delete?")
        }

        return super.onOptionsItemSelected(item)
    }

    private fun deleteConfirm(title: String, text: String){
        dialogBuilder = AlertDialog.Builder(binding.root.context)
        var popupView = layoutInflater.inflate(R.layout.confirm_popup, null)

        val titletext = popupView.findViewById<TextView>(R.id.title_text)
        val noBtn = popupView.findViewById<TextView>(R.id.noBtn)
        val yesBtn = popupView.findViewById<TextView>(R.id.yesBtn)
        val confirmtext = popupView.findViewById<TextView>(R.id.confirm_text)
        yesBtn.setTextColor(resources.getColor(R.color.red))
        titletext.text = title
        confirmtext.text = text

        dialogBuilder.setView(popupView)
        dialog = dialogBuilder.create()
        dialog.show()
        val window: Window? = dialog.window
        if (window != null) {
            window.attributes.windowAnimations = R.style.ScaleDialogAnimation
            window.setLayout(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT)
            noBtn.setOnClickListener{
                dialog.cancel()
            }
            yesBtn.setOnClickListener{
                val firebaseUser = authManager.firebaseUser

                database.child(path).child(firebaseUser!!.uid).child(skill_uid).removeEventListener(skillListener)
                database.child(path).child(firebaseUser!!.uid).child(currentSkill?.uid!!).removeValue().addOnSuccessListener { e->
                    onBackPressed()
                    Toast.makeText(binding.root.context, "Skill - ${currentSkill.name} deleted", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener{ e->
                    Toast.makeText(binding.root.context, "Unable to delete skill: $e", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    override fun onDestroy() {
        super.onDestroy()
        finish()
    }

}