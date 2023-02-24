package com.bek.lvlapp

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bek.lvlapp.databinding.ActivityMainBinding
import com.bek.lvlapp.helpers.AuthManager
import com.bek.lvlapp.models.Icon
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import me.ibrahimsn.lib.OnItemSelectedListener
import me.ibrahimsn.lib.SmoothBottomBar


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var firebaseAuth: FirebaseAuth
    private var email = ""
    private lateinit var navView: SmoothBottomBar
    private lateinit var navController: NavController

    private var menu_list: ArrayList<Int> = arrayListOf<Int>(
        R.id.nav_home,
        R.id.nav_todo,
        R.id.nav_skills,
        R.id.nav_actions,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        navView = binding.appBarMain.bottomBar
        navController = findNavController(R.id.nav_host_fragment_content_main)

        setupSmoothBottomMenu()

//        setupActionBarWithNavController(navController)

        navView.itemTextColor = resources.getColor(R.color.red)

        navView.setOnItemSelectedListener { item ->
            navView.itemTextColor = resources.getColor(R.color.red)
            navController.navigate(menu_list.get(item))
        }

        val firebaseUser = AuthManager().firebaseUser
        AuthManager().checkUser(this)
        email = AuthManager().getUserEmail()
    }

    private fun setupSmoothBottomMenu() {
        val popupMenu = PopupMenu(this, null)
        popupMenu.inflate(R.menu.menu)
        val menu = popupMenu.menu
        navView.setupWithNavController(menu, navController)

    }


//    override fun onSupportNavigateUp(): Boolean {
//        val navController = findNavController(R.id.nav_host_fragment_content_main)
//        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
//    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }


}