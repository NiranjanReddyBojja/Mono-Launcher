package com.focushome.launcher.ads

import android.app.Activity
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Helper class that handles Google User Messaging Platform (UMP) SDK consent requests.
 * Required for AdMob compliance (GDPR, CCPA, etc.).
 */
class ConsentManager(private val activity: Activity) {
    private val consentInformation: ConsentInformation = UserMessagingPlatform.getConsentInformation(activity)
    private val isMobileAdsInitializeCalled = AtomicBoolean(false)

    fun gatherConsent(
        onConsentGathered: (Error?) -> Unit
    ) {
        // For testing purposes, you can reset consent state:
        // consentInformation.reset()

        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    // Consent has been gathered.
                    onConsentGathered(if (formError != null) Error(formError.message) else null)
                }
            },
            { requestConsentError ->
                // Consent gathering failed.
                onConsentGathered(Error(requestConsentError.message))
            }
        )
    }

    /**
     * Helper variable to determine if the app can request ads.
     */
    val canRequestAds: Boolean
        get() = consentInformation.canRequestAds()

    /**
     * Helper variable to determine if the privacy options form is required.
     */
    val isPrivacyOptionsRequired: Boolean
        get() = consentInformation.privacyOptionsRequirementStatus == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    fun showPrivacyOptionsForm(
        onFormDismissed: (Error?) -> Unit
    ) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
            onFormDismissed(if (formError != null) Error(formError.message) else null)
        }
    }
}
