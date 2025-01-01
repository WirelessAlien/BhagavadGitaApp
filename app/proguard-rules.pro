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

# Keep all classes in Gson
-keep class com.google.gson.** { *; }

# Keep Gson internal serialization classes
-keep class sun.misc.** { *; }
-keep class com.google.gson.stream.** { *; }

# GSON
-keepattributes Signature
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# Coroutines
-keepattributes Signature
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# Preserve models
#-keep class com.wirelessalien.android.bhagavadgita.data.**
#-keepclassmembers class com.wirelessalien.android.bhagavadgita.data.** {
# !transient <fields>;
#}
-keep class com.wirelessalien.android.bhagavadgita.data.** {
    !transient <fields>;
}

# Preserve Serializable Classes
-keepclassmembers,allowobfuscation,allowshrinking class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object readResolve();
    java.lang.Object writeReplace();
}

