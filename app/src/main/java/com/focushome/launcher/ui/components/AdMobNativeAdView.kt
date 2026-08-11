package com.focushome.launcher.ui.components

import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.focushome.launcher.R
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

@Composable
fun AdMobNativeAdView(isProUser: Boolean) {
    if (isProUser) return

    val context = LocalContext.current
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }

    DisposableEffect(context) {
        val adLoader = AdLoader.Builder(context, "ca-app-pub-3940256099942544/2247696110") // Test ID
            .forNativeAd { ad ->
                nativeAd = ad
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    // Handle failure
                }
            })
            .build()

        adLoader.loadAd(AdRequest.Builder().build())

        onDispose {
            nativeAd?.destroy()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .background(Color(0xFF121212), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(Color(0xFFFBC02D), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "AD",
                    color = Color.Black,
                    fontSize = 10.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (nativeAd != null) {
            AndroidView(
                factory = { ctx ->
                    // For better compliance, use a layout XML for Native Ads
                    val adView = NativeAdView(ctx)
                    val inflater = LayoutInflater.from(ctx)
                    
                    // Fallback to manual view creation if layout isn't available
                    // Usually we'd use: val view = inflater.inflate(R.layout.ad_unified, adView)
                    // For this task, we will create a simple robust view manually to avoid missing R.layout errors
                    
                    val root = android.widget.LinearLayout(ctx).apply {
                        orientation = android.widget.LinearLayout.VERTICAL
                    }
                    
                    val headline = TextView(ctx).apply {
                        setTextColor(android.graphics.Color.WHITE)
                        textSize = 18f
                        setTypeface(null, android.graphics.Typeface.BOLD)
                    }
                    adView.headlineView = headline
                    root.addView(headline)
                    
                    val body = TextView(ctx).apply {
                        setTextColor(android.graphics.Color.LTGRAY)
                        textSize = 14f
                        setPadding(0, 8, 0, 8)
                    }
                    adView.bodyView = body
                    root.addView(body)
                    
                    val cta = Button(ctx).apply {
                        setBackgroundColor(android.graphics.Color.DKGRAY)
                        setTextColor(android.graphics.Color.WHITE)
                    }
                    adView.callToActionView = cta
                    root.addView(cta)
                    
                    adView.addView(root)
                    adView
                },
                update = { view ->
                    nativeAd?.let { ad ->
                        (view.headlineView as? TextView)?.text = ad.headline
                        (view.bodyView as? TextView)?.text = ad.body
                        (view.callToActionView as? Button)?.text = ad.callToAction
                        
                        view.bodyView?.visibility = if (ad.body != null) View.VISIBLE else View.GONE
                        view.callToActionView?.visibility = if (ad.callToAction != null) View.VISIBLE else View.GONE
                        
                        view.setNativeAd(ad)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Box(
                modifier = Modifier.fillMaxWidth().height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Sponsored Content", color = Color.Gray, fontSize = 14.sp)
            }
        }
    }
}
