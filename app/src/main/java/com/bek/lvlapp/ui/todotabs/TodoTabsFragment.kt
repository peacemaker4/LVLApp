package com.bek.lvlapp.ui.todotabs

import android.app.ActionBar
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.transition.Fade
import android.transition.Slide
import android.transition.Transition
import android.transition.TransitionManager
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.bek.lvlapp.LoginActivity
import com.bek.lvlapp.R
import com.bek.lvlapp.adapters.PagerAdapter
import com.bek.lvlapp.databinding.FragmentTodoTabsBinding
import com.bek.lvlapp.helpers.AuthManager
import com.bek.lvlapp.models.Todo
import com.bek.lvlapp.ui.todo.TodoFragment
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import java.time.LocalDateTime
import java.util.*


class TodoTabsFragment : Fragment() {

    private lateinit var todoViewModel: TodoTabsViewModel
    private var _binding: FragmentTodoTabsBinding? = null

    private val binding get() = _binding!!

    //todo add popup
    private lateinit var dialogBuilder: AlertDialog.Builder
    private lateinit var dialog: AlertDialog

    private lateinit var editTodoTitle: EditText
    private lateinit var btnSave: Button

    val authManager = AuthManager()

    //Firebase db
    lateinit var database: DatabaseReference
    private val url = "https://lvlapp-ff610-default-rtdb.europe-west1.firebasedatabase.app"
    private val path = "todos"

    private var email = ""

    private var new_task = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        todoViewModel =
            ViewModelProvider(this).get(TodoTabsViewModel::class.java)

        _binding = FragmentTodoTabsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        requireActivity().title = "Todos"
        findNavController().enableOnBackPressed(true)

        authManager.checkUser(binding.root.context)
        val firebaseUser = authManager.firebaseUser
        if (firebaseUser != null) {
            database = Firebase.database(url).reference
        }

        val pageAdapter =  PagerAdapter(childFragmentManager)
        pageAdapter.addFragment(TodoFragment(), "All")
        pageAdapter.addFragment(TodoFragment("Todo"), "Todo")
        pageAdapter.addFragment(TodoFragment("Done"), "Done")

        val viewPager : ViewPager = binding.viewPager
        val tabLayout : TabLayout = binding.tabLayout

        tabLayout.setupWithViewPager(viewPager)
        viewPager.adapter = pageAdapter

        setHasOptionsMenu(true)

        val btnAdd = binding.btnAddtodo
        var btn_anim: Animation = AnimationUtils.loadAnimation(binding.root.context, R.anim.bounce)
        btnAdd.setOnClickListener{
            btnAdd.startAnimation(btn_anim)

            val transition: Transition = Fade()
            transition.setDuration(250)
            transition.addTarget(binding.btnAddtodo)
            TransitionManager.beginDelayedTransition(binding.todoTabsLayout, transition)
            btnAdd.visibility = View.GONE

            createNewTodo()
        }


        return root
    }

    private fun createNewTodo(){
        dialogBuilder = AlertDialog.Builder(binding.root.context)
        var popupView = layoutInflater.inflate(R.layout.todo_popup, null)
        editTodoTitle = popupView.findViewById(R.id.title_todo)
        btnSave = popupView.findViewById(R.id.btn_save)

        dialogBuilder.setView(popupView)
        dialog = dialogBuilder.create()
        dialog.show()
        val window: Window? = dialog.window
        if (window != null) {
            window.attributes.windowAnimations = R.style.SlideDialogAnimation
            window.setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT)

            if(editTodoTitle.requestFocus()) {
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            }
        }
        btnSave.setOnClickListener{
            validateData()
        }
    }

    private fun validateData(){
        new_task = editTodoTitle.text.toString().trim()

        if(TextUtils.isEmpty(new_task)){
        }
        else{
            AddTodo()
            dialog.cancel()
        }
    }

    private fun AddTodo(){
        val firebaseUser = authManager.firebaseUser

            val new_todo = Todo(new_task, false, false, null)
            new_todo.created_at = LocalDateTime.now().toString()
            new_todo.updated_at = LocalDateTime.now().toString()

            database.child(path).child(firebaseUser!!.uid).push().setValue(new_todo).addOnSuccessListener { e->
//            Toast.makeText(binding.root.context, "Todo been added", Toast.LENGTH_SHORT).show()
//            binding.editTextNewtodo.setText("")
            }.addOnFailureListener{ e->
                Toast.makeText(binding.root.context, "Error while adding the note: $e", Toast.LENGTH_SHORT).show()
            }

    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.main_archive_button, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_archive ->{

            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

