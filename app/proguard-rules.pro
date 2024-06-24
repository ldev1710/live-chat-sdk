# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-keep public class com.test.excute.core.MiCallFactory{
   *;
}

-keep public interface com.test.excute.listener.publisher.MiCallStateListener{
    *;
}



-keep public class com.test.excute.model.CallStateEnum{
    *;
}
-keep public class com.test.excute.model.RegistrationStateEnum{
    *;
}
-keep public class com.test.excute.model.account.MiCallAccount{
    *;
}

# Retrofit2
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keepclassmembernames interface * {
    @retrofit2.http.* <methods>;
}
-keep public class * extends *
-keep public class * implements org.linphone.core.CoreListener
-keep interface * { *; }
-keep class org.linphone.** { *; }
-keepattributes Signature

-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-dontwarn com.squareup.okhttp.**

-dontwarn rx.**
-dontwarn retrofit.**
-dontwarn okio.**
-keep class retrofit.** { *; }
-keepclasseswithmembers class * {
    @retrofit.http.* <methods>;
}
# GSON Annotations
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}
# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile