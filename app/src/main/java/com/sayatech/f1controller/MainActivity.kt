package com.sayatech.f1controller

import android.content.SharedPreferences
import android.graphics.PointF
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sayatech.f1controller.ui.theme.F1ControllerTheme
import com.sayatech.f1controller.views.ConnectionDialog
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

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
                    color = Color(0xFF050505)
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
        val dialogStatus = remember { mutableStateOf(true) }
        
        if (this::socketHandler.isInitialized && dialogStatus.value) {
            if (socketHandler.getStatus() == ConnectionStatus.NOT_CONNECTED) {
                ConnectionDialog(
                    onConnectClicked = {
                        GlobalScope.launch(Dispatchers.IO) {
                            val connection = async { socketHandler.connect() }
                            val status = connection.await()
                            socketHandler.setStatus(if (status) ConnectionStatus.CONNECTED else ConnectionStatus.NOT_CONNECTED)
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
            // Left: BRAKE (37.5%)
            PedalBox(
                label = "BRAKE",
                modifier = Modifier.fillMaxHeight().weight(0.375f)
            )

            // Center: DPAD over Face Buttons (25%)
            Column(
                modifier = Modifier.fillMaxHeight().weight(0.25f),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // DPAD Stack
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                    Row(modifier = Modifier.height(60.dp)) {
                        Spacer(Modifier.width(60.dp))
                        ControlButton("▲", Modifier.size(60.dp))
                        Spacer(Modifier.width(60.dp))
                    }
                    Row(modifier = Modifier.height(60.dp)) {
                        ControlButton("◀", Modifier.size(60.dp))
                        Spacer(Modifier.width(60.dp))
                        ControlButton("▶", Modifier.size(60.dp))
                    }
                    Row(modifier = Modifier.height(60.dp)) {
                        Spacer(Modifier.width(60.dp))
                        ControlButton("▼", Modifier.size(60.dp))
                        Spacer(Modifier.width(60.dp))
                    }
                }

                // Face Buttons Stack
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                    Row(modifier = Modifier.height(60.dp)) {
                        Spacer(Modifier.width(60.dp))
                        ControlButton("△", Modifier.size(60.dp))
                        Spacer(Modifier.width(60.dp))
                    }
                    Row(modifier = Modifier.height(60.dp)) {
                        ControlButton("□", Modifier.size(60.dp))
                        Spacer(Modifier.width(60.dp))
                        ControlButton("○", Modifier.size(60.dp))
                    }
                    Row(modifier = Modifier.height(60.dp)) {
                        Spacer(Modifier.width(60.dp))
                        ControlButton("✕", Modifier.size(60.dp))
                        Spacer(Modifier.width(60.dp))
                    }
                }
            }

            // Right: THROTTLE (37.5%)
            PedalBox(
                label = "THROTTLE",
                modifier = Modifier.fillMaxHeight().weight(0.375f)
            )
        }
    }

    @Composable
    fun PedalBox(label: String, modifier: Modifier) {
        Box(
            modifier = modifier
                .padding(16.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.08f),
                            Color.White.copy(alpha = 0.02f)
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.2f),
                            Color.White.copy(alpha = 0.05f)
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 28.sp,
                fontWeight = FontWeight.Thin,
                letterSpacing = 10.sp
            )
        }
    }

    @Composable
    fun ControlButton(label: String, modifier: Modifier) {
        Box(
            modifier = modifier
                .padding(4.dp)
                .background(
                    color = Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(12.dp)
                )
                .border(
                    width = 0.5.dp,
                    color = Color.White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 24.sp,
                fontWeight = FontWeight.Light
            )
        }
    }

    private fun handlePress(event: MotionEvent) {
        val action = event.actionMasked
        val pointerIndex = event.actionIndex

        when(action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                val point = PointF(event.getX(pointerIndex), event.getY(pointerIndex))
                val index = getIdFromCoordinates(point, true)
                if (index != 0) {
                    if (index != ACC) {
                        socketHandler.buttonPress(index, true)
                    } else {
                        updateAcceleration(point)
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                val point = PointF(event.getX(pointerIndex), event.getY(pointerIndex))
                val index = getIdFromCoordinates(point, false)
                if (index != 0) {
                    socketHandler.buttonPress(index, false)
                    if (index == ACC) accelerationValue = 0f
                }
            }
            MotionEvent.ACTION_MOVE -> {
                for (i in 0 until event.pointerCount) {
                    val point = PointF(event.getX(i), event.getY(i))
                    if (point.x >= 0.625 * width) {
                        updateAcceleration(point)
                    }
                }
            }
        }
    }

    private fun getIdFromCoordinates(point: PointF, state: Boolean): Int {
        if (point.x < 0.375 * width) {
            // Brake
            return BRAKE
        } else if (point.x < 0.625 * width) {
            // Center (DPAD on top of Face Buttons)
            val relativeX = (point.x - 0.375 * width) / (0.25 * width)
            val col = (relativeX * 3).toInt().coerceIn(0, 2)
            
            if (point.y < height / 2) {
                // DPAD
                val row = (point.y / (height / 6)).toInt().coerceIn(0, 2)
                return when (row) {
                    0 -> if (col == 1) DPAD_UP else 0
                    1 -> when(col) {
                        0 -> DPAD_LEFT
                        2 -> DPAD_RIGHT
                        else -> 0
                    }
                    2 -> if (col == 1) DPAD_DOWN else 0
                    else -> 0
                }
            } else {
                // Face Buttons
                val relativeY = point.y - height / 2
                val row = (relativeY / (height / 6)).toInt().coerceIn(0, 2)
                return when (row) {
                    0 -> if (col == 1) TRI_BUTTON else 0
                    1 -> when(col) {
                        0 -> SQ_BUTTON
                        2 -> CIR_BUTTON
                        else -> 0
                    }
                    2 -> if (col == 1) X_BUTTON else 0
                    else -> 0
                }
            }
        } else {
            // Throttle
            accelerationValue = if (state) 1.000f else 0.000f
            return ACC
        }
    }

    private fun updateAcceleration(point: PointF) {
        if (point.x > ((1 - 0.15) / 2 * width)) {
            val clippedAcceleration = if (point.y / height > 0.0f) 1 - point.y / height else 0.0f
            accelerationValue = if (clippedAcceleration <= 0.4) {
                val newAcceleration = (((clippedAcceleration - 0) * (1 - 0)) / (0.4f - 0.05f)) + 0
                if (newAcceleration <= 0) {
                    0.000f
                } else {
                    newAcceleration
                }
            } else {
                1.000f
            }
        }
    }

    private val accelerationCallback: () -> Float = {
        (accelerationValue * 1000).roundToInt() / 1000f
    }

    private fun registerOrientationListener() {
        if (socketHandler.getStatus() == ConnectionStatus.CONNECTED) {
            sensorManager.registerListener(sensorChannel, sensor, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    private fun unRegisterOrientationListener() {
        if (socketHandler.getStatus() == ConnectionStatus.CONNECTED) {
            try { sensorManager.unregisterListener(sensorChannel) } catch (_: Exception) {}
        }
    }

    override fun onResume() { super.onResume() ; registerOrientationListener() }
    override fun onPause() { super.onPause() ; unRegisterOrientationListener() }

    @Preview(showBackground = true, device = Devices.AUTOMOTIVE_1024p, widthDp = 720, heightDp = 360)
    @Composable
    fun User() { UserInterface() }
}
