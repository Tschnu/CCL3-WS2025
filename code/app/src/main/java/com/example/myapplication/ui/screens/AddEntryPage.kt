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
    onNavigateBack: () -> Unit
) {
    val localDate = remember(date) { LocalDate.parse(date) }

    LaunchedEffect(localDate) {
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
        ValueImageRow(
            items = listOf(
                R.drawable.nothing to 0,
                R.drawable.big_blood_full to 3,
                R.drawable.middle_blood_full to 2,
                R.drawable.little_blood_full to 1
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

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.saveEntry()
                onNavigateBack()
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

/**
 * Row that decouples UI order from stored values.
 * items = listOf(imageRes to storedValue)
 */
@Composable
fun ValueImageRow(
    items: List<Pair<Int, Int>>,
    selectedValue: Int,
    onSelect: (Int) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items.forEach { (imageRes, value) ->
            Image(
                painter = painterResource(imageRes),
                contentDescription = null,
                modifier = Modifier
                    .size(56.dp)
                    .border(
                        width = if (value == selectedValue) 2.dp else 0.dp,
                        color = Color.Black,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onSelect(value) }
            )
        }
    }
}
