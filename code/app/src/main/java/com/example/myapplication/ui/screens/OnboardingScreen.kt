package com.example.myapplication.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.ui.navigation.Screen
import com.example.myapplication.ui.theme.Brown
import com.example.myapplication.ui.theme.Softsoftyellow
import com.example.myapplication.viewModel.ProfileViewModel
import com.example.myapplication.R


@Composable
fun OnboardingScreen(
    navController: NavController,
    profileVm: ProfileViewModel = viewModel()
) {
    var step by remember { mutableStateOf(0) }
    var name by remember { mutableStateOf("") }
    var selectedFlower by remember { mutableStateOf<Int?>(null) }

    val canGoNext = when (step) {
        0 -> name.isNotBlank()
        1 -> selectedFlower != null
        else -> true
    }





    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(
                onClick = { step-- },
                enabled = step > 0
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = if (step > 0) Brown else Color.Transparent
                )
            }

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                OnboardingProgressDots(currentStep = step)
            }

            Spacer(modifier = Modifier.width(48.dp))
        }


        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            when (step) {
                0 -> UsernameStep(name = name, onNameChange = { name = it })
                1 -> FlowerStep(selectedFlower = selectedFlower, onSelect = { selectedFlower = it })
                2 -> CalendarInfoStep()
                3 -> CalendarMultiselectStep()
                4 -> AddEntryInfoStep()
                5 -> StatisticsInfoStep()
            }
        }

        // BUTTON
        Button(
            enabled = canGoNext,
            onClick = {
                when (step) {
                    0 -> profileVm.setName(name)
                    1 -> profileVm.setFlowerPicture(selectedFlower!!)
                }

                if (step < 4) step++
                else {
                    profileVm.finishOnboarding()
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Brown),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = if (step == 4) "Finish" else "Next",
                color = Softsoftyellow
            )
        }
    }

}

@Composable
fun UsernameStep(
    name: String,
    onNameChange: (String) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Welcome  to QuietBloom",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(Modifier.height(12.dp))

        Text("First, tell us your name.")

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Your name") },
            singleLine = true
        )
    }
}

@Composable
fun FlowerStep(
    selectedFlower: Int?,
    onSelect: (Int) -> Unit
) {
    val flowers = listOf(
        R.drawable.flower_1,
        R.drawable.flower_2,
        R.drawable.flower_3,
        R.drawable.flower_4,
        R.drawable.flower_5
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Choose your flower 🌼",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(Modifier.height(12.dp))

        Text("This will be your profile symbol.")

        Spacer(Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            flowers.forEachIndexed { index, res ->
                Surface(
                    shape = CircleShape,
                    color = if (selectedFlower == index)
                        Brown.copy(alpha = 0.2f)
                    else
                        Color.Transparent,
                    modifier = Modifier
                        .size(64.dp)
                        .clickable { onSelect(index) }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(res),
                            contentDescription = "Flower $index",
                            modifier = Modifier.size(42.dp)
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun CalendarInfoStep() {
    TutorialStep(
        imageRes = R.drawable.info_info,
        title = "Info-buttons",
        text = "Click on the buttons to learn more about the page or the ."
    )
}

@Composable
fun CalendarMultiselectStep() {
    TutorialStep(
        imageRes = R.drawable.multiselect_info,
        title = "Multiselect-tool",
        text = "With this tool you can select and de-select multiple days at once to add as a period."
    )
}


@Composable
fun StatisticsInfoStep() {
    TutorialStep(
        imageRes = R.drawable.statistics_info,
        title = "Statistics & Analytics",
        text = "On this page you can see different graphs, can go to the journal overview and see other data. \n" +
                "You can also filter through blood flow, pain, mood and energy level to see correlation throughout the month."
    )
}


@Composable
fun AddEntryInfoStep() {
    TutorialStep(
        imageRes = R.drawable.addentry_info,
        title = "Add Entry",
        text = "Log your mood, pain, energy and notes for the day. \n" +
                "You can also add an entry to any day prior to the current day."
    )
}



@Composable
fun TutorialStep(
    @DrawableRes imageRes: Int,
    title: String,
    text: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Image(
            painter = painterResource(imageRes),
            contentDescription = title,
            modifier = Modifier
                .fillMaxWidth()
                .height(450.dp)
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun OnboardingProgressDots(
    currentStep: Int,
    totalSteps: Int = 6
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(totalSteps) { index ->
            Box(
                modifier = Modifier
                    .size(if (index == currentStep) 10.dp else 8.dp)
                    .background(
                        color = if (index == currentStep)
                            Brown
                        else
                            Brown.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
            )

            if (index < totalSteps - 1) {
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}
