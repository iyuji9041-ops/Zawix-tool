# Proguard rules for Zaswix Tools
# Room Database keep rules
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# ZXing QR Code Core library
-keep class com.google.zxing.** { *; }

# Google Mobile Ads (AdMob) keep rules
-keep class com.google.android.gms.ads.** { *; }
-keep public class com.google.ads.** { *; }
-keep class com.google.android.gms.ads.mediation.** { *; }
-keep class com.google.android.gms.internal.ads.** { *; }
-dontwarn com.google.android.gms.ads.**

# Coroutines and AndroidX
-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.room.* *;
}
