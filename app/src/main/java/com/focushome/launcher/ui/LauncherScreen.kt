package com.focushome.launcher.ui

import android.content.Context
import android.os.BatteryManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focushome.launcher.ui.components.AdMobBannerAdView
import com.focushome.launcher.ui.components.AdMobNativeAdView
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LauncherScreen(
    viewModel: AppListViewModel,
    onGoPro: () -> Unit
) {
    var isDrawerOpen by remember { mutableStateOf(false) }
    val isProUser by viewModel.isProUser.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        if (isDrawerOpen) {
            AppDrawer(
                viewModel = viewModel,
                onClose = { isDrawerOpen = false },
                isProUser = isProUser,
                onGoPro = onGoPro
            )
        } else {
            HomeScreen(
                viewModel = viewModel,
                onOpenDrawer = { isDrawerOpen = true }
            )
        }
    }
}

@Composable
fun HomeScreen(
    viewModel: AppListViewModel,
    onOpenDrawer: () -> Unit
) {
    val pinnedApps by viewModel.pinnedApps.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onOpenDrawer() }
            .padding(32.dp),
        horizontalAlignment = Alignment.Start
    ) {
        DateTimeBatteryWidget()
        
        Spacer(modifier = Modifier.height(64.dp))
        
        pinnedApps.forEach { app ->
            Text(
                text = app.label,
                color = Color.White,
                fontSize = 24.sp,
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .clickable { viewModel.launchApp(app.packageName) }
            )
        }
    }
}

@Composable
fun DateTimeBatteryWidget() {
    val context = LocalContext.current
    var time by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var battery by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            val now = Calendar.getInstance().time
            time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)
            date = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(now)
            
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            battery = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            
            delay(1000)
        }
    }

    Column {
        Text(text = time, color = Color.White, fontSize = 48.sp)
        Text(text = date, color = Color.LightGray, fontSize = 18.sp)
        Text(text = "$battery%", color = Color.LightGray, fontSize = 18.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDrawer(
    viewModel: AppListViewModel,
    onClose: () -> Unit,
    isProUser: Boolean,
    onGoPro: () -> Unit
) {
    val apps by viewModel.filteredApps.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "←",
                color = Color.White,
                fontSize = 24.sp,
                modifier = Modifier
                    .clickable { onClose() }
                    .padding(end = 16.dp)
            )
            
            BasicTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1A1A1A), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                textStyle = TextStyle(color = Color.White, fontSize = 18.sp),
                cursorBrush = SolidColor(Color.White),
                decorationBox = { innerTextField ->
                    if (searchQuery.isEmpty()) {
                        Text("Search apps...", color = Color.Gray, fontSize = 18.sp)
                    }
                    innerTextField()
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(apps) { app ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { 
                            viewModel.launchApp(app.packageName)
                            onClose()
                        }
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = app.label, color = Color.White, fontSize = 20.sp)
                    Text(
                        text = if (app.isPinned) "★" else "☆",
                        color = if (app.isPinned) Color.Yellow else Color.Gray,
                        modifier = Modifier.clickable { viewModel.togglePinnedApp(app) }
                    )
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
                if (!isProUser) {
                    Button(
                        onClick = onGoPro,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Remove Ads / Go Pro")
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                   AdMobBannerAdView(isProUser = isProUser)
                Spacer(modifier = Modifier.height(16.dp))
                AdMobNativeAdView(isProUser = isProUser)
            }
        }
    }
}

private fun RoundedCornerShape(size: androidx.compose.ui.unit.Dp) = 
    androidx.compose.foundation.shape.RoundedCornerShape(size)
