package com.example.smartscheduler.presentation.today.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.smartscheduler.R
import com.example.smartscheduler.domain.model.TimeSlot
import com.example.smartscheduler.ui.theme.SmartSchedulerTheme



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FastAddBottomSheet(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    onSaveDefault: (title: String, description: String) -> Unit,
    titleState: TextFieldState,
    descriptionState: TextFieldState,
    chipsContent: @Composable () -> Unit
) {
    val sheetState = rememberBottomSheetState(SheetValue.PartiallyExpanded)

    ModalBottomSheet(
        modifier = modifier.fillMaxHeight(),
        sheetState = sheetState,
        onDismissRequest = onDismissRequest
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {

            SmartBorderlessTextField(
                state = titleState,
                placeholder = "Add title",
                textStyle = MaterialTheme.typography.headlineSmallEmphasized
            )
            SmartBorderlessTextField(
                state = descriptionState,
                placeholder = "Description",
                textStyle = MaterialTheme.typography.labelLarge
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            )
            {
                chipsContent()
                FilledIconButton(
                    modifier = Modifier.size(40.dp),
                    onClick = {
                        onSaveDefault(
                            titleState.text.toString(),
                            descriptionState.text.toString()
                        )
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        painterResource(R.drawable.ic_app_check_small),
                        contentDescription = null
                    )
                }
            }

        }

    }
}

@Preview(apiLevel = 34)
@Composable
private fun FastAddTaskBottomSheetPreview() {
    SmartSchedulerTheme() {

    }
}


@Composable
private fun SmartBorderlessTextField(
    state: TextFieldState,
    placeholder: String,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = TextStyle.Default
) {
    BasicTextField(
        state = state,
        modifier = modifier,
        textStyle = textStyle,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        decorator = { innerTextField ->
            Box {
                if (state.text.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = textStyle.copy(color = MaterialTheme.colorScheme.secondary)
                    )
                }
                innerTextField()
            }
        }
    )
}