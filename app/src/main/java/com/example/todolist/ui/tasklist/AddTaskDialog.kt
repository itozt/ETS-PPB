package com.example.todolist.ui.tasklist

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.PaddingValues
import com.example.todolist.R
import com.example.todolist.domain.model.Task
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun AddTaskDialog(
    initialTask: Task? = null,
    initialDateMillis: Long? = null,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        notes: String?,
        deadlineMillis: Long?,
        deadlineHasTime: Boolean,
        repeatMode: com.example.todolist.domain.model.RepeatMode,
        repeatDays: String?,
        repeatCount: Int?
    ) -> Unit
) {
    var title by remember { mutableStateOf(initialTask?.title ?: "") }
    var notes by remember { mutableStateOf(initialTask?.notes ?: "") }
    var selectedDateMillis by remember { mutableStateOf<Long?>(initialTask?.deadlineMillis ?: initialDateMillis) }
    var selectedHourMinute by remember { mutableStateOf<Pair<Int, Int>?>(
        initialTask?.deadlineMillis?.takeIf { initialTask.deadlineHasTime }?.let {
            val cal = Calendar.getInstance().apply { timeInMillis = it }
            Pair(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
        }
    ) }
    var repeatMode by remember { mutableStateOf(initialTask?.repeatMode ?: com.example.todolist.domain.model.RepeatMode.NONE) }
    var customDays by remember { mutableStateOf<Set<String>>(
        initialTask?.repeatDays?.split(",")?.filter { it.isNotBlank() }?.toSet() ?: emptySet()
    ) }
    var repeatDropdownExpanded by remember { mutableStateOf(false) }
    var repeatCountText by remember { mutableStateOf("") }

    val horizontalScrollState = remember { ScrollState(0) }

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val locale = Locale.forLanguageTag("id-ID")
    val addTitleLabel = if (initialTask == null) stringResource(id = R.string.dialog_tambah_tugas) else "Edit Tugas"
    val titleLabel = stringResource(id = R.string.judul)
    val notesLabel = stringResource(id = R.string.catatan_opsional)
    val dateLabel = stringResource(id = R.string.tanggal)
    val timeLabel = stringResource(id = R.string.jam)
    val notSelectedLabel = stringResource(id = R.string.belum_dipilih)
    val optionalLabel = stringResource(id = R.string.opsional)
    val pickDateLabel = stringResource(id = R.string.pilih_tanggal)
    val pickTimeLabel = stringResource(id = R.string.pilih_jam)
    val clearDeadlineLabel = stringResource(id = R.string.hapus_deadline)
    val saveLabel = stringResource(id = R.string.simpan)
    val cancelLabel = stringResource(id = R.string.batal)

    val dateText = remember(selectedDateMillis) {
        selectedDateMillis?.let { millis ->
            SimpleDateFormat("dd MMM yyyy", locale).format(millis)
        } ?: notSelectedLabel
    }

    val timeText = selectedHourMinute?.let { (hour, minute) ->
        String.format(locale, "%02d:%02d", hour, minute)
    } ?: optionalLabel

    val daysOfWeekMap = remember {
        mapOf(
            Calendar.SUNDAY to "Sun",
            Calendar.MONDAY to "Mon",
            Calendar.TUESDAY to "Tue",
            Calendar.WEDNESDAY to "Wed",
            Calendar.THURSDAY to "Thu",
            Calendar.FRIDAY to "Fri",
            Calendar.SATURDAY to "Sat"
        )
    }

    val daysOfWeekIndonesian = remember {
        mapOf(
            "Sun" to "Minggu",
            "Mon" to "Senin",
            "Tue" to "Selasa",
            "Wed" to "Rabu",
            "Thu" to "Kamis",
            "Fri" to "Jumat",
            "Sat" to "Sabtu"
        )
    }

    LaunchedEffect(selectedDateMillis, repeatMode) {
        if (repeatMode == com.example.todolist.domain.model.RepeatMode.CUSTOM_DAYS && selectedDateMillis != null) {
            val cal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis!! }
            val dayStr = daysOfWeekMap[cal.get(Calendar.DAY_OF_WEEK)]
            if (dayStr != null && !customDays.contains(dayStr)) {
                customDays = customDays + dayStr
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = addTitleLabel) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(text = titleLabel) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(text = notesLabel) },
                    modifier = Modifier.fillMaxWidth()
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = "$dateLabel: $dateText")
                    Text(text = "$timeLabel: $timeText")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = {
                            val calendar = Calendar.getInstance()
                            selectedDateMillis?.let { calendar.timeInMillis = it }
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    val picked = Calendar.getInstance().apply {
                                        set(year, month, dayOfMonth, 0, 0, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }
                                    selectedDateMillis = picked.timeInMillis
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }) {
                            Text(text = pickDateLabel)
                        }

                        Button(
                            enabled = selectedDateMillis != null,
                            onClick = {
                                val now = Calendar.getInstance()
                                TimePickerDialog(
                                    context,
                                    { _, hourOfDay, minute ->
                                        selectedHourMinute = hourOfDay to minute
                                    },
                                    now.get(Calendar.HOUR_OF_DAY),
                                    now.get(Calendar.MINUTE),
                                    true
                                ).show()
                            }
                        ) {
                            Text(text = pickTimeLabel)
                        }
                    }

                    Button(
                        onClick = {
                            selectedDateMillis = null
                            selectedHourMinute = null
                        }
                    ) {
                        Text(text = clearDeadlineLabel)
                    }

                    if (selectedDateMillis != null && initialTask == null) {
                        androidx.compose.material3.HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Text(text = "Ulangi:", style = androidx.compose.material3.MaterialTheme.typography.labelLarge)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            androidx.compose.material3.OutlinedButton(
                                onClick = { repeatDropdownExpanded = true }
                            ) {
                                val modeTranslate = when (repeatMode) {
                                    com.example.todolist.domain.model.RepeatMode.NONE -> "Tidak"
                                    com.example.todolist.domain.model.RepeatMode.DAILY -> "Harian"
                                    com.example.todolist.domain.model.RepeatMode.WEEKLY -> "Mingguan"
                                    com.example.todolist.domain.model.RepeatMode.MONTHLY -> "Bulanan"
                                    com.example.todolist.domain.model.RepeatMode.CUSTOM_DAYS -> "Kustom"
                                }
                                Text(text = modeTranslate)
                            }

                            androidx.compose.material3.DropdownMenu(
                                expanded = repeatDropdownExpanded,
                                onDismissRequest = { repeatDropdownExpanded = false }
                            ) {
                                com.example.todolist.domain.model.RepeatMode.values().forEach { mode ->
                                    val modeName = when (mode) {
                                        com.example.todolist.domain.model.RepeatMode.NONE -> "Tidak"
                                        com.example.todolist.domain.model.RepeatMode.DAILY -> "Harian"
                                        com.example.todolist.domain.model.RepeatMode.WEEKLY -> "Mingguan"
                                        com.example.todolist.domain.model.RepeatMode.MONTHLY -> "Bulanan"
                                        com.example.todolist.domain.model.RepeatMode.CUSTOM_DAYS -> "Kustom"
                                    }
                                    androidx.compose.material3.DropdownMenuItem(
                                        text = { Text(text = modeName) },
                                        onClick = {
                                            repeatMode = mode
                                            repeatDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        if (repeatMode != com.example.todolist.domain.model.RepeatMode.NONE) {
                            OutlinedTextField(
                                value = repeatCountText,
                                onValueChange = { repeatCountText = it },
                                label = { Text(text = "Berapa kali?") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                        }

                        if (repeatMode == com.example.todolist.domain.model.RepeatMode.CUSTOM_DAYS) {
                            val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                            val dayInitials = listOf("S", "S", "R", "K", "J", "S", "M")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                daysOfWeek.forEachIndexed { index, day ->
                                    val isSelected = customDays.contains(day)
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(
                                                color = if (isSelected) androidx.compose.material3.MaterialTheme.colorScheme.primary else androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant,
                                                shape = CircleShape
                                            )
                                            .clickable {
                                                if (isSelected) {
                                                    val newCustomDays = customDays - day
                                                    customDays = newCustomDays

                                                    selectedDateMillis?.let { dateMillis ->
                                                        val cal = Calendar.getInstance().apply { timeInMillis = dateMillis }
                                                        val currentDayStr = daysOfWeekMap[cal.get(Calendar.DAY_OF_WEEK)]

                                                        if (currentDayStr == day && newCustomDays.isNotEmpty()) {
                                                            val searchCal = Calendar.getInstance().apply { timeInMillis = dateMillis }
                                                            for (i in 0..6) {
                                                                if (newCustomDays.contains(daysOfWeekMap[searchCal.get(Calendar.DAY_OF_WEEK)])) {
                                                                    selectedDateMillis = searchCal.timeInMillis
                                                                    break
                                                                }
                                                                searchCal.add(Calendar.DAY_OF_YEAR, 1)
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    customDays = customDays + day
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = dayInitials[index],
                                            color = if (isSelected) androidx.compose.material3.MaterialTheme.colorScheme.onPrimary else androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = title.isNotBlank(),
                onClick = {
                    val deadlineMillis = selectedDateMillis?.let { dateMillis ->
                        val calendar = Calendar.getInstance().apply {
                            timeInMillis = dateMillis
                            val time = selectedHourMinute
                            set(Calendar.HOUR_OF_DAY, time?.first ?: 0)
                            set(Calendar.MINUTE, time?.second ?: 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        calendar.timeInMillis
                    }
                    val deadlineHasTime = selectedDateMillis != null && selectedHourMinute != null
                    val repeatDaysStr = if (repeatMode == com.example.todolist.domain.model.RepeatMode.CUSTOM_DAYS) {
                        customDays.joinToString(",")
                    } else null
                    
                    val repeatCountNum = repeatCountText.toIntOrNull()

                    onSave(
                        title,
                        notes.ifBlank { null },
                        deadlineMillis,
                        deadlineHasTime,
                        repeatMode,
                        repeatDaysStr,
                        repeatCountNum
                    )
                }
            ) {
                Text(text = saveLabel)
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text(text = cancelLabel)
            }
        }
    )
}