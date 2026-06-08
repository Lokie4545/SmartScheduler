package com.example.smartscheduler.presentation.calendar.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.smartscheduler.R
import com.example.smartscheduler.presentation.calendar.CalendarMonthChipUiModel
import com.example.smartscheduler.ui.theme.SmartSchedulerTheme
import java.time.Month

@Composable
fun CalendarMonthChipRow(
    months: List<CalendarMonthChipUiModel>,
    onMonthClick: (Month) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(months, key = { it.month.value }) { month ->
            FilterChip(
                selected = month.selected,
                onClick = { onMonthClick(month.month) },
                label = {
                    Text(
                        text = month.label,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    )
                },
                leadingIcon = if (month.selected) {
                    {
                        Icon(
                            painter = painterResource(R.drawable.ic_app_check_small),
                            contentDescription = null,
                        )
                    }
                } else {
                    null
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        }
    }
}

@Preview(apiLevel = 35, showBackground = true)
@Composable
private fun CalendarMonthChipRowPreview() {
    val selectedMonth = Month.APRIL
    SmartSchedulerTheme(dynamicColor = false) {
        CalendarMonthChipRow(
            months = Month.values().map { month ->
                CalendarMonthChipUiModel(
                    month = month,
                    label = month.name.lowercase().replaceFirstChar { it.uppercase() },
                    selected = month == selectedMonth,
                )
            },
            onMonthClick = {},
        )
    }
}
