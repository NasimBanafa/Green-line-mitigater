package com.example.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TestPatternScreen(
    onDismiss: () -> Unit
) {
    var currentColor by remember { mutableStateOf(Color(0xFF00FF00)) }
    var showControls by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { showControls = !showControls }
            .testTag("test_pattern_fullscreen")
    ) {
        Crossfade(targetState = currentColor, label = "colorTransition") { color ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
            )
        }

        if (showControls) {
            Surface(
                color = Color.Black.copy(alpha = 0.85f),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "OLED Screen Defect Inspector",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Exit", tint = TextSecondary)
                        }
                    }

                    Text(
                        text = "Tap anywhere on screen to hide/show this control card",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ColorButton(color = Color(0xFF00FF00), label = "Green") { currentColor = it }
                        ColorButton(color = Color(0xFFFF0000), label = "Red") { currentColor = it }
                        ColorButton(color = Color(0xFF0088FF), label = "Blue") { currentColor = it }
                        ColorButton(color = Color(0xFFFFFFFF), label = "White") { currentColor = it }
                        ColorButton(color = Color(0xFF000000), label = "Black") { currentColor = it }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorButton(color: Color, label: String, onClick: (Color) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick(color) }
    ) {
        Box(
            modifier = Modifier
                .padding(4.dp)
                .height(36.dp)
                .width(48.dp)
                .background(color, RoundedCornerShape(8.dp))
        )
        Text(text = label, fontSize = 11.sp, color = TextPrimary)
    }
}
