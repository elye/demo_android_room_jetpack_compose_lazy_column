package com.example.mycomposetodo

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class MainViewModel(private val todoDao: TodoDao) : ViewModel() {
    private var todoList = mutableStateListOf<TodoItem>()
    private val _todoListFlow = MutableStateFlow(todoList)

    val todoListFlow: StateFlow<List<TodoItem>> get() = _todoListFlow
    private var postExecute: (() -> Unit)? = null

    init {
        loadTodoList()
    }

    private fun loadTodoList() {
        viewModelScope.launch {
            todoDao.getAll().collect {
                todoList = it.toMutableStateList()
                _todoListFlow.value = todoList
                postExecute?.invoke()
            }
        }
    }

    fun setUrgent(index: Int, value: Boolean) {
        val editedTodo = todoList[index].copy(urgent = value)
        viewModelScope.launch(Dispatchers.IO) {
            todoDao.update(editedTodo)
            postExecute = null
        }
    }

    fun generateRandomTodo(postGenerate: (() -> Unit)? = null) {
        val numberOfTodo = (10..20).random()
        val mutableTodoList = mutableStateListOf<TodoItem>()
        (0..numberOfTodo).forEach {
            val todoItem = TodoItem(it, "Item $it: ${randomWord()}", Random.nextBoolean())
            mutableTodoList.add(todoItem)
        }

        viewModelScope.launch(Dispatchers.IO) {
            todoDao.nukeTable()
            todoDao.insertAll(*mutableTodoList.toList().toTypedArray())
            postExecute = postGenerate
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

    fun addRecord(titleText: String, urgency: Boolean, postInsert: (() -> Unit)? = null) {
        val id = todoList.lastOrNull()?.id ?: -1
        val todoItem = TodoItem(id + 1, titleText, urgency)
        viewModelScope.launch(Dispatchers.IO) {
            todoDao.insertAll(todoItem)
            postExecute = postInsert
        }
    }

    fun removeRecord(todoItem: TodoItem, postRemove: (() -> Unit)? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            todoDao.delete(todoItem)
            postExecute = postRemove
        }
    }
}
