package com.aitu.lvlapp.ui.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.aitu.lvlapp.R
import com.aitu.lvlapp.WelcomeActivity
import com.aitu.lvlapp.databinding.FragmentHomeBinding
import com.aitu.lvlapp.ui.todo.TodoFragment
import com.google.firebase.auth.FirebaseAuth
import androidx.navigation.findNavController


class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null

    private lateinit var firebaseAuth: FirebaseAuth

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var email = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        firebaseAuth = FirebaseAuth.getInstance()
        val firebaseUser = firebaseAuth.currentUser

        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner, Observer {
            if (firebaseUser != null) {
                textView.text = it + firebaseUser.email
            }
            else{
                textView.text = it
            }
        })

        //handle logout click
        val logBtn = root.findViewById(R.id.logout_btn) as Button?
        if (logBtn != null) {
            logBtn.setOnClickListener(View.OnClickListener {
                firebaseAuth.signOut()
                checkUser()
            })
        }

        val todoBtn = root.findViewById(R.id.btn_todo) as Button?
        if(todoBtn != null){
            todoBtn.setOnClickListener(View.OnClickListener {
                binding.root.findNavController().navigate(R.id.nav_todo)
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
            binding.root.context.startActivity(Intent(binding.root.context, WelcomeActivity::class.java))
            val activity = context as Activity?
            activity!!.finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}