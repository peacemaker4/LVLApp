package com.bek.lvlapp.ui.skills

import android.app.AlertDialog
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.transition.Fade
import android.transition.Transition
import android.transition.TransitionManager
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bek.lvlapp.LoginActivity
import com.bek.lvlapp.R
import com.bek.lvlapp.adapters.IconAdapter
import com.bek.lvlapp.databinding.ActivitySkillsAddBinding
import com.bek.lvlapp.helpers.IconsManager
import com.bek.lvlapp.models.Icon
import com.bek.lvlapp.models.Skill
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.madrapps.pikolo.HSLColorPicker
import com.madrapps.pikolo.listeners.SimpleColorSelectionListener
import com.ramijemli.percentagechartview.PercentageChartView
import java.time.LocalDateTime


class SkillsAddActivity : AppCompatActivity() {

    //View binding
    private lateinit var binding: ActivitySkillsAddBinding

    private lateinit var firebaseAuth: FirebaseAuth

    //Firebase db
    lateinit var database: DatabaseReference
    private val url = "https://lvlapp-ff610-default-rtdb.europe-west1.firebasedatabase.app"
    private val path = "skills"

    //todo add popup
    private lateinit var dialogBuilder: AlertDialog.Builder
    private lateinit var dialog: AlertDialog

    //ActionBar
    private lateinit var actionBar: ActionBar

    //List
    private var iconAdapter: IconAdapter? = null
    private var recyclerView: RecyclerView? = null
    private lateinit var imageIcon: ImageView
    private var currColor: Int = Color.parseColor ("#FFFFFF")
    private lateinit var currIcon: String

    private lateinit var editSkill: EditText
    private lateinit var progressBar: PercentageChartView

    private var new_skill_text = ""

    private lateinit var iconList: ArrayList<Icon>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySkillsAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        actionBar = supportActionBar!!
        actionBar.title = "Skills Add"
        actionBar.setBackgroundDrawable(ColorDrawable(getResources().getColor(R.color.red)))
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setDisplayShowHomeEnabled(true)

        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            database = Firebase.database(url).reference
        }

        var colorPicker: HSLColorPicker = findViewById(R.id.colorPicker);
        iconList = IconsManager.GetAllIcons()

        currIcon = resources.getResourceEntryName(iconList?.get(0)?.icon!!)

        imageIcon = findViewById(R.id.imageSkill)
        progressBar = findViewById(R.id.progress_bar)
        var imageView: ImageView = findViewById(R.id.imageSkill)

        progressBar.setProgress(0f, false)
        progressBar.setProgress(100f, true)

        colorPicker.setColorSelectionListener(object : SimpleColorSelectionListener() {
            override fun onColorSelected(color: Int) {
                // Do whatever you want with the color
                currColor = color
                progressBar.progressColor = color
                imageView.background.setColorFilter(color, PorterDuff.Mode.MULTIPLY)
//                editSkill.setTextColor(color)
                progressBar.setProgress(0f, false)
                progressBar.setProgress(100f, true)
            }
        })

        var imageSkillIcon: ImageView = findViewById(R.id.imageSkill)

        imageSkillIcon.setOnClickListener{
            iconPicker()
        }

        editSkill = findViewById(R.id.edit_skill)

//        if(editSkill.requestFocus()) {
//            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
//        }

        var btn_anim: Animation = AnimationUtils.loadAnimation(binding.root.context, R.anim.bounce)

        val btnSave : Button = findViewById(R.id.save_button)
        btnSave.setOnClickListener {
            btnSave.startAnimation(btn_anim)
            validateData()
        }

    }

    private fun iconPicker(){
        dialogBuilder = AlertDialog.Builder(binding.root.context)
        var popupView = layoutInflater.inflate(R.layout.icon_picker_popup, null)
        dialogBuilder.setView(popupView)

        recyclerView = popupView.findViewById(R.id.list_icons)

        recyclerView!!.setLayoutManager(LinearLayoutManager(binding.root.context))

        iconAdapter = IconAdapter(binding.root.context, iconList!!)
        iconAdapter!!.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.ALLOW

        iconAdapter?.onItemClick = { icon ->
            imageIcon?.setBackgroundResource(icon.icon!!)
            currIcon = resources.getResourceEntryName(icon.icon!!)
            imageIcon?.background?.setColorFilter(currColor, PorterDuff.Mode.MULTIPLY)
            dialog.cancel()
            progressBar.setProgress(0f, false)
            progressBar.setProgress(100f, true)
        }


        recyclerView!!.layoutManager =
            StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        recyclerView!!.adapter = iconAdapter

        dialog = dialogBuilder.create()
        dialog.show()
        val window: Window? = dialog.window
        if (window != null) {
            window.setLayout(android.app.ActionBar.LayoutParams.MATCH_PARENT, android.app.ActionBar.LayoutParams.WRAP_CONTENT)
        }
    }

    private fun validateData(){
        new_skill_text = editSkill.text.toString().trim()

        if(TextUtils.isEmpty(new_skill_text)){
        }
        else{
            AddSkill()
        }
    }

    private fun AddSkill(){
        val firebaseUser = firebaseAuth.currentUser

        database.child(path).child(firebaseUser!!.uid).get().addOnSuccessListener {
            val new_skill = Skill(new_skill_text, currIcon, currColor)
            new_skill.pos = it.childrenCount.toInt()
            new_skill.created_at = LocalDateTime.now().toString()
            new_skill.updated_at = LocalDateTime.now().toString()

            database.child(path).child(firebaseUser!!.uid).push().setValue(new_skill).addOnSuccessListener { e->
                onBackPressed()
                overridePendingTransition(R.transition.no_animation, R.transition.slide_down);
            }.addOnFailureListener{ e->
                Toast.makeText(binding.root.context, "Error while adding the skill: $e", Toast.LENGTH_LONG).show()
            }
        }

    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        overridePendingTransition(R.transition.no_animation, R.transition.slide_down);
        return super.onSupportNavigateUp()
    }

    private fun checkUser() {
        //if user is already logged in go to main activity
        val firebaseUser = firebaseAuth.currentUser
        if(firebaseUser == null){
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}