package com.example.myapplication.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.myapplication.R

@Composable
fun AddEntryPage(date: String) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        Text(
            text = "Add Entry",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = date,
            style = MaterialTheme.typography.bodyMedium
        )

        SectionTitle("Blood Flow")
        ImageRow(
            images = listOf(
                R.drawable.big_blood_full,
                R.drawable.middle_blood_full,
                R.drawable.little_blood_full,
                R.drawable.nothing
            )
        )

        SectionTitle("Pain")
        ImageRow(
            images = listOf(
                R.drawable.very_big_pain,
                R.drawable.big_pain,
                R.drawable.moderate_pain,
                R.drawable.little_pain,
                R.drawable.nothing
            )
        )

        SectionTitle("Energy Level")
        ImageRow(
            images = listOf(
                R.drawable.full_energy,
                R.drawable.little_less_energy,
                R.drawable.middle_energy,
                R.drawable.little_energy,
                R.drawable.no_energy
            )
        )

        SectionTitle("Notes")
        OutlinedTextField(
            value = "",
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            placeholder = { Text("I am in pain…") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { /* DB kommt später */ },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Save")
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
fun ImageRow(images: List<Int>) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        images.forEach { res ->
            Image(
                painter = painterResource(res),
                contentDescription = null,
                modifier = Modifier
                    .size(56.dp)
                    .clickable { }
            )
        }
    }
}
