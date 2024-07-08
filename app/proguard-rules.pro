
-keep public class com.mitek.build.live.chat.sdk.core.MiCallFactory{
   public *;
}

-keep public class com.mitek.build.live.chat.sdk.listener.publisher.MiCallStateListener{
    public *;
}
-keep public class org.pjsip.**{
    *;
}
# Obfuscate tất cả các class trong package này
-repackageclasses
-keepclassmembers class com.mitek.build.live.chat.core.** {
    *;
}

-keep public class com.mitek.build.live.chat.model.**{
    *;
}
# Loại bỏ các thông tin gỡ lỗi và các phương thức không cần thiết
#-dontobfuscate
#-dontoptimize
#-dontpreverify
#-keepattributes Exceptions, InnerClasses, Signature, Deprecated, SourceFile, LineNumberTable, *Annotation*, EnclosingMethod
#-keepclassmembers public class * {
#    public protected *;
#}

# Chèn các runtime exceptions vào nội dung phương thức
#-assumenosideeffects class com.mitek.build.micall.sdk.core.** {
#    <methods>;
#}

-obfuscationdictionary proguard_dict.txt
# Retrofit2
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keepclassmembernames interface * {
    @retrofit2.http.* <methods>;
}
-keep public class * extends *
-keep interface * { *; }
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