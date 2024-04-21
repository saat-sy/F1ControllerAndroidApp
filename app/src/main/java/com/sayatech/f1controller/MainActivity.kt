package com.sayatech.f1controller

import android.content.SharedPreferences
import android.graphics.PointF
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sayatech.f1controller.ui.theme.F1ControllerTheme
import com.sayatech.f1controller.views.ConnectionDialog
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

@OptIn(DelicateCoroutinesApi::class, ExperimentalComposeUiApi::class)
class MainActivity : ComponentActivity() {
    private lateinit var socketHandler: SocketHandler
    private var height: Float = 0f
    private var width: Float = 0f
    private var accelerationValue: Float = 0f
    private lateinit var sensorManager: SensorManager
    private lateinit var sensor: Sensor
    private lateinit var sensorChannel: SensorChannel
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            F1ControllerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF000000)
                ) {
                    UserInterface()
                }
            }
        }

        sharedPreferences = getSharedPreferences(SHARED_KEY, MODE_PRIVATE)
        socketHandler = SocketHandler(
            sharedPreferences.getString(IP_KEY, "")!!
        )

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        sensorChannel = SensorChannel(socketHandler)
        sensorChannel.process(accelerationCallback)
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
                            registerOrientationListener()
                            dialogStatus.value = false
                        }
                    },
                    sharedPreferences
                )
            }
        }

        Controller()
    }

    @Composable
    fun Controller() {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .pointerInteropFilter {
                    handlePress(it)
                    true
                }
                .onGloballyPositioned {
                    height = it.size.height.toFloat()
                    width = it.size.width.toFloat()
                },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(fraction = 0.15f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextButton(
                    onClick = {},
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                ) {
                    Text(text = "Kers")
                }
                TextButton(
                    onClick = {},
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                ) {
                    Text(text = "DRS")
                }
            }
            Divider(
                color = Color.White,
                modifier = Modifier
                    .fillMaxHeight()  //fill the max height
                    .width(1.dp)
            )
            TextButton(
                onClick = {},
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(3f)
            ) {
                Text(text = "Brake")
            }
            TextButton(
                onClick = {},
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(3f)
            ) {
                Text(text = "Accelerate")
            }
        }
    }

    private fun handlePress(event: MotionEvent) {
        when(event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val index = getIdFromCoordinates(PointF(event.x, event.y), true)
                socketHandler.buttonPress(
                    index,
                    true
                )
            }
            MotionEvent.ACTION_UP -> {
                val index = getIdFromCoordinates(PointF(event.x, event.y), false)
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
                    ), true
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
                    ), false
                )
                socketHandler.buttonPress(
                    index,
                    false
                )
            }
            MotionEvent.ACTION_MOVE -> {
                updateAcceleration(PointF(event.x, event.y))
            }
        }
    }

    private fun getIdFromCoordinates(point: PointF, state: Boolean): Int {
        return if (point.x < 0.15 * width) {
            if (point.y < 0.5 * height) {
                KERS
            } else {
                DRS
            }
        } else {
            if (point.x < ((1 - 0.15) / 2 * width)) {
                BRAKE
            } else {
                accelerationValue = if (state) {
                    1f
                } else {
                    0f
                }
                ACC
            }
        }
    }

    private fun updateAcceleration(point: PointF) {
        if (point.x > ((1 - 0.15) / 2 * width)) {
            accelerationValue = if (point.y > height/2) {
                if (point.y / height > 0.0f) 1 - point.y / height else 0.0f
            } else {
                1.0f
            }
        }
    }

    private val accelerationCallback: () -> Float = {
        Math.round(accelerationValue * 1000.0) / 1000.0f
    }

    private fun registerOrientationListener() {
        if (socketHandler.getStatus() == ConnectionStatus.CONNECTED) {
            sensorManager.registerListener(
                sensorChannel,
                sensor,
                SensorManager.SENSOR_DELAY_GAME
            )
        }
    }

    private fun unRegisterOrientationListener() {
        if (socketHandler.getStatus() == ConnectionStatus.CONNECTED) {
            try {
                sensorManager.unregisterListener(sensorChannel)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        registerOrientationListener()
    }

    override fun onPause() {
        super.onPause()
        unRegisterOrientationListener()
    }

    @Preview(showBackground = true, device = Devices.AUTOMOTIVE_1024p, widthDp = 720, heightDp = 360)
    @Composable
    fun User() {
        UserInterface()
    }
}