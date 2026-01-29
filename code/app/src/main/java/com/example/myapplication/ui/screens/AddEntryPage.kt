package com.example.myapplication.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.ui.theme.Brown
import com.example.myapplication.ui.theme.RedDark
import com.example.myapplication.ui.theme.Softsoftyellow
import com.example.myapplication.viewModel.EntryViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.material.icons.automirrored.filled.ArrowBack


@Composable
fun AddEntryHeader(
    date: LocalDate,
    onBack: () -> Unit
) {
    val formattedDate = remember(date) {
        date.format(
            DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())
        )
    }

    var backEnabled by remember { mutableStateOf(true) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            modifier = Modifier
                .size(28.dp)
                .clickable(enabled = backEnabled) {
                    backEnabled = false
                    onBack()
                },
            tint = if (backEnabled) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "Entry for $formattedDate",
            style = MaterialTheme.typography.titleLarge
        )
    }
}



@Composable
fun AddEntryPage(
    date: String,
    viewModel: EntryViewModel,
    onNavigateBack: () -> Unit
) {
    val localDate = remember(date) { LocalDate.parse(date) }
    var showDeleteDialog by remember { mutableStateOf(false) }


    LaunchedEffect(localDate) {
        viewModel.loadEntryForDate(localDate)
    }

    val bloodflow by viewModel.bloodflowCategory.collectAsState()
    val pain by viewModel.painCategory.collectAsState()
    val energy by viewModel.energyCategory.collectAsState()
    val mood by viewModel.moodCategory.collectAsState()
    val journal by viewModel.journalText.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
//        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AddEntryHeader(
            date = localDate,
            onBack = onNavigateBack
        )

        Spacer(Modifier.height(16.dp))

        HorizontalDivider(color = Brown, thickness = 2.dp)

        SectionTitle(
            text = "Blood Flow",
            infoText = "Here you can select how strong your bleeding was this day.\nChoose between: no bleeding, light flow, medium flow and heavy flow.")
        HorizontalDivider(color = Brown, thickness = 2.dp)

        Spacer(Modifier.height(16.dp))

        ValueImageRow(
            items = listOf(
                R.drawable.nothing to 0,
                R.drawable.splatter_light to 1,
                R.drawable.splatter_medium to 2,
                R.drawable.splatter_heavy to 3
            ),
            selectedValue = bloodflow,
            onSelect = viewModel::setBloodflowCategory
        )



        Spacer(Modifier.height(16.dp))

        HorizontalDivider(color = Brown, thickness = 2.dp)

        SectionTitle(
            text = "Pain",
            infoText = "Here you can fill in how strong your cramps were."
        )

        HorizontalDivider(color = Brown, thickness = 2.dp)

        Spacer(Modifier.height(16.dp))
        ValueImageRow(
            items = listOf(
                R.drawable.nothing to 0,
                R.drawable.little_pain to 1,
                R.drawable.moderate_pain to 2,
                R.drawable.big_pain to 3,
                R.drawable.very_big_pain to 4
            ),
            selectedValue = pain,
            onSelect = viewModel::setPainCategory
        )

        Spacer(Modifier.height(16.dp))

        HorizontalDivider(color = Brown, thickness = 2.dp)

        SectionTitle(
            text = "Energy Level",
            infoText = "Enter here how your overall energy level was today."
        )

        HorizontalDivider(color = Brown, thickness = 2.dp)

        Spacer(Modifier.height(16.dp))
        ValueImageRow(
            items = listOf(
                R.drawable.no_energy to 0,
                R.drawable.little_energy to 1,
                R.drawable.middle_energy to 2,
                R.drawable.little_less_energy to 3,
                R.drawable.full_energy to 4
            ),
            selectedValue = energy,
            onSelect = viewModel::setEnergyCategory
        )

        Spacer(Modifier.height(16.dp))

        HorizontalDivider(color = Brown, thickness = 2.dp)

        SectionTitle(
            text = "Mood",
            infoText = "How was your mood today?"
        )

        HorizontalDivider(color = Brown, thickness = 2.dp)

        Spacer(Modifier.height(16.dp))

        MoodImageRow(
            items = listOf(
                R.drawable.awful to 0,
                R.drawable.bad to 1,
                R.drawable.okay to 2,
                R.drawable.happy to 3,
                R.drawable.veryhappy to 4
            ),
            selectedValue = mood,
            onSelect = viewModel::setMoodCategory
        )


        Spacer(Modifier.height(16.dp))

        HorizontalDivider(color = Brown, thickness = 2.dp)

        SectionTitle(
            text = "Journal",
            infoText = "Use this space to reflect.\n" +
                    "Write about how you felt today, what you’re grateful for, or anything you want to remember."


        )

        HorizontalDivider(color = Brown, thickness = 2.dp)

        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = journal,
            onValueChange = viewModel::setJournalText,
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width =2.dp,
                    color = Brown,
                    shape = RoundedCornerShape(12.dp)
                )
                .height(140.dp),

            placeholder = { Text("How do you feel today?")},
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(Modifier.height(16.dp))

        val hasInput =
            journal.isNotBlank()
                    || bloodflow != 0
                    || pain != 0
                    || energy != -1
                    || mood != -1

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LEFT: Trash (only show if there is something to delete)
            if (hasInput) {
                IconButton(
                    onClick = { showDeleteDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete entry",
                        tint = Brown
                    )
                }
            } else {
                // keeps spacing so Save stays on the right
                Spacer(Modifier.size(48.dp))
            }

            // RIGHT: Save
            Button(
                onClick = {
                    viewModel.saveEntry()
                    onNavigateBack()
                }
            ) {
                Text(text = "Save", color = Softsoftyellow)
            }
        }

    }


    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = Softsoftyellow, // 👈 dialog background
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "Delete entry?",
                    style = MaterialTheme.typography.titleMedium,
                    color = Brown
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete this entry? This can’t be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Brown
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteCurrentEntry()
                        onNavigateBack()
                    }
                ) {
                    Text(
                        "Delete",
                        style = MaterialTheme.typography.labelLarge,
                        color = RedDark
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text(
                        "Cancel",
                        style = MaterialTheme.typography.labelLarge,
                        color = Brown
                    )
                }
            }
        )
    }



}



