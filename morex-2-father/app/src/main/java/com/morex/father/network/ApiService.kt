package com.morex.father.network

import com.morex.father.network.models.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * عقد الـ API مع morex-1-server. كل نوع مطابق لرد السيرفر الفعلي (انظر docs/ENDPOINTS_MAP.md).
 * القوائم تعود مصفوفة مباشرة؛ الإنشاء/التعديل يعيد غلافاً مثل { geofence: {...} }.
 */
interface ApiService {

    // ═════════ المصادقة ═════════
    @POST("auth/otp/send")
    suspend fun sendOtp(@Body body: Map<String, String>): Response<OtpSendResponse>

    @POST("auth/otp/verify")
    suspend fun verifyOtp(@Body body: Map<String, String>): Response<AuthResponse>

    /** ينشئ/يحدّث الحساب ويرسل OTP (لا يُصدر توكناً). */
    @POST("auth/register")
    suspend fun register(@Body body: Map<String, String>): Response<OtpSendResponse>

    @POST("auth/logout")
    suspend fun logout(): Response<GenericResponse>

    @GET("auth/me")
    suspend fun getMe(): Response<ParentEnvelope>

    @POST("auth/refresh")
    suspend fun refreshToken(): Response<RefreshResponse>

    @POST("auth/pin/set")
    suspend fun setPin(@Body body: Map<String, String>): Response<GenericResponse>

    @POST("auth/pin/verify")
    suspend fun verifyPin(@Body body: Map<String, String>): Response<PinVerifyResponse>

    // ═════════ الملف الشخصي ═════════
    @GET("parent/profile")
    suspend fun getProfile(): Response<ParentEnvelope>

    @PUT("parent/profile")
    suspend fun updateProfile(@Body body: Map<String, String>): Response<ParentEnvelope>

    @POST("parent/fcm-token")
    suspend fun updateFcmToken(@Body body: Map<String, String>): Response<GenericResponse>

    @GET("parent/block-status")
    suspend fun getBlockStatus(): Response<BlockStatus>

    // ═════════ الأبناء ═════════
    @GET("parent/children")
    suspend fun getChildren(): Response<List<Child>>

    @GET("parent/children/{id}")
    suspend fun getChild(@Path("id") id: String): Response<Child>

    @POST("parent/children")
    suspend fun addChild(@Body body: Map<String, String>): Response<Child>

    @PUT("parent/children/{id}")
    suspend fun updateChild(@Path("id") id: String, @Body body: Map<String, String>): Response<Child>

    @DELETE("parent/children/{id}")
    suspend fun deleteChild(@Path("id") id: String): Response<GenericResponse>

    @POST("parent/children/{id}/pairing-code")
    suspend fun generatePairingCode(@Path("id") id: String): Response<ChildPairingResponse>

    @GET("parent/children/{id}/pairing-status")
    suspend fun getPairingStatus(@Path("id") id: String): Response<PairingStatusResponse>

    @DELETE("parent/children/{id}/pairing-code")
    suspend fun cancelPairingCode(@Path("id") id: String): Response<GenericResponse>

    // ═════════ الموقع ═════════
    @GET("parent/location/live")
    suspend fun getLiveLocations(): Response<LiveLocationsResponse>

    /** المعامل يقبل childId أو deviceId (السيرفر يحوّل child → أحدث جهاز). */
    @GET("parent/location/history/{id}")
    suspend fun getLocationHistory(
        @Path("id") childOrDeviceId: String,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
        @Query("limit") limit: Int = 500
    ): Response<LocationHistoryPointsResponse>

    @GET("parent/location/summary/{id}")
    suspend fun getLocationSummary(@Path("id") childOrDeviceId: String): Response<LocationSummaryResponse>

    // ═════════ السياج ═════════
    @GET("parent/geofences/{childId}")
    suspend fun getGeofences(@Path("childId") childId: String): Response<List<Geofence>>

    @POST("parent/geofences")
    suspend fun addGeofence(@Body geofence: Geofence): Response<GeofenceEnvelope>

