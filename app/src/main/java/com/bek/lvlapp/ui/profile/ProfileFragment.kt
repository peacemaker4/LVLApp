package com.bek.lvlapp.ui.profile

import android.content.Intent
import android.os.Bundle
import android.transition.Slide
import android.transition.Transition
import android.transition.TransitionManager
import android.view.*
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bek.lvlapp.R
import com.bek.lvlapp.databinding.FragmentProfileBinding
import com.bek.lvlapp.helpers.AuthManager
import com.bek.lvlapp.ui.skills.SkillsAddActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null

    private var authManager = AuthManager()

    private val binding get() = _binding!!

    //Firebase db
    lateinit var database: DatabaseReference
    private val url = "https://lvlapp-ff610-default-rtdb.europe-west1.firebasedatabase.app"
    private val path = "users"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        requireActivity().title = "Profile"

        val firebaseUser = authManager.firebaseUser
        if (firebaseUser != null) {
            database = Firebase.database(url).reference
        }

        setHasOptionsMenu(true)

        return root
    }

//    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
//        menuInflater.inflate(R.menu.main_settings, menu)
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
//        R.id.action_settings ->{
//            findNavController().navigate(R.id.action_nav_profile_to_settingsFragment)
//            true
//        }
//
//        else -> {
//            super.onOptionsItemSelected(item)
//        }
//    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}