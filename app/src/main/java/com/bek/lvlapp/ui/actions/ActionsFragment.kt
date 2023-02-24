package com.bek.lvlapp.ui.actions

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.transition.Slide
import android.transition.Transition
import android.transition.TransitionManager
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bek.lvlapp.R
import com.bek.lvlapp.adapters.ActionAdapter
import com.bek.lvlapp.adapters.DragGesture
import com.bek.lvlapp.adapters.SkillAdapter
import com.bek.lvlapp.adapters.SkillSelectAdapter
import com.bek.lvlapp.databinding.FragmentActionsBinding
import com.bek.lvlapp.helpers.LevelCalculator
import com.bek.lvlapp.models.Action
import com.bek.lvlapp.models.Skill
import com.bek.lvlapp.models.SkillAction
import com.bek.lvlapp.ui.skills.SkillActivity
import com.github.johnpersano.supertoasts.library.Style
import com.github.johnpersano.supertoasts.library.SuperActivityToast
import com.github.johnpersano.supertoasts.library.SuperActivityToast.OnButtonClickListener
import com.google.android.flexbox.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.like.LikeButton
import com.like.OnLikeListener
import java.time.LocalDateTime
import java.util.*


class ActionsFragment : Fragment() {

    private var _binding: FragmentActionsBinding? = null

    private lateinit var firebaseAuth: FirebaseAuth

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    //Firebase db
    lateinit var database: DatabaseReference
    private val url = "https://lvlapp-ff610-default-rtdb.europe-west1.firebasedatabase.app"
    private val path = "actions"
    private val skills_path = "skills"
    private val skills_actions_path = "skills_actions"

    private var actionAdapter: ActionAdapter? = null
    private var recyclerView: RecyclerView? = null

    private var actionList: ArrayList<Action>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentActionsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        requireActivity().title = "Actions"

        setHasOptionsMenu(true)

        firebaseAuth = FirebaseAuth.getInstance()
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            database = Firebase.database(url).reference
        }

        recyclerView = binding.listActions
        recyclerView!!.setLayoutManager(LinearLayoutManager(binding.root.context))

        actionList = ArrayList()

        binding.progressBar.start()
        val actionsListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                actionList = ArrayList()

                for (actionsSnapshot in dataSnapshot.children) {
                    var action = actionsSnapshot.getValue<Action>()!!
                    action.uid = actionsSnapshot.key
                    actionList!!.add(action)
                }
                if(_binding != null) {

                    actionList!!.sortBy { a -> a.pos }

                    binding.progressBar.visibility = View.GONE

                    val transition: Transition = Slide(Gravity.TOP)
                    transition.setDuration(500)
                    transition.addTarget(binding.listActions)
                    TransitionManager.beginDelayedTransition(binding.actionsLayout, transition)

                    binding.listActions.visibility = View.VISIBLE
                    actionAdapter = ActionAdapter(binding.root.context, actionList!!)
                    actionAdapter!!.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.ALLOW

                    val layoutManager = FlexboxLayoutManager(context).apply {
                        justifyContent = JustifyContent.SPACE_EVENLY
                        alignItems = AlignItems.CENTER
                        flexDirection = FlexDirection.ROW
                        flexWrap = FlexWrap.WRAP
                    }

                    recyclerView!!.layoutManager = layoutManager
                    recyclerView!!.adapter = actionAdapter

                    actionAdapter?.onItemClick = { action ->
                        val intent = Intent(context, ActionActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        intent.putExtra("action_uid", action.uid)
                        context!!.startActivity(intent)
                        requireActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                    }
                    actionAdapter?.onItemButtonClick = { action ->
                          database.child(skills_path).child(firebaseUser!!.uid).child(action.skill_uid!!).get().addOnSuccessListener { e ->
                              var skill = e.getValue<Skill>()
                              skill!!.uid = dataSnapshot.key
                              skill.updated_at = LocalDateTime.now().toString()
                              skill.xp = skill.xp?.plus(action.xp_give!!)
                              val curr_xp_to_lvl = LevelCalculator.XPToLevel(skill.xp!!)
                              if(curr_xp_to_lvl > skill.level!!){
                                  skill.level = curr_xp_to_lvl
                              }

                              database.child(skills_path).child(firebaseUser!!.uid).child(action.skill_uid!!).setValue(skill).addOnSuccessListener { e->
                                  val toast = SuperActivityToast.create(requireActivity(), Style(), Style.TYPE_STANDARD)
                                      .setText("${action.name} + ${action.xp_give} XP")
                                      .setDuration(Style.DURATION_VERY_SHORT)
                                      .setFrame(Style.FRAME_KITKAT)
                                      .setColor(action.color!!)
                                      .setIconResource(R.drawable.ic_icon_stars)
                                      .setHeight(88)
                                      .setAnimations(Style.ANIMATIONS_FLY)
                                  toast.show()

                                  val skill_action = SkillAction()
                                  skill_action.action_uid = action.uid
                                  skill_action.skill_uid = action.skill_uid
                                  skill_action.xp_give = action.xp_give
                                  skill_action.created_at = LocalDateTime.now().toString()

                                  database.child(skills_actions_path).child(firebaseUser!!.uid).child(action.uid!!).push().setValue(skill_action).addOnCanceledListener {
                                  }
                              }.addOnFailureListener{ e->
                                  Toast.makeText(binding.root.context, "Error while updating the skill: $e", Toast.LENGTH_LONG).show()
                              }
                          }


                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        }
        database.child(path).child(firebaseUser!!.uid).addValueEventListener(actionsListener)


//        binding.likeBtn.setOnClickListener{
//            binding.likeBtn.isLiked = true
//            Toast.makeText(binding.root.context, "LIKED", Toast.LENGTH_SHORT).show()
//        }
//
//        binding.likeBtn.setOnLikeListener(object : OnLikeListener {
//            override fun liked(likeButton: LikeButton) {
//                Toast.makeText(binding.root.context, "LIKED", Toast.LENGTH_SHORT).show()
//            }
//            override fun unLiked(likeButton: LikeButton) {
//                Toast.makeText(binding.root.context, "UNLIKED", Toast.LENGTH_SHORT).show()
//            }
//        })
        return root
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.main_add_button, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_add ->{
            val intent = Intent(requireActivity(), ActionsAddActivity::class.java)
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.slide_up, R.anim.no_animation)
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