@Composable
fun MoodImageRow(
    items: List<Pair<Int, Int>>,
    selectedValue: Int,
    onSelect: (Int) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items.forEach { (imageRes, value) ->
            Box(
                modifier = Modifier
                    .size(56.dp) // SAME size as other categories
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .border(
                        width = 2.dp,
                        color = if (value == selectedValue) Brown else Color.LightGray,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onSelect(value) },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(imageRes),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp) // 👈 SMALLER icon
                )
            }
        }
    }
}




@Composable
fun SectionTitle(
    text: String,
    infoText: String? = null
) {
    var showInfo by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 0.dp, horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(vertical = 0.dp, horizontal = 4.dp)
        )

        Spacer(modifier = Modifier.weight(1f))
        if (infoText != null) {
            IconButton(
                onClick = { showInfo = true },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Info",
                    tint = Brown
                )
            }
        }
    }

    if (showInfo && infoText != null) {
        StyledInfoDialog(
            title = text,
            message = infoText,
            onDismiss = { showInfo = false }
        )
    }

}

@Composable
fun ValueImageRow(
    items: List<Pair<Int, Int>>,
    selectedValue: Int,
    onSelect: (Int) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items.forEach { (imageRes, value) ->
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .border(
                        width = if (value == selectedValue) 2.dp else 2.dp,
                        color = if (value == selectedValue) Brown else Color.LightGray,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onSelect(value) },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(imageRes),
                    contentDescription = null,
                    modifier = Modifier.size(50.dp)
                )
            }
        }
    }
}



@Composable
fun StyledInfoDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    confirmText: String = "OK"
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Softsoftyellow,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Brown
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Brown
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Brown)
            ) {
                Text(confirmText, color = Softsoftyellow)
            }
        }
    )
}



