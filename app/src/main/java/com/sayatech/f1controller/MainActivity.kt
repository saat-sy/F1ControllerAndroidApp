package com.sayatech.f1controller

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.sayatech.f1controller.ui.theme.F1ControllerTheme

class MainActivity : ComponentActivity() {
    private lateinit var socketHandler: SocketHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            F1ControllerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Button(onClick = { socketHandler.connect() }) {
                        Text(text = "Connect")
                    }
                }
            }
        }

        socketHandler = SocketHandler("192.168.1.10")
    }
}