    @PUT("parent/geofences/{id}")
    suspend fun updateGeofence(@Path("id") id: String, @Body geofence: Geofence): Response<GeofenceEnvelope>

    @DELETE("parent/geofences/{id}")
    suspend fun deleteGeofence(@Path("id") id: String): Response<GenericResponse>

    // ═════════ الأوامر ═════════
    @POST("parent/commands")
    suspend fun sendCommand(@Body body: Map<String, @JvmSuppressWildcards Any>): Response<CommandEnvelope>

    @GET("parent/commands")
    suspend fun getCommandHistory(
        @Query("device_id") deviceId: String? = null,
        @Query("status") status: String? = null,
        @Query("limit") limit: Int = 50
    ): Response<List<Command>>

    @POST("parent/commands/{id}/cancel")
    suspend fun cancelCommand(@Path("id") id: String): Response<GenericResponse>

    // ═════════ التنبيهات و SOS ═════════
    @GET("parent/alerts")
    suspend fun getAlerts(
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0,
        @Query("child_id") childId: String? = null,
        @Query("type") type: String? = null
    ): Response<List<Alert>>

    @PUT("parent/alerts/{id}/read")
    suspend fun markAlertRead(@Path("id") id: String): Response<GenericResponse>

    @PUT("parent/alerts/read-all")
    suspend fun markAllAlertsRead(): Response<GenericResponse>

    @DELETE("parent/alerts/{id}")
    suspend fun deleteAlert(@Path("id") id: String): Response<GenericResponse>

    @GET("parent/sos/active")
    suspend fun getActiveSos(): Response<List<Alert>>

    @PUT("parent/sos/{id}/resolve")
    suspend fun resolveSos(@Path("id") id: String): Response<GenericResponse>

    // ═════════ التطبيقات ═════════
    @GET("parent/apps/{childId}")
    suspend fun getAppRules(@Path("childId") childId: String): Response<List<AppRule>>

    @POST("parent/apps/{childId}/block")
    suspend fun blockApp(@Path("childId") childId: String, @Body body: Map<String, String>): Response<GenericResponse>

    @POST("parent/apps/{childId}/allow")
    suspend fun allowApp(@Path("childId") childId: String, @Body body: Map<String, String>): Response<GenericResponse>

    @GET("parent/apps/{childId}/usage")
    suspend fun getAppUsage(@Path("childId") childId: String, @Query("date") date: String? = null): Response<AppUsageResponse>

    @GET("parent/apps/{childId}/requests")
    suspend fun getAppRequests(@Path("childId") childId: String): Response<List<AppApprovalRequest>>

    @POST("parent/apps/requests/{id}/respond")
    suspend fun respondAppRequest(@Path("id") id: String, @Body body: Map<String, Boolean>): Response<GenericResponse>

    // ═════════ الويب ═════════
    @GET("parent/web/{childId}/rules")
    suspend fun getWebRules(@Path("childId") childId: String): Response<List<WebRule>>

    /** body: domain + list_type (black|white). */
    @POST("parent/web/{childId}/rules")
    suspend fun addWebRule(@Path("childId") childId: String, @Body body: Map<String, String>): Response<GenericResponse>

    @DELETE("parent/web/rules/{id}")
    suspend fun deleteWebRule(@Path("id") id: String): Response<GenericResponse>

    @GET("parent/web/{childId}/history")
    suspend fun getWebHistory(@Path("childId") childId: String): Response<List<WebHistory>>

    @GET("parent/web/{childId}/keywords")
    suspend fun getKeywords(@Path("childId") childId: String): Response<List<KeywordRule>>

    /** body: keyword + severity (warning|danger). */
    @POST("parent/web/{childId}/keywords")
    suspend fun addKeyword(@Path("childId") childId: String, @Body body: Map<String, String>): Response<KeywordEnvelope>

    // ═════════ الوسائط ═════════
    @GET("parent/media/{childId}")
    suspend fun getMediaCaptures(@Path("childId") childId: String): Response<List<MediaCapture>>

