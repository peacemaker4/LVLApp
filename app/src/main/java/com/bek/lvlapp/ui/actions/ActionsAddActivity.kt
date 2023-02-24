package com.bek.lvlapp.ui.actions

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.Window
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bek.lvlapp.R
import com.bek.lvlapp.adapters.IconAdapter
import com.bek.lvlapp.adapters.SkillSelectAdapter
import com.bek.lvlapp.databinding.ActivityActionsAddBinding
import com.bek.lvlapp.helpers.AuthManager
import com.bek.lvlapp.helpers.IconsManager
import com.bek.lvlapp.models.Action
import com.bek.lvlapp.models.Icon
import com.bek.lvlapp.models.Skill
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.madrapps.pikolo.HSLColorPicker
import com.madrapps.pikolo.listeners.SimpleColorSelectionListener
import com.xw.repo.BubbleSeekBar
import java.time.LocalDateTime
import kotlin.collections.ArrayList

class ActionsAddActivity : AppCompatActivity() {

    //View binding
    private lateinit var binding: ActivityActionsAddBinding

    val authManager = AuthManager()

    //Firebase db
    lateinit var database: DatabaseReference
    private val url = "https://lvlapp-ff610-default-rtdb.europe-west1.firebasedatabase.app"
    private val path = "actions"
    private val skills_path = "skills"

    //todo add popup
    private lateinit var dialogBuilder: AlertDialog.Builder
    private lateinit var dialog: AlertDialog

    //ActionBar
    private lateinit var actionBar: ActionBar

    //List
    private var iconAdapter: IconAdapter? = null
    private var iconRecyclerView: RecyclerView? = null
    private lateinit var imageIcon: ImageView
    private var currColor: Int = Color.parseColor ("#FFFFFF")
    private lateinit var currIcon: String

    private lateinit var editActionName: EditText
    private lateinit var editActionDesc: EditText
    private lateinit var editActionSkill: EditText
    private lateinit var bubbleSeekBarXpGive: BubbleSeekBar
    private lateinit var bg_imageView: ImageView

    private var edit_action_text = ""
    private var description = ""

    private lateinit var iconList: ArrayList<Icon>

    private var skillSelectAdapter: SkillSelectAdapter? = null
    private var skillRecyclerView: RecyclerView? = null
    private var skillList: java.util.ArrayList<Skill>? = null
    private var selected_skill: Skill? = null

    private var currentAction: Action? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityActionsAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        actionBar = supportActionBar!!
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setDisplayShowHomeEnabled(true)

        authManager.checkUser(binding.root.context)
        val firebaseUser = authManager.firebaseUser
        if (firebaseUser != null) {
            database = Firebase.database(url).reference
        }
        var colorPicker: HSLColorPicker = findViewById(R.id.colorPicker);
        iconList = IconsManager.GetAllIcons()

        imageIcon = findViewById(R.id.imageSkill)
        editActionName = findViewById(R.id.edit_action_name)
        editActionDesc = findViewById(R.id.edit_action_desc)
        bubbleSeekBarXpGive = findViewById(R.id.bubbleSeekBar)
        editActionSkill = findViewById(R.id.select_action_skill)
        bg_imageView = findViewById(R.id.imageBg)

        skillList = java.util.ArrayList()


        val skillListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                skillList = java.util.ArrayList()

                for (skillSnapshot in dataSnapshot.children) {
                    var skill = skillSnapshot.getValue<Skill>()!!
                    skill.uid = skillSnapshot.key
                    skillList!!.add(skill)
                }
                if(binding != null) {
                    skillList!!.sortBy { s -> s.pos }

                    skillSelectAdapter = SkillSelectAdapter(binding.root.context, skillList!!)
                    skillSelectAdapter!!.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.ALLOW

                    val extras = intent.extras
                    if(extras != null){
                        for(s in skillList!!){
                            if(s.uid == currentAction!!.skill_uid){
                                selected_skill = s
                            }
                        }
                        editActionSkill.setText(selected_skill!!.name)
                        editActionSkill.setCompoundDrawablesWithIntrinsicBounds(resources.getIdentifier(selected_skill!!.icon, "drawable",
                            this@ActionsAddActivity.packageName), 0, 0, 0)
                        editActionSkill.setTextColor(selected_skill!!.color!!.toInt())
                        editActionSkill.compoundDrawableTintList = ColorStateList.valueOf(selected_skill!!.color!!)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        }
        database.child(skills_path).child(firebaseUser!!.uid).addValueEventListener(skillListener)



        val extras = intent.extras
        if(extras != null){
            actionBar.setBackgroundDrawable(ColorDrawable(getResources().getColor(R.color.dark_bg)))

            currentAction = Gson().fromJson(extras.getString("action"), Action::class.java)

            editActionName.setText(currentAction!!.name.toString())
            actionBar.title = "Action: " + currentAction!!.name + "*"
            currColor = currentAction!!.color!!
            currIcon = currentAction!!.icon!!
            imageIcon?.setBackgroundResource(resources.getIdentifier(currIcon, "drawable", "com.bek.lvlapp"))
            imageIcon.background.setColorFilter(currColor, PorterDuff.Mode.MULTIPLY)
            editActionDesc.setText(currentAction!!.description)
            colorPicker.setColor(currColor)
            bubbleSeekBarXpGive.setProgress(currentAction!!.xp_give!!.toFloat())
            bg_imageView.background.setColorFilter(currColor, PorterDuff.Mode.MULTIPLY)

        }
        else{
            actionBar.title = "Actions Add"
            actionBar.setBackgroundDrawable(ColorDrawable(getResources().getColor(R.color.dark_prime)))
            currIcon = resources.getResourceEntryName(iconList?.get(0)?.icon!!)
        }

        colorPicker.setColorSelectionListener(object : SimpleColorSelectionListener() {
            override fun onColorSelected(color: Int) {
                // Do whatever you want with the color
                currColor = color
                bg_imageView.background.setColorFilter(color, PorterDuff.Mode.MULTIPLY)
                imageIcon.background.setColorFilter(color, PorterDuff.Mode.MULTIPLY)
            }
        })

        var imageSkillIcon: ImageView = findViewById(R.id.imageSkill)

        imageSkillIcon.setOnClickListener{
            iconPicker()
        }

        var btn_anim: Animation = AnimationUtils.loadAnimation(binding.root.context, R.anim.bounce)
        val btnSave : Button = findViewById(R.id.save_button)
        btnSave.setOnClickListener {
            btnSave.startAnimation(btn_anim)
            validateData()
        }


        editActionSkill.setOnClickListener{
            skillPicker()
        }
    }

