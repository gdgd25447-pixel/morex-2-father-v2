package com.morex.father.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Debug
import android.provider.Settings
import android.util.Base64
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object SecurityUtils {

    fun isRooted(): Boolean {
        return checkRootMethod1() || checkRootMethod2() ||
                checkRootMethod3() || checkRootMethod4()
    }

    private fun checkRootMethod1(): Boolean {
        val tags = Build.TAGS ?: return false
        return tags.contains("test-keys")
    }

    private fun checkRootMethod2(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su", "/system/bin/su", "/system/xbin/su",
            "/data/local/xbin/su", "/data/local/bin/su",
            "/system/sd/xbin/su", "/system/bin/failsafe/su",
            "/data/local/su", "/su/bin/su",
            "/magisk/.core/bin/su",
            "/system/xbin/daemonsu"
        )
        return paths.any { File(it).exists() }
    }

    private fun checkRootMethod3(): Boolean {
        var process: Process? = null
        return try {
            process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
            BufferedReader(InputStreamReader(process.inputStream)).readLine() != null
        } catch (t: Throwable) {
            false
        } finally {
            process?.destroy()
        }
    }

    private fun checkRootMethod4(): Boolean {
        return try {
            val exec = Runtime.getRuntime().exec("su -c id")
            val reader = BufferedReader(InputStreamReader(exec.inputStream))
            val line = reader.readLine()
            line?.contains("uid=0") == true
        } catch (e: Exception) {
            false
        }
    }

    fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk" == Build.PRODUCT
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu"))
    }

    fun isDebuggerAttached(): Boolean {
        return Debug.isDebuggerConnected() || Debug.waitingForDebugger()
    }

    fun isDeveloperModeEnabled(context: Context): Boolean {
        return try {
            Settings.Global.getInt(
                context.contentResolver,
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0
            ) == 1
        } catch (e: Exception) {
            false
        }
    }

    fun verifySignature(context: Context, expectedSha: String): Boolean {
        return try {
            val info = context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNATURES
            )
            info.signatures?.any { sig ->
                val sha = sha256(sig.toByteArray())
                sha.equals(expectedSha, ignoreCase = true)
            } == true
        } catch (e: Exception) {
            false
        }
    }

    fun sha256(input: ByteArray): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(input)
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun sha256String(input: String): String = sha256(input.toByteArray())

    private const val AES_KEY = "M0rexF@th3r2026SecKeyAES256Bit!"

    fun encrypt(plain: String): String {
        return try {
            val key = AES_KEY.take(32).toByteArray()
            val iv = ByteArray(16) { 0 }
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(iv))
            Base64.encodeToString(cipher.doFinal(plain.toByteArray()), Base64.NO_WRAP)
        } catch (e: Exception) {
            ""
        }
    }

    fun decrypt(encrypted: String): String {
        return try {
            val key = AES_KEY.take(32).toByteArray()
            val iv = ByteArray(16) { 0 }
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(iv))
            String(cipher.doFinal(Base64.decode(encrypted, Base64.NO_WRAP)))
        } catch (e: Exception) {
            ""
        }
    }

    fun getDeviceFingerprint(context: Context): String {
        val androidId = Settings.Secure.getString(
            context.contentResolver, Settings.Secure.ANDROID_ID
        ) ?: "unknown"
        val raw = "$androidId|${Build.MANUFACTURER}|${Build.MODEL}|${Build.DEVICE}"
        return sha256String(raw)
    }

    fun isAppTampered(context: Context): Boolean {
        return try {
            val info = context.packageManager.getPackageInfo(
                context.packageName, 0
            )
            !info.applicationInfo.sourceDir.startsWith("/data/app/")
        } catch (e: Exception) {
            false
        }
    }

    fun performSecurityCheck(context: Context): SecurityReport {
        return SecurityReport(
            rooted = isRooted(),
            emulator = isEmulator(),
            debugger = isDebuggerAttached(),
            developerMode = isDeveloperModeEnabled(context),
            tampered = isAppTampered(context)
        )
    }

    data class SecurityReport(
        val rooted: Boolean,
        val emulator: Boolean,
        val debugger: Boolean,
        val developerMode: Boolean,
        val tampered: Boolean
    ) {
        val isCompromised: Boolean
            get() = rooted || emulator || debugger || tampered
    }
}
