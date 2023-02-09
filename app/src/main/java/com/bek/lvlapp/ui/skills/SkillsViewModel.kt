package com.bek.lvlapp.ui.skills

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SkillsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Skills"
    }
    val text: LiveData<String> = _text
}