package com.morex.father.network

import android.content.Context
import com.morex.father.BuildConfig
import com.morex.father.data.Prefs
import com.morex.father.utils.Constants
import okhttp3.CertificatePinner
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URI
import java.util.concurrent.TimeUnit

object ApiClient {

    private var retrofit: Retrofit? = null
    private var service: ApiService? = null
    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
        FileLogger.init(context)
        buildRetrofit()
    }

    /**
     * سجل خفيف: الطريقة + المسار + الكود + الزمن فقط.
     * لا يُسجَّل: التوكن، الـ headers، الـ query، أو محتوى الطلب/الرد (قد يحوي OTP أو PIN).
     */
    private val safeLogInterceptor = Interceptor { chain ->
        val req = chain.request()
        val path = req.url.encodedPath
        val t0 = System.currentTimeMillis()
        try {
            val resp = chain.proceed(req)
            FileLogger.write("${req.method} $path -> ${resp.code} (${System.currentTimeMillis() - t0}ms)")
            resp
        } catch (e: Exception) {
            FileLogger.write("${req.method} $path !! ${e.javaClass.simpleName}")
            throw e
        }
    }

    private val authInterceptor = Interceptor { chain ->
        val builder = chain.request().newBuilder().header("Accept", "application/json")
        // لا نضبط Content-Type يدوياً: Retrofit يضبطه من الـ body (ويلزم boundary في رفع الملفات)
        val token = Prefs.getToken(appContext)
        if (!token.isNullOrBlank()) builder.header("Authorization", "Bearer $token")
        chain.proceed(builder.build())
    }

    /** Certificate pinning: يُفعَّل عند وجود دبابيس في MOREX_CERT_PINS (انظر README / scripts/get_pins.sh). */
    private fun configuredPins(): List<String> =
        BuildConfig.CERT_PINS.split(',').map { it.trim() }.filter { it.startsWith("sha256/") }

    /** وضع الإنفاذ (اختياري): يفشل الاتصال عند عدم التطابق. */
    private fun pinnerOrNull(): CertificatePinner? {
        val pins = configuredPins()
        if (pins.isEmpty() || !BuildConfig.PIN_ENFORCE) return null
        val host = URI.create(Constants.BASE_URL).host ?: return null
        return CertificatePinner.Builder().apply { pins.forEach { add(host, it) } }.build()
    }

    /**
     * الوضع الافتراضي: تسجيل فقط. يقارن SPKI لكل شهادة في السلسلة بالدبابيس، وعند عدم التطابق يكتب سطراً في السجل
     * ولا يمنع الطلب. (تنبيه أمني: هذا الوضع لا يحمي من MITM؛ هو للمراقبة فقط.)
     */
    private val pinReportInterceptor = Interceptor { chain ->
        val resp = chain.proceed(chain.request())
        val pins = configuredPins()
        if (pins.isNotEmpty() && !BuildConfig.PIN_ENFORCE) {
            try {
                val certs = resp.handshake?.peerCertificates.orEmpty()
                val matched = certs.any { c -> CertificatePinner.pin(c) in pins }
                if (!matched) FileLogger.write("PIN_MISMATCH host=${chain.request().url.host} (report-only)")
            } catch (_: Exception) { /* لا نكسر الطلب بسبب التقرير */ }
        }
        resp
    }

    private fun buildRetrofit() {
        val builder = OkHttpClient.Builder()
            // Render المجاني قد يحتاج حتى دقيقة للإقلاع البارد
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .addInterceptor(safeLogInterceptor)
            .addNetworkInterceptor(pinReportInterceptor)

        pinnerOrNull()?.let { builder.certificatePinner(it) }

        if (BuildConfig.DEBUG) {
            builder.addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                    redactHeader("Authorization")
                }
            )
        }

        val base = Constants.API_BASE.trimEnd('/') + "/"
        retrofit = Retrofit.Builder()
            .baseUrl(base)
            .client(builder.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        service = retrofit?.create(ApiService::class.java)
    }

    fun get(): ApiService {
        if (service == null) buildRetrofit()
        return service!!
    }

    fun rebuild() = buildRetrofit()

    fun setAuthToken(token: String?) {
        if (token != null) {
            Prefs.saveToken(appContext, token)
        } else {
            Prefs.clearAuth(appContext)
        }
        rebuild()
    }
}
