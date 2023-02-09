package com.bek.lvlapp.ui.actions

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.Window
import android.view.WindowManager
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
import com.bek.lvlapp.databinding.ActivityActionsAddBinding
import com.bek.lvlapp.databinding.ActivitySkillsAddBinding
import com.bek.lvlapp.helpers.IconsManager
import com.bek.lvlapp.models.Action
import com.bek.lvlapp.models.Icon
import com.bek.lvlapp.models.Skill
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.madrapps.pikolo.HSLColorPicker
import com.madrapps.pikolo.listeners.SimpleColorSelectionListener
import java.time.LocalDateTime

class ActionsAddActivity : AppCompatActivity() {

    //View binding
    private lateinit var binding: ActivityActionsAddBinding

    private lateinit var firebaseAuth: FirebaseAuth

    //Firebase db
    lateinit var database: DatabaseReference
    private val url = "https://lvlapp-ff610-default-rtdb.europe-west1.firebasedatabase.app"
    private val path = "actions"

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

    private lateinit var editActionName: EditText
    private lateinit var editActionDesc: EditText

    private var new_action_text = ""
    private var description = ""

    private lateinit var iconList: ArrayList<Icon>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityActionsAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        actionBar = supportActionBar!!
        actionBar.title = "Actions Add"
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

        val btnSave : Button = findViewById(R.id.save_button)
        btnSave.setOnClickListener {
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
        new_action_text = editActionName.text.toString().trim()
        description = editActionDesc.text.toString().trim()

        if(TextUtils.isEmpty(new_action_text)){
        }
        else{
            AddAction()
        }
    }

    private fun AddAction(){
        val firebaseUser = firebaseAuth.currentUser

        database.child(path).child(firebaseUser!!.uid).get().addOnSuccessListener {
            val new_action = Action(new_action_text, description, currIcon, currColor)
            new_action.skill_uid = ""
            new_action.xp_give = 1
            new_action.pos = it.childrenCount.toInt()
            new_action.created_at = LocalDateTime.now().toString()
            new_action.updated_at = LocalDateTime.now().toString()

            database.child(path).child(firebaseUser!!.uid).push().setValue(new_action).addOnSuccessListener { e->
                onBackPressed()
                overridePendingTransition(R.transition.no_animation, R.transition.slide_down);
            }.addOnFailureListener{ e->
                Toast.makeText(binding.root.context, "Error while adding the action: $e", Toast.LENGTH_LONG).show()
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