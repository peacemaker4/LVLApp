package com.bek.lvlapp.ui.skills

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.transition.ChangeBounds
import android.transition.Explode
import android.transition.Fade
import android.transition.Slide
import android.transition.Transition
import android.transition.TransitionManager
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bek.lvlapp.R
import com.bek.lvlapp.adapters.DragGesture
import com.bek.lvlapp.adapters.SkillAdapter
import com.bek.lvlapp.databinding.FragmentSkillsBinding
import com.bek.lvlapp.models.Skill
import com.bek.lvlapp.models.Todo
import com.github.johnpersano.supertoasts.library.Style
import com.github.johnpersano.supertoasts.library.SuperActivityToast
import com.github.johnpersano.supertoasts.library.utils.PaletteUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import java.util.*


class SkillsFragment : Fragment() {

    private lateinit var skillsViewModel: SkillsViewModel
    private var _binding: FragmentSkillsBinding? = null

    private lateinit var firebaseAuth: FirebaseAuth

    //Firebase db
    lateinit var database: DatabaseReference
    private val url = "https://lvlapp-ff610-default-rtdb.europe-west1.firebasedatabase.app"
    private val path = "skills"

    private val binding get() = _binding!!

    private var email = ""

    private var skillAdapter: SkillAdapter? = null
    private var recyclerView: RecyclerView? = null

    private var skillList: ArrayList<Skill>? = null
    private var list_reordered = false

    //skill add popup
    private lateinit var dialogBuilder: AlertDialog.Builder
    private lateinit var dialog: AlertDialog
    private lateinit var touchHelper: ItemTouchHelper

    private var edit_pos: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        skillsViewModel =
            ViewModelProvider(this).get(SkillsViewModel::class.java)

        _binding = FragmentSkillsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        firebaseAuth = FirebaseAuth.getInstance()
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            database = Firebase.database(url).reference
        }


        recyclerView = binding.listSkill
        recyclerView!!.setLayoutManager(LinearLayoutManager(binding.root.context))

        skillList = ArrayList()


        binding.progressBar.start()
        val skillListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                skillList = ArrayList()

                for (skillSnapshot in dataSnapshot.children) {
                    var skill = skillSnapshot.getValue<Skill>()!!
                    skill.uid = skillSnapshot.key
                    skillList!!.add(skill)
                }
                if(_binding != null) {

                    skillList!!.sortBy { s -> s.pos }

                    binding.progressBar.visibility = View.GONE

                    val transition: Transition = Slide(Gravity.TOP)
                    transition.setDuration(500)
                    transition.addTarget(binding.listSkill)
                    TransitionManager.beginDelayedTransition(binding.viewContainer, transition)

                    binding.listSkill.visibility = View.VISIBLE
                    skillAdapter = SkillAdapter(binding.root.context, skillList!!)
                    skillAdapter!!.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.ALLOW

                    recyclerView!!.layoutManager =
                        StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
                    recyclerView!!.adapter = skillAdapter

                    //swipeGesture
                    val swipe = object : DragGesture(binding.root.context){
                        override fun onMove(
                            recyclerView: RecyclerView,
                            viewHolder: RecyclerView.ViewHolder,
                            target: RecyclerView.ViewHolder
                        ): Boolean {

                                val from_pos = viewHolder.layoutPosition
                                val to_pos = target.layoutPosition
//                            Toast.makeText(binding.root.context, todoList!![from_pos].pos.toString() + " ->" + todoList!![to_pos].pos.toString(), Toast.LENGTH_SHORT).show()
                                Collections.swap(skillList, from_pos, to_pos)
                                skillAdapter!!.notifyItemMoved(from_pos, to_pos)
                            list_reordered = true



                            return false
                        }
                    }
                    touchHelper = ItemTouchHelper(swipe)

                    skillAdapter?.onItemClick = { skill ->
                        val intent = Intent(context, SkillActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        intent.putExtra("skill_uid", skill.uid)
                        context!!.startActivity(intent)
                        requireActivity().overridePendingTransition(R.transition.fade_in, R.transition.fade_out)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        }
        database.child(path).child(firebaseUser!!.uid).addValueEventListener(skillListener)

        binding.btnSave.setOnClickListener {
            if(list_reordered){
                var c = 0
                binding.listSkill.visibility = View.GONE

                for(s in skillList!!){
                    s.pos = c
                    c++
                    database.child(path).child(firebaseUser!!.uid).child(s.uid!!).setValue(s).addOnSuccessListener { e ->
                    }
                }
            }


            val transition: Transition = Fade()
            transition.setDuration(250)
            transition.addTarget(binding.btnSave)
            TransitionManager.beginDelayedTransition(binding.viewContainer, transition)

            binding.viewContainer.background = AppCompatResources.getDrawable(binding.root.context, R.color.dark_bg)

            binding.btnSave.visibility = View.GONE
            touchHelper.attachToRecyclerView(null)
        }

        setHasOptionsMenu(true)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.main_add_order_button, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_add ->{
            val intent = Intent(requireActivity(), SkillsAddActivity::class.java)
            startActivity(intent)
            requireActivity().overridePendingTransition(R.transition.slide_up, R.transition.no_animation)
            true
        }
        R.id.action_order->{
            if(binding.btnSave.visibility == View.GONE){
                list_reordered = false
                val transition: Transition = Slide(Gravity.BOTTOM)
                transition.setDuration(600)
                transition.addTarget(binding.btnSave)
                TransitionManager.beginDelayedTransition(binding.viewContainer, transition)

                binding.viewContainer.background = AppCompatResources.getDrawable(binding.root.context, R.color.dark)
                binding.btnSave.visibility = View.VISIBLE
                touchHelper.attachToRecyclerView(recyclerView)

                requireActivity().invalidateOptionsMenu()
            }
            else{
                if(!list_reordered){
                    binding.viewContainer.background = AppCompatResources.getDrawable(binding.root.context, R.color.dark_bg)

                    val transition: Transition = Slide(Gravity.BOTTOM)
                    transition.setDuration(600)
                    transition.addTarget(binding.btnSave)
                    TransitionManager.beginDelayedTransition(binding.viewContainer, transition)

                    binding.btnSave.visibility = View.GONE
                    touchHelper.attachToRecyclerView(null)
                }
            }

            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }
}