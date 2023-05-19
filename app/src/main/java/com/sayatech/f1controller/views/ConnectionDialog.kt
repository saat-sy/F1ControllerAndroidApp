package com.sayatech.f1controller.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.sayatech.f1controller.ui.theme.F1ControllerTheme

@Composable
fun ConnectionDialog(onConnectClicked: () -> Unit, progressStatus: Boolean) {
    Dialog(onDismissRequest = {}) {
        Surface(
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(
                modifier = Modifier.padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (progressStatus) {
                        CircularProgressIndicator()
                    }
                    Button(
                        onClick = onConnectClicked
                    ) {
                        Text(text = "Connect")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ConnectionDialog(
        onConnectClicked = {},
        true
    )
}