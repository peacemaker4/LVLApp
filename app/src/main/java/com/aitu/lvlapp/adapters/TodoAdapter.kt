package com.aitu.lvlapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.aitu.lvlapp.R
import com.aitu.lvlapp.models.Todo
import java.util.ArrayList

class TodoAdapter (private val mContext: Context, private val mArrayList: ArrayList<Todo>) :
    RecyclerView.Adapter<TodoAdapter.ViewHolder>() {

    var onItemClick: ((Todo) -> Unit)? = null
    var onCheckClick: ((Todo) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.todo_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentTodo = mArrayList[position]

        //swipeGesture
        val swipe = object : SwipeGesture(){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                when(direction){
                    ItemTouchHelper.LEFT -> {
                        //delete
                    }

                    ItemTouchHelper.RIGHT -> {
                        //archive
                    }
                }
            }
        }

        holder.task.text = currentTodo.task
        holder.check.isChecked = currentTodo.check!!
        if(currentTodo.task.isNullOrEmpty()){
            holder.task.visibility = View.GONE
        }
        if(currentTodo.check){
            holder.task.setTextColor(ContextCompat.getColor(mContext, R.color.dark_50))
        }
        holder.itemView.setOnClickListener{
            onItemClick?.invoke(currentTodo)
        }
        holder.check.setOnClickListener{
            onCheckClick?.invoke(currentTodo)
        }
    }

    override fun getItemCount(): Int {
        return mArrayList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val task: TextView
        val check: CheckBox

        init {
            task = itemView.findViewById(R.id.task_id)
            check =  itemView.findViewById(R.id.check_id)
        }

    }

    companion object {
        // setting the TAG for debugging purposes
        private const val TAG = "TaskAdapter"
    }
}