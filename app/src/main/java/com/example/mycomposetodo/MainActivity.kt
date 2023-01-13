package com.example.mycomposetodo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.ui.Modifier
import com.example.mycomposetodo.ui.theme.MyComposeTodoTheme

class MainActivity : ComponentActivity() {
    private val viewModel by lazy {
        MainViewModel((application as MyApplication).db.todoDao())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyComposeTodoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    MainTodoView(viewModel)
                }
            }
        }
    }
}
