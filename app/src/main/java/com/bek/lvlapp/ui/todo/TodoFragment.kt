package com.bek.lvlapp.ui.todo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bek.lvlapp.LoginActivity
import com.bek.lvlapp.R
import com.bek.lvlapp.adapters.SwipeGesture
import com.bek.lvlapp.adapters.TodoAdapter
import com.bek.lvlapp.databinding.FragmentTodoBinding
import com.bek.lvlapp.models.Todo
import com.google.android.material.floatingactionbutton.FloatingActionButton
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


class TodoFragment(var list_type: String = "All") : Fragment() {

    private lateinit var todoViewModel: TodoViewModel
    private var _binding: FragmentTodoBinding? = null

    private val binding get() = _binding!!

    private lateinit var firebaseAuth: FirebaseAuth

    //Firebase db
    lateinit var database: DatabaseReference
    private val url = "https://lvlapp-ff610-default-rtdb.europe-west1.firebasedatabase.app"
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

        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            database = Firebase.database(url).reference
        }

        recyclerView = binding.listTodo
        recyclerView!!.setLayoutManager(LinearLayoutManager(binding.root.context))

        todoList = ArrayList()

        if(savedInstanceState?.getString("LIST_TYPE", "All") != null)
            list_type = savedInstanceState?.getString("LIST_TYPE", "All").toString()

        binding.progressBar.start()

        val todoListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                todoList = ArrayList()

                for (todoSnapshot in dataSnapshot.children) {
                    var todo = todoSnapshot.getValue<Todo>()!!
                    if(list_type == "All" || list_type == "Todo" && todo.check == false || list_type == "Done" && todo.check == true){
                        if(todo.archived == false){
                            todo.uid = todoSnapshot.key
                            todoList!!.add(todo)
                        }
                    }
                }
                if(_binding != null) {
                    todoList!!.reverse()
                    if(list_type == "All")
                        todoList!!.sortBy { t -> t.check }

                    binding.progressBar.visibility = View.GONE
                    todoAdapter = TodoAdapter(binding.root.context, todoList!!)
                    todoAdapter!!.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.ALLOW

                    recyclerView!!.layoutManager =
                        StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
                    recyclerView!!.adapter = todoAdapter


                    //swipeGesture
                    val swipe = object : SwipeGesture(binding.root.context){
                        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                            when(direction){
                                ItemTouchHelper.LEFT -> {
                                    database.child(path).child(firebaseUser!!.uid).child(todoList!!.get(viewHolder.layoutPosition).uid!!).removeValue().addOnSuccessListener { e->
                                    }.addOnFailureListener{ e->
                                        Toast.makeText(binding.root.context, "Unable to delete todo: $e", Toast.LENGTH_SHORT).show()
                                    }
                                }

                                ItemTouchHelper.RIGHT -> {
                                    var arch_todo = todoList!!.get(viewHolder.layoutPosition)
                                    arch_todo.archived = true
                                    database.child(path).child(firebaseUser!!.uid).child(todoList!!.get(viewHolder.layoutPosition).uid!!).setValue(arch_todo).addOnSuccessListener { e->
                                    }.addOnFailureListener{ e->
                                        Toast.makeText(binding.root.context, "Unable to archive todo: $e", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    }


                    todoAdapter?.onCheckClick = { todo ->
                        val upd_todo = Todo(todo.task, !todo.check!!, todo.archived, todo.uid, todo.pos, todo.created_at, LocalDateTime.now().toString())

                        database.child(path).child(firebaseUser!!.uid).child(todo.uid!!).setValue(upd_todo).addOnSuccessListener { e->
                        }
                    }

                    val touchHelper = ItemTouchHelper(swipe)
                    touchHelper.attachToRecyclerView(recyclerView)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        }
        database.child(path).child(firebaseUser!!.uid).addValueEventListener(todoListener)

        return root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("LIST_TYPE", list_type)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}