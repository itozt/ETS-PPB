package com.example.todolist.ui.calendar

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.todolist.R
import com.example.todolist.domain.model.Task
import com.example.todolist.ui.tasklist.TaskItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun CalendarScreen(
    tasks: List<Task>,
    onTaskCheckedChange: (Task, Boolean) -> Unit,
    onDeleteTask: (Task) -> Unit,
    onEditTask: (Task) -> Unit,
    onAddTask: (String, String?, Long?, Boolean, com.example.todolist.domain.model.RepeatMode, String?, Int?) -> Unit
) {
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    val coroutineScope = rememberCoroutineScope()
    val initialPage = Int.MAX_VALUE / 2
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { Int.MAX_VALUE })
    val startMonthMillis = remember { Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1) }.timeInMillis }
    val currentMonth = Calendar.getInstance().apply { 
        timeInMillis = startMonthMillis
        add(Calendar.MONTH, pagerState.currentPage - initialPage)
    }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    if (taskToEdit != null) {
        com.example.todolist.ui.tasklist.AddTaskDialog(
            initialTask = taskToEdit,
            onDismiss = { taskToEdit = null },
            onSave = { title, notes, deadlineMillis, deadlineHasTime, repeatMode, repeatDays, repeatCount ->
                onEditTask(
                    taskToEdit!!.copy(
                        title = title,
                        notes = notes,
                        deadlineMillis = deadlineMillis,
                        deadlineHasTime = deadlineHasTime,
                        repeatMode = repeatMode,
                        repeatDays = repeatDays
                    )
                )
                taskToEdit = null
            }
        )
    }

    if (showAddDialog) {
        com.example.todolist.ui.tasklist.AddTaskDialog(
            initialDateMillis = selectedDate.timeInMillis,
            onDismiss = { showAddDialog = false },
            onSave = { title, notes, deadlineMillis, deadlineHasTime, repeatMode, repeatDays, repeatCount ->
                onAddTask(title, notes, deadlineMillis, deadlineHasTime, repeatMode, repeatDays, repeatCount)
                showAddDialog = false
            }
        )
    }

    val tasksOnSelectedDate = tasks.filter { task ->
        task.deadlineMillis != null && isSameDay(task.deadlineMillis, selectedDate.timeInMillis)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        @OptIn(ExperimentalMaterial3Api::class)
        TopAppBar(
            title = {
                Text(
                    text = "Kalender",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
            }) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Bulan Sebelumnya")
            }
            Text(
                text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(currentMonth.time),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = {
                coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
            }) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Bulan Berikutnya")
            }
        }

        val daysOfWeek = listOf("S", "S", "R", "K", "J", "S", "M")
        Row(modifier = Modifier.fillMaxWidth()) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        HorizontalPager(state = pagerState) { page ->
            val pageMonth = Calendar.getInstance().apply {
                timeInMillis = startMonthMillis
                add(Calendar.MONTH, page - initialPage)
            }
            val daysInMonth = pageMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
            val firstDayOfMonth = Calendar.getInstance().apply {
                timeInMillis = pageMonth.timeInMillis
                set(Calendar.DAY_OF_MONTH, 1)
            }.get(Calendar.DAY_OF_WEEK)
            val daysShift = if (firstDayOfMonth == Calendar.SUNDAY) 6 else firstDayOfMonth - 2

            Column {
                for (week in 0..5) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (dayOfWeek in 0..6) {
                            val dayNumber = week * 7 + dayOfWeek - daysShift + 1
                            if (dayNumber in 1..daysInMonth) {
                                val currentDate = Calendar.getInstance().apply {
                                    timeInMillis = pageMonth.timeInMillis
                                    set(Calendar.DAY_OF_MONTH, dayNumber)
                                    set(Calendar.HOUR_OF_DAY, 0)
                                    set(Calendar.MINUTE, 0)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }

                            val isSelected = isSameDay(currentDate.timeInMillis, selectedDate.timeInMillis)
                            val hasTasks = tasks.any { task ->
                                task.deadlineMillis != null && isSameDay(task.deadlineMillis, currentDate.timeInMillis)
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(4.dp)
                                    .size(40.dp)
                                    .background(
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        selectedDate = currentDate.clone() as Calendar
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = dayNumber.toString(),
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (hasTasks) {
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .background(
                                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                                    shape = CircleShape
                                                )
                                        )
                                    }
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
        }

        androidx.compose.material3.HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tugas: ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(selectedDate.time)}",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Tugas")
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items = tasksOnSelectedDate, key = { it.id }) { task ->
                AnimatedVisibility(
                    visible = true,
                    exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
                ) {
                    TaskItem(
                        task = task,
                        onCheckedChange = { isChecked -> onTaskCheckedChange(task, isChecked) },
                        onDelete = { onDeleteTask(task) },
                        onEdit = { taskToEdit = task }
                    )
                }
            }
        }
    }
}

private fun isSameDay(millis1: Long, millis2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = millis1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = millis2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}