    private fun iconPicker(){
        dialogBuilder = AlertDialog.Builder(binding.root.context)
        var popupView = layoutInflater.inflate(R.layout.list_picker_popup, null)
        dialogBuilder.setView(popupView)

        var picker_title = popupView.findViewById<TextView>(R.id.picker_title)
        picker_title.text = "Select Icon"

        iconRecyclerView = popupView.findViewById(R.id.list_icons)

        iconRecyclerView!!.setLayoutManager(LinearLayoutManager(binding.root.context))

        iconAdapter = IconAdapter(binding.root.context, iconList!!)
        iconAdapter!!.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.ALLOW

        iconAdapter?.onItemClick = { icon ->
            imageIcon?.setBackgroundResource(icon.icon!!)
            currIcon = resources.getResourceEntryName(icon.icon!!)
            imageIcon?.background?.setColorFilter(currColor, PorterDuff.Mode.MULTIPLY)
            dialog.cancel()
        }

        iconRecyclerView!!.layoutManager =
            StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        iconRecyclerView!!.adapter = iconAdapter

        dialog = dialogBuilder.create()
        dialog.show()
        val window: Window? = dialog.window
        if (window != null) {
            window.setLayout(android.app.ActionBar.LayoutParams.MATCH_PARENT, android.app.ActionBar.LayoutParams.WRAP_CONTENT)
        }
    }

    private fun skillPicker(){
        dialogBuilder = AlertDialog.Builder(binding.root.context)
        var popupView = layoutInflater.inflate(R.layout.list_picker_popup, null)
        dialogBuilder.setView(popupView)

        var picker_title = popupView.findViewById<TextView>(R.id.picker_title)
        picker_title.text = "Select Skill"

        skillRecyclerView = popupView.findViewById(R.id.list_icons)

        skillRecyclerView!!.setLayoutManager(LinearLayoutManager(binding.root.context))

        skillRecyclerView!!.layoutManager =
            StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
        skillRecyclerView!!.adapter = skillSelectAdapter

        skillSelectAdapter?.onItemClick = { skill ->
            selected_skill = skill
            editActionSkill.setText(skill.name)
            editActionSkill.setCompoundDrawablesWithIntrinsicBounds(resources.getIdentifier(skill.icon, "drawable",
                this.packageName), 0, 0, 0)
            editActionSkill.setTextColor(skill.color!!.toInt())
            editActionSkill.compoundDrawableTintList = ColorStateList.valueOf(skill.color!!)
            dialog.cancel()
        }

        dialog = dialogBuilder.create()
        dialog.show()
        val window: Window? = dialog.window
        if (window != null) {
            window.setLayout(android.app.ActionBar.LayoutParams.MATCH_PARENT, android.app.ActionBar.LayoutParams.WRAP_CONTENT)
        }
    }

    private fun validateData(){
        edit_action_text = editActionName.text.toString().trim()
        description = editActionDesc.text.toString().trim()

        if(TextUtils.isEmpty(edit_action_text)){
        }
        else{
            AddAction()
        }
    }

    private fun AddAction(){
        val firebaseUser = authManager.firebaseUser
        if(currentAction == null){
            database.child(path).child(firebaseUser!!.uid).get().addOnSuccessListener {
                val new_action = Action(edit_action_text, description, currIcon, currColor)
                new_action.xp_give = bubbleSeekBarXpGive.progress
                new_action.pos = it.childrenCount.toInt()
                new_action.skill_uid = selected_skill!!.uid
                new_action.created_at = LocalDateTime.now().toString()
                new_action.updated_at = LocalDateTime.now().toString()

                database.child(path).child(firebaseUser!!.uid).push().setValue(new_action).addOnSuccessListener { e->
                    onBackPressed()
                }.addOnFailureListener{ e->
                    Toast.makeText(binding.root.context, "Error while adding the action: $e", Toast.LENGTH_LONG).show()
                }
            }
        }
        else{
            val upd_skill = currentAction
            upd_skill?.updated_at = LocalDateTime.now().toString()
            upd_skill?.name = edit_action_text
            upd_skill?.description = description
            upd_skill?.skill_uid = selected_skill!!.uid
            upd_skill?.xp_give = bubbleSeekBarXpGive.progress
            upd_skill?.color = currColor
            upd_skill?.icon = currIcon

            database.child(path).child(firebaseUser!!.uid).child(upd_skill?.uid!!).setValue(upd_skill).addOnSuccessListener { e->
                onBackPressed()
            }.addOnFailureListener{ e->
                Toast.makeText(binding.root.context, "Error while saving the skill: $e", Toast.LENGTH_LONG).show()
            }
        }


    }

    override fun onBackPressed() {
        super.onBackPressed()
        val extras = intent.extras
        if(extras != null){
            overridePendingTransition(R.anim.no_animation, R.anim.fade_out)
        }
        else{
            overridePendingTransition(R.anim.no_animation, R.anim.slide_down)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}