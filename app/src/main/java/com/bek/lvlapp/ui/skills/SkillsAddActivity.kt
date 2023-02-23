package com.bek.lvlapp.ui.skills

import android.app.AlertDialog
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
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bek.lvlapp.R
import com.bek.lvlapp.adapters.IconAdapter
import com.bek.lvlapp.databinding.ActivitySkillsAddBinding
import com.bek.lvlapp.helpers.AuthManager
import com.bek.lvlapp.helpers.IconsManager
import com.bek.lvlapp.models.Icon
import com.bek.lvlapp.models.Skill
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.madrapps.pikolo.HSLColorPicker
import com.madrapps.pikolo.listeners.SimpleColorSelectionListener
import com.ramijemli.percentagechartview.PercentageChartView
import java.time.LocalDateTime


class SkillsAddActivity : AppCompatActivity() {

    //View binding
    private lateinit var binding: ActivitySkillsAddBinding

    val authManager = AuthManager()

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

    private var edit_skill_text = ""
    private var skill_uid: String = ""

    private lateinit var iconList: ArrayList<Icon>

    private var currentSkill: Skill? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySkillsAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        actionBar = supportActionBar!!

        editSkill = findViewById(R.id.edit_skill)
        progressBar = findViewById(R.id.progress_bar)

        iconList = IconsManager.GetAllIcons()
        currIcon = resources.getResourceEntryName(iconList?.get(0)?.icon!!)
        imageIcon = findViewById(R.id.imageSkill)
        var imageView: ImageView = findViewById(R.id.imageSkill)
        var colorPicker: HSLColorPicker = findViewById(R.id.colorPicker);

        val extras = intent.extras
        if(extras != null){
            actionBar.setBackgroundDrawable(ColorDrawable(getResources().getColor(R.color.dark_bg)))

            currentSkill = Gson().fromJson(extras.getString("skill"), Skill::class.java)

            edit_skill_text = currentSkill!!.name.toString()
            actionBar.title = "Skill: " + edit_skill_text + "*"
            currColor = currentSkill!!.color!!
            currIcon = currentSkill!!.icon!!
            imageIcon?.setBackgroundResource(resources.getIdentifier(currIcon, "drawable", "com.bek.lvlapp"))
            imageIcon.background.setColorFilter(currColor, PorterDuff.Mode.MULTIPLY)
            editSkill.setText(edit_skill_text)
            progressBar.progressColor = currColor
            colorPicker.setColor(currColor)
        }
        else{
            actionBar.title = "Skills Add"
            actionBar.setBackgroundDrawable(ColorDrawable(getResources().getColor(R.color.dark_prime)))
        }

        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setDisplayShowHomeEnabled(true)

        authManager.checkUser(this)
        val firebaseUser = authManager.firebaseUser
        if (firebaseUser != null) {
            database = Firebase.database(url).reference
        }


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
        var popupView = layoutInflater.inflate(R.layout.list_picker_popup, null)
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
            window.attributes.windowAnimations = R.style.ScaleDialogAnimation
            window.setLayout(android.app.ActionBar.LayoutParams.MATCH_PARENT, android.app.ActionBar.LayoutParams.WRAP_CONTENT)
        }
    }

    private fun validateData(){
        edit_skill_text = editSkill.text.toString().trim()

        if(TextUtils.isEmpty(edit_skill_text)){
        }
        else{
            AddSkill()
        }
    }

    private fun AddSkill(){
        val firebaseUser = authManager.firebaseUser
        if(currentSkill == null){
            database.child(path).child(firebaseUser!!.uid).get().addOnSuccessListener {
                val new_skill = Skill(edit_skill_text, currIcon, currColor)

                new_skill.pos = it.childrenCount.toInt()
                new_skill.created_at = LocalDateTime.now().toString()
                new_skill.updated_at = LocalDateTime.now().toString()

                database.child(path).child(firebaseUser!!.uid).push().setValue(new_skill).addOnSuccessListener { e->
                    onBackPressed()
                }.addOnFailureListener{ e->
                    Toast.makeText(binding.root.context, "Error while adding the skill: $e", Toast.LENGTH_LONG).show()
                }
            }
        }
        else{
            val upd_skill = currentSkill
            upd_skill?.updated_at = LocalDateTime.now().toString()
            upd_skill?.name = edit_skill_text
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
            overridePendingTransition(R.anim.no_animation, R.anim.fade_out);
        }
        else{
            overridePendingTransition(R.anim.no_animation, R.anim.slide_down);
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()

        return super.onSupportNavigateUp()
    }
}