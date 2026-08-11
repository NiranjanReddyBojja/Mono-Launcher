# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\bojja\AppData\Local\Android\Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools-proguard.html

# Add any custom keep rules here that your app might need.

# Jetpack Compose rules (usually handled by Compose compiler but good to have)
-keepclassmembers class * extends androidx.compose.runtime.RecomposeScope { *; }

# Google Play Services / AdMob
-keep class com.google.android.gms.ads.** { *; }
-keep interface com.google.android.gms.ads.** { *; }

# Billing Library
-keep class com.android.billingclient.api.** { *; }
