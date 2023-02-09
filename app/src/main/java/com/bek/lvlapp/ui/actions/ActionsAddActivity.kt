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

    private var new_action_text = ""
    private var description = ""

    private lateinit var iconList: ArrayList<Icon>

    private var skillSelectAdapter: SkillSelectAdapter? = null
    private var skillRecyclerView: RecyclerView? = null
    private var skillList: java.util.ArrayList<Skill>? = null
    private var selected_skill: Skill? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityActionsAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        actionBar = supportActionBar!!
        actionBar.title = "Actions Add"
        actionBar.setBackgroundDrawable(ColorDrawable(getResources().getColor(R.color.main)))
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setDisplayShowHomeEnabled(true)

        authManager.checkUser(binding.root.context)
        val firebaseUser = authManager.firebaseUser
        if (firebaseUser != null) {
            database = Firebase.database(url).reference
        }

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

                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        }
        database.child(skills_path).child(firebaseUser!!.uid).addValueEventListener(skillListener)

        var colorPicker: HSLColorPicker = findViewById(R.id.colorPicker);
        iconList = IconsManager.GetAllIcons()

        currIcon = resources.getResourceEntryName(iconList?.get(0)?.icon!!)

        imageIcon = findViewById(R.id.imageSkill)
        var bg_imageView: ImageView = findViewById(R.id.imageBg)
        var imageView: ImageView = findViewById(R.id.imageSkill)

        colorPicker.setColorSelectionListener(object : SimpleColorSelectionListener() {
            override fun onColorSelected(color: Int) {
                // Do whatever you want with the color
                currColor = color
                bg_imageView.background.setColorFilter(color, PorterDuff.Mode.MULTIPLY)
                imageView.background.setColorFilter(color, PorterDuff.Mode.MULTIPLY)
            }
        })

        var imageSkillIcon: ImageView = findViewById(R.id.imageSkill)

        imageSkillIcon.setOnClickListener{
            iconPicker()
        }

        editActionName = findViewById(R.id.edit_action_name)
        editActionDesc = findViewById(R.id.edit_action_desc)
        bubbleSeekBarXpGive = findViewById(R.id.bubbleSeekBar)

        var btn_anim: Animation = AnimationUtils.loadAnimation(binding.root.context, R.anim.bounce)
        val btnSave : Button = findViewById(R.id.save_button)
        btnSave.setOnClickListener {
            btnSave.startAnimation(btn_anim)
            validateData()
        }

        editActionSkill = findViewById(R.id.select_action_skill)

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
        new_action_text = editActionName.text.toString().trim()
        description = editActionDesc.text.toString().trim()

        if(TextUtils.isEmpty(new_action_text)){
        }
        else{
            AddAction()
        }
    }

    private fun AddAction(){
        val firebaseUser = authManager.firebaseUser

        database.child(path).child(firebaseUser!!.uid).get().addOnSuccessListener {
            val new_action = Action(new_action_text, description, currIcon, currColor)
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

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        overridePendingTransition(R.anim.no_animation, R.anim.slide_down);
        return super.onSupportNavigateUp()
    }
}