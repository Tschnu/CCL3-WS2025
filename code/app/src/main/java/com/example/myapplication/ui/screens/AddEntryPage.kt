package com.example.myapplication.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.viewModel.EntryViewModel
import java.time.LocalDate

@Composable
fun AddEntryPage(
    date: String,
    viewModel: EntryViewModel,
    onNavigateBack: () -> Unit  // Add this parameter
) {
    val localDate = remember(date) { LocalDate.parse(date) }

    LaunchedEffect(Unit) {
        viewModel.loadEntryForDate(localDate)
    }

    val bloodflow by viewModel.bloodflowCategory.collectAsState()
    val pain by viewModel.painCategory.collectAsState()
    val energy by viewModel.energyCategory.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        Text("Add Entry", style = MaterialTheme.typography.titleMedium)
        Text(date, style = MaterialTheme.typography.bodyMedium)

        SectionTitle("Blood Flow")
        ImageRow(
            images = listOf(
                R.drawable.big_blood_full,
                R.drawable.middle_blood_full,
                R.drawable.little_blood_full,
                R.drawable.nothing
            ),
            selectedIndex = bloodflow,
            onSelect = viewModel::setBloodflowCategory
        )

        SectionTitle("Pain")
        ImageRow(
            images = listOf(
                R.drawable.very_big_pain,
                R.drawable.big_pain,
                R.drawable.moderate_pain,
                R.drawable.little_pain,
                R.drawable.nothing
            ),
            selectedIndex = pain,
            onSelect = viewModel::setPainCategory
        )

        SectionTitle("Energy Level")
        ImageRow(
            images = listOf(
                R.drawable.full_energy,
                R.drawable.little_less_energy,
                R.drawable.middle_energy,
                R.drawable.little_energy,
                R.drawable.no_energy
            ),
            selectedIndex = energy,
            onSelect = viewModel::setEnergyCategory
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.saveEntry()
                onNavigateBack()  // Navigate back after saving
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Save")
        }
    }
}


@Composable
fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge)
}

@Composable
fun ImageRow(
    images: List<Int>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        images.forEachIndexed { index, res ->
            Image(
                painter = painterResource(res),
                contentDescription = null,
                modifier = Modifier
                    .size(56.dp)
                    .border(
                        width = if (index == selectedIndex) 2.dp else 0.dp,
                        color = Color.Black,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onSelect(index) }
            )
        }
    }
}

