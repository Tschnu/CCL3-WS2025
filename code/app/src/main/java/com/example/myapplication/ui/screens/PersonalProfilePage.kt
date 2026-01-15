package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.viewModel.PersonalViewModel

@Composable
fun PersonalProfilePage(
    viewModel: PersonalViewModel,
    onNavigateBack: () -> Unit
) {
    val profile by viewModel.profile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // ---- Colors used in your design ----
    val bg = Color(0xFFF3E8B8)          // warm beige
    val line = Color(0xFF3D2B1F)        // brown
    val green = Color(0xFF214D18)       // button green

    // ---- TODO: replace these with real ViewModel values when you have them ----
    // If you later expose flows like viewModel.avgCycleLength, etc., just collect them here.
    val avgCycleLength = remember(profile) { 27.6f }     // TODO from ViewModel
    val avgPeriodLength = remember(profile) { 4.3f }     // TODO from ViewModel
    val cycleFluctuations = remember(profile) { 2.1f }   // TODO from ViewModel

    // Manual period length slider (you can store it in DB later if you want)
    var manualPeriodLength by remember { mutableIntStateOf(21) } // TODO load from profile if stored

    // Name editing
    var nameInput by remember(profile?.name) { mutableStateOf(profile?.name ?: "") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ---------- Top bar ----------
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ---------- Title ----------
            Text(
                text = "Quiet Bloom",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.Black,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            Divider(
                color = line,
                thickness = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
            )
            Divider(
                color = line,
                thickness = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp, bottom = 18.dp)
            )

            // ---------- Loading ----------
            if (isLoading) {
                CircularProgressIndicator()
                return@Column
            }

            val currentProfile = profile

            // ---------- Profile section ----------
            // (square image + username)
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .border(2.dp, line, RoundedCornerShape(12.dp))
                    .background(Color.White, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                // If you have a flower image drawable, replace this Icon with Image(painterResource(...))
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = line,
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = currentProfile?.name ?: "Username",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(18.dp))

            // ---------- "Averages in days" header with lines ----------
            Divider(color = line, thickness = 2.dp, modifier = Modifier.fillMaxWidth())
            Text(
                text = "Averages in days",
                style = MaterialTheme.typography.labelLarge,
                color = line,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Divider(color = line, thickness = 2.dp, modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(12.dp))

            // ---------- Average rows ----------
            StatRow(label = "Cycle lenght", value = avgCycleLength, lineColor = line)
            Spacer(modifier = Modifier.height(10.dp))
            StatRow(label = "Average Period", value = avgPeriodLength, lineColor = line)
            Spacer(modifier = Modifier.height(10.dp))
            StatRow(label = "Cycle Fluctuations", value = cycleFluctuations, lineColor = line)

            Spacer(modifier = Modifier.height(16.dp))

            Divider(color = line, thickness = 2.dp, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(6.dp))

            // ---------- Manual edit label ----------
            Text(
                text = "Edit Period length manually",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = line
            )

            Divider(
                color = line,
                thickness = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ---------- Slider number bubble ----------
            Box(
                modifier = Modifier
                    .border(2.dp, line, RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = manualPeriodLength.toString(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ---------- Slider ----------
            Slider(
                value = manualPeriodLength.toFloat(),
                onValueChange = { manualPeriodLength = it.toInt().coerceIn(15, 60) },
                valueRange = 15f..60f,
                steps = 60 - 15 - 1,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(18.dp))

            // ---------- Name edit (optional, but you had it before) ----------
            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = line,
                    unfocusedBorderColor = line,
                    focusedLabelColor = line,
                    unfocusedLabelColor = line,
                    cursorColor = line
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            // ---------- Save button ----------
            Button(
                onClick = {
                    // Save name (existing)
                    viewModel.updateUserName(nameInput)

                    // TODO: save manualPeriodLength if you decide to store it in Profile table
                    // viewModel.updateManualPeriodLength(manualPeriodLength)

                },
                colors = ButtonDefaults.buttonColors(containerColor = green),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .width(180.dp)
                    .height(50.dp)
            ) {
                Text("Save", color = Color.White)
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun StatRow(
    label: String,
    value: Float,
    lineColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF3D2B1F),
            modifier = Modifier.weight(1f)
        )

        Box(
            modifier = Modifier
                .width(78.dp)
                .height(34.dp)
                .border(2.dp, lineColor, RoundedCornerShape(10.dp))
                .background(Color.White, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = String.format("%.1f", value),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black
            )
        }
    }
}
