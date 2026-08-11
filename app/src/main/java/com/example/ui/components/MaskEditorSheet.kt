package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MaskBarEntity
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaskEditorSheet(
    mask: MaskBarEntity,
    onSave: (MaskBarEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name by remember { mutableStateOf(mask.name) }
    var isVertical by remember { mutableStateOf(mask.isVertical) }
    var xPosRatio by remember { mutableFloatStateOf(mask.xPosRatio) }
    var yPosRatio by remember { mutableFloatStateOf(mask.yPosRatio) }
    var thicknessDp by remember { mutableIntStateOf(mask.thicknessDp) }
    var lengthRatio by remember { mutableFloatStateOf(mask.lengthRatio) }
    var angleDegrees by remember { mutableFloatStateOf(mask.angleDegrees) }
    var opacity by remember { mutableFloatStateOf(mask.opacity) }
    var fineStep by remember { mutableFloatStateOf(0.001f) } // 0.1% fine tuning step (~1px)
    var colorHex by remember { mutableStateOf(mask.colorHex) }
    var touchPassThrough by remember { mutableStateOf(mask.touchPassThrough) }
    var hardwareLockOrientation by remember { mutableStateOf(mask.hardwareLockOrientation) }
    var orientationCondition by remember { mutableStateOf(mask.orientationCondition) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurface,
        scrimColor = Color.Black.copy(alpha = 0.7f),
        modifier = Modifier.testTag("mask_editor_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (mask.id == 0L) "Create Black Bar Mask" else "Edit Mask Parameters",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("dismiss_editor")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF10141D))
                    .border(1.dp, CyanAccent.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .graphicsLayer {
                                translationX = xPosRatio * 320.dp.toPx()
                                translationY = yPosRatio * 180.dp.toPx()
                            }
                            .size(
                                if (isVertical) 2.dp else 160.dp,
                                if (isVertical) 160.dp else 2.dp
                            )
                            .background(Color(0xFF00FF66))
                    )

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .graphicsLayer {
                                translationX = (xPosRatio * 320.dp.toPx()) - (if (isVertical) (thicknessDp.dp.toPx() / 2) else 0f)
                                translationY = (yPosRatio * 180.dp.toPx()) - (if (!isVertical) (thicknessDp.dp.toPx() / 2) else 0f)
                                rotationZ = angleDegrees
                            }
                            .size(
                                if (isVertical) thicknessDp.dp else (180.dp * lengthRatio),
                                if (isVertical) (180.dp * lengthRatio) else thicknessDp.dp
                            )
                            .background(try { Color(android.graphics.Color.parseColor(colorHex)).copy(alpha = opacity) } catch (e: Exception) { Color.Black.copy(alpha = opacity) })
                    )

                    Surface(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "LIVE SIMULATION PREVIEW",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Mask Label") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mask_name_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanAccent,
                    unfocusedBorderColor = DarkCardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilterChip(
                    selected = isVertical,
                    onClick = { isVertical = true },
                    label = { Text("Vertical Line Mask") },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyanAccent,
                        selectedLabelColor = Color.Black
                    )
                )
                FilterChip(
                    selected = !isVertical,
                    onClick = { isVertical = false },
                    label = { Text("Horizontal Line Mask") },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyanAccent,
                        selectedLabelColor = Color.Black
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "X Position (Horizontal): ${String.format(java.util.Locale.US, "%.1f", xPosRatio * 100)}%",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        NudgeButton(label = "-1%") { xPosRatio = (xPosRatio - 0.01f).coerceIn(0f, 1f) }
                        NudgeButton(label = "-0.1%") { xPosRatio = (xPosRatio - 0.001f).coerceIn(0f, 1f) }
                        NudgeButton(label = "+0.1%") { xPosRatio = (xPosRatio + 0.001f).coerceIn(0f, 1f) }
                        NudgeButton(label = "+1%") { xPosRatio = (xPosRatio + 0.01f).coerceIn(0f, 1f) }
                    }
                }

                Slider(
                    value = xPosRatio,
                    onValueChange = { xPosRatio = it },
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = CyanAccent,
                        activeTrackColor = CyanAccent,
                        inactiveTrackColor = DarkCardBorder
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Y Position (Vertical): ${String.format(java.util.Locale.US, "%.1f", yPosRatio * 100)}%",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        NudgeButton(label = "-1%") { yPosRatio = (yPosRatio - 0.01f).coerceIn(0f, 1f) }
                        NudgeButton(label = "-0.1%") { yPosRatio = (yPosRatio - 0.001f).coerceIn(0f, 1f) }
                        NudgeButton(label = "+0.1%") { yPosRatio = (yPosRatio + 0.001f).coerceIn(0f, 1f) }
                        NudgeButton(label = "+1%") { yPosRatio = (yPosRatio + 0.01f).coerceIn(0f, 1f) }
                    }
                }

                Slider(
                    value = yPosRatio,
                    onValueChange = { yPosRatio = it },
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = CyanAccent,
                        activeTrackColor = CyanAccent,
                        inactiveTrackColor = DarkCardBorder
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Precision Placement D-Pad Controller
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkCard)
                    .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp))
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Precision Placement Pad",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Text(
                        text = "Step: ${(fineStep * 100).let { if (it < 0.2f) "0.1% (~1px)" else if (it < 0.8f) "0.5% (~5px)" else "1.0% (~10px)" }}",
                        fontSize = 11.sp,
                        color = CyanAccent,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterChip(
                        selected = fineStep == 0.001f,
                        onClick = { fineStep = 0.001f },
                        label = { Text("0.1% Fine", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CyanAccent, selectedLabelColor = Color.Black)
                    )
                    FilterChip(
                        selected = fineStep == 0.005f,
                        onClick = { fineStep = 0.005f },
                        label = { Text("0.5% Med", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CyanAccent, selectedLabelColor = Color.Black)
                    )
                    FilterChip(
                        selected = fineStep == 0.010f,
                        onClick = { fineStep = 0.010f },
                        label = { Text("1.0% Coarse", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CyanAccent, selectedLabelColor = Color.Black)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // D-Pad Grid
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // UP Button
                    IconButton(
                        onClick = { yPosRatio = (yPosRatio - fineStep).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(DarkCardBorder)
                    ) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = "Nudge Up", tint = CyanAccent)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // LEFT - CENTER RESET - RIGHT
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        IconButton(
                            onClick = { xPosRatio = (xPosRatio - fineStep).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(DarkCardBorder)
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Nudge Left", tint = CyanAccent)
                        }

                        IconButton(
                            onClick = {
                                xPosRatio = 0.5f
                                yPosRatio = 0.5f
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(IndigoAccent.copy(alpha = 0.3f))
                        ) {
                            Icon(Icons.Default.CenterFocusStrong, contentDescription = "Center Reset", tint = IndigoAccent)
                        }

                        IconButton(
                            onClick = { xPosRatio = (xPosRatio + fineStep).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(DarkCardBorder)
                        ) {
                            Icon(Icons.Default.ArrowForward, contentDescription = "Nudge Right", tint = CyanAccent)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // DOWN Button
                    IconButton(
                        onClick = { yPosRatio = (yPosRatio + fineStep).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(DarkCardBorder)
                    ) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = "Nudge Down", tint = CyanAccent)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mask Thickness: ${thicknessDp} dp",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { thicknessDp = (thicknessDp - 1).coerceAtLeast(1) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = CyanAccent)
                        }
                        Text(
                            text = "$thicknessDp",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        IconButton(
                            onClick = { thicknessDp = (thicknessDp + 1).coerceAtMost(60) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase", tint = CyanAccent)
                        }
                    }
                }

                Slider(
                    value = thicknessDp.toFloat(),
                    onValueChange = { thicknessDp = it.toInt() },
                    valueRange = 1f..60f,
                    colors = SliderDefaults.colors(
                        thumbColor = EmeraldGreen,
                        activeTrackColor = EmeraldGreen,
                        inactiveTrackColor = DarkCardBorder
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mask Opacity / Transparency: ${(opacity * 100).toInt()}%",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row {
                        NudgeButton(label = "100%") { opacity = 1.0f }
                        Spacer(modifier = Modifier.width(4.dp))
                        NudgeButton(label = "75%") { opacity = 0.75f }
                        Spacer(modifier = Modifier.width(4.dp))
                        NudgeButton(label = "50%") { opacity = 0.50f }
                    }
                }

                Slider(
                    value = opacity,
                    onValueChange = { opacity = it },
                    valueRange = 0.05f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = CyanAccent,
                        activeTrackColor = CyanAccent,
                        inactiveTrackColor = DarkCardBorder
                    ),
                    modifier = Modifier.testTag("opacity_slider")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column {
                Text(
                    text = "Angle Tilt: ${angleDegrees.toInt()}°",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Slider(
                    value = angleDegrees,
                    onValueChange = { angleDegrees = it },
                    valueRange = 0f..360f,
                    colors = SliderDefaults.colors(
                        thumbColor = CyanAccent,
                        activeTrackColor = CyanAccent,
                        inactiveTrackColor = DarkCardBorder
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkCard)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Touch Pass-Through",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = "Allows clicks and gestures to pass directly through the black bar to apps underneath",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Switch(
                    checked = touchPassThrough,
                    onCheckedChange = { touchPassThrough = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = CyanAccent
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkCard)
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ScreenRotation,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Display Condition (Orientation)",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Render condition for phone orientation",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = orientationCondition == "ALL",
                        onClick = { orientationCondition = "ALL" },
                        label = { Text("Both", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanAccent,
                            selectedLabelColor = Color.Black,
                            containerColor = DarkCardBorder,
                            labelColor = TextPrimary
                        )
                    )
                    FilterChip(
                        selected = orientationCondition == "PORTRAIT_ONLY" || orientationCondition == "VERTICAL_ONLY",
                        onClick = { orientationCondition = "PORTRAIT_ONLY" },
                        label = { Text("Vertical Only", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanAccent,
                            selectedLabelColor = Color.Black,
                            containerColor = DarkCardBorder,
                            labelColor = TextPrimary
                        )
                    )
                    FilterChip(
                        selected = orientationCondition == "LANDSCAPE_ONLY" || orientationCondition == "HORIZONTAL_ONLY",
                        onClick = { orientationCondition = "LANDSCAPE_ONLY" },
                        label = { Text("Horizontal Only", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanAccent,
                            selectedLabelColor = Color.Black,
                            containerColor = DarkCardBorder,
                            labelColor = TextPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Mask Color Palette",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ColorSwatch(hex = "#000000", selectedHex = colorHex, label = "OLED Black") { colorHex = it }
                ColorSwatch(hex = "#121212", selectedHex = colorHex, label = "Dark Slate") { colorHex = it }
                ColorSwatch(hex = "#FFFFFF", selectedHex = colorHex, label = "Pure White") { colorHex = it }
                ColorSwatch(hex = "#00FF00", selectedHex = colorHex, label = "Alignment Lime") { colorHex = it }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val updated = mask.copy(
                        name = name.ifBlank { "Line Mask" },
                        isVertical = isVertical,
                        xPosRatio = xPosRatio,
                        yPosRatio = yPosRatio,
                        thicknessDp = thicknessDp,
                        lengthRatio = lengthRatio,
                        angleDegrees = angleDegrees,
                        opacity = opacity,
                        colorHex = colorHex,
                        touchPassThrough = touchPassThrough,
                        hardwareLockOrientation = hardwareLockOrientation,
                        orientationCondition = orientationCondition
                    )
                    onSave(updated)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_mask_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyanAccent,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save Black Bar Parameters",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
private fun NudgeButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(DarkCardBorder)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = label, fontSize = 12.sp, color = CyanAccent, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ColorSwatch(
    hex: String,
    selectedHex: String,
    label: String,
    onSelect: (String) -> Unit
) {
    val isSelected = hex.equals(selectedHex, ignoreCase = true)
    val color = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { Color.Black }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onSelect(hex) }
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(color)
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) CyanAccent else DarkCardBorder,
                    shape = CircleShape
                )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, fontSize = 10.sp, color = if (isSelected) CyanAccent else TextMuted)
    }
}
