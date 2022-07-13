package com.cagrisahinoglu.volumesetting

import android.graphics.Paint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.center
import androidx.compose.ui.unit.sp
import com.cagrisahinoglu.volumesetting.ui.theme.VolumeSettingTheme
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VolumeSettingTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    VolumeSetting()
                }
            }
        }
    }
}

@Composable
fun VolumeSetting(
    dotCountSize: Int = 30,
    selectedDotColor: Color = Color.Green,
    unselectedDotColor: Color = Color.Red,
    selectedDotRadius: Float = 20f,
    unselectedDotRadius: Float = 10f,
    circleColor: Color = Color.Gray,
    circleRadius: Float = 0f,
    volumeTextColor: Color = MaterialTheme.colors.onPrimary,
    volumeTextSize: TextUnit = 30.sp,
    knobRadius: Float = 50f
) {
    val dotCount = when {
        dotCountSize < 2 -> 2
        dotCountSize > 40 -> 40
        else -> dotCountSize
    }

    var degree = 270f
    val degreeBetweenDots = 360f / dotCount
    var knobDegree by remember { mutableStateOf(0.0) }
    var selectedDotAngle = 0.0
    val volumeTextPaint = Paint().apply {
        textSize = with(LocalDensity.current) { volumeTextSize.toPx() }
        color = volumeTextColor.toArgb()
        textAlign = Paint.Align.CENTER
    }

    Canvas(
        modifier = Modifier
            .pointerInput(true) {
                detectDragGestures { change, _ ->
                    val touchAngle = atan2(
                        y = size.center.x - change.position.x,
                        x = size.center.y - change.position.y
                    ) * (180f / Math.PI.toFloat()) * -1
                    knobDegree = touchAngle.toDouble()
                    selectedDotAngle = knobDegree
                    if (knobDegree == 0.0) {
                        selectedDotAngle = 0.0
                    }
                    if (knobDegree < 0) {
                        selectedDotAngle = 180.0
                        selectedDotAngle += 180 + knobDegree
                    }
                }
            }
    ) {
        val radius = if (circleRadius == 0f) size.width * .35f else circleRadius
        drawCircle(
            color = circleColor,
            style = Stroke(
                width = radius * .08f
            ),
            radius = radius,
            center = size.center
        )

        for (dotNumber in 0 until dotCount) {
            if (degree >= 360f) {
                val remain = degree - 360
                degree = remain
            }
            val outsideDotX =
                ((radius * .85f) * cos(Math.toRadians(degree.toDouble()))) + size.center.x
            val outsideDotY =
                ((radius * .85f) * sin(Math.toRadians(degree.toDouble()))) + size.center.y

            val isSelected = dotNumber * degreeBetweenDots < selectedDotAngle
            val dotColor = if (isSelected) selectedDotColor else unselectedDotColor
            val dotSize = if (isSelected) selectedDotRadius else unselectedDotRadius

            drawCircle(
                color = dotColor,
                radius = dotSize,
                center = Offset(outsideDotX.toFloat(), outsideDotY.toFloat())
            )
            degree += degreeBetweenDots
        }
        val knobX = (radius * .55f) * cos(Math.toRadians(knobDegree - 90)) + size.center.x
        val knobY = (radius * .55f) * sin(Math.toRadians(knobDegree - 90)) + size.center.y

        drawCircle(
            color = Color.Green,
            radius = knobRadius,
            center = Offset(knobX.toFloat(), knobY.toFloat())
        )

        val volumeText = ((selectedDotAngle * 100) / 360)

        drawIntoCanvas { canvas ->
            canvas.nativeCanvas.drawText(
                "%" + String.format("%.1f", volumeText),
                size.center.x,
                size.center.y,
                volumeTextPaint
            )
        }
    }
}
