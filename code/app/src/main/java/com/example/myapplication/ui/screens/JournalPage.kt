package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.db.dailyEntry.DailyEntryEntity
import com.example.myapplication.ui.navigation.Screen
import com.example.myapplication.viewModel.EntryViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun JournalPage(
    navController: NavController,
    viewModel: EntryViewModel
) {
    val entries by viewModel.journalEntries.collectAsState()

    if (entries.isEmpty()) {
        Text(
            "No journal entries yet ✨",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(24.dp)
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(entries) { entry ->
            JournalEntryCard(
                entry = entry,
                onEdit = {
                    val dateString = Instant.ofEpochMilli(entry.date)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                        .toString()

                    navController.navigate(
                        Screen.AddEntry.createRoute(dateString)
                    )
                },
                onDelete = {
                    viewModel.deleteEntry(entry)
                }
            )
        }
    }
}

@Composable
fun JournalEntryCard(
    entry: DailyEntryEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val formattedDate = remember(entry.date) {
        Instant.ofEpochMilli(entry.date)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = entry.journalText.ifBlank { "— no journal entry —" },
                style = MaterialTheme.typography.bodyMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}
