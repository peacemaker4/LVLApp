package com.bek.lvlapp.adapters

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bek.lvlapp.R
import com.bek.lvlapp.helpers.LevelCalculator
import com.bek.lvlapp.models.Skill
import com.bek.lvlapp.models.Todo
import java.util.ArrayList

class SkillAdapter (private val mContext: Context, private val mArrayList: ArrayList<Skill>) :
    RecyclerView.Adapter<SkillAdapter.ViewHolder>() {

    var onItemClick: ((Skill) -> Unit)? = null
    var viewholder: ViewHolder? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.skill_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentSkill = mArrayList[position]

        holder.name.text = currentSkill.name
        holder.name.setTextColor(currentSkill.color!!)
        holder.icon.background = mContext.resources.getIdentifier(currentSkill.icon, "drawable", "com.bek.lvlapp")?.let { ContextCompat.getDrawable(mContext, it) }
        currentSkill.color?.let { holder.icon.background.setColorFilter(it, PorterDuff.Mode.MULTIPLY) }
        holder.level.text = currentSkill.level.toString()
        holder.level.setTextColor(currentSkill.color!!)
        holder.xp.text = currentSkill.xp.toString() + " XP"
        holder.progress.progress = LevelCalculator.LevelProgress(currentSkill.xp!!)
        holder.progress.progressTintList = ColorStateList.valueOf(currentSkill.color!!)

        holder.itemView.setOnClickListener{
            onItemClick?.invoke(currentSkill)
        }
        viewholder = holder
    }

    override fun getItemCount(): Int {
        return mArrayList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView
        val icon: ImageView
        val level: TextView
        val xp: TextView
        val progress: ProgressBar

        init {
            name =  itemView.findViewById(R.id.name_id)
            icon =  itemView.findViewById(R.id.icon_id)
            level =  itemView.findViewById(R.id.level_id)
            xp =  itemView.findViewById(R.id.xp_id)
            progress =  itemView.findViewById(R.id.progress_id)
        }

    }

    companion object {
        // setting the TAG for debugging purposes
        private const val TAG = "SkillAdapter"
    }
}