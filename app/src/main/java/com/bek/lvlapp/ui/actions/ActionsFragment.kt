package com.bek.lvlapp.ui.actions

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bek.lvlapp.R
import com.bek.lvlapp.databinding.FragmentActionsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.like.LikeButton
import com.like.OnLikeListener


class ActionsFragment : Fragment() {

    private var _binding: FragmentActionsBinding? = null

    private lateinit var firebaseAuth: FirebaseAuth

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    //Firebase db
    lateinit var database: DatabaseReference
    private val url = "https://lvlapp-ff610-default-rtdb.europe-west1.firebasedatabase.app"
    private val path = "quotes"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentActionsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setHasOptionsMenu(true)

//        firebaseAuth = FirebaseAuth.getInstance()
//        val firebaseUser = firebaseAuth.currentUser
//        if (firebaseUser != null) {
//            database = Firebase.database(url).reference
//        }

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
        menuInflater.inflate(R.menu.main_add_order_button, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_add ->{
            val intent = Intent(requireActivity(), ActionsAddActivity::class.java)
            startActivity(intent)
            requireActivity().overridePendingTransition(R.transition.slide_up, R.transition.no_animation)
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