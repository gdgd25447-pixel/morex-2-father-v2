# ═══════════════ MOREX Father — ProGuard / R8 (release) ═══════════════

# ── عام ──
-keepattributes Signature, InnerClasses, EnclosingMethod, *Annotation*, Exceptions
-keepattributes SourceFile, LineNumberTable
-renamesourcefileattribute SourceFile
-optimizationpasses 5
-allowaccessmodification

# ── إزالة سجلات التصحيح من نسخة release (لا تسريب بيانات في logcat) ──
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# ── النماذج (Gson يعتمد على أسماء الحقول عبر @SerializedName + الانعكاس) ──
-keep class com.morex.father.network.models.** { *; }
-keepclassmembers class com.morex.father.network.models.** { *; }

# ── Gson ──
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-dontwarn sun.misc.**

# ── Retrofit + OkHttp + Okio ──
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-keepclasseswithmembers class * { @retrofit2.http.* <methods>; }
-keep interface com.morex.father.network.ApiService { *; }
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# ── Socket.io / Engine.io ──
-keep class io.socket.** { *; }
-dontwarn io.socket.**

# ── Kotlin / Coroutines ──
-keep class kotlin.Metadata { *; }
-dontwarn kotlinx.coroutines.**
-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }

# ── AndroidX Security Crypto + Tink (EncryptedSharedPreferences) ──
-keep class androidx.security.crypto.** { *; }
-keep class com.google.crypto.tink.** { *; }
-dontwarn com.google.crypto.tink.**
-dontwarn com.google.errorprone.annotations.**
-dontwarn javax.annotation.concurrent.**

# ── bcrypt (PIN المحلي) ──
-keep class at.favre.lib.crypto.bcrypt.** { *; }
-dontwarn at.favre.lib.**

# ── Biometric ──
-keep class androidx.biometric.** { *; }

# ── Firebase / Play Services ──
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**
-keep class * extends com.google.firebase.messaging.FirebaseMessagingService

# ── Room ──
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# ── Glide ──
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** { **[] $VALUES; public *; }
-dontwarn com.bumptech.glide.**

# ── MPAndroidChart / Lottie / ZXing ──
-keep class com.github.mikephil.charting.** { *; }
-dontwarn com.github.mikephil.charting.**
-dontwarn com.airbnb.lottie.**
-keep class com.google.zxing.** { *; }

# ── مكونات أندرويد ──
-keep public class * extends android.app.Activity
-keep public class * extends androidx.fragment.app.Fragment
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# ── ViewBinding (يُستدعى بالاسم عبر inflate) ──
-keep class com.morex.father.databinding.** { *; }

# ── Parcelable / Serializable / enums ──
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keepclassmembers class * implements java.io.Serializable {
    private static final java.io.ObjectStreamField[] serialVersionUID;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
}

# ── BuildConfig (يُقرأ فيه الـ pins) ──
-keep class com.morex.father.BuildConfig { *; }
