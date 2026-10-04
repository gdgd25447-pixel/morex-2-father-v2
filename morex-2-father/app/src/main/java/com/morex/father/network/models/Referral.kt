package com.morex.father.network.models

import com.google.gson.annotations.SerializedName

/** GET /parent/referral/code */
data class ReferralCode(
    @SerializedName("code") val code: String? = null,
    @SerializedName("referral_code") val referralCode: String? = null
)

/** referrals مجمّعة حسب الحالة (GET /parent/referral/stats → { code, stats }) */
data class ReferralStats(
    @SerializedName("pending_count") val pending: Int = 0,
    @SerializedName("completed_count") val completed: Int = 0,
    @SerializedName("eligible_count") val eligible: Int = 0,
    @SerializedName("paid_count") val paid: Int = 0,
    @SerializedName("cancelled_count") val cancelled: Int = 0,
    @SerializedName("total_earned") val totalEarned: Double = 0.0
)

data class ReferralStatsResponse(
    @SerializedName("code") val code: String? = null,
    @SerializedName("stats") val stats: ReferralStats? = null
)

/** صف من GET /parent/referral/history */
data class Referral(
    @SerializedName("id") val id: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("reward_yer") val rewardYer: Double = 0.0,
    @SerializedName("registered_at") val registeredAt: String? = null,
    @SerializedName("eligible_at") val eligibleAt: String? = null,
    @SerializedName("paid_at") val paidAt: String? = null,
    @SerializedName("referred_name") val referredName: String? = null
)
