package com.focushome.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.focushome.launcher.billing.BillingManager
import com.focushome.launcher.data.PreferencesManager
import com.focushome.launcher.ui.AppListViewModel
import com.focushome.launcher.ui.LauncherScreen
import com.focushome.launcher.ads.ConsentManager
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var preferencesManager: PreferencesManager
    private lateinit var billingManager: BillingManager
    private lateinit var viewModel: AppListViewModel
    private lateinit var consentManager: ConsentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        
        consentManager = ConsentManager(this)
        consentManager.gatherConsent { error ->
            if (error == null) {
                // Initialize AdMob if consent gathered or not required
                MobileAds.initialize(this) {}
            }
        }

        preferencesManager = PreferencesManager(applicationContext)
        billingManager = BillingManager(applicationContext, preferencesManager, lifecycleScope)
        viewModel = AppListViewModel(applicationContext, preferencesManager)

        setContent {
            LauncherScreen(
                viewModel = viewModel,
                onGoPro = {
                    billingManager.launchPurchaseFlow(this)
                }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh app list when returning to launcher
        viewModel.loadApps()
    }
}