    /** body: device_id */
    @POST("parent/media/screenshot")
    suspend fun requestScreenshot(@Body body: Map<String, String>): Response<GenericResponse>

    /** body: device_id + facing (front|back) */
    @POST("parent/media/camera")
    suspend fun requestCameraPhoto(@Body body: Map<String, String>): Response<GenericResponse>

    /** body: device_id (+ duration_sec) */
    @POST("parent/media/mic")
    suspend fun requestMicRecord(@Body body: Map<String, @JvmSuppressWildcards Any>): Response<CommandEnvelope>

    // ═════════ الجدولة ═════════
    @GET("parent/schedules/{childId}")
    suspend fun getSchedules(@Path("childId") childId: String): Response<List<ScreenSchedule>>

    @POST("parent/schedules")
    suspend fun addSchedule(@Body body: Map<String, @JvmSuppressWildcards Any>): Response<ScheduleEnvelope>

    @PUT("parent/schedules/{id}")
    suspend fun updateSchedule(@Path("id") id: String, @Body body: Map<String, @JvmSuppressWildcards Any>): Response<ScheduleEnvelope>

    @DELETE("parent/schedules/{id}")
    suspend fun deleteSchedule(@Path("id") id: String): Response<GenericResponse>

    // ═════════ المكالمات والرسائل ═════════
    @GET("parent/comms/{childId}/calls")
    suspend fun getCallLogs(@Path("childId") childId: String): Response<List<CallLog>>

    @GET("parent/comms/{childId}/sms")
    suspend fun getSmsLogs(@Path("childId") childId: String): Response<List<SmsLog>>

    // ═════════ الاشتراك ═════════
    @GET("parent/subscription")
    suspend fun getSubscription(): Response<SubscriptionResponse>

    @GET("parent/subscription/plans")
    suspend fun getPlans(): Response<PlansResponse>

    @GET("parent/subscription/wallets")
    suspend fun getWallets(): Response<List<Wallet>>

    /**
     * رفع سند الدفع. حقول السيرفر الإلزامية: plan (أو plan_id)، wallet_key، reference_number، sender_name،
     * وصورة السند باسم الحقل `receipt`. اختياري: sender_phone.
     */
    @Multipart
    @POST("parent/subscription/receipt")
    suspend fun uploadReceipt(
        @Part receipt: MultipartBody.Part,
        @Part("plan_id") planId: RequestBody,
        @Part("wallet_key") walletKey: RequestBody,
        @Part("reference_number") referenceNumber: RequestBody,
        @Part("sender_name") senderName: RequestBody,
        @Part("sender_phone") senderPhone: RequestBody? = null
    ): Response<GenericResponse>

    // ═════════ الإحالة ═════════
    @GET("parent/referral/code")
    suspend fun getReferralCode(): Response<ReferralCode>

    @GET("parent/referral/stats")
    suspend fun getReferralStats(): Response<ReferralStatsResponse>

    @GET("parent/referral/history")
    suspend fun getReferralHistory(): Response<List<Referral>>

    @POST("parent/referral/apply")
    suspend fun applyReferral(@Body body: Map<String, String>): Response<GenericResponse>

    // ═════════ الدعم ═════════
    @GET("parent/support/conversations")
    suspend fun getSupportConversations(): Response<List<SupportConversation>>

    @POST("parent/support/conversations")
    suspend fun createConversation(@Body body: Map<String, String>): Response<SupportConversationEnvelope>

    @GET("parent/support/conversations/{id}/messages")
    suspend fun getSupportMessages(@Path("id") id: String): Response<List<SupportMessage>>

    /** body: content (+ message_type, attachment). */
    @POST("parent/support/conversations/{id}/messages")
    suspend fun sendSupportMessage(
        @Path("id") id: String,
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Response<SupportMessageEnvelope>

    @Multipart
    @POST("parent/support/upload")
    suspend fun uploadSupportImage(@Part file: MultipartBody.Part): Response<SupportUploadResponse>
}
