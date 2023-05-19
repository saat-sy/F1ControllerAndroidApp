package com.sayatech.f1controller

import android.graphics.PointF
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.constraintlayout.widget.ConstraintSet.Motion
import com.sayatech.f1controller.ui.theme.F1ControllerTheme
import com.sayatech.f1controller.views.ConnectionDialog
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(DelicateCoroutinesApi::class, ExperimentalComposeUiApi::class)
class MainActivity : ComponentActivity() {
    private lateinit var socketHandler: SocketHandler
    private var height: Float = 0f
    private var width: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            F1ControllerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    UserInterface()
                }
            }
        }

        socketHandler = SocketHandler()
    }

    @Composable
    fun UserInterface() {
        val dialogStatus = remember {
            mutableStateOf(true)
        }
        val progressState = remember {
            mutableStateOf(false)
        }
        if (this::socketHandler.isInitialized && dialogStatus.value) {
            if (socketHandler.getStatus() == ConnectionStatus.NOT_CONNECTED) {
                ConnectionDialog(
                    onConnectClicked = {
                        progressState.value = true
                        GlobalScope.launch(Dispatchers.IO) {
                            val connection = async { socketHandler.connect() }
                            val status = connection.await()
                            socketHandler.setStatus(
                                if (status) {
                                    ConnectionStatus.CONNECTED
                                }
                                else {
                                    ConnectionStatus.NOT_CONNECTED
                                }
                            )
                            dialogStatus.value = false
                        }
                    },
                    progressState.value
                )
            }
        }

        Controller(dialogStatus)
    }

    @Composable
    fun Controller(
        dialogStatus: MutableState<Boolean>
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
                .pointerInteropFilter {
                    handlePress(it)
                    true
                }
                .onGloballyPositioned {
                    height = it.size.height.toFloat()
                    width = it.size.width.toFloat()
                },
            horizontalAlignment = Alignment.End
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().fillMaxHeight(0.4f),
                horizontalArrangement = Arrangement.SpaceEvenly,

            ) {
                TextButton(
                    onClick = {},
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.5f)
                        .weight(1f)
                ) {
                    Text(text = "Gear Down")
                }
                TextButton(
                    onClick = {},
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.5f)
                        .weight(1f)
                ) {
                    Text(text = "Gear Up")
                }
            }
            TextButton(
                onClick = {
                    if (socketHandler.getStatus() == ConnectionStatus.NOT_CONNECTED) {
                        dialogStatus.value = true
                    } else {
                        socketHandler.disconnect()
                        dialogStatus.value = true
                    }
                },
            ) {
                Text(text = socketHandler.getStatus().toString())
            }
            Row(
                modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                TextButton(
                    onClick = {},
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.5f)
                        .weight(1f)
                ) {
                    Text(text = "Brake")
                }
                TextButton(
                    onClick = {},
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.5f)
                        .weight(1f)
                ) {
                    Text(text = "Accelerate")
                }
            }
        }
    }

    private fun handlePress(event: MotionEvent) {
        when(event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val index = getIdFromCoordinates(PointF(event.x, event.y))
                socketHandler.buttonPress(
                    index,
                    true
                )
            }
            MotionEvent.ACTION_UP -> {
                val index = getIdFromCoordinates(PointF(event.x, event.y))
                socketHandler.buttonPress(
                    index,
                    false
                )
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                val index = getIdFromCoordinates(
                    PointF(
                        event.getX(event.actionIndex),
                        event.getY(event.actionIndex)
                    )
                )
                socketHandler.buttonPress(
                    index,
                    true
                )
            }
            MotionEvent.ACTION_POINTER_UP -> {
                val index = getIdFromCoordinates(
                    PointF(
                        event.getX(event.actionIndex),
                        event.getY(event.actionIndex)
                    )
                )
                socketHandler.buttonPress(
                    index,
                    false
                )
            }
        }
    }

    private fun getIdFromCoordinates(point: PointF): Int {
        return if (point.y < 0.4 * height) {
            if (point.x < 0.5 * width) {
                G_DOWN
            } else {
                G_UP
            }
        } else {
            if (point.x < 0.5 * width) {
                BRAKE
            } else {
                ACC
            }
        }
    }
}