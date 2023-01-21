package com.example.mycomposetodo

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity
data class TodoItem(
    @PrimaryKey val id: Int,
    @ColumnInfo val title: String,
    @ColumnInfo var urgent: Boolean = false
)

@Dao
interface TodoDao {

    @Query("SELECT * FROM TodoItem")
    fun getAll(): Flow<List<TodoItem>>

    @Insert
    fun insertAll(vararg todos: TodoItem)

    @Delete
    fun delete(todo: TodoItem)

    @Update
    fun update(note: TodoItem)

    @Query("DELETE FROM TodoItem")
    fun nukeTable()
}

@Database(entities = [TodoItem::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun todoDao(): TodoDao
}
