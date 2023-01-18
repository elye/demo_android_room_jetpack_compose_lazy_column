package com.example.mycomposetodo

import android.app.Application
import androidx.room.Room

class MyApplication: Application() {
    val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "todo-database"
        ).build()
    }
}
