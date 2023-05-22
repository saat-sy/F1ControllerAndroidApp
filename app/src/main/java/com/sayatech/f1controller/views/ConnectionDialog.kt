package com.sayatech.f1controller.views

import android.content.SharedPreferences
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.sayatech.f1controller.IP_KEY

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionDialog(onConnectClicked: () -> Unit, sharedPreferences: SharedPreferences) {
    var text by rememberSaveable { mutableStateOf(sharedPreferences.getString(IP_KEY, "")!!) }
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
                    TextField(
                        value = text,
                        onValueChange = {
                            text = it
                            val edit = sharedPreferences.edit()
                            edit.putString(IP_KEY, text)
                            edit.apply()
                        }
                    )
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