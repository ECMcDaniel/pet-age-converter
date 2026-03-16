@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.petageconverter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.petageconverter.ui.theme.PetAgeConverterTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PetAgeConverterTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PetAgeConverterScreen()
                }
            }
        }
    }
}

@Composable
fun PetAgeConverterScreen() {

    /* ---------- state ---------- */
    var pickedDate      by remember { mutableStateOf<LocalDate?>(null) }
    var showPicker      by remember { mutableStateOf(false) }
    var approxYears     by remember { mutableFloatStateOf(0f) }
    var result          by remember { mutableStateOf("") }
    val datePickerState =  rememberDatePickerState()

    /* ---------- date-picker dialog ---------- */
    if (showPicker) {
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text("OK")
                }
            }
        ) {
            DatePicker(datePickerState)
            LaunchedEffect(datePickerState.selectedDateMillis) {
                datePickerState.selectedDateMillis?.let {
                    pickedDate = Instant.ofEpochMilli(it)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                }
            }
        }
    }

    /* ---------- UI ---------- */
    Box(Modifier.fillMaxSize()) {

        // Background color
        Box(
            Modifier
                .matchParentSize()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        )

        /* foreground controls */
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            /* calendar row */
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = { showPicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        pickedDate?.let {
                            "📅  ${it.month.name.lowercase()
                                .replaceFirstChar(Char::uppercase)} ${it.year}"
                        } ?: "Pick birth date"
                    )
                }

                if (pickedDate != null) {
                    IconButton(onClick = { pickedDate = null }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear date")
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            /* slider — disabled when an exact birthdate is chosen */
            Text(
                text  = "Pet's chronological age: ${"%.0f".format(approxYears)} yr",
                style = MaterialTheme.typography.titleMedium,
                color = if (pickedDate != null)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.38f)
                else
                    MaterialTheme.colorScheme.primary
            )

            Slider(
                value = approxYears,
                onValueChange = { approxYears = it.roundToInt().toFloat() },
                valueRange = 0f..30f,
                steps = 29,
                enabled = pickedDate == null,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    activeTrackColor   = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.24f),
                    thumbColor         = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(Modifier.height(24.dp))

            /* dog / cat buttons */
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Button(
                    onClick = {
                        result = convert(pickedDate, approxYears.toDouble(), ::toDogYears)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6F8BCD),
                        contentColor   = Color.White
                    )
                ) { Text("🐶 Dog Years") }

                Button(
                    onClick = {
                        result = convert(pickedDate, approxYears.toDouble(), ::toCatYears)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFB57EDC),
                        contentColor   = Color.White
                    )
                ) { Text("🐱 Cat Years") }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                result,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

/* ---------- helpers ---------- */

/**
 * Common standard for Pet to Human age conversion:
 * 1st year ≈ 15 human years
 * 2nd year ≈ 24 human years (total)
 *
 * Dogs (Average size): +5 human years per year after age 2
 * Cats: +4 human years per year after age 2
 */
private fun convert(
    birthDate: LocalDate?,
    approxYears: Double,
    toHumanEquivalent: (Double) -> Double
): String {
    val chronologicalAge = birthDate?.let { bd ->
        ChronoUnit.DAYS.between(bd, LocalDate.now()) / 365.25
    } ?: approxYears

    val humanAge = toHumanEquivalent(chronologicalAge)
    
    return if (birthDate != null) {
        "= ${humanAge.roundToInt()} human years"
    } else {
        "≈ %.1f human years".format(humanAge)
    }
}

private fun toDogYears(petAge: Double) = when {
    petAge <= 0 -> 0.0
    petAge <= 1 -> petAge * 15
    petAge <= 2 -> 15 + (petAge - 1) * 9
    else        -> 24 + (petAge - 2) * 5
}

private fun toCatYears(petAge: Double) = when {
    petAge <= 0 -> 0.0
    petAge <= 1 -> petAge * 15
    petAge <= 2 -> 15 + (petAge - 1) * 9
    else        -> 24 + (petAge - 2) * 4
}
