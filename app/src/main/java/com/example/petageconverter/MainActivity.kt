@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.petageconverter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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
    var pickedDate  by remember { mutableStateOf<LocalDate?>(null) }
    var showPicker  by remember { mutableStateOf(false) }
    var approxYears by remember { mutableStateOf(0f) }
    var result      by remember { mutableStateOf("") }

    /* ---------- date-picker dialog ---------- */
    if (showPicker) {
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = { TextButton({ showPicker = false }) { Text("OK") } }
        ) {
            val state = rememberDatePickerState()
            DatePicker(state)
            LaunchedEffect(state.selectedDateMillis) {
                state.selectedDateMillis?.let {
                    pickedDate = Instant.ofEpochMilli(it)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                }
            }
        }
    }

    /* ---------- background + UI ---------- */
    Box(Modifier.fillMaxSize()) {

        Image(
            painter = painterResource(R.drawable.cc),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        // dim the photo for contrast
        Box(
            Modifier
                .matchParentSize()
                .background(
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.05f)
                )
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

            /* slider */
            Text(
                text  = "Approximate age: ${"%.0f".format(approxYears)} yr",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary      // ← colored label
            )

            Slider(
                value = approxYears,
                onValueChange = { approxYears = it.roundToInt().toFloat() },
                valueRange = 0f..30f,
                steps = 0,
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
                ) { Text("🐶") }

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
                ) { Text("🐱") }
            }

            Spacer(Modifier.height(16.dp))

            Text(result, style = MaterialTheme.typography.headlineSmall)
        }
    }
}

/* ---------- helpers ---------- */
private fun convert(
    birthDate: LocalDate?,
    approxYears: Double,
    toPet: (Double) -> Double
): String {
    val humanYears = birthDate?.let { bd ->
        ChronoUnit.MONTHS.between(
            bd.withDayOfMonth(1),
            LocalDate.now().withDayOfMonth(1)
        ) / 12.0
    } ?: approxYears

    return "≈ %.1f pet years".format(toPet(humanYears))
}

private fun toDogYears(h: Double) =
    if (h <= 2) h * 10 else 20 + (h - 2) * 4

private fun toCatYears(h: Double) = when {
    h < 1  -> h * 15
    h < 2  -> 15 + (h - 1) * 9
    else   -> 24 + (h - 2) * 4
}
