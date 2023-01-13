package com.example.mycomposetodo

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.random.Random

class MainViewModel(private val todoDao: TodoDao) : ViewModel() {
    private var todoList = todoDao.getAll().toMutableStateList()

    private val _todoListFlow = MutableStateFlow(todoList)

    val todoListFlow: StateFlow<List<TodoItem>> get() = _todoListFlow

    fun setUrgent(index: Int, value: Boolean) {
        todoList[index] = todoList[index].copy(urgent = value)
        todoDao.update(todoList[index])
    }

    fun generateRandomTodo() {
        val numberOfTodo = (10..20).random()
        val mutableTodoList = mutableStateListOf<TodoItem>()
        (0..numberOfTodo).forEach {
            val todoItem = TodoItem(it, "Item $it: ${randomWord()}", Random.nextBoolean())
            mutableTodoList.add(todoItem)
        }
        todoList = mutableTodoList
        _todoListFlow.value = mutableTodoList

        todoDao.nukeTable()
        todoDao.insertAll(*mutableTodoList.toList().toTypedArray())

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
        todoList.add(todoItem)
        todoDao.insertAll(todoItem)
    }

    fun removeRecord(todoItem: TodoItem) {
        val index = todoList.indexOf(todoItem)
        val todoItem = todoList[index]
        todoList.remove(todoItem)
        todoDao.delete(todoItem)
    }
}
