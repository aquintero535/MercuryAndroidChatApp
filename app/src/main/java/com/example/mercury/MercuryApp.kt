package com.example.mercury

import android.app.Application
import com.google.firebase.database.FirebaseDatabase

class MercuryApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance().setPersistenceEnabled(false)
    }


}