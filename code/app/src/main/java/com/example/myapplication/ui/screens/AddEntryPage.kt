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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.ui.theme.Brown
import com.example.myapplication.ui.theme.RedDark
import com.example.myapplication.ui.theme.Softsoftyellow
import com.example.myapplication.viewModel.EntryViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale


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
            imageVector = Icons.Default.ArrowBack,
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
            text = "Add entry for $formattedDate",
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
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        AddEntryHeader(
            date = localDate,
            onBack = onNavigateBack
        )

        SectionTitle("Blood Flow")
        ValueImageRow(
            items = listOf(
                R.drawable.nothing to 0,
                R.drawable.little_blood_full to 1,
                R.drawable.middle_blood_full to 2,
                R.drawable.big_blood_full to 3
            ),
            selectedValue = bloodflow,
            onSelect = viewModel::setBloodflowCategory
        )

        SectionTitle("Pain")
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

        SectionTitle("Energy Level")
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

        SectionTitle("Mood")
        MoodBar(
            moods = listOf(
                R.drawable.awful,
                R.drawable.bad,
                R.drawable.okay,
                R.drawable.happy,
                R.drawable.veryhappy
            ),
            selectedValue = mood,
            onSelect = viewModel::setMoodCategory
        )

        SectionTitle("Journal")
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

        Button(
            onClick = {
                viewModel.saveEntry()
                onNavigateBack()
            },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        ) {
            Text(text = "Save", color = Softsoftyellow)
        }
        if (journal.isNotBlank()
            || bloodflow != 0
            || pain != 0
            || energy != 0
            || mood != 0
        ) {
            Button(
                onClick = {
                    viewModel.deleteCurrentEntry()
                    onNavigateBack()
                },
                colors = ButtonDefaults.buttonColors(
                    contentColor = RedDark
                ),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
            ) {
                Text(text = "Delete entry", color = Softsoftyellow)
            }
        }

    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge
    )
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
fun MoodBar(
    moods: List<Int>,
    selectedValue: Int,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(2.dp, Brown, RoundedCornerShape(12.dp))
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        moods.forEachIndexed { index, res ->
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = if (index == selectedValue) Color(0xFFEFECE5) else Color.Transparent,
                        shape = RoundedCornerShape(50)
                    )
                    .clickable { onSelect(index) },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(res),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
