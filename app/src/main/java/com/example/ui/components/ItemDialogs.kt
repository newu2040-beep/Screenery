package com.example.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ScreeneryPrimary
import com.example.ui.theme.ScreeneryRecordRed
import com.example.ui.theme.ScreeneryTextPrimary
import com.example.ui.theme.ScreeneryTextSecondary

@Composable
fun RenameDialog(
    currentTitle: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var newTitle by remember { mutableStateOf(currentTitle) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Rename Recording",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = ScreeneryTextPrimary
            )
        },
        text = {
            OutlinedTextField(
                value = newTitle,
                onValueChange = { newTitle = it },
                label = { Text("Video Title") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("rename_input_field")
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newTitle.isNotBlank()) {
                        onConfirm(newTitle)
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ScreeneryPrimary),
                modifier = Modifier.testTag("confirm_rename_button")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = ScreeneryTextSecondary)
            }
        },
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.testTag("rename_dialog")
    )
}

@Composable
fun DeleteConfirmationDialog(
    itemTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Delete Recording?",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = ScreeneryTextPrimary
            )
        },
        text = {
            Text(
                text = "Are you sure you want to delete \"$itemTitle\"? This file will be permanently removed from your device.",
                fontSize = 14.sp,
                color = ScreeneryTextSecondary
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ScreeneryRecordRed),
                modifier = Modifier.testTag("confirm_delete_button")
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = ScreeneryTextSecondary)
            }
        },
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.testTag("delete_dialog")
    )
}
