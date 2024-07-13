package com.example.hostel1.ui.Support

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SupportViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Support Fragment"
    }
    val text: LiveData<String> = _text
}