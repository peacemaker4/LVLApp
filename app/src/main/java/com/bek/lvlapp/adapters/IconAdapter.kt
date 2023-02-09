package com.bek.lvlapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bek.lvlapp.R
import com.bek.lvlapp.models.Icon
import com.bek.lvlapp.models.Todo
import java.util.ArrayList

class IconAdapter (private val mContext: Context, private val mArrayList: ArrayList<Icon>) :
    RecyclerView.Adapter<IconAdapter.ViewHolder>() {

    var onItemClick: ((Icon) -> Unit)? = null
    var viewholder: ViewHolder? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.icon_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentIcon = mArrayList[position]

        currentIcon.icon?.let { holder.icon.setImageResource(it) }
        holder.name.text = currentIcon.name

        holder.itemView.setOnClickListener{
            onItemClick?.invoke(currentIcon)
        }
        viewholder = holder
    }

    override fun getItemCount(): Int {
        return mArrayList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView
        val name: TextView

        init {
            icon = itemView.findViewById(R.id.icon_id)
            name =  itemView.findViewById(R.id.name_id)
        }

    }

    companion object {
        // setting the TAG for debugging purposes
        private const val TAG = "IconAdapter"
    }
}