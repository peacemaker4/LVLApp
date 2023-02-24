package com.bek.lvlapp.ui.actions

import android.app.ActionBar
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.transition.Fade
import android.transition.Slide
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
import com.bek.lvlapp.databinding.ActivityActionBinding
import com.bek.lvlapp.databinding.ActivitySkillBinding
import com.bek.lvlapp.helpers.AuthManager
import com.bek.lvlapp.helpers.LevelCalculator
import com.bek.lvlapp.models.Action
import com.bek.lvlapp.models.Skill
import com.bek.lvlapp.models.SkillAction
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
import com.kizitonwose.calendar.compose.HeatMapCalendar
import com.victor.loading.rotate.RotateLoading
import java.util.ArrayList

class ActionActivity(bcontext: Context? = null) : AppCompatActivity() {

    //View binding
    private lateinit var binding: ActivityActionBinding

    val authManager = AuthManager()

    val bcontext = bcontext

    //Firebase db
    lateinit var database: DatabaseReference
    private val url = "https://lvlapp-ff610-default-rtdb.europe-west1.firebasedatabase.app"
    private val skills_path = "skills"
    private val path = "actions"
    private val skills_actions_path = "skills_actions"
    private var action_uid = ""
    private lateinit var currentAction: Action

    //action delete popup
    private lateinit var dialogBuilder: AlertDialog.Builder
    private lateinit var dialog: AlertDialog

    //ActionBar
    private lateinit var actionBar: androidx.appcompat.app.ActionBar
    private lateinit var actionListener: ValueEventListener

    private lateinit var skillActionListener: ValueEventListener
    private var skillActionList: ArrayList<SkillAction>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityActionBinding.inflate(layoutInflater)
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

        binding.actionIcon.visibility = View.GONE
        binding.actionName.visibility = View.GONE
        binding.actionDesc.visibility = View.GONE
        binding.skillName.visibility = View.GONE
        binding.skillXpGive.visibility = View.GONE

        val extras = intent.extras
        if (extras != null) {
            action_uid = extras.getString("action_uid").toString()
        }

        var action: Action
        actionListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                action = dataSnapshot.getValue<Action>()!!
                if (binding != null) {
                    try {
                        currentAction = action
                        currentAction.uid = dataSnapshot.key

                        actionBar.title = "Action: " + action.name
                        binding.actionIcon.background = ContextCompat.getDrawable(
                            this@ActionActivity,
                            resources.getIdentifier(action.icon!!, "drawable", "com.bek.lvlapp")
                        )
                        binding.actionIcon.background.setColorFilter(
                            action.color!!,
                            PorterDuff.Mode.MULTIPLY
                        )
                        binding.actionName.text = action.name
                        binding.actionName.setTextColor(action.color!!)
                        binding.skillXpGive.text = "+ " +action.xp_give.toString() + " XP"
                        binding.actionDesc.text  = action.description

                        database.child(skills_path).child(firebaseUser!!.uid).child(action.skill_uid!!).get().addOnSuccessListener { e ->
                            var skill = e.getValue<Skill>()
                            binding.skillName.text = skill!!.name
                            binding.skillName.setTextColor(skill.color!!)
                        }

                        val transition: Transition = Slide(Gravity.LEFT)
                        transition.setDuration(750)
                        transition.addTarget(binding.actionIcon)
                        transition.addTarget(binding.actionName)
                        transition.addTarget(binding.actionDesc)
                        transition.addTarget(binding.skillName)
                        transition.addTarget(binding.skillXpGive)
                        TransitionManager.beginDelayedTransition(binding.actionLayout, transition)

                        binding.actionIcon.visibility = View.VISIBLE
                        binding.actionName.visibility = View.VISIBLE
                        binding.actionDesc.visibility = View.VISIBLE
                        binding.skillName.visibility = View.VISIBLE
                        binding.skillXpGive.visibility = View.VISIBLE
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        }
        database.child(path).child(firebaseUser!!.uid).child(action_uid).addValueEventListener(actionListener)


        skillActionListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                skillActionList = ArrayList()

                for (s in dataSnapshot.children) {
                    var skilAction = s.getValue<SkillAction>()!!
                    skillActionList!!.add(skilAction)
                }

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        }

        database.child(skills_actions_path).child(firebaseUser!!.uid).child(action_uid).addValueEventListener(skillActionListener)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_edit_button, menu)
        menuInflater.inflate(R.menu.main_delete_button, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.action_edit){
            val intent = Intent(this, ActionsAddActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra("action", Gson().toJson(currentAction))
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

                database.child(path).child(firebaseUser!!.uid).child(action_uid).removeEventListener(actionListener)
                database.child(path).child(firebaseUser!!.uid).child(currentAction?.uid!!).removeValue().addOnSuccessListener { e->
                    onBackPressed()
                    Toast.makeText(binding.root.context, "Action - ${currentAction.name} deleted", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener{ e->
                    Toast.makeText(binding.root.context, "Unable to delete action: $e", Toast.LENGTH_SHORT).show()
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