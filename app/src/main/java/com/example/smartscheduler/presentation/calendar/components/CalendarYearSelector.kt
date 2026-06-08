package com.example.smartscheduler.presentation.calendar.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.smartscheduler.R
import com.example.smartscheduler.ui.theme.SmartSchedulerTheme

@Composable
fun CalendarYearSelector(
    year: Int,
    yearOptions: List<Int>,
    onYearSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier.clickable { expanded = true },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = year.toString(),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Icon(
                painter = painterResource(R.drawable.ic_app_arrow_drop_down),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            yearOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.toString()) },
                    onClick = {
                        expanded = false
                        onYearSelected(option)
                    },
                )
            }
        }
    }
}

@Preview(apiLevel = 35, showBackground = true)
@Composable
private fun CalendarYearSelectorPreview() {
    SmartSchedulerTheme(dynamicColor = false) {
        CalendarYearSelector(
            year = 2026,
            yearOptions = (2021..2031).toList(),
            onYearSelected = {},
        )
    }
}
