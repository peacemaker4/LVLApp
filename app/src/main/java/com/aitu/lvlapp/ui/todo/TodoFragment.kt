package com.aitu.lvlapp.ui.todo

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.TextView
import android.widget.Toast
import androidx.core.text.set
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.aitu.lvlapp.LoginActivity
import com.aitu.lvlapp.R
import com.aitu.lvlapp.adapters.TodoAdapter
import com.aitu.lvlapp.databinding.FragmentTodoBinding
import com.aitu.lvlapp.models.Todo
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import java.util.*


class TodoFragment : Fragment() {

    private lateinit var todoViewModel: TodoViewModel
    private var _binding: FragmentTodoBinding? = null

    private val binding get() = _binding!!

    private lateinit var firebaseAuth: FirebaseAuth

    //Firebase db
    lateinit var database: DatabaseReference
    private val url = "https://lvlapp-3a0e2-default-rtdb.europe-west1.firebasedatabase.app"
    private val path = "todos"

    private var email = ""

    private var todoAdapter: TodoAdapter? = null
    private var recyclerView: RecyclerView? = null

    private var todoList: ArrayList<Todo>? = null

    private var new_task = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        todoViewModel =
            ViewModelProvider(this).get(TodoViewModel::class.java)

        _binding = FragmentTodoBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setHasOptionsMenu(true)

        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            database = Firebase.database(url).reference
        }

        recyclerView = binding.listTodo
        recyclerView!!.setLayoutManager(LinearLayoutManager(binding.root.context))

        todoList = ArrayList()

        val todoListener = object : ValueEventListener {
            @SuppressLint("ResourceType")
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if(todoList!!.isEmpty()){
                    todoList = ArrayList()
                    for (todoSnapshot in dataSnapshot.getChildren()) {
                        var todo = todoSnapshot.getValue<Todo>()!!
                        todo.uid = todoSnapshot.key
                        todoList!!.add(todo)
                    }
                    if(_binding != null) {
                        binding.progressBar.visibility = View.GONE
                        todoAdapter = TodoAdapter(binding.root.context, todoList!!)
                        recyclerView!!.layoutManager =
                            StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
                        recyclerView!!.adapter = todoAdapter

    //                    todoAdapter?.onItemLongClick = { todo ->
    ////                        Toast.makeText(binding.root.context, "" + todo.task, Toast.LENGTH_SHORT).show()
    //                        database.child(path).child(firebaseUser!!.uid).child(todo.uid!!).removeValue().addOnSuccessListener { e->
    //                        }.addOnFailureListener{ e->
    //                            Toast.makeText(binding.root.context, "Unable to delete todo: $e", Toast.LENGTH_SHORT).show()
    //                        }
    //                    }
                        todoAdapter?.onCheckClick = { todo ->
                            val upd_todo = Todo(todo.task, !todo.check!!)
                            database.child(path).child(firebaseUser!!.uid).child(todo.uid!!).setValue(upd_todo).addOnSuccessListener { e->
                            }
                        }
                    }

                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(ContentValues.TAG, "Read failed", databaseError.toException())
            }
        }
        database.child(path).child(firebaseUser!!.uid).addValueEventListener(todoListener)

        val addBtn = root.findViewById(R.id.btn_addtodo) as FloatingActionButton?
        if (addBtn != null) {
            addBtn.setOnClickListener(View.OnClickListener {
                validateData()
            })
        }

        return root
    }

    private fun checkUser() {
        //check if user logged in
        val firebaseUser = firebaseAuth.currentUser
        if(firebaseUser != null){
            email = firebaseUser.email.toString();
        }
        else{
            binding.root.context.startActivity(Intent(binding.root.context, LoginActivity::class.java))
            val activity = context as Activity?
            activity!!.finish()
        }
    }

    private fun validateData(){
        new_task = binding.editTextNewtodo.text.toString().trim()

        if(TextUtils.isEmpty(new_task)){
        }
        else{
            AddTodo()
        }
    }

    private fun AddTodo(){
        val firebaseUser = firebaseAuth.currentUser

        val new_todo = Todo(new_task, false)

        database.child(path).child(firebaseUser!!.uid).push().setValue(new_todo).addOnSuccessListener { e->
//            Toast.makeText(binding.root.context, "Todo been added", Toast.LENGTH_SHORT).show()
            binding.editTextNewtodo.setText("")
            todoList!!.add(new_todo)
        }.addOnFailureListener{ e->
            Toast.makeText(binding.root.context, "Error while adding the note: $e", Toast.LENGTH_SHORT).show()
        }
    }

//    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
//        menuInflater.inflate(R.menu.search, menu)
//        val item = menu?.findItem(R.id.action_search)
//        val searchView = item?.actionView as SearchView
//
//        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
//            override fun onQueryTextSubmit(p0: String?): Boolean {
//                TODO("Not yet implemented")
//            }
//
//
//            override fun onQueryTextChange(newText: String?): Boolean{
//                searchList!!.clear()
//                val searchText = newText!!.toLowerCase(Locale.getDefault())
//                if(searchText.isNotEmpty()){
//                    notesList!!.forEach{
//                        if(it.subject!!.toLowerCase(Locale.getDefault()).contains(searchText) || it.note!!.toLowerCase(
//                                Locale.getDefault()).contains(searchText)){
//                            searchList!!.add(it)
//                        }
//                    }
//                    recyclerView!!.adapter!!.notifyDataSetChanged()
//                }
//                else{
//                    searchList!!.clear()
//                    searchList!!.addAll(notesList!!)
//                    recyclerView!!.adapter!!.notifyDataSetChanged()
//                }
//
//                return false
//            }
//
//        })
//
//        super.onCreateOptionsMenu(menu, menuInflater)
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}