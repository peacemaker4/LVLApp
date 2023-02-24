package com.bek.lvlapp.adapters

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bek.lvlapp.R
import com.bek.lvlapp.helpers.LevelCalculator
import com.bek.lvlapp.models.Action
import java.util.ArrayList

class ActionAdapter (private val mContext: Context, private val mArrayList: ArrayList<Action>) :
    RecyclerView.Adapter<ActionAdapter.ViewHolder>() {

    var onItemClick: ((Action) -> Unit)? = null
    var onItemButtonClick: ((Action) -> Unit)? = null
    var viewholder: ViewHolder? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.action_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentAction = mArrayList[position]

        holder.name.text = currentAction.name
//        holder.name.setTextColor(currentAction.color!!)
        holder.desc.text = currentAction.description
        holder.icon.background = mContext.resources.getIdentifier(currentAction.icon, "drawable", "com.bek.lvlapp")?.let { ContextCompat.getDrawable(mContext, it) }
        currentAction.color?.let { holder.icon.background.setColorFilter(it, PorterDuff.Mode.MULTIPLY) }
        holder.xp_give.text = "+ " + currentAction.xp_give.toString() + " XP"
        holder.border.backgroundTintList = ColorStateList.valueOf(currentAction.color!!)
        holder.add_btn.imageTintList = ColorStateList.valueOf(currentAction.color!!)

        holder.itemView.setOnClickListener{
            onItemClick?.invoke(currentAction)
        }

        holder.add_btn.setOnClickListener{
            onItemButtonClick?.invoke(currentAction)
        }
        viewholder = holder
    }

    override fun getItemCount(): Int {
        return mArrayList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView
        val desc: TextView
        val icon: ImageView
        val xp_give: TextView
        val border: LinearLayout
        val add_btn: ImageButton

        init {
            name =  itemView.findViewById(R.id.name_id)
            desc =  itemView.findViewById(R.id.desc_id)
            icon =  itemView.findViewById(R.id.icon_id)
            xp_give =  itemView.findViewById(R.id.xp_id)
            border =  itemView.findViewById(R.id.skill_layout_border)
            add_btn =  itemView.findViewById(R.id.add_btn_id)
        }

    }

    companion object {
        // setting the TAG for debugging purposes
        private const val TAG = "ActionAdapter"
    }
}