package com.bek.lvlapp

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.widget.Toast
import androidx.preference.Preference
import com.bek.lvlapp.databinding.FragmentSkillsBinding
import com.bek.lvlapp.helpers.AuthManager
import com.takisoft.preferencex.PreferenceFragmentCompat


class SettingsFragment : PreferenceFragmentCompat() {



    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater!!)
        menu.clear()
    }

    override fun onCreatePreferencesFix(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        val logout_btn: Preference? = findPreference(getString(R.string.logout))
        if (logout_btn != null) {
            logout_btn.setOnPreferenceClickListener(object : Preference.OnPreferenceClickListener {
                override fun onPreferenceClick(preference: Preference): Boolean {
                    val authManager = AuthManager()
                    authManager.logout()
                    authManager.checkUser(requireContext())
                    return true
                }
            })
        }

        val color_pick: Preference? = findPreference("pref_color")
        if (color_pick != null) {
            color_pick.setOnPreferenceChangeListener { preference, newValue ->
                Toast.makeText(requireContext(), "" + preference + " " + newValue, Toast.LENGTH_SHORT).show()

                true
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }
}