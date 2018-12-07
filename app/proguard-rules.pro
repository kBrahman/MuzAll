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

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
 -keepattributes Signature, InnerClasses, EnclosingMethod, Annotation
 -keepclassmembers,allowshrinking,allowobfuscation interface * {
     @retrofit2.http.* <methods>;
 }
 -dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
 -dontwarn javax.annotation.**
 -dontwarn kotlin.Unit
 -dontwarn retrofit2.-KotlinExtensions
 -dontwarn okhttp3.**
 -keep class okhttp3.** { *;}
 -keep interface okhttp3.** { *; }
 -dontwarn okio.**
 -keep class kotlin.** { *; }
 -keep class kotlin.Metadata { *; }
 -dontwarn kotlin.**
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
}
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
-optimizations !class/unboxing/enum
-keep class com.example.app.json.** { *; }
