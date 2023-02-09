package com.bek.lvlapp

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bek.lvlapp.databinding.ActivityMainBinding
import com.bek.lvlapp.helpers.AuthManager
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private lateinit var firebaseAuth: FirebaseAuth
    private var email = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,
                R.id.nav_todo,
                R.id.nav_skills,
                R.id.nav_actions,
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


//        val firebaseUser = FirebaseAuth.getInstance().currentUser
//        if(firebaseUser != null){
//            val email = firebaseUser.email.toString();
//            val emailtext = binding.root.findViewById(R.id.text_user_email) as TextView
//            if(emailtext != null){
//                emailtext.setText(email)
//            }
//        }

        val firebaseUser = AuthManager().firebaseUser
        AuthManager().checkUser(this)
        email = AuthManager().getUserEmail()

        val navigationView = findViewById<View>(R.id.nav_view) as NavigationView
        navigationView.background = resources.getDrawable(R.color.dark_bg)
        val headerView: View = navigationView.getHeaderView(0)
        headerView.findViewById<TextView>(R.id.text_user_email).text = email

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_settings) {
            findNavController(R.id.nav_host_fragment_content_main).navigate(R.id.settingsFragment)
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}