package com.morex.father.network.models

import com.google.gson.annotations.SerializedName

/** parents (ما يعيده /auth/me و /parent/profile). */
data class Parent(
    @SerializedName("id") val id: String,
    @SerializedName("full_name") val name: String? = null,
    @SerializedName("phone") val phone: String = "",
    @SerializedName("email") val email: String? = null,
    @SerializedName("avatar_url") val avatar: String? = null,
    @SerializedName("language") val language: String? = null,
    @SerializedName("referral_code") val referralCode: String? = null,
    @SerializedName("is_verified") val isVerified: Boolean = false,
    @SerializedName("created_at") val createdAt: String? = null
)

/** GET /auth/me و GET/PUT /parent/profile تعيد { parent: {...} } */
data class ParentEnvelope(
    @SerializedName("parent") val parent: Parent? = null
)
