package com.example.mycomposetodo

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun MainTodoView(viewModel: MainViewModel) {
    val todoListState = viewModel.todoListFlow.collectAsState()
    val lazyListState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        var popupControl by remember { mutableStateOf(false) }

        Button(onClick = {
            viewModel.generateRandomTodo()
            scope.launch {
                lazyListState.scrollToItem(0)
            }
        }) {
            Text("Randomly Generate Todo")
        }
        Button(onClick = { popupControl = true }) {
            Text("Add Record")
        }
        if (popupControl) {
            Popup(
                popupPositionProvider = WindowCenterOffsetPositionProvider(),
                onDismissRequest = { popupControl = false },
                properties = PopupProperties(focusable = true)
            ) {
                AddRecord({
                    popupControl = false
                    scope.launch {
                        lazyListState.scrollToItem(todoListState.value.size)
                    }
                }, viewModel)
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxHeight(),
            state = lazyListState
        ) {
            Log.d("Track", "FullList ${todoListState.value.toMutableList()}")
            items(
                items = todoListState.value,
                key = { todoItem -> todoItem.id },
                itemContent = { item ->
                    Log.d("Track", "Showing $item")
                    val currentItem by rememberUpdatedState(item)
                    val dismissState = rememberDismissState(
                        confirmStateChange = {
                            if (it == DismissValue.DismissedToStart || it == DismissValue.DismissedToEnd) {
                                val index = todoListState.value.indexOf(currentItem)
                                viewModel.removeRecord(index)
                            }
                            true
                        }
                    )

                    SwipeToDismiss(
                        state = dismissState,
                        modifier = Modifier
                            .padding(vertical = 1.dp)
                            .animateItemPlacement(),
                        directions = setOf(
                            DismissDirection.StartToEnd,
                            DismissDirection.EndToStart
                        ),
                        dismissThresholds = { direction ->
                            FractionalThreshold(if (direction == DismissDirection.StartToEnd) 0.25f else 0.4f)
                        },
                        background = {
                            SwipeBackground(dismissState)
                        },
                        dismissContent = {
                            TodoItemRow(item, todoListState, viewModel)
                        })
                })
        }
    }
}

@Composable
@OptIn(ExperimentalMaterialApi::class)
private fun SwipeBackground(dismissState: DismissState) {
    val direction = dismissState.dismissDirection ?: return
    val color by animateColorAsState(
        when (dismissState.targetValue) {
            DismissValue.Default -> Color.LightGray
            DismissValue.DismissedToEnd -> Color.Green
            DismissValue.DismissedToStart -> Color.Red
        }
    )
    val alignment = when (direction) {
        DismissDirection.StartToEnd -> Alignment.CenterStart
        DismissDirection.EndToStart -> Alignment.CenterEnd
    }
    val icon = when (direction) {
        DismissDirection.StartToEnd -> Icons.Default.Done
        DismissDirection.EndToStart -> Icons.Default.Delete
    }
    val scale by animateFloatAsState(
        if (dismissState.targetValue == DismissValue.Default) 0.75f else 1f
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(color)
            .padding(horizontal = 20.dp),
        contentAlignment = alignment
    ) {
        Icon(
            icon,
            contentDescription = "Localized description",
            modifier = Modifier.scale(scale)
        )
    }
}

@Composable
private fun TodoItemRow(
    item: TodoItem,
    todoListState: State<List<TodoItem>>,
    viewModel: MainViewModel
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Yellow),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier
                .weight(1f)
                .padding(8.dp),
            text = item.title
        )
        Checkbox(
            checked = item.urgent,
            onCheckedChange = {
                val index = todoListState.value.indexOf(item)
                viewModel.setUrgent(index, it)
            }
        )
    }
}
