package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.MainViewModel
import com.example.ui.components.MaskCard
import com.example.ui.components.MaskEditorSheet
import com.example.ui.components.RootLsposedGuide
import com.example.ui.components.TestPatternScreen
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.OledBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

import androidx.compose.ui.graphics.Brush
import com.example.ui.theme.GlassCard
import com.example.ui.theme.GlassCardBorder
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.IndigoDarkBg
import com.example.ui.theme.SlateDarkBg

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val masks by viewModel.allMasks.collectAsStateWithLifecycle()
    val isOverlayGranted by viewModel.isOverlayPermissionGranted.collectAsStateWithLifecycle()
    val isServiceRunning by viewModel.isServiceRunning.collectAsStateWithLifecycle()
    val isRootAvailable by viewModel.isRootAvailable.collectAsStateWithLifecycle()
    val editingMask by viewModel.editingMask.collectAsStateWithLifecycle()
    val rootMessage by viewModel.rootMessage.collectAsStateWithLifecycle()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showTestPatternScreen by remember { mutableStateOf(false) }

    if (showTestPatternScreen) {
        TestPatternScreen(onDismiss = { showTestPatternScreen = false })
        return
    }

    val bgGradient = Brush.verticalGradient(
        colors = listOf(IndigoDarkBg, SlateDarkBg)
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        floatingActionButton = {
            if (selectedTabIndex == 0) {
                FloatingActionButton(
                    onClick = { viewModel.createNewMask() },
                    containerColor = IndigoAccent,
                    contentColor = Color.Black,
                    modifier = Modifier.testTag("add_mask_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add New Black Bar")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgGradient)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(IndigoAccent.copy(alpha = 0.2f))
                                .border(1.dp, IndigoAccent.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = "App Logo",
                                tint = IndigoAccent,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Line Mask",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = "OLED Screen Green Line Mask",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    val powerBgColor by animateColorAsState(
                        targetValue = if (isServiceRunning) EmeraldGreen else GlassCard,
                        label = "powerBg"
                    )
                    Button(
                        onClick = { viewModel.toggleService(context) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = powerBgColor,
                            contentColor = if (isServiceRunning) Color.Black else TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .border(1.dp, GlassCardBorder, RoundedCornerShape(12.dp))
                            .testTag("toggle_service_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isServiceRunning) "OVERLAY ON" else "START OVERLAY",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(visible = !isOverlayGranted) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = AmberWarning.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Overlay Permission Needed",
                                fontWeight = FontWeight.Bold,
                                color = AmberWarning,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Required to draw black bars over other applications",
                                fontSize = 12.sp,
                                color = TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                context.startActivity(viewModel.requestOverlayPermissionIntent(context))
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AmberWarning,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Grant", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TabChip(
                    label = "Masks (${masks.size})",
                    icon = Icons.Default.Layers,
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 }
                )
                TabChip(
                    label = "Orientation Lock",
                    icon = Icons.Default.ScreenRotation,
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 }
                )
                TabChip(
                    label = "Inspector",
                    icon = Icons.Default.Visibility,
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 }
                )
                TabChip(
                    label = "Root",
                    icon = Icons.Default.Code,
                    selected = selectedTabIndex == 3,
                    onClick = { selectedTabIndex = 3 }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTabIndex) {
                0 -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                text = "QUICK PRESETS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                item {
                                    PresetChip("Vertical Right Green Line") { viewModel.addPreset(it) }
                                }
                                item {
                                    PresetChip("Vertical Center Line") { viewModel.addPreset(it) }
                                }
                                item {
                                    PresetChip("Horizontal Top Line") { viewModel.addPreset(it) }
                                }
                                item {
                                    PresetChip("Camera Punchhole Mask") { viewModel.addPreset(it) }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "ACTIVE BLACK BARS (${masks.count { it.isEnabled }} / ${masks.size})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 1.sp
                            )
                        }

                        if (masks.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No black bars configured.\nTap + to add your first mask!",
                                        color = TextMuted,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        } else {
                            items(masks, key = { it.id }) { mask ->
                                MaskCard(
                                    mask = mask,
                                    onToggleEnabled = { enabled ->
                                        viewModel.toggleMaskEnabled(mask, enabled)
                                    },
                                    onEdit = { viewModel.setEditingMask(mask) },
                                    onDelete = { viewModel.deleteMask(mask) }
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }

                1 -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = DarkCard),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Lock, contentDescription = null, tint = CyanAccent)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "Orientation Lock Control",
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextPrimary
                                                )
                                            )
                                            Text(
                                                text = "Keep display rotation fixed for stable mask alignment",
                                                fontSize = 12.sp,
                                                color = TextSecondary
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Button(
                                        onClick = { viewModel.toggleOrientationLock(context) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = CyanAccent,
                                            contentColor = Color.Black
                                        ),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.ScreenRotation, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Toggle Global System Orientation Lock", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = DarkCard),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "How Screen Rotation Works with Defect Masks",
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        fontSize = 15.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "When hardware green lines appear on an OLED display, the line defect is physically bonded to the glass panel. When software rotates from Portrait to Landscape, physical display coordinates shift relative to app views.",
                                        fontSize = 13.sp,
                                        color = TextSecondary,
                                        lineHeight = 18.sp
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Line Mask includes two solutions:\n1. 'Hardware Fixed Placement' mapping: Translates coordinates dynamically so the black bar stays glued to the physical defect.\n2. 'Orientation Lock': Prevents auto-rotation while masks are active.",
                                        fontSize = 13.sp,
                                        color = CyanAccent,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }

                2 -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = DarkCard),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = EmeraldGreen,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Full Screen OLED Inspector",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Launch a full screen solid color test to locate green lines, pink lines, or dead pixels across your panel.",
                                    fontSize = 13.sp,
                                    color = TextSecondary,
                                    lineHeight = 18.sp
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Button(
                                    onClick = { showTestPatternScreen = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = EmeraldGreen,
                                        contentColor = Color.Black
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("launch_inspector_btn")
                                ) {
                                    Text("Launch Full Screen Inspector", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                3 -> {
                    RootLsposedGuide(
                        isRootAvailable = isRootAvailable,
                        rootMessage = rootMessage,
                        onGrantOverlayViaRoot = { viewModel.grantOverlayViaRoot() },
                        onClearRootMessage = { viewModel.clearRootMessage() }
                    )
                }
            }
        }
    }
    }

    editingMask?.let { mask ->
        MaskEditorSheet(
            mask = mask,
            onSave = { updated -> viewModel.saveMask(updated) },
            onDismiss = { viewModel.setEditingMask(null) }
        )
    }
}

@Composable
private fun TabChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = IndigoAccent,
            selectedLabelColor = Color.White,
            containerColor = GlassCard,
            labelColor = TextSecondary
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = GlassCardBorder,
            selectedBorderColor = IndigoAccent
        ),
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
private fun PresetChip(
    name: String,
    onSelect: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(GlassCard)
            .border(1.dp, GlassCardBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("preset_$name")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(2.dp)
        ) {
            Text(
                text = "+ $name",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = IndigoAccent,
                modifier = Modifier.padding(end = 4.dp)
            )
            Button(
                onClick = { onSelect(name) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = IndigoAccent.copy(alpha = 0.25f),
                    contentColor = IndigoAccent
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(24.dp)
            ) {
                Text("Add", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
