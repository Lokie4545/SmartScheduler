package com.example.smartscheduler.presentation.today.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.smartscheduler.R
import com.example.smartscheduler.ui.theme.SmartSchedulerTheme

private object FastAddSpacing {
    val Small = 8.dp
    val Large = 16.dp
    val ExtraLarge = 24.dp
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FastAddBottomSheet(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    onSaveDefault: (title: String, description: String) -> Unit,
    titleState: TextFieldState,
    descriptionState: TextFieldState,
    chipsContent: @Composable RowScope.() -> Unit
) {
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.PartiallyExpanded
    )

    ModalBottomSheet(
        modifier = modifier.fillMaxWidth(),
        sheetState = sheetState,
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 0.dp
    ) {
        FastAddBottomSheetContent(
            titleState = titleState,
            descriptionState = descriptionState,
            onSaveDefault = onSaveDefault,
            chipsContent = chipsContent,
            modifier = Modifier
        )
    }
}

@Composable
fun FastAddBottomSheetContent(
    titleState: TextFieldState,
    descriptionState: TextFieldState,
    onSaveDefault: (title: String, description: String) -> Unit,
    chipsContent: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier
) {
    val canSave = remember(titleState) {
        derivedStateOf { titleState.text.isNotBlank() }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = FastAddSpacing.ExtraLarge,
                end = FastAddSpacing.ExtraLarge,
                bottom = FastAddSpacing.ExtraLarge
            ),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(FastAddSpacing.ExtraLarge)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(FastAddSpacing.Large)
        ) {
            SmartBorderlessTextField(
                state = titleState,
                placeholder = "Add title",
                modifier = Modifier
                    .fillMaxWidth(),
                textStyle = MaterialTheme.typography.headlineMedium
            )
            SmartBorderlessTextField(
                state = descriptionState,
                placeholder = "Description",
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.titleMedium
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            chipsContent()

            FastAddSaveButton(
                onClick = {
                    if (!canSave.value) return@FastAddSaveButton
                    onSaveDefault(
                        titleState.text.toString().trim(),
                        descriptionState.text.toString().trim()
                    )
                },
                enabled = canSave.value,
            )
        }
    }
}

@Composable
private fun FastAddSaveButton(
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    FilledIconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.size(48.dp),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Icon(
            painterResource(R.drawable.ic_app_check_small),
            contentDescription = "Save quick add"
        )
    }
}

@Composable
private fun SmartBorderlessTextField(
    state: TextFieldState,
    placeholder: String,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = TextStyle.Default
) {
    val contentTextStyle = textStyle.copy(color = MaterialTheme.colorScheme.onSurface)

    BasicTextField(
        state = state,
        modifier = modifier,
        textStyle = contentTextStyle,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        decorator = { innerTextField ->
            Box {
                if (state.text.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = textStyle.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
                innerTextField()
            }
        }
    )
}

@Preview(name = "Fast add content", widthDp = 393, apiLevel = 35, showBackground = true)
@Composable
private fun FastAddBottomSheetContentPreview() {
    SmartSchedulerTheme(dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
            FastAddBottomSheetContent(
                titleState = rememberTextFieldState(),
                descriptionState = rememberTextFieldState(),
                onSaveDefault = { _, _ -> },
                chipsContent = {

                }
            )
        }
    }
}
