package com.example.mycomposetodo

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.random.Random

class MainViewModel(private val todoDao: TodoDao) : ViewModel() {
    private var todoList = listOf<TodoItem>()

    val todoListFlow = todoDao.getAll()

    init {
        loadTodoList()
    }

    private fun loadTodoList() {
        viewModelScope.launch {
            todoDao.getAll().collect {
                todoList = it.toMutableStateList()
            }
        }
    }

    fun setUrgent(index: Int, value: Boolean) {
        val editedTodo = todoList[index].copy(urgent = value)
        viewModelScope.launch(Dispatchers.IO) {
            todoDao.update(editedTodo)
        }
    }

    fun generateRandomTodo() {
        val numberOfTodo = (10..20).random()
        val mutableTodoList = mutableStateListOf<TodoItem>()
        (0..numberOfTodo).forEach {
            val todoItem = TodoItem(it, "Item $it: ${randomWord()}", Random.nextBoolean())
            mutableTodoList.add(todoItem)
        }

        viewModelScope.launch(Dispatchers.IO) {
            todoDao.nukeTable()
            todoDao.insertAll(*mutableTodoList.toList().toTypedArray())
        }
    }

    private fun randomWord(): String {
        val random = Random
        val sb = StringBuilder()
        for (i in 1..random.nextInt(10) + 5) {
            sb.append(('a' + random.nextInt(26)))
        }
        return sb.toString()
    }

    fun addRecord(titleText: String, urgency: Boolean) {
        val id = todoList.lastOrNull()?.id ?: -1
        val todoItem = TodoItem(id + 1, titleText, urgency)
        viewModelScope.launch(Dispatchers.IO) {
            todoDao.insertAll(todoItem)
        }
    }

    fun removeRecord(todoItem: TodoItem) {
        viewModelScope.launch(Dispatchers.IO) {
            todoDao.delete(todoItem)
        }
    }
}
