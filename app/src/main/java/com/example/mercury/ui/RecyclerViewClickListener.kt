package com.example.mercury.ui

import android.view.View

interface RecyclerViewClickListener {
    fun onClick(view: View?, position: Int)
}