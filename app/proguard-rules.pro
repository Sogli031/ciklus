# Add project specific ProGuard rules here.
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

-keep class com.injekcije.app.data.** { *; }

# Google Play Services Auth
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# Google API Client
-keep class com.google.api.** { *; }
-keep class com.google.api.client.** { *; }
-keep class com.google.api.services.** { *; }
-keep class * extends com.google.api.client.json.GenericJson { *; }
-dontwarn com.google.api.**

# Google HTTP Client
-keep class com.google.http.** { *; }
-dontwarn com.google.http.**

# Gson (used by Google API)
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**

# OkHttp (for REST API calls)
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**

# Apache HTTP Client (transitive dependency from Google API)
-dontwarn org.apache.http.**
-dontwarn javax.naming.**
-dontwarn org.ietf.jgss.**

# Strip debug logging in release builds
-assumenosideeffects class android.util.Log {
    public static int d(...);
    public static int v(...);
}
