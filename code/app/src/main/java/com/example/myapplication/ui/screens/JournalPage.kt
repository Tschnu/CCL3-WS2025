package com.example.myapplication.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.myapplication.ui.theme.Softsoftyellow
import com.example.myapplication.viewModel.EntryViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter


@Composable
fun JournalHeader(
    onBack: () -> Unit
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ){
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Back",
            modifier = Modifier
                .size(28.dp)
                .clickable{onBack()}
        )
        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "Journal Entries",
            style = MaterialTheme.typography.titleLarge
        )
    }
}
@Composable
fun JournalPage(
    navController: NavController,
    viewModel: EntryViewModel
) {
    val entries by viewModel.journalEntries.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        JournalHeader(
            onBack = { navController.popBackStack() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (entries.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No journal entries yet ✨",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
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
        colors = CardDefaults.cardColors(
            containerColor = Softsoftyellow
        ),
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
            }
        }
    }